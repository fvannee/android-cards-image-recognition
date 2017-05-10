package com.fnee.pbn;/*
 * File   :     PbnGameData.java
 * Author :     Tis Veugen
 * Date   :     1999-10-03
 * PBN    :     2.0
 *
 * History
 * -------
 * 1999-10-03 Added mEndPosSide.
 */

public class PbnGameData
{
  PbnSituation                  mSituation;
  PbnDeal                       mDeal;
  PbnContract                   mContract;
  PbnResult                     mResult;
  PbnAuction                    mAuction;
  PbnPlay                       mPlay;
  PbnSide                       mEndPosSide;

  public                        PbnGameData()
  {
    mSituation  = new PbnSituation();
    mDeal       = new PbnDeal();
    mContract   = new PbnContract();
    mResult     = new PbnResult();
    mAuction    = new PbnAuction();
    mPlay       = new PbnPlay();
    mEndPosSide = new PbnSide();
  }

  public PbnSituation           GetSituation()
  {
    return mSituation;
  }

  public PbnDeal                GetDeal()
  {
    return mDeal;
  }

  public PbnContract            GetContract()
  {
    return mContract;
  }

  public PbnResult              GetResult()
  {
    return mResult;
  }

  public PbnAuction             GetAuction()
  {
    return mAuction;
  }

  public PbnPlay                GetPlay()
  {
    return mPlay;
  }

  public PbnSide                GetEndPosSide()
  {
    return mEndPosSide;
  }
}
