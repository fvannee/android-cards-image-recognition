package com.fnee.pbn;/*
 * File   :     PbnTrump.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

/**
 * Definition of Trump
 */
public class PbnTrump
{
  public static final int       IDLE     = -1;
  public static final int       CLUBS    = 0;
  public static final int       DIAMONDS = 1;
  public static final int       HEARTS   = 2;
  public static final int       SPADES   = 3;
  public static final int       NOTRUMP  = 4;
  public static final int       NONE     = 5;
  public static final int       NUMBER   = 5;

  private static final String   S_IDLE     = "_";
  private static final String   S_CLUBS    = "C";
  private static final String   S_DIAMONDS = "D";
  private static final String   S_HEARTS   = "H";
  private static final String   S_SPADES   = "S";
  private static final String   S_NOTRUMP  = "NT";
  private static final String   S_NONE     = "";

  private int                   mTrump;

  private int                   TrumpToSuit()
  {
    int                         iSuit;
    
    switch ( mTrump )
    {
    case CLUBS:
      iSuit = PbnSuit.CLUBS;
      break;
    case DIAMONDS:
      iSuit = PbnSuit.DIAMONDS;
      break;
    case HEARTS:
      iSuit = PbnSuit.HEARTS;
      break;
    case SPADES:
      iSuit = PbnSuit.SPADES;
      break;
    default:
      iSuit = PbnSuit.NONE;
      break;
    }
    
    return iSuit;
  }
  
  public                        PbnTrump()
  {
    mTrump = IDLE;
  }

  public                        PbnTrump(
  int                           iTrump )
  {
    mTrump = iTrump;
  }

  public                        PbnTrump(
  PbnTrump                      oTrump )
  {
    mTrump = oTrump.mTrump;
  }

  public int                    Get()
  {
    return mTrump;
  }

  public void                   Set(
  int                           iTrump )
  {
    mTrump = iTrump;
  }

  public void                   Set(
  PbnTrump                      oTrump )
  {
    mTrump = oTrump.mTrump;
  }

  public boolean                equals(
  PbnTrump                      oTrump )
  {
    return mTrump == oTrump.mTrump;
  }

  public boolean                Is(
  int                           iTrump )
  {
    return mTrump == iTrump;
  }

  public boolean                IsTrump(
  PbnSuit                       oSuit )
  {
    if ( ! IsValid() )
    {
      return false;
    }

    PbnSuit                     lSuit = this.ToSuit();

    return oSuit.equals( lSuit );
  }

  public boolean                IsValid()
  {
    return ( CLUBS <= mTrump ) && ( mTrump <= NOTRUMP );
  }

  public PbnSuit                ToSuit()
  {
    return new PbnSuit( TrumpToSuit() );
  }

  public boolean                GT(
  PbnTrump                      oTrump )
  {
    return ( mTrump > oTrump.mTrump );
  }

  public String                 toString()
  {
    String                      string = S_IDLE;
    
    switch ( mTrump )
    {
    case CLUBS:
      string = S_CLUBS;
      break;

    case DIAMONDS:
      string = S_DIAMONDS;
      break;

    case HEARTS:
      string = S_HEARTS;
      break;

    case SPADES:
      string = S_SPADES;
      break;

    case NOTRUMP:
      string = S_NOTRUMP;
      break;

    case NONE:
      string = S_NONE;
      break;
    }

    return string;
  }
}
