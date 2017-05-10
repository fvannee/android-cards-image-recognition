package com.fnee.carddetector.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Floris on 24-4-2017.
 */

public enum Suit {
    Spades('S'),
    Hearts('H'),
    Diamonds('D'),
    Clubs('C');

    private char shortName;

    Suit(char shortName)
    {
        this.shortName = shortName;
    }

    public char getShortName()
    {
        return shortName;
    }

    public static Suit getSuit(char suit) {
        if (suitMap == null)
            initMap();
        return suitMap.get(suit);
    }

    private static Map<Character, Suit> suitMap;

    private static void initMap() {
        suitMap = new HashMap<>();
        for (Suit s : Suit.values())
        {
            suitMap.put(s.getShortName(), s);
        }
    }
}
