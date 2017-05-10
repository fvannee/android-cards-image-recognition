package com.fnee.carddetector.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Floris on 27-4-2017.
 */

public class Deal implements Serializable {
    private Position dealer;
    private Vulnerability vulnerability;
    private Hand[] hands;

    public Deal()
    {
        dealer = Position.North;
        vulnerability = Vulnerability.None;
        hands = new Hand[Position.values().length];
        for (int i = 0; i < hands.length; i++)
            hands[i] = new Hand();
    }

    public Hand[] getHands() {
        return hands;
    }

    public Hand getHand(Position position)
    {
        return hands[position.ordinal()];
    }

    public void setHands(Hand[] hands) {
        this.hands = hands;
    }

    public Vulnerability getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(Vulnerability vulnerability) {
        this.vulnerability = vulnerability;
    }

    public Position getDealer() {
        return dealer;
    }

    public void setDealer(Position dealer) {
        this.dealer = dealer;
    }

    public void clear() {
        dealer = Position.North;
        vulnerability = Vulnerability.None;
        for (int i = 0; i < hands.length; i++)
            hands[i].clearAll();
    }

    public boolean isValidStartDeal()
    {
        int cards[] = new int[Suit.values().length];
        boolean valid = true;
        int numRanks = Rank.values().length;
        for (int i = 0; i < getHands().length && valid; i++)
        {
            Set<Card> cardsHand = getHands()[i].getCards();
            valid = cardsHand.size() == numRanks;
            for (Card c : cardsHand)
            {
                valid = (cards[c.getSuit().ordinal()] & (1 << c.getRank().ordinal())) == 0;
                if (!valid)
                    break;
                cards[c.getSuit().ordinal()] |= (1 << c.getRank().ordinal());
            }
        }
        return valid;
    }

    public Set<Card> filterCardsInHand(Collection<Card> cards)
    {
        Set<Card> ret = new TreeSet<>();
        for(Card c : cards)
        {
            if (!isCardInHand(c))
                ret.add(c);
        }
        return ret;
    }

    public boolean isCardInHand(Card card)
    {
        boolean inUse = false;
        for (int i = 0; i < hands.length && !inUse; i++)
        {
            inUse = hands[i].getCards().contains(card);
        }
        return inUse;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Deal)
        {
            Deal other = (Deal)obj;
            return other.dealer == dealer && other.vulnerability == vulnerability && Arrays.equals(other.hands, hands);
        }
        return false;
    }
}
