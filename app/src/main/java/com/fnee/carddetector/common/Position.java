package com.fnee.carddetector.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Floris on 24-4-2017.
 */

public enum Position {
    North('N'),
    East('E'),
    South('S'),
    West('W');

    private char shortName;

    Position(char shortName)
    {
        this.shortName = shortName;
    }

    public char getShortName()
    {
        return shortName;
    }

    public static Position getPosition(char position) {
        if (positionMap == null)
            initMap();
        return positionMap.get(position);
    }

    private static Map<Character, Position> positionMap;

    private static void initMap() {
        positionMap = new HashMap<>();
        for (Position s : Position.values())
        {
            positionMap.put(s.getShortName(), s);
        }
    }
}
