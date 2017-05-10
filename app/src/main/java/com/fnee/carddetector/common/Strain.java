package com.fnee.carddetector.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Floris on 28-4-2017.
 */

public enum Strain {
    Spades("S"),
    Hearts("H"),
    Diamonds("D"),
    Clubs("C"),
    NoTrump("NT");

    private String shortName;

    Strain(String shortName)
    {
        this.shortName = shortName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public static Strain getStrain(String strain) {
        if (strainMap == null)
            initMap();
        return strainMap.get(strain);
    }

    private static Map<String, Strain> strainMap;

    private static void initMap() {
        strainMap = new HashMap<>();
        for (Strain s : Strain.values())
        {
            strainMap.put(s.getShortName(), s);
        }
    }
}
