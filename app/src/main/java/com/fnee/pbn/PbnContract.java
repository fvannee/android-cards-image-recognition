package com.fnee.pbn;/*
 * File   :     PbnContract.java
 * Author :     Tis Veugen
 * Date   :     2002-08-25
 * PBN    :     2.0
 *
 * History
 * -------
 * 2002-08-25 Added IsPlayable()
 */

public class PbnContract
{
  private PbnSide               mDeclarer;
  private int                   mNrTricks;
  private PbnTrump              mTrump;
  private PbnRisk               mRisk;
  private boolean               mbIrregularDeclarer;

  public                        PbnContract()
  {
    mDeclarer = new PbnSide();
    mNrTricks = 0;
    mTrump    = new PbnTrump();
    mRisk     = new PbnRisk();
    mbIrregularDeclarer = false;    
  }

  public PbnSide                GetDeclarer()
  {
    return mDeclarer;
  }

  public int                    GetNrTricks()
  {
    return mNrTricks;
  }

  public PbnTrump               GetTrump()
  {
    return mTrump;
  }

  public PbnRisk                GetRisk()
  {
    return mRisk;
  }

  public boolean                GetIrregularDeclarer()
  {
    return mbIrregularDeclarer;
  }

  public void                   SetNrTricks(
  int                           iNrTricks )
  {
    mNrTricks = iNrTricks;
  }

  public void                   SetIrregularDeclarer(
  boolean                       bIrregularDeclarer )
  {
    mbIrregularDeclarer = bIrregularDeclarer;
  }

  public PbnError               IsPlayable(
  boolean                       bAllCards )
  {
    if ( ! bAllCards )
    { /*
       * For end-play positions, only valid trump is needed.
       */
      if ( ! mTrump.IsValid() )
      {
        return new PbnError( PbnError.BAD_CONTRACT );
      }
      return new PbnError();
    }

    if ( ! mTrump.IsValid() )
    {
      return new PbnError( PbnError.BAD_CONTRACT );
    }

    if (  mNrTricks == 0 )
    {
      return new PbnError( PbnError.NO_CONTRACT );
    }

    if ( ! mDeclarer.IsValid() )
    {
      return new PbnError( PbnError.NO_DECLARER );
    }

    return new PbnError();
  }
}
