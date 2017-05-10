package com.fnee.carddetector.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Floris on 24-4-2017.
 */

public enum Rank {
    Two('2'),
    Three('3'),
    Four('4'),
    Five('5'),
    Six('6'),
    Seven('7'),
    Eight('8'),
    Nine('9'),
    Ten('T'),
    Jack('J'),
    Queen('Q'),
    King('K'),
    Ace('A');

    private char shortName;

    Rank(char shortName)
    {
        this.shortName = shortName;
    }

    public char getShortName()
    {
        return shortName;
    }


    public static Rank getRank(char rank) {
        if (rankMap == null)
            initMap();
        return rankMap.get(rank);
    }

    private static Map<Character, Rank> rankMap;

    private static void initMap() {
        rankMap = new HashMap<>();
        for (Rank s : Rank.values())
        {
            rankMap.put(s.getShortName(), s);
        }
    }

}
