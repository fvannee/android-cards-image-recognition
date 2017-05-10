package com.fnee.carddetector.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.fnee.carddetector.R;
import com.fnee.carddetector.common.Deal;
import com.fnee.carddetector.common.DoubleDummyAnalysis;
import com.fnee.carddetector.common.Game;
import com.fnee.carddetector.common.PbnReadAdapter;
import com.fnee.carddetector.common.PbnWriteAdapter;
import com.fnee.carddetector.common.Position;
import com.fnee.carddetector.common.Vulnerability;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ShowHandsFragment extends Fragment implements DealUpdateListener {

    private static final String GAMES = "GAMES";
    private static final String CURRENT_DEAL = "CURRENT_DEAL";
    private static final String SAVE_FILE = "SAVE_FILE";

    private static final String DEFAULT = "default";
    private static final String PBN_EXT = ".pbn";
    private static final String DEFAULT_FILE = "default" + PBN_EXT;

    private ArrayList<Game> games;
    private int currentDeal;
    private String saveFileNoExtension;

    private Spinner spinnerDeals;
    private ArrayList<String> spinnerDealsData;

    private DealView dealView;

    private DealUpdateable dealUpdater;
    private DealRetrievable dealRetriever;

    public ShowHandsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_show_hands, container, false);

        Deal deal;
        dealRetriever = (DealRetrievable)getActivity();
        deal = dealRetriever.getDeal();
        dealUpdater = (DealUpdateable)getActivity();

        if (savedInstanceState != null)
        {
            games = (ArrayList<Game>)savedInstanceState.getSerializable(GAMES);
            currentDeal = savedInstanceState.getInt(CURRENT_DEAL);
            games.get(currentDeal).setDeal(deal);
            saveFileNoExtension = savedInstanceState.getString(SAVE_FILE);
        }
        else
        {
            saveFileNoExtension = DEFAULT;
            try {
                FileInputStream input = getActivity().openFileInput(saveFileNoExtension + PBN_EXT);
                games = PbnReadAdapter.readGames(input);
                if (games.size() == 0)
                {
                    Game g = new Game();
                    g.setDeal(deal);
                    g.setEvent(DEFAULT);
                    games.add(g);
                }
            } catch (Exception e) {
                games = new ArrayList<>();
                Game g = new Game();
                g.setDeal(deal);
                g.setEvent(DEFAULT);
                games.add(g);
            }

        }

        spinnerDealsData = new ArrayList<>();
        createSpinnerDealsData();

        spinnerDeals = (Spinner)rootView.findViewById(R.id.spinner_deals);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, spinnerDealsData);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerDeals.setAdapter(adapter);
        spinnerDeals.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setCurrentDeal(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button buttonNew = (Button)rootView.findViewById(R.id.button_new_deal);
        buttonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Game g = new Game();
                g.setBoardNumber(games.get(games.size() - 1).getBoardNumber() + 1);
                g.setEvent(games.get(games.size() - 1).getEvent());
                games.add(g);
                spinnerDealsData.add("" + games.size() + ": " + g.toString());
                ArrayAdapter<CharSequence> adap = (ArrayAdapter<CharSequence>)spinnerDeals.getAdapter();
                adap.notifyDataSetChanged();
                int selection = games.size() - 1;
                spinnerDeals.setSelection(selection);
                setCurrentDeal(selection);
            }
        });

        Button buttonClearDeal = (Button)rootView.findViewById(R.id.button_clear_deal);
        buttonClearDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (games.size() > 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure you want to clear this deal?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    games.get(currentDeal).getDeal().clear();
                                    setCurrentDeal(currentDeal);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        dealView = (DealView)rootView.findViewById(R.id.deal_view);
        dealView.setGame(games.get(currentDeal));
        dealView.setDealViewUpdateListener(new DealView.DealViewUpdateListener() {
            @Override
            public void onDealerUpdated(DealView view, Position dealer) {
                Deal d = games.get(currentDeal).getDeal();
                d.setDealer(dealer);
                dealUpdater.setDeal(d);
            }

            @Override
            public void onVulnerabilityUpdated(DealView view, Vulnerability vulnerability) {
                Deal d = games.get(currentDeal).getDeal();
                d.setVulnerability(vulnerability);
                dealUpdater.setDeal(d);
            }

            @Override
            public void onAnalysisUpdated(DealView view, DoubleDummyAnalysis analysis) {
                games.get(currentDeal).setDoubleDummyAnalysis(analysis);
                view.setGame(games.get(currentDeal));
            }
        });

        Button changeButton = (Button)rootView.findViewById(R.id.button_change_deals);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Change games name");

                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveGamesToFile();
                        saveFileNoExtension = input.getText().toString();
                        loadGamesFromFile();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        return rootView;
    }

    private void createSpinnerDealsData() {
        spinnerDealsData.clear();
        for (int i = 0; i < games.size(); i++)
        {
            spinnerDealsData.add("" + (i + 1) + ": " + games.get(i).toString());
        }
    }

    public void setCurrentDeal(int index)
    {
        currentDeal = index;
        dealUpdater.setDeal(games.get(currentDeal).getDeal());
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // save current instance state
        super.onSaveInstanceState(outState);

        outState.putSerializable(GAMES, games);
        outState.putInt(CURRENT_DEAL, currentDeal);
        outState.putString(SAVE_FILE, saveFileNoExtension);

        saveGamesToFile();
    }

    private void saveGamesToFile()
    {
        try {
            FileOutputStream fos = getActivity().openFileOutput(saveFileNoExtension + PBN_EXT, Context.MODE_PRIVATE);
            PbnWriteAdapter.writeGames(games, fos);
            fos.close();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadGamesFromFile()
    {
        try {
            FileInputStream input = getActivity().openFileInput(saveFileNoExtension + PBN_EXT);
            games = PbnReadAdapter.readGames(input);
            if (games.size() == 0)
            {
                Game g = new Game();
                g.setEvent(saveFileNoExtension);
                games.add(g);
            }
        } catch (Exception e) {
            games = new ArrayList<>();
            Game g = new Game();
            g.setEvent(saveFileNoExtension);
            games.add(g);
        }
        createSpinnerDealsData();
        ArrayAdapter<CharSequence> adap = (ArrayAdapter<CharSequence>)spinnerDeals.getAdapter();
        adap.notifyDataSetChanged();

        spinnerDeals.setSelection(0);
        setCurrentDeal(0);
    }

    @Override
    public void onDealUpdated(Deal deal) {
        Game game = games.get(currentDeal);
        game.setDeal(deal);
        if (!deal.isValidStartDeal())
            game.setDoubleDummyAnalysis(null);
        dealView.setGame(game);
    }
}
