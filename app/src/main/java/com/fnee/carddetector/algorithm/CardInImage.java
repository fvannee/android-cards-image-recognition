package com.fnee.carddetector.algorithm;

import com.fnee.carddetector.common.Card;

import org.opencv.core.Point;

/**
 * Created by Floris on 24-3-2017.
 */

public class CardInImage {
    private Card card;
    private Point point;

    public CardInImage(Card card, Point point)
    {
        this.card = card;
        this.point = point;
    }

    public Point getPoint()
    {
        return point;
    }

    public Card getCard()
    {
        return card;
    }
}
