package com.fnee.carddetector.ui;

import com.fnee.carddetector.common.Card;
import com.fnee.carddetector.common.Deal;
import com.fnee.carddetector.common.Position;
import com.fnee.carddetector.common.Rank;
import com.fnee.carddetector.common.Suit;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Floris on 26-4-2017.
 */

public interface DealUpdateable {
    boolean addCardsToHand(Position position, Collection<Card> cards);
    void removeCardsFromHand(Position position, Collection<Card> card);
    void removeCardFromHand(Position position, Card card);
    boolean addCardToHand(Position position, Card card);
    void setDeal(Deal deal);
    void clearAllCards();
}
