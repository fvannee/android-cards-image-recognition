package com.fnee.pbn;/*
 * File   :     PbnSuit.java
 * Author :     Tis Veugen
 * Date   :     1999-02-12
 * PBN    :     1.0
 *
 * History
 * -------
 * 1999-02-12 Added FromChar.
 */

/**
 * Definition of Suit
 */
public class PbnSuit
{
  public static final int       IDLE     = -1;
  public static final int       CLUBS    = 0;
  public static final int       DIAMONDS = 1;
  public static final int       HEARTS   = 2;
  public static final int       SPADES   = 3;
  public static final int       NONE     = 4;
  public static final int       UNKNOWN  = 5;
  public static final int       NUMBER   = 4;

  private static final String   S_IDLE     = "_";
  private static final String   S_CLUBS    = "Clubs";
  private static final String   S_DIAMONDS = "Diamonds";
  private static final String   S_HEARTS   = "Hearts";
  private static final String   S_SPADES   = "Spades";
  private static final String   S_NONE     = "";
  private static final String   S_UNKNOWN  = "?";

  int                           mSuit;

  public                        PbnSuit()
  {
    mSuit = IDLE;
  }

  public                        PbnSuit(
  int                           iSuit )
  {
    mSuit = iSuit;
  }

  public                        PbnSuit(
  PbnSuit                       oSuit )
  {
    mSuit = oSuit.mSuit;
  }

  public                        PbnSuit(
  PbnTrump                      oTrump )
  {
    switch ( oTrump.Get() )
    {
    case PbnTrump.CLUBS:
      mSuit = CLUBS;
      break;
    case PbnTrump.DIAMONDS:
      mSuit = DIAMONDS;
      break;
    case PbnTrump.HEARTS:
      mSuit = HEARTS;
      break;
    case PbnTrump.SPADES:
      mSuit = SPADES;
      break;
    default:
      mSuit = NONE;
      break;
    }
  }

  public int                    Get()
  {
    return mSuit;
  }

  public void                   Set(
  int                           iSuit )
  {
    mSuit = iSuit;
  }

  public void                   Set(
  PbnSuit                       oSuit )
  {
    mSuit = oSuit.mSuit;
  }

  public boolean                Is(
  int                           iSuit )
  {
    return mSuit == iSuit;
  }

  public boolean                IsValid()
  {
    return ( CLUBS <= mSuit ) && ( mSuit <= SPADES );
  }

  public boolean                equals(
  PbnSuit                       oSuit )
  {
    return mSuit == oSuit.mSuit;
  }

  public int                    Next()
  {
    if ( mSuit < SPADES )
    {
      mSuit++;
    }

    return mSuit;
  }

  public int                    Previous()
  {
    if ( mSuit > CLUBS )
    {
      mSuit--;
    }

    return mSuit;
  }

  public String                 toString()
  {
    String                      string = S_IDLE;

    switch ( mSuit )
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

    case NONE:
      string = S_NONE;
      break;

    case UNKNOWN:
      string = S_UNKNOWN;
      break;
    }

    return string;
  }

  public String                 toCharacter()
  {
    String                      string = this.toString();

    if ( string.length() == 0 )
    {
      return string;
    }
    return string.substring( 0, 1 );
  }

  public static int             FromChar(
  char                          cSuit )
  {
    int                         suit;

    switch ( cSuit )
    {
    case 's':
    case 'S':
      suit = PbnSuit.SPADES;
      break;
    case 'h':
    case 'H':
      suit = PbnSuit.HEARTS;
      break;
    case 'd':
    case 'D':
      suit = PbnSuit.DIAMONDS;
      break;
    case 'c':
    case 'C':
      suit = PbnSuit.CLUBS;
      break;
    default:
      suit = PbnSuit.IDLE;
      break;
    }

    return suit;
  }
}
