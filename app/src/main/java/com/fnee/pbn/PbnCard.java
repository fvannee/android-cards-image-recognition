package com.fnee.pbn;/*
 * File   :     PbnCard.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

public class PbnCard
{
  PbnSuit                       mSuit;
  PbnRank                       mRank;

  public                        PbnCard()
  {
    mSuit = new PbnSuit();
    mRank = new PbnRank();
  }

  public                        PbnCard(
  PbnCard                       oCard )
  {
    mSuit = new PbnSuit( oCard.mSuit );
    mRank = new PbnRank( oCard.mRank );
  }

  public                        PbnCard(
  PbnSuit                       oSuit,
  PbnRank                       oRank )
  {
    mSuit = new PbnSuit( oSuit );
    mRank = new PbnRank( oRank );
  }

  public PbnSuit                GetSuit()
  {
    return mSuit;
  }

  public PbnRank                GetRank()
  {
    return mRank;
  }

  public void                   Set(
  PbnCard                       oCard )
  {
    mSuit.Set( oCard.mSuit.Get() );
    mRank.Set( oCard.mRank.Get() );
  }

  public boolean                IsUsed()
  {
    return (! mRank.IsNone());
  }

  public String                 toString()
  {
    if ( ! mRank.IsNormal() )
    {
      return "-";
    }

    return mSuit.toCharacter() + mRank.toCharacter();
  }
}
