package com.fnee.carddetector.common;

/**
 * Created by Floris on 27-4-2017.
 */

import com.fnee.pbn.*;

import java.io.FileOutputStream;
import java.util.Collection;

public class PbnWriteAdapter {

    private static int positionToPbnSide(Position position)
    {
        return (position.ordinal() + 2) % PbnSide.NUMBER;
    }

    private static int vulnerabilityToPbnVulnerability(Vulnerability vulnerability)
    {
        return vulnerability.ordinal();
    }

    private static int rankToPbnRank(Rank rank)
    {
        return 1 << (rank.ordinal() + 1);
    }

    private static int suitToPbnSuit(Suit suit)
    {
        return 3 - suit.ordinal();
    }

    public static void writeGame(Game game, FileOutputStream fos)
    {
        PbnExport export = new PbnExport();
        export.NoCR(true);
        export.SetExportFile(fos);
        Deal deal = game.getDeal();
        PbnGameData pbnGame = new PbnGameData();
        PbnGameTags pbnTags = new PbnGameTags();
        pbnTags.SetTagValue(new PbnTagId(PbnTagId.EVENT), game.getEvent());
        pbnTags.SetTagValue(new PbnTagId(PbnTagId.BOARD), "" + game.getBoardNumber());

        PbnSituation pbnSituation = pbnGame.GetSituation();
        pbnSituation.GetDealer().Set(positionToPbnSide(deal.getDealer()));
        pbnSituation.GetVulner().Set(vulnerabilityToPbnVulnerability(deal.getVulnerability()));

        PbnDeal pbnDeal = pbnGame.GetDeal();
        for (Position pos : Position.values())
        {
            PbnSide pbnSide = new PbnSide(positionToPbnSide(pos));
            PbnHand pbnHand = pbnDeal.GetHand(pbnSide);
            Hand hand = deal.getHand(pos);
            for (Card c : hand.getCards()) {
                pbnHand.AddRank(new PbnSuit(suitToPbnSuit(c.getSuit())), new PbnRank(rankToPbnRank(c.getRank())));
            }
        }

        if (game.getDoubleDummyAnalysis() != null)
        {
            DoubleDummyAnalysis analysis = game.getDoubleDummyAnalysis();
            PbnTable pbnTable = pbnTags.GetOptimumResultTable();

            pbnTable.SetHeader(new String[] {"Declarer", "Denomination\\2R", "Result\\2R" });
            pbnTags.SetTagValue(new PbnTagId(PbnTagId.OPTIMUMRESULTTABLE), "Declarer;Denomination\\2R;Result\\2R");
            for (Position pos : Position.values())
            {
                for (Strain strain : Strain.values())
                {
                    pbnTable.AddEntry(new String[] { "" + pos.getShortName(), strain.getShortName(), "" + analysis.getNrOfAvailableTricks(strain, pos) });
                }
            }
        }

        export.Write(pbnGame, pbnTags);
        export.Flush();
    }

    public static void writeGames(Collection<Game> games, FileOutputStream fos)
    {
        for (Game game : games)
        {
            writeGame(game, fos);
        }
    }
}
