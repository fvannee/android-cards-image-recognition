package com.fnee.carddetector.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fnee.carddetector.R;
import com.fnee.carddetector.common.DdsAdapter;
import com.fnee.carddetector.common.Deal;
import com.fnee.carddetector.common.DoubleDummyAnalysis;
import com.fnee.carddetector.common.Game;
import com.fnee.carddetector.common.Position;
import com.fnee.carddetector.common.Strain;
import com.fnee.carddetector.common.Vulnerability;

/**
 * Created by Floris on 24-4-2017.
 */

public class DealView extends RelativeLayout {
    public interface DealViewUpdateListener {
        void onDealerUpdated(DealView view, Position dealer);
        void onVulnerabilityUpdated(DealView view, Vulnerability vulnerability);
        void onAnalysisUpdated(DealView view, DoubleDummyAnalysis analysis);
    }

    private Game game;

    private DealViewUpdateListener dealViewUpdateListener;

    private HandView[] handViews;
    private Spinner spinnerDealer, spinnerVulnerability;
    private Button buttonAnalyze;
    private TableLayout tableAnalyze;
    private TextView[][] textAnalysis;

    public DealView(Context context) {
        super(context);
        initializeViews(context);
    }

    public DealView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public DealView(Context context,
                    AttributeSet attrs,
                    int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.deal_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Position[] positions = Position.values();
        int numHv = positions.length;
        handViews = new HandView[numHv];
        handViews[Position.North.ordinal()] = (HandView)findViewById(R.id.hand_north);
        handViews[Position.East.ordinal()] = (HandView)findViewById(R.id.hand_east);
        handViews[Position.South.ordinal()] = (HandView)findViewById(R.id.hand_south);
        handViews[Position.West.ordinal()] = (HandView)findViewById(R.id.hand_west);

        spinnerDealer = (Spinner)findViewById(R.id.spinner_dealer);
        spinnerDealer.setAdapter(new ArrayAdapter<Position>(getContext(), android.R.layout.simple_spinner_item, Position.values()));
        spinnerDealer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (dealViewUpdateListener != null)
                    dealViewUpdateListener.onDealerUpdated(DealView.this, (Position)spinnerDealer.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerVulnerability = (Spinner)findViewById(R.id.spinner_vulnerability);
        spinnerVulnerability.setAdapter(new ArrayAdapter<Vulnerability>(getContext(), android.R.layout.simple_spinner_item, Vulnerability.values()));
        spinnerVulnerability.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (dealViewUpdateListener != null)
                    dealViewUpdateListener.onVulnerabilityUpdated(DealView.this, (Vulnerability) spinnerVulnerability.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonAnalyze = (Button)findViewById(R.id.button_analyze);
        buttonAnalyze.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DoubleDummyAnalysis dds = DdsAdapter.calcTable(game.getDeal());
                dealViewUpdateListener.onAnalysisUpdated(DealView.this, dds);
            }
        });

        tableAnalyze = (TableLayout)findViewById(R.id.table_analysis);
        int numStrains = Strain.values().length;
        int numPos = Position.values().length;
        textAnalysis = new TextView[numStrains][numPos];
        for (int i = 0; i < numStrains; i++)
        {
            TableRow row = (TableRow)tableAnalyze.getChildAt(i + 1);
            for (int j = 0; j < numPos; j++)
            {
                textAnalysis[i][j] = (TextView)row.getChildAt(j + 1);
            }
        }
    }

    public void setDealViewUpdateListener(DealViewUpdateListener dealViewUpdateListener) {
        this.dealViewUpdateListener = dealViewUpdateListener;
    }

    public void setGame(Game game)
    {
        this.game = game;

        Deal deal = game.getDeal();
        spinnerDealer.setSelection(deal.getDealer().ordinal());
        spinnerVulnerability.setSelection(deal.getVulnerability().ordinal());
        for (int i = 0; i < handViews.length; i++)
        {
            handViews[i].setHand(deal.getHands()[i]);
        }

        if (game.getDoubleDummyAnalysis() == null && deal.isValidStartDeal())
        {
            buttonAnalyze.setVisibility(VISIBLE);
        }
        else
        {
            buttonAnalyze.setVisibility(GONE);
        }

        if (game.getDoubleDummyAnalysis() != null)
        {
            tableAnalyze.setVisibility(VISIBLE);
            int[][] analysis = game.getDoubleDummyAnalysis().getTable();
            for (int i = 0; i < textAnalysis.length; i++)
            {
                int rowInResults = i == 4 ? i : 3 - i;
                for (int j = 0; j < textAnalysis[i].length; j++)
                {
                    textAnalysis[i][j].setText("" + analysis[rowInResults][j]);
                }
            }
        }
        else
        {
            tableAnalyze.setVisibility(GONE);
        }
    }
}
