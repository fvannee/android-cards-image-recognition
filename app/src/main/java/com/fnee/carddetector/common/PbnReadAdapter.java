package com.fnee.carddetector.common;

import com.fnee.pbn.PbnDeal;
import com.fnee.pbn.PbnError;
import com.fnee.pbn.PbnExport;
import com.fnee.pbn.PbnGameData;
import com.fnee.pbn.PbnGameTags;
import com.fnee.pbn.PbnHand;
import com.fnee.pbn.PbnImport;
import com.fnee.pbn.PbnInherit;
import com.fnee.pbn.PbnInputStream;
import com.fnee.pbn.PbnRank;
import com.fnee.pbn.PbnRanks;
import com.fnee.pbn.PbnSide;
import com.fnee.pbn.PbnSituation;
import com.fnee.pbn.PbnSuit;
import com.fnee.pbn.PbnTagId;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Floris on 27-4-2017.
 */

public class PbnReadAdapter {
    private static Position pbnSideToPosition(int pbnSide)
    {
        return Position.values()[(pbnSide + 2) % PbnSide.NUMBER];
    }

    private static Vulnerability pbnVulnerabilityToVulnerability(int pbnVulnerability)
    {
        return Vulnerability.values()[pbnVulnerability];
    }

    private static Set<Rank> pbnRanksToRanks(int pbnRank)
    {
        Set<Rank> ranks = new TreeSet<Rank>();
        Rank[] values = Rank.values();
        int rank = 0;
        while (pbnRank > 1)
        {
            if ((pbnRank & 0x2) != 0)
            {
                ranks.add(values[rank]);
            }
            pbnRank >>= 1;
            rank++;
        }
        return ranks;
    }

    private static Suit pbnSuitToSuit(int pbnSuit)
    {
        return Suit.values()[3 - pbnSuit];
    }

    public static ArrayList<Game> readGames(InputStream fos)
    {
        ArrayList<Game> games = new ArrayList<>();
        boolean readMoreDeals = true;
        PbnImport pbnImport = new PbnImport();
        PbnInputStream pbnInput = new PbnInputStream(fos);
        pbnImport.SetInputFile(pbnInput);

        while (readMoreDeals) {
            PbnGameData pbnGame = new PbnGameData();
            PbnGameTags pbnTags = new PbnGameTags();
            PbnInherit pbnInherit = new PbnInherit();
            pbnImport.SetInherit(pbnInherit);
            PbnError error = pbnImport.Read(pbnInput.Tell(), pbnGame, pbnTags);

            if (error.HasSeverity(PbnError.SEV_FATAL)) {
                throw new RuntimeException("Unable to read pbn input file: " + error.toString());
            }

            if (!error.Is(PbnError.NO_TAG)) {
                Game game = new Game();
                game.setEvent(pbnTags.GetTagValue(new PbnTagId(PbnTagId.EVENT)));
                try {
                    game.setBoardNumber(Integer.parseInt(pbnTags.GetTagValue(new PbnTagId(PbnTagId.BOARD))));
                }
                catch (NumberFormatException e) {
                    game.setBoardNumber(-1);
                }

                Deal deal = game.getDeal();
                PbnSituation pbnSituation = pbnGame.GetSituation();
                deal.setDealer(pbnSideToPosition(pbnSituation.GetDealer().Get()));
                deal.setVulnerability(pbnVulnerabilityToVulnerability(pbnSituation.GetVulner().Get()));

                PbnDeal pbnDeal = pbnGame.GetDeal();
                for (int i = 0; i < PbnSide.NUMBER; i++) {
                    PbnSide pbnSide = new PbnSide(i);
                    PbnHand pbnHand = pbnDeal.GetHand(pbnSide);
                    Position pos = pbnSideToPosition(i);
                    Hand hand = deal.getHand(pos);
                    for (int j = 0; j < PbnSuit.NUMBER; j++) {
                        PbnSuit pbnSuit = new PbnSuit(j);
                        Suit suit = pbnSuitToSuit(j);
                        PbnRanks pbnRanks = pbnHand.GetRanks(pbnSuit);
                        Set<Rank> ranks = pbnRanksToRanks(pbnRanks.Get());
                        for (Rank rank : ranks) {
                            hand.addCard(Card.getCard(suit, rank));
                        }
                    }
                }

                if (pbnTags.UsedTagValue(PbnTagId.OPTIMUMRESULTTABLE))
                {
                    DoubleDummyAnalysis analysis = new DoubleDummyAnalysis();
                    Enumeration<String[]> e = pbnTags.GetOptimumResultTable().GetRows();
                    while (e.hasMoreElements())
                    {
                        String[] row = e.nextElement();
                        Position pos = Position.getPosition(row[0].charAt(0));
                        Strain strain = Strain.getStrain(row[1]);
                        analysis.getTable()[strain.ordinal()][pos.ordinal()] = Integer.parseInt(row[2]);
                    }
                    game.setDoubleDummyAnalysis(analysis);
                }
                games.add(game);
            }
            else
            {
                readMoreDeals = false;
            }
        }
        return games;
    }
}
