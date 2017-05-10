package com.fnee.carddetector.common;

import java.io.Serializable;

/**
 * Created by Floris on 27-4-2017.
 */

public class Game implements Serializable {

    private DoubleDummyAnalysis doubleDummyAnalysis;
    private Deal deal;
    private int boardNumber;
    private String event;

    public Game()
    {
        deal = new Deal();
        boardNumber = 1;
        event = "";
    }

    public DoubleDummyAnalysis getDoubleDummyAnalysis() {
        return doubleDummyAnalysis;
    }

    public void setDoubleDummyAnalysis(DoubleDummyAnalysis doubleDummyAnalysis) {
        this.doubleDummyAnalysis = doubleDummyAnalysis;
    }

    public Deal getDeal() {
        return deal;
    }

    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    public int getBoardNumber() {
        return boardNumber;
    }

    public void setBoardNumber(int boardNumber) {
        this.boardNumber = boardNumber;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return getEvent() + " (" + getBoardNumber() + ")";
    }

}
