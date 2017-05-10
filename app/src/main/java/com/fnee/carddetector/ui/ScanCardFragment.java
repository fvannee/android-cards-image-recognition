package com.fnee.carddetector.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fnee.carddetector.R;
import com.fnee.carddetector.algorithm.CardDetector;
import com.fnee.carddetector.algorithm.CardInImage;
import com.fnee.carddetector.common.Card;
import com.fnee.carddetector.common.Deal;
import com.fnee.carddetector.common.Position;
import com.fnee.carddetector.common.Rank;
import com.fnee.carddetector.common.Suit;
import com.fnee.opencv.CameraBridgeViewBase2;
import com.fnee.opencv.JavaCameraView2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class ScanCardFragment extends Fragment implements View.OnTouchListener, CameraBridgeViewBase2.CvCameraViewListener2, View.OnLongClickListener, View.OnClickListener, DealUpdateListener {
    private static final String TAG = "CardDetector::Activity";

    private static final String TAG_SUIT_SELECT = "SuitSelectionTag";

    private static final String CURRENT_POS = "CURRENT_POS";
    private static final String IS_SCANNING = "IS_SCANNING";
    private static final String CARD_SET_INDEX = "CARD_SET_INDEX";

    private JavaCameraView2 openCvCameraView;
    private BaseLoaderCallback loaderCallback;

    private CardDetector detector;
    private Mat rgba, bw, coefficients, gray, dbg;
    private boolean isScanning;
    private int cardSetIndex;

    private HandView[] handViews;
    private Position currentPos;
    private Spinner spinnerCardSet;

    private DealRetrievable dealRetriever;
    private DealUpdateable dealUpdater;

    public static class SuitDialogSelection extends DialogFragment {
        private Position position;

        private DealUpdateable updater;
        private Suit suit;

        private boolean[] checkedItems;

        private static final String SUIT = "SUIT";
        private static final String RANKS = "RANKS";
        private static final String POSITION = "POSITION";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Rank[] ranks = Rank.values();
            String[] strRanks = new String[ranks.length];

            if (savedInstanceState != null)
            {
                suit = (Suit)savedInstanceState.getSerializable(SUIT);
                checkedItems = (boolean[])savedInstanceState.getSerializable(RANKS);
                position = (Position)savedInstanceState.getSerializable(POSITION);
            }

            for (int i = 0; i < ranks.length; i++)
            {
                strRanks[i] = ranks[i].toString();
            }

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle("Card Selection: " + suit.toString())
                    .setMultiChoiceItems(strRanks, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                                    if (isChecked)
                                    {
                                        if (!updater.addCardToHand(position, Card.getCard(suit, ranks[item])))
                                        {
                                            checkedItems[item] = false;
                                            ((AlertDialog) dialog).getListView().setItemChecked(item, false);
                                            Toast.makeText(getContext(), "Cannot add card that is already present in another hand", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else
                                    {
                                        updater.removeCardFromHand(position, Card.getCard(suit, ranks[item]));
                                    }
                                }
                            });
            return builder.create();
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            // save current instance state
            super.onSaveInstanceState(outState);

            outState.putSerializable(SUIT, suit);
            outState.putSerializable(POSITION, position);
            outState.putSerializable(RANKS, checkedItems);
        }

        public void setUpdater(DealUpdateable updater) {
            this.updater = updater;
        }

        public void setPosition(Position position)
        {
            this.position = position;
        }

        public void setSuit(Suit suit) {
            this.suit = suit;
        }

        public void setChecked(Set<Rank> cards) {
            final Rank[] ranks = Rank.values();
            checkedItems = new boolean[ranks.length];
            for (int i = 0; i < ranks.length; i++)
            {
                if (cards.contains(ranks[i]))
                    checkedItems[i] = true;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // save current instance state
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_SCANNING, isScanning);
        outState.putSerializable(CURRENT_POS, currentPos);
        outState.putInt(CARD_SET_INDEX, cardSetIndex);

        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(CARD_SET_INDEX, cardSetIndex);
        editor.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_scan_card, container, false);

        openCvCameraView = (JavaCameraView2)rootView.findViewById(R.id.surface_view_camera);
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);

        Position[] positions = Position.values();
        handViews = new HandView[positions.length];
        handViews[Position.North.ordinal()] = (HandView)rootView.findViewById(R.id.hand_north);
        handViews[Position.East.ordinal()] = (HandView)rootView.findViewById(R.id.hand_east);
        handViews[Position.South.ordinal()] = (HandView)rootView.findViewById(R.id.hand_south);
        handViews[Position.West.ordinal()] = (HandView)rootView.findViewById(R.id.hand_west);

        Deal deal;
        dealRetriever = (DealRetrievable)getActivity();
        deal = dealRetriever.getDeal();
        dealUpdater = (DealUpdateable)getActivity();

        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (savedInstanceState != null)
        {
            currentPos = (Position)savedInstanceState.getSerializable(CURRENT_POS);
            isScanning = savedInstanceState.getBoolean(IS_SCANNING);
            cardSetIndex = savedInstanceState.getInt(CARD_SET_INDEX);
        }
        else
        {
            currentPos = Position.North;
            isScanning = false;
            cardSetIndex = pref.getInt(CARD_SET_INDEX, 0);
        }

        ArrayList<String> spinnerCardSetData = new ArrayList<String>();
        spinnerCardSetData.add("Set 1");
        spinnerCardSetData.add("Set 2");

        spinnerCardSet = (Spinner)rootView.findViewById(R.id.spinner_card_set);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, spinnerCardSetData);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerCardSet.setAdapter(adapter);
        spinnerCardSet.setSelection(cardSetIndex);
        spinnerCardSet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerCardSet.getChildAt(0) != null)
                    ((TextView) spinnerCardSet.getChildAt(0)).setTextColor(Color.WHITE);
                if (cardSetIndex != position) {
                    cardSetIndex = position;
                    switchCardSet(cardSetIndex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        handViews[currentPos.ordinal()].setFocus(true);
        for (final Position pos : positions)
        {
            HandView hv = handViews[pos.ordinal()];
            hv.setHand(deal.getHands()[pos.ordinal()]);

            hv.setClickable(true);
            hv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handViews[currentPos.ordinal()].setFocus(false);
                    currentPos = ((HandView)v).getPosition();
                    handViews[currentPos.ordinal()].setFocus(true);
                }
            });
            hv.setSuitLongClickListener(new SuitLongClickListener() {
                @Override
                public boolean onSuitLongClick(final HandView hv, final Suit suit) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    SuitDialogSelection dialog = new SuitDialogSelection();
                    dialog.setChecked(hv.getCards(suit));
                    dialog.setSuit(suit);
                    dialog.setPosition(pos);
                    dialog.setUpdater(dealUpdater);
                    dialog.show(fragmentManager, TAG_SUIT_SELECT);
                    return true;
                }
            });
        }

        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(TAG_SUIT_SELECT);
        if (fragment != null)
        {
            SuitDialogSelection dialog = (SuitDialogSelection)fragment;
            dialog.setUpdater(dealUpdater);
        }

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (openCvCameraView != null) {
            if (isVisibleToUser) {
                openCvCameraView.enableView();
            }
            else {
                openCvCameraView.disableView();
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        loaderCallback = new BaseLoaderCallback(this.getActivity()) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                    {
                        Log.i(TAG, "OpenCV loaded successfully");

                        coefficients = new Mat(1, 4, CvType.CV_32F);
                        coefficients.put(0, 0, -1);
                        coefficients.put(0, 1, 1.5);
                        coefficients.put(0, 2, 0.5);
                        coefficients.put(0, 3, 0);

                        switchCardSet(cardSetIndex);

                        if (getUserVisibleHint())
                            openCvCameraView.enableView();
                        //openCvCameraView.enableFpsMeter();
                        openCvCameraView.setOnTouchListener(ScanCardFragment.this);
                        openCvCameraView.setLongClickable(true);
                        openCvCameraView.setOnLongClickListener(ScanCardFragment.this);
                        openCvCameraView.setOnClickListener(ScanCardFragment.this);
                    } break;
                    default:
                    {
                        super.onManagerConnected(status);
                    } break;
                }
            }
        };

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this.getActivity(), loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void switchCardSet(int cardSet)
    {
        InputStream inp;
        InputStream inp2;
        if (cardSet == 0) {
            inp = getResources().openRawResource(R.raw.samples_25);
            inp2 = getResources().openRawResource(R.raw.responses_25);
        }
        else if (cardSet == 1)
        {
            inp = getResources().openRawResource(R.raw.cartamundi_samples_25);
            inp2 = getResources().openRawResource(R.raw.cartamundi_responses_25);
        }
        else{
            throw new RuntimeException("Invalid card set index " + cardSet);
        }

        List<List<Double>> samples = readMatrix(inp);
        List<List<Double>> responses = readMatrix(inp2);

        detector = constructModel(samples, responses);
    }

    public void onDestroy() {
        super.onDestroy();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public void onDealUpdated(Deal deal) {
        for (int i = 0; i < deal.getHands().length; i++) {
            handViews[i].setHand(deal.getHands()[i]);
        }
    }

    public void onCameraViewStarted(int width, int height) {
        rgba = new Mat(height, width, CvType.CV_8UC4);
        bw = new Mat(height, width, CvType.CV_8UC1);
        dbg = new Mat();
        gray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to clear all hands?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < handViews.length; i++)
                        {
                            handViews[i].clear();
                        }
                        dealUpdater.clearAllCards();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    @Override
    public void onClick(View v) {
        isScanning = !isScanning;
        Toast.makeText(getContext(), isScanning ? "Starting..." : "Stopping...", Toast.LENGTH_SHORT).show();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase2.CvCameraViewFrame inputFrame) {
        rgba = inputFrame.rgba();
        if (isScanning && dealRetriever.getDeal().getHand(currentPos).getCards().size() < 13)
        {
            Core.transform(rgba, gray, coefficients);
            Imgproc.adaptiveThreshold(gray, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 101, 35);
            //Imgproc.cvtColor(bw, dbg, Imgproc.COLOR_GRAY2BGR);
            //detector.setDebugImg(dbg);
            final List<CardInImage> cards = detector.detectCardsInImage(bw, rgba);
            //for (CardInImage card : cards)
            //{
            //    Imgproc.putText(gray, card.getCard().getShortName(), card.getPoint(), 0, 2, new Scalar(0, 255, 75), 5, Core.LINE_AA, false);
            //}

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Set<Card> cards_set = CardDetector.CardsToSet(cards);
                    dealUpdater.addCardsToHand(currentPos, cards_set);
                }
            });
        }
        return rgba;
    }

    private static CardDetector constructModel(List<List<Double>> samples, List<List<Double>> responses)
    {
        int numBlack = 0;
        int numRed = 0;
        int total = samples.size();
        for (int i = 0; i < total; i++)
        {
            char response = (char)responses.get(i).get(0).intValue();
            if (response != 'S' && response != 'C')
            {
                numRed++;
            }
            if (response != 'H' && response != 'D')
            {
                numBlack++;
            }
        }

        Mat matRedSamples = new Mat(numRed, samples.get(0).size(), CvType.CV_32F);
        Mat matBlackSamples = new Mat(numBlack, samples.get(0).size(), CvType.CV_32F);
        Mat matRedResponses = new Mat(numRed, responses.get(0).size(), CvType.CV_32F);
        Mat matBlackResponses = new Mat(numBlack, responses.get(0).size(), CvType.CV_32F);
        for (int i = 0; i < total; i++)
        {
            List<Double> sample = samples.get(i);
            char response = (char)responses.get(i).get(0).intValue();
            if (response != 'S' && response != 'C')
            {
                for (int j = 0; j < sample.size(); j++)
                {
                    matRedSamples.put(i, j, sample.get(j));
                }
                matRedResponses.put(i, 0, (double)response);
            }
            if (response != 'H' && response != 'D')
            {
                for (int j = 0; j < sample.size(); j++)
                {
                    matBlackSamples.put(i, j, sample.get(j));
                }
                matBlackResponses.put(i, 0, (double)response);
            }
        }
        CardDetector c = new CardDetector(matRedSamples, matRedResponses, matBlackSamples, matBlackResponses);
        return c;
    }

    private static List<List<Double>> readMatrix(InputStream inp) {
        BufferedReader in = new BufferedReader(new InputStreamReader(inp));
        String line = null;
        List<List<Double>> matrix = new ArrayList<List<Double>>();
        try {
            while((line = in.readLine()) != null) {
                String[] splitted = line.split(" ");
                List<Double> row = new ArrayList<Double>();
                for (String s : splitted)
                {
                    double d = Double.parseDouble(s);
                    row.add(d);
                }
                matrix.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

}
