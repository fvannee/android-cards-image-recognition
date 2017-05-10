package com.fnee.pbn;/*
 * File   :     PbnCall.java
 * Author :     Tis Veugen
 * Date   :     1999-01-03
 * PBN    :     1.0
 *
 * History
 * -------
 * 1999-01-03 Added GetNrTricks()
 */

public class PbnCall
{
  public static final int       TYPE_PASS   = 0;
  public static final int       TYPE_DBL    = 1;
  public static final int       TYPE_RDB    = 2;
  public static final int       TYPE_REAL   = 3;
  public static final int       TYPE_INSUFF = 4;
  public static final int       TYPE_SKIP   = 5;
  public static final int       TYPE_NONE   = 6;

  // The strings of (re)double are the same as for the contract risk.
  public static final String    S_PASS = "Pass";
  public static final String    S_DBL  = "X";
  public static final String    S_RDB  = "XX";

  public static final String    S_INSUFF = "^I";
  public static final String    S_SKIP   = "^S";

  private PbnSide               mSide;
  private int                   mType;
  private int                   mNrTricks;
  private PbnTrump              mTrump;

  public                        PbnCall()
  {
    mSide     = new PbnSide();
    mType     = TYPE_NONE;
    mNrTricks = 0;
    mTrump    = new PbnTrump();
  }

  public PbnSide                GetSide()
  {
    return mSide;
  }

  public int                    GetType()
  {
    return mType;
  }

  public void                   SetType(
  int                           iType )
  {
    mType = iType;
  }

  public int                    GetNrTricks()
  {
    return mNrTricks;
  }

  public void                   SetNrTricks(
  int                           iNrTricks )
  {
    mNrTricks = iNrTricks;
  }

  public PbnTrump               GetTrump()
  {
    return mTrump;
  }

  public boolean                IsBigger(
  PbnCall                       oCall )
  {
    return( (mNrTricks > oCall.mNrTricks) ||
            ( (mNrTricks == oCall.mNrTricks) &&
              (mTrump.GT( oCall.mTrump )   ) ) );
  }

  public String                 toString()
  {
    switch ( mType )
    {
    case TYPE_PASS:
      return S_PASS;

    case TYPE_DBL:
      return S_DBL;

    case TYPE_RDB:
      return S_RDB;

    case TYPE_REAL:
      return "" + mNrTricks + mTrump.toString();

    case TYPE_INSUFF:
      return S_INSUFF + " " + mNrTricks + mTrump.toString();

    case TYPE_SKIP:
      return S_SKIP;

    default:
      return "";
    }
  }
}
