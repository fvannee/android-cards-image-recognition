package com.fnee.pbn;/*
 * File   :     PbnSuffix.java
 * Author :     Tis Veugen
 * Date   :     1999-01-03
 * PBN    :     1.0
 *
 * History
 * -------
 * 1999-01-03 Added Get().
 */

public class PbnSuffix
{
  public static final int       NONE         = 0;
  public static final int       GOOD         = 1;
  public static final int       POOR         = 2;
  public static final int       VERY_GOOD    = 3;
  public static final int       VERY_POOR    = 4;
  public static final int       SPECULATIVE  = 5;
  public static final int       QUESTIONABLE = 6;
  public static final int       NUMBER       = 7;

  private int                   mSuffix;

  public                        PbnSuffix()
  {
    mSuffix = NONE;
  }

  public                        PbnSuffix(
  int                           iSuffix )
  {
    mSuffix = iSuffix;
  }

  public                        PbnSuffix(
  PbnSuffix                     oSuffix )
  {
    mSuffix = oSuffix.mSuffix;
  }

  public int                    Get()
  {
    return mSuffix;
  }

  public void                   Set(
  PbnSuffix                     oSuffix )
  {
    mSuffix = oSuffix.mSuffix;
  }

  public boolean                IsValid()
  {
    return IsValid( mSuffix );
  }

  public static boolean         IsValid(
  int                           iSuffix )
  {
    return(  (GOOD <= iSuffix) && (iSuffix <= QUESTIONABLE) );
  }

  public PbnNag                 Convert(
  boolean                       bAuction )
  {
    int                         iNag;

    iNag = ( bAuction ) ? PbnNag.AUCTION_GOOD : PbnNag.PLAY_GOOD;

    return new PbnNag( iNag + mSuffix - GOOD );
  }
}
