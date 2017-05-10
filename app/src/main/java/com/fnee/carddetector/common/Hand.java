package com.fnee.carddetector.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Floris on 27-4-2017.
 */

public class Hand implements Serializable {
    private TreeSet<Card> cards;

    public Hand()
    {
        cards = new TreeSet<Card>();
    }

    public Set<Card> getCards() {
        return cards;
    }

    public boolean addCard(Card card) {
        return cards.add(card);
    }

    public boolean addCards(Collection<Card> cards) { return this.cards.addAll(cards); }

    public boolean removeCards(Collection<Card> cards) { return this.cards.removeAll(cards); }

    public boolean removeCard(Card card) {
        return cards.remove(card);
    }

    public void setCards(Collection<Card> cardsToClear)
    {
        cards.clear();
        cards.addAll(cardsToClear);
    }

    public void clearAll() {
        cards.clear();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Hand)
        {
            Hand other = (Hand)obj;
            return other.cards.equals(cards);
        }
        return false;
    }
}
