package com.fnee.carddetector.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fnee.carddetector.R;
import com.fnee.carddetector.common.Card;
import com.fnee.carddetector.common.Hand;
import com.fnee.carddetector.common.Position;
import com.fnee.carddetector.common.Rank;
import com.fnee.carddetector.common.Suit;

import java.util.*;

/**
 * Created by Floris on 24-4-2017.
 */

public class HandView extends LinearLayout {
    private TextView[] cardTexts;
    private TextView topText, nrCardsText;
    private SuitLongClickListener suitLongClickListener;

    private Position position;
    private SortedSet<Rank>[] ranks;

    public HandView(Context context) {
        super(context);
        initializeViews(context);
    }

    public HandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
        parseAttrs(context, attrs);
    }

    public HandView(Context context,
                       AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
        parseAttrs(context, attrs);
    }

    private void parseAttrs(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HandView,
                0, 0);

        try {
            Position[] positions = Position.values();
            position = positions[a.getInteger(R.styleable.HandView_positionInDeal, 0)];
        } finally {
            a.recycle();
        }
    }

    private void initializeViews(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(getResources().getColor(R.color.opaqueBackground));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.hand_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Suit[] suits = Suit.values();
        int numSuits = suits.length;

        nrCardsText = (TextView)findViewById(R.id.text_nr_cards);

        cardTexts = new TextView[numSuits];
        cardTexts[Suit.Spades.ordinal()] = (TextView)findViewById(R.id.text_spades);
        cardTexts[Suit.Hearts.ordinal()] = (TextView)findViewById(R.id.text_hearts);
        cardTexts[Suit.Diamonds.ordinal()] = (TextView)findViewById(R.id.text_diamonds);
        cardTexts[Suit.Clubs.ordinal()] = (TextView)findViewById(R.id.text_clubs);

        topText = (TextView)findViewById(R.id.text_top);

        ranks = new SortedSet[numSuits];
        for (final Suit s : suits)
        {
            ranks[s.ordinal()] = new TreeSet<>(Collections.reverseOrder());
            updateSuitText(s);

            cardTexts[s.ordinal()].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    HandView.this.callOnClick();
                }
            });
            cardTexts[s.ordinal()].setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean ret = false;
                    if (suitLongClickListener != null)
                    {
                        ret = suitLongClickListener.onSuitLongClick(HandView.this, s);
                    }
                    return ret;
                }
            });
        }

        setPosition(position);
    }

    public void setSuitLongClickListener(SuitLongClickListener suitLongClickListener)
    {
        this.suitLongClickListener = suitLongClickListener;
    }

    public SortedSet<Rank> getCards(Suit suit)
    {
        return ranks[suit.ordinal()];
    }

    public void setHand(Hand hand)
    {
        for (int i = 0; i < ranks.length; i++)
        {
            ranks[i].clear();
        }
        for (Card card : hand.getCards())
        {
            ranks[card.getSuit().ordinal()].add(card.getRank());
        }
        for (Suit s : Suit.values())
        {
            updateSuitText(s);
        }
        nrCardsText.setText("" + hand.getCards().size());
    }

    public void setPosition(Position position)
    {
        this.position = position;
        topText.setText(position.toString());
    }

    private void updateSuitText(Suit suit)
    {
        TextView tv = cardTexts[suit.ordinal()];
        StringBuilder sb = new StringBuilder();
        sb.append(suit.getShortName());
        sb.append(": ");
        for (Rank r : ranks[suit.ordinal()])
        {
            sb.append(r.getShortName());
        }
        tv.setText(sb.toString());
    }

    public Position getPosition() {
        return position;
    }

    public void clear() {
        for (int i = 0; i < ranks.length; i++)
        {
            ranks[i].clear();
            updateSuitText(Suit.values()[i]);
        }
        nrCardsText.setText("0");
    }

    public void setFocus(boolean toFocus)
    {
        setBackgroundResource(toFocus ? R.drawable.border : 0);
    }
}
