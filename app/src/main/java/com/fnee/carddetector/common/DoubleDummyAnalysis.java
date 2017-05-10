package com.fnee.carddetector.common;

import java.io.Serializable;

/**
 * Created by Floris on 28-4-2017.
 */

public class DoubleDummyAnalysis implements Serializable {

    private int[][] table;

    public DoubleDummyAnalysis()
    {
        this.table = new int[Strain.values().length][Position.values().length];
    }

    public int[][] getTable() {
        return table;
    }

    public int getNrOfAvailableTricks(Strain strain, Position declarer)
    {
        return table[strain.ordinal()][declarer.ordinal()];
    }

}
