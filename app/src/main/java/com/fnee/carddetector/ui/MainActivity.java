package com.fnee.carddetector.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.view.Window;

import com.fnee.carddetector.R;
import com.fnee.carddetector.common.Card;
import com.fnee.carddetector.common.DdsAdapter;
import com.fnee.carddetector.common.Deal;
import com.fnee.carddetector.common.Hand;
import com.fnee.carddetector.common.Position;

import java.util.Collection;

public class MainActivity extends FragmentActivity implements DealRetrievable, DealUpdateable {

    private static final String CURRENT_DEAL = "CURRENT_DEAL";

    CardCollectionPagerAdapter cardCollectionPagerAdapter;
    ViewPager viewPager;

    Deal currentDeal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
        {
            currentDeal = (Deal)savedInstanceState.getSerializable(CURRENT_DEAL);
        }
        else
        {
            Position[] positions = Position.values();
            currentDeal = new Deal();
        }

        cardCollectionPagerAdapter =
                new CardCollectionPagerAdapter(
                        getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(cardCollectionPagerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // save current instance state
        super.onSaveInstanceState(outState);

        outState.putSerializable(CURRENT_DEAL, currentDeal);
    }

    @Override
    public boolean addCardsToHand(Position position, Collection<Card> cards) {
        cards = currentDeal.filterCardsInHand(cards);
        if (currentDeal.getHand(position).addCards(cards)) {
            notifyCardUpdateListeners();
            return true;
        }
        return false;
    }

    @Override
    public void removeCardsFromHand(Position position, Collection<Card> cards) {
        if (currentDeal.getHand(position).removeCards(cards))
            notifyCardUpdateListeners();
    }

    @Override
    public void removeCardFromHand(Position position, Card card) {
        if (currentDeal.getHand(position).removeCard(card))
            notifyCardUpdateListeners();
    }

    @Override
    public boolean addCardToHand(Position position, Card card) {
        if (!currentDeal.isCardInHand(card)) {
            currentDeal.getHand(position).addCard(card);
            notifyCardUpdateListeners();
            return true;
        }
        return false;
    }

    @Override
    public void setDeal(Deal deal) {
        currentDeal = deal;
        notifyCardUpdateListeners();
    }

    @Override
    public void clearAllCards() {
        for (Hand hand : currentDeal.getHands()){
            hand.clearAll();
        }
        notifyCardUpdateListeners();
    }

    @Override
    public Deal getDeal() {
        return currentDeal;
    }

    private void notifyCardUpdateListeners()
    {
        for (int i = 0; i < cardCollectionPagerAdapter.getCount(); i++)
        {
            Fragment f = cardCollectionPagerAdapter.getRegisteredFragment(i);
            if (f != null && f instanceof DealUpdateListener)
            {
                ((DealUpdateListener)f).onDealUpdated(currentDeal);
            }
        }
    }

    public static class CardCollectionPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> mRegisteredFragments = new SparseArray<Fragment>();

        public CardCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            if (i == 1) {
                fragment = new ScanCardFragment();
            }
            else {
                fragment = new ShowHandsFragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mRegisteredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mRegisteredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return mRegisteredFragments.get(position);
        }
    }
}
