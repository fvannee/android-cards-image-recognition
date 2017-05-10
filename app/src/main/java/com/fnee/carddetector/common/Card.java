package com.fnee.carddetector.common;

import java.io.Serializable;

/**
 * Created by Floris on 24-4-2017.
 */

public class Card implements Comparable<Card>, Serializable {
    private Suit suit;
    private Rank rank;

    private Card(Suit suit, Rank rank)
    {
        this.suit = suit;
        this.rank = rank;
    }

    public static Card getCard(Suit suit, Rank rank)
    {
        return new Card(suit, rank);
    }

    public String getShortName()
    {
        return "" + suit.getShortName() + rank.getShortName();
    }

    public Suit getSuit() { return suit; }

    public Rank getRank() { return rank; }

    @Override
    public int compareTo(Card o) {
        int cmp = suit.compareTo(o.suit);
        if (cmp == 0)
            cmp = rank.compareTo(o.rank);
        return cmp;
    }
}
