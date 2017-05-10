package com.fnee.pbn;/*
 * File   :     PbnImportAdmin.java
 * Author :     Tis Veugen
 * Date   :     2009-10-03
 * PBN    :     2.0
 *
 * History
 * -------
 * 1999-06-26 check for last call/card
 * 1999-08-02 contract of end position
 * 1999-09-06 added RubberVulner
 * 2009-10-03 added access functions
 */

public class PbnImportAdmin
{
  static final int              ILLEGAL_NONE   = 0x00;
  static final int              ILLEGAL_INSUFF = 0x01;
  static final int              ILLEGAL_LEAD   = 0x02;
  static final int              ILLEGAL_REVOKE = 0x04;
  static final int              ILLEGAL_SKIP   = 0x08;

  static final int              SCORING_NONE      = 0;
  static final int              SCORING_CAVENDISH = 1;
  static final int              SCORING_CHICAGO   = 2;
  static final int              SCORING_RUBBER    = 3;
  static final int              SCORING_IMP       = 4;
  static final int              SCORING_MP        = 5;
  static final int              SCORING_BAM       = 6;

  public int                    mSection;
  public int                    mScoring;

  public int                    mNrCalls;
  public int                    mNrTricks;
  public int                    mNrPlayed;

  public PbnSide                mAuctionSide;
  public PbnSide                mPlaySide;
  public PbnSide                mLeader;
  public PbnSide                mTagPlaySide;

  public PbnDeal                mDeal;

  private boolean []            mabUnknownSide;
  private PbnHand               mUnknownHand;

  private int                   mIllegalMove;
  private int                   mIllegalTrick;
  private int []                maIllegalPlay;

  private int []                maNrWon;

  public boolean                mbIncompleteAuction;
  public boolean                mbIncompletePlay;

  public boolean                mbFirstCall;
  public boolean                mbInformComment;

  public PbnVulner              RubberVulner;

  private boolean               mbLastCall;
  private int                   mLastCardTrick;
  private PbnSide               mLastCardSide;

  public                        PbnImportAdmin()
  {
    mSection = PbnGen.SECTION_IDENT;
    
    mNrCalls  = -1;
    mNrTricks = -1;
    mNrPlayed = PbnSide.NUMBER - 1;

    mAuctionSide = new PbnSide();
    mPlaySide    = new PbnSide();
    mLeader      = new PbnSide();
    mTagPlaySide = new PbnSide();

    mDeal = new PbnDeal();

    mabUnknownSide = new boolean[ PbnSide.NUMBER ];
    mUnknownHand = new PbnHand();

    maIllegalPlay = new int[ PbnSide.NUMBER ];

    maNrWon = new int[ PbnSide.NUMBER ];

    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      mabUnknownSide[ iSide ] = false;
      maNrWon[ iSide ] = 0;
    }

    PbnSuit                     lSuit = new PbnSuit( PbnSuit.CLUBS );
    PbnRanks                    lRanks = new PbnRanks( PbnRank.ALL );
    for ( int iSuit = 0; iSuit < PbnSuit.NUMBER; iSuit++ )
    {
      mUnknownHand.SetRanks( lSuit, lRanks );
      lSuit.Next();
    }

    mbIncompleteAuction = false;
    mbIncompletePlay    = false;
    mbFirstCall         = false;
    mbInformComment     = false;

    RubberVulner = new PbnVulner();

    mbLastCall = false;
    mLastCardTrick = -2;
    mLastCardSide = new PbnSide();
  }

  public void                   DealSet(
  PbnDeal                       oDeal )
  {
    mDeal = new PbnDeal( oDeal );
  }

  public boolean                UnknownHandRemoveCard(
  PbnCard                       oCard )
  {
    return mUnknownHand.PlayCard( oCard );
  }

  public void                   UnknownHandRemoveRanks(
  PbnSuit                       oSuit,
  PbnRanks                      oRanks )
  {
    mUnknownHand.RemoveRanks( oSuit, oRanks );
  }

  public void                   UnknownSideSet(
  PbnSide                       oSide )
  {
    mabUnknownSide[ oSide.Get() ] = true;
  }

  public boolean                UnknownSideIs(
  PbnSide                       oSide )
  {
    return( mabUnknownSide[ oSide.Get() ] );
  }

  public void                   IllegalMoveAdd(
  int                           iIllegal )
  {
    mIllegalMove = PbnU.BitsUp( mIllegalMove, iIllegal);
  }

  public void                   IllegalMoveClear()
  {
    mIllegalMove = ILLEGAL_NONE;
  }

  public void                   IllegalMoveCopy()
  {
    maIllegalPlay[ mPlaySide.Get() ] = mIllegalMove;
    mIllegalTrick = PbnU.BitsUp( mIllegalTrick, mIllegalMove );
  }

  public boolean                IllegalMoveHas(
  int                           iIllegal )
  {
    return PbnU.BitsHas( mIllegalMove, iIllegal);
  }

  public void                   IllegalMoveSet(
  int                           iIllegal )
  {
    mIllegalMove = iIllegal;
  }

  public void                   IllegalTrickClear()
  {
    mIllegalTrick = ILLEGAL_NONE;
  }

  public boolean                IllegalTrickHas(
  int                           iIllegal )
  {
    return PbnU.BitsHas( mIllegalTrick, iIllegal);
  }

  public boolean                IllegalLeadGet(
  PbnSide                       oPlaySide )
  {
    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      if ( PbnU.BitsHas( maIllegalPlay[ iSide ], ILLEGAL_LEAD ) )
      {
        oPlaySide.Set( iSide );
        return( true );
      }
    }

    return( false );
  }

  public boolean                IllegalPlayHasRevoke(
  PbnSide                       oSide )
  {
    return PbnU.BitsHas( maIllegalPlay[ oSide.Get() ], ILLEGAL_REVOKE );
  }

  public void                   NrWonInc(
  PbnSide                       oSide )
  {
    maNrWon[ oSide.Get() ]++;
  }

  public int                    NrWonGetNS()
  {
    return maNrWon[ PbnSide.NORTH ] + maNrWon[ PbnSide.SOUTH ];
  }

  public int                    NrWonGetEW()
  {
    return maNrWon[ PbnSide.EAST ] + maNrWon[ PbnSide.WEST ];
  }

  public void                   CallInc()
  {
    mNrCalls++;
  }

  public boolean                CallsToBeChecked()
  {
    return ( (mNrCalls % PbnSide.NUMBER) == PbnSide.NUMBER-1 );    
  }

  public void                   CardInc()
  {
    if ( ++mNrPlayed >= PbnSide.NUMBER )
    {
      mNrPlayed = 0;
      mNrTricks++;
    }
    mPlaySide = new PbnSide( mTagPlaySide.Get() + mNrPlayed );
  }

  public boolean                CardsToBeChecked()
  {
    return ( mNrPlayed == PbnSide.NUMBER-1 );    
  }

  public boolean                SetLastCall()
  {
    if ( mbLastCall )
    {
      return false;
    }

    mbLastCall = true;
    return true;
  }

  public boolean                SetLastCard()
  {
    if ( mLastCardTrick >= 0 )
    {
      return false;
    }

    mLastCardTrick = mNrTricks;
    mLastCardSide.Set( mPlaySide );
    return true;
  }

  public boolean                IsLastCall()
  {
    return mbLastCall;
  }

  public boolean                IsLastCardTrick()
  {
    return mLastCardTrick == mNrTricks;
  }

  public boolean                IsLastCardSide(
  PbnSide                       oSide )
  {
    return mLastCardSide.equals( oSide );
  }
}
