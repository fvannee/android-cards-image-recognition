package com.fnee.pbn;/*
 * File   :     PbnSituation.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

public class PbnSituation
{
  private PbnSide               mDealer;
  private PbnVulner             mVulner;

  public                        PbnSituation()
  {
    mDealer = new PbnSide();
    mVulner = new PbnVulner();
  }

  public PbnSide                GetDealer()
  {
    return mDealer;
  }

  public PbnVulner              GetVulner()
  {
    return mVulner;
  }
}
