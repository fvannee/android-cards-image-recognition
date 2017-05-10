package com.fnee.carddetector;

import com.fnee.carddetector.common.Card;
import com.fnee.carddetector.common.Deal;
import com.fnee.carddetector.common.Game;
import com.fnee.carddetector.common.PbnReadAdapter;
import com.fnee.carddetector.common.PbnWriteAdapter;
import com.fnee.carddetector.common.Position;
import com.fnee.carddetector.common.Rank;
import com.fnee.carddetector.common.Suit;
import com.fnee.carddetector.common.Vulnerability;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void writeDealToFile() throws Exception {
        ArrayList<Game> games = new ArrayList<>();
        Game g = new Game();
        g.setEvent("test");
        g.setBoardNumber(2);
        Deal d = g.getDeal();
        d.setVulnerability(Vulnerability.All);
        d.setDealer(Position.South);
        d.getHands()[0].addCard(Card.getCard(Suit.Clubs, Rank.Ace));

        Game g2 = new Game();
        Deal d2 = g2.getDeal();
        d2.setVulnerability(Vulnerability.EW);
        d2.setDealer(Position.West);
        d2.getHands()[0].addCard(Card.getCard(Suit.Spades, Rank.Ace));
        d2.getHands()[0].addCard(Card.getCard(Suit.Clubs, Rank.Queen));
        d2.getHands()[0].addCard(Card.getCard(Suit.Diamonds, Rank.Ten));
        d2.getHands()[1].addCard(Card.getCard(Suit.Hearts, Rank.Two));
        d2.getHands()[1].addCard(Card.getCard(Suit.Hearts, Rank.Four));
        d2.getHands()[1].addCard(Card.getCard(Suit.Hearts, Rank.Three));
        d2.getHands()[2].addCard(Card.getCard(Suit.Diamonds, Rank.Two));
        d2.getHands()[2].addCard(Card.getCard(Suit.Diamonds, Rank.Four));
        d2.getHands()[3].addCard(Card.getCard(Suit.Diamonds, Rank.Three));
        games.add(g);
        games.add(g2);
        games.add(g2);

        FileOutputStream f = new FileOutputStream("testFile.pbn");
        PbnWriteAdapter.writeGames(games, f);
        f.close();

        ArrayList<Game> games2 = PbnReadAdapter.readGames(new FileInputStream("testFile.pbn"));

        FileOutputStream f2 = new FileOutputStream("testFile2.pbn");
        PbnWriteAdapter.writeGames(games2, f2);
        f2.close();

    }
}