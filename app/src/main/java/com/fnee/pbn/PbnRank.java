package com.fnee.pbn;/*
 * File   :     PbnRank.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 *
 * History
 * -------
 * 1999-02-12 Added FromChar.
 */

/**
 * Definition of Rank
 */
public class PbnRank
{
  public static final int       NONE    = 0x0000;
  public static final int       MIN     = 0x0001;
  public static final int       TWO     = 0x0002;
  public static final int       THREE   = 0x0004;
  public static final int       FOUR    = 0x0008;
  public static final int       FIVE    = 0x0010;
  public static final int       SIX     = 0x0020;
  public static final int       SEVEN   = 0x0040;
  public static final int       EIGHT   = 0x0080;
  public static final int       NINE    = 0x0100;
  public static final int       TEN     = 0x0200;
  public static final int       JACK    = 0x0400;
  public static final int       QUEEN   = 0x0800;
  public static final int       KING    = 0x1000;
  public static final int       ACE     = 0x2000;
  public static final int       HIDDEN  = 0x4000;
  public static final int       UNKNOWN = 0x8000;
  public static final int       ALL     = 0x3ffe;
  public static final int       NUMBER  = 13;

  private static String         RankNames = "-23456789TJQKA?_";

  private int                   mRank;

  public                        PbnRank()
  {
    mRank = NONE;
  }

  public                        PbnRank(
  int                           iRank )
  {
    mRank = iRank;
  }

  public                        PbnRank(
  PbnRank                       oRank )
  {
    mRank = oRank.mRank;
  }

  public int                    Get()
  {
    return mRank;
  }

  public void                   Set(
  int                           iRank )
  {
    mRank = iRank;
  }

  public void                   Set(
  PbnRank                       oRank )
  {
    mRank = oRank.mRank;
  }

  public boolean                LT(
  PbnRank                       oRank )
  {
    return mRank < oRank.mRank;
  }

  public boolean                IsNone()
  {
    return mRank == NONE;
  }

  public boolean                IsUnknown()
  {
    return mRank == UNKNOWN;
  }

  public boolean                IsNormal()
  {
    return (mRank != NONE) && ((mRank & ALL) == mRank);
  }

  public boolean                Has(
  int                           iRank )
  {
    return (mRank & iRank) != 0;
  }

  public int                    Next()
  {
    if ( mRank <= ACE )
    {
      mRank <<= 1;
    }

    return mRank;
  }

  public int                    Previous()
  {
    if ( mRank >= TWO )
    {
      mRank >>= 1;
    }

    return mRank;
  }

  public String                 toCharacter()
  {
    int           Index;

    switch ( mRank )
    {
    case NONE:
      Index = 0;
      break;
    case TWO:
      Index = 1;
      break;
    case THREE:
      Index = 2;
      break;
    case FOUR:
      Index = 3;
      break;
    case FIVE:
      Index = 4;
      break;
    case SIX:
      Index = 5;
      break;
    case SEVEN:
      Index = 6;
      break;
    case EIGHT:
      Index = 7;
      break;
    case NINE:
      Index = 8;
      break;
    case TEN:
      Index = 9;
      break;
    case JACK:
      Index = 10;
      break;
    case QUEEN:
      Index = 11;
      break;
    case KING:
      Index = 12;
      break;
    case ACE:
      Index = 13;
      break;
    case HIDDEN:
      Index = 14;
      break;
    case UNKNOWN:
    default:
      Index = 15;
      break;
    }

    return RankNames.substring( Index, Index+1 );
  }

  public static int             FromChar(
  char                          cRank )
  {
    int                         rank  = PbnRank.NONE;

    switch ( cRank )
    {
    case '2':
      rank = PbnRank.TWO;
      break;
    case '3':
      rank = PbnRank.THREE;
      break;
    case '4':
      rank = PbnRank.FOUR;
      break;
    case '5':
      rank = PbnRank.FIVE;
      break;
    case '6':
      rank = PbnRank.SIX;
      break;
    case '7':
      rank = PbnRank.SEVEN;
      break;
    case '8':
      rank = PbnRank.EIGHT;
      break;
    case '9':
      rank = PbnRank.NINE;
      break;
    case 't':
    case 'T':
      rank = PbnRank.TEN;
      break;
    case 'j':
    case 'J':
      rank = PbnRank.JACK;
      break;
    case 'q':
    case 'Q':
      rank = PbnRank.QUEEN;
      break;
    case 'k':
    case 'K':
      rank = PbnRank.KING;
      break;
    case 'a':
    case 'A':
      rank = PbnRank.ACE;
      break;
    case '?':
      rank = PbnRank.HIDDEN;
      break;
    case '-':
    default:
      break;
    }

    return rank;
  }
}
