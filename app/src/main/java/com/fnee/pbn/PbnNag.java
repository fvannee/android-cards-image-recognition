package com.fnee.pbn;/*
 * File   :     PbnNag.java
 * Author :     Tis Veugen
 * Date   :     2007-06-24
 * PBN    :     2.1
 *
 * History
 * -------
 * 1998-10-25 Fixed nag checking.
 * 1999-07-10 Added new nag checking.
 * 2007-06-24 Added VERSION_21
 */

public class PbnNag
{
  public static final int       NONE                 = 0;
  public static final int       AUCTION_GOOD         = 1;
  public static final int       AUCTION_POOR         = 2;
  public static final int       AUCTION_VERY_GOOD    = 3;
  public static final int       AUCTION_VERY_POOR    = 4;
  public static final int       AUCTION_SPECULATIVE  = 5;
  public static final int       AUCTION_QUESTIONABLE = 6;
  public static final int       PLAY_GOOD            = 7;
  public static final int       PLAY_POOR            = 8;
  public static final int       PLAY_VERY_GOOD       = 9;
  public static final int       PLAY_VERY_POOR       = 10;
  public static final int       PLAY_SPECULATIVE     = 11;
  public static final int       PLAY_QUESTIONABLE    = 12;
  public static final int       CALL_CORRECTED       = 13;
  public static final int       CARD_CORRECTED       = 14;
  public static final int       CALL_EXPLAIN_FIRST   = 20;
  public static final int       CALL_EXPLAIN_LAST    = 93;
  public static final int       CARD_EXPLAIN_FIRST   = 200;
  public static final int       CARD_EXPLAIN_LAST    = 238;
  public static final int       NUMBER               = 256;

  private int                   mNag;

  public                        PbnNag()
  {
    mNag = NONE;
  }

  public                        PbnNag(
  int                           iNag )
  {
    mNag = iNag;
  }

  public                        PbnNag(
  PbnNag                        oNag )
  {
    mNag = oNag.mNag;
  }

  public int                    Get()
  {
    return mNag;
  }

  public void                   Set(
  PbnNag                        oNag )
  {
    mNag = oNag.mNag;
  }

  public void                   Set(
  int                           iNag )
  {
    mNag = iNag;
  }

  public boolean                equals(
  PbnNag                        oNag )
  {
    return mNag == oNag.mNag;
  }

  public boolean                IsSuffix()
  {
    return IsAuctionSuffix() || IsPlaySuffix();
  }

  public boolean                IsValid()
  {
    return ( (0 <= mNag) && (mNag < NUMBER) );
  }

  public boolean                IsAuctionNag()
  {
    if ( IsAuctionSuffix() )
    {
      return true;
    }
    if ( PbnGen.GetVersion() != PbnGen.VERSION_10 )
    {
      if ( mNag == CALL_CORRECTED )
      {
        return true;
      }
      if ( (CALL_EXPLAIN_FIRST <= mNag) && (mNag <= CALL_EXPLAIN_LAST) )
      {
        return true;
      }
    }

    return false;
  }

  public boolean                IsPlayNag()
  {
    if ( IsPlaySuffix() )
    {
      return true;
    }
    if ( PbnGen.GetVersion() != PbnGen.VERSION_10 )
    {
      if ( mNag == CARD_CORRECTED )
      {
        return true;
      }
      if ( (CARD_EXPLAIN_FIRST <= mNag) && (mNag <= CARD_EXPLAIN_LAST) )
      {
        return true;
      }
    }

    return false;
  }

  public boolean                IsAuctionSuffix()
  {
    if ( (AUCTION_GOOD <= mNag) && (mNag <= AUCTION_QUESTIONABLE) )
    {
      return true;
    }

    return false;
  }

  public boolean                IsPlaySuffix()
  {
    if ( (PLAY_GOOD <= mNag) && (mNag <= PLAY_QUESTIONABLE) ) 
    {
      return true;
    }

    return false;
  }
}
