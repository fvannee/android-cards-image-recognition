package com.fnee.carddetector.common;

import java.util.Collection;

/**
 * Created by Floris on 28-4-2017.
 */

public class DdsAdapter {

    static {
        System.loadLibrary("native-lib");
    }

    private static int  DDS_HANDS = 4;
    private static int  DDS_SUITS = 4;
    private static int  DDS_STRAINS = 5;
    private static int  MAXNOOFBOARDS = 200;
    private static int  MAXNOOFTABLES = 32;

    private static int suitToDdsSuit(Suit suit)
    {
        return suit.ordinal();
    }

    private static int positionToDdsPosition(int position)
    {
        return position;
    }

    private static int ddsPositionToPosition(int ddsPosition)
    {
        return ddsPosition;
    }

    private static int ddsStrainToStrain(int ddsStrain)
    {
        return ddsStrain;
    }

    private static int rankToDdsRank(Rank rank)
    {
        return 1 << (rank.ordinal() + 2);
    }

    private static void ranksToDdsRanks(Collection<Card> cards, int[] ddsCards, int offset)
    {
        for (Card c : cards)
        {
            int suit = suitToDdsSuit(c.getSuit());
            ddsCards[offset + suit] |= rankToDdsRank(c.getRank());
        }
    }

    public static DoubleDummyAnalysis calcTable(Deal deal)
    {
        int[] cards = new int[DDS_HANDS*DDS_SUITS];
        int[] results = new int[DDS_STRAINS*DDS_HANDS];
        for (int i = 0; i < deal.getHands().length; i++)
        {
            Hand h = deal.getHands()[i];
            ranksToDdsRanks(h.getCards(), cards, positionToDdsPosition(i) * DDS_SUITS);
        }
        int result = nativeCalcTable(cards, results);
        if (result != 1)
            throw new RuntimeException("Unable to calculate table for hand. Error: " + result);

        DoubleDummyAnalysis analysis = new DoubleDummyAnalysis();
        int [][] table = analysis.getTable();
        for (int i = 0; i < results.length; i++)
        {
            int row = ddsStrainToStrain(i / DDS_HANDS);
            int col = ddsPositionToPosition(i % DDS_HANDS);
            table[row][col] = results[i];
        }
        return analysis;
    }

    public static void releaseInternalMemory()
    {
        nativeReleaseInternalMemory();
    }

    private static native int nativeCalcTable(int[] cards, int[] results);

    private static native void nativeReleaseInternalMemory();

}
