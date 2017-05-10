package com.fnee.pbn;/*
 * File   :     PbnAuction.java
 * Author :     Tis Veugen
 * Date   :     1999-05-14
 * PBN    :     1.0
 *
 * History
 * -------
 * 1999-01-03 Bug fix Viewer
 * 1999-05-14 Check number of tricks in contract
 */

public class PbnAuction
{
  private PbnCall []            maCalls;
  private int                   mNrCalls;

  private static final int      SIDE_HIGH = 0xFFFF;

  public                        PbnAuction()
  {
    maCalls = new PbnCall[ 0 ];
    mNrCalls = 0;
  }

  public int                    GetNrCalls()
  {
    return mNrCalls;
  }

  public PbnCall                GetCall(
  int                           iIndex )
  {
    return maCalls[ iIndex ];
  }

  public int                    GetNrToPass()
  {
    int                         NrPasses = 0;
    boolean                     bBid = false;

    for ( int i = 0; i < mNrCalls; i++ )
    {
      switch ( maCalls[i].GetType() )
      {
      case PbnCall.TYPE_PASS:
        NrPasses++;
        break;
      case PbnCall.TYPE_DBL:
      case PbnCall.TYPE_RDB:
        NrPasses = 0;
        break;
      case PbnCall.TYPE_REAL:
      case PbnCall.TYPE_INSUFF:
        bBid = true;
        NrPasses = 0;
        break;
      case PbnCall.TYPE_SKIP:
        NrPasses = 0;
        break;
      }
    }

    if ( bBid )
    {
      return PbnSide.NUMBER - 1 - NrPasses;
    }

    return PbnSide.NUMBER - NrPasses;
  }

  public PbnError               CheckValidCall(
  PbnCall                       oCall )
  {
    PbnError                    lPbnError = new PbnError();
    int                         NrPasses   = 0;
    int                         IxCallReal = -1;
    int                         IxCallRdbl = -1;

    // Determine the last bid, and the last risk.
    for ( int i = 0; i < mNrCalls; i++ )
    {
      switch ( maCalls[i].GetType() )
      {
      case PbnCall.TYPE_PASS:
        NrPasses++;
        break;
      case PbnCall.TYPE_DBL:
      case PbnCall.TYPE_RDB:
        IxCallRdbl = i;
        NrPasses = 0;
        break;
      case PbnCall.TYPE_REAL:
      case PbnCall.TYPE_INSUFF:
        IxCallReal = i;
        IxCallRdbl = -1;
        NrPasses = 0;
        break;
      case PbnCall.TYPE_SKIP:
        NrPasses = 0;
        break;
      }
    }

    boolean                     bOk;

    /*
     * A call is not allowed any more if either,
     * all 4 players have passed, or
     * after a bid, all 3 other players have passed.
     */
    bOk = ( NrPasses < PbnSide.NUMBER-1 )
       || ( ( IxCallReal == -1 ) && ( NrPasses < PbnSide.NUMBER ) );

    if ( bOk )
    {
      switch ( oCall.GetType() )
      {
      case PbnCall.TYPE_PASS:
        break;

      case PbnCall.TYPE_DBL:
        /*
         * A double is only allowed if the last call was
         * a bid from an opponent.
         */
        bOk = ( IxCallReal > IxCallRdbl )
           && ( (mNrCalls & 1) != (IxCallReal & 1) );
        break;

      case PbnCall.TYPE_RDB:
        /*
         * A redouble is only allowed if the last call was
         * a double call from an opponent.
         */
        bOk = ( IxCallRdbl > IxCallReal )
           && ( (mNrCalls & 1) != (IxCallRdbl & 1) )
           && ( maCalls[ IxCallRdbl ].GetType() == PbnCall.TYPE_DBL );
        break;

      case PbnCall.TYPE_REAL:
        /*
         * The bid must be higher than the previous bid.
         */
        bOk = ( IxCallReal == -1 )
           || ( oCall.IsBigger( maCalls[ IxCallReal ] ) );
        break;

      case PbnCall.TYPE_INSUFF:
        /*
         * The bid must be lower than the previous bid.
         */
        bOk = ( IxCallReal >= 0 )
           && ( ! oCall.IsBigger( maCalls[ IxCallReal ] ) );
        break;

      case PbnCall.TYPE_SKIP:
        break;

      case PbnCall.TYPE_NONE:
      default:
        bOk = false;
        break;
      }
    }

    if ( ! bOk )
    {
      if ( oCall.GetType() == PbnCall.TYPE_INSUFF )
      {
        lPbnError.Set( PbnError.SUFFICIENT );
      }
      else
      {
        lPbnError.Set( PbnError.BAD_CALL );
      }
    }

    return( lPbnError );
  }

  public PbnError               StoreCall(
  PbnCall                       oCall )
  {
    PbnError                    lPbnError;

    lPbnError = CheckValidCall( oCall );
    if ( lPbnError.IsOK() )
    {
      maCalls = (PbnCall []) PbnU.ArrayInc( maCalls );

      maCalls[ mNrCalls ] = oCall;
      mNrCalls++;
    }

    return lPbnError;
  }

  public PbnError               StorePasses()
  {
    PbnError                    lPbnError = new PbnError();
    int                         NrPasses = GetNrToPass();

    for ( ; NrPasses > 0; NrPasses-- )
    {
      PbnCall                   oCall = new PbnCall();

      oCall.SetType( PbnCall.TYPE_PASS );
      lPbnError = StoreCall( oCall );
    }

    return lPbnError;
  }

/*
 * If (part of) the auction is not consistent with the 'Declarer', then
 * the tag value is preceded by '^'.
 * 1. Auction complete:
 *  a. Declarer known:
 *     - begin same: OK
 *     - begin same + irregular: ERROR
 *     - begin differs: ERROR
 *     - begin differs + irregular: OK
 *  b. Declarer unknown: compute
 * 2. Auction incomplete:
 *  a. Declarer known:
 *     - begin same: OK
 *     - begin same + irregular: ERROR
 *     - begin differs: ERROR
 *     - begin differs + irregular: OK
 *     - no begin: OK
 *  b. Declarer unknown: "?"
 * 3. Auction absent:
 *  a. Declarer known: OK
 *  b. Declarer unknown: "?"
 */
  public PbnError               Verify(
  PbnSide                       oDealer,
  PbnContract                   oContract )
  {
    PbnError                    lError = new PbnError();
    int [][]                    laaSides;
    PbnSide                     lDeclarer;
    PbnTrump                    lTrump;
    PbnSide                     lCallSide;
    PbnSide                     lCallDeclarer;
    PbnTrump                    lCallTrump;
    PbnRisk                     lCallRisk;
    int                         lCallNrTricks = 0;
    int                         NrPasses;
    PbnSide                     lPartner;
    PbnCall                     lCall;

    laaSides = new int[PbnSide.NUMBER][PbnTrump.NUMBER];

    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      for ( int iTrump = 0; iTrump < PbnTrump.NUMBER; iTrump++ )
      {
        laaSides[iSide][iTrump] = SIDE_HIGH;
      }
    }

    lDeclarer = new PbnSide( oContract.GetDeclarer() );
    lTrump    = new PbnTrump( oContract.GetTrump() );

    lCallSide  = new PbnSide( oDealer );
    lCallDeclarer = new PbnSide();
    lCallTrump    = new PbnTrump();
    lCallRisk     = new PbnRisk( PbnRisk.NONE );
    NrPasses      = 0;

    for ( int i = 0; i < mNrCalls; i++ )
    {
      lCall = maCalls[i];
      lCall.GetSide().Set( lCallSide );

      switch ( lCall.GetType() )
      {
      case PbnCall.TYPE_PASS:
        NrPasses++;
        break;
      case PbnCall.TYPE_DBL:
        NrPasses = 0;
        lCallRisk.Set( PbnRisk.DOUBLE );
        break;
      case PbnCall.TYPE_RDB:
        NrPasses = 0;
        lCallRisk.Set( PbnRisk.REDOUBLE );
        break;
      case PbnCall.TYPE_REAL:
      case PbnCall.TYPE_INSUFF:
        NrPasses = 0;
        lCallRisk.Set( PbnRisk.NONE );

        if ( laaSides[ lCallSide.Get() ][ lCall.GetTrump().Get() ]
                == SIDE_HIGH )
        {
          laaSides[ lCallSide.Get() ][ lCall.GetTrump().Get() ] = i+1;
        }
        lCallDeclarer.Set( lCallSide );
        lCallTrump.Set( lCall.GetTrump() );
        lCallNrTricks = lCall.GetNrTricks();
        break;
      case PbnCall.TYPE_SKIP:
        NrPasses = 0;
        break;
      }
      lCallSide.Next();
    }

    if ( NrPasses == PbnSide.NUMBER )
    {
      if ( lDeclarer.Is( PbnSide.IDLE ) )
      {
        oContract.GetDeclarer().Set( PbnSide.EMPTY );
      }
      else
      if ( ! lDeclarer.Is( PbnSide.EMPTY ) )
      {
        lError.Set( PbnError.BAD_DECLARER );
      }

      if ( lError.IsOK() )
      {
        if ( lTrump.Is( PbnTrump.IDLE ) )
        {
          oContract.GetTrump().Set( PbnTrump.NONE );
          oContract.GetRisk().Set( PbnRisk.NONE );
        }
        else
        if ( ! lTrump.Is( PbnTrump.NONE ) )
        {
          lError.Set( PbnError.BAD_TRUMP );
        }
      }
    }
    else
    if ( ( mNrCalls >= PbnSide.NUMBER   )
      && ( NrPasses == PbnSide.NUMBER-1 ) )
    { /*
       * Auction complete.
       */
      lPartner = lCallDeclarer.GetPartner();
      if ( laaSides[ lPartner.Get() ][ lCallTrump.Get() ] <
           laaSides[ lCallDeclarer.Get() ][ lCallTrump.Get() ] )
      {
        lCallDeclarer.Set( lPartner );
      }

      if ( lTrump.Is( PbnTrump.NONE ) )
      { /*
         * According to the contract, all players pass.
         */
        lError.Set( PbnError.BAD_TRUMP );
      }
      else
      if ( lDeclarer.Is( PbnSide.IDLE ) )
      {
        oContract.GetDeclarer().Set( lCallDeclarer );
      }
      else
      if ( lDeclarer.IsValid() )
      { /*
         * Check computed declarer against tag value.
         */
        if ( oContract.GetIrregularDeclarer() )
        { /*
           * The partner of the tag value should have been declarer.
           */
          lDeclarer = lDeclarer.GetPartner();
        }

        if ( ! lDeclarer.equals( lCallDeclarer ) )
        {
          lError.Set( PbnError.BAD_DECLARER );
        }
      }

      if ( lError.IsOK() )
      {
        if ( lTrump.Is( PbnTrump.IDLE ) )
        {
          oContract.GetTrump().Set( lCallTrump );
          oContract.GetRisk().Set( lCallRisk );
          oContract.SetNrTricks( lCallNrTricks );
        }
        else
        if ( ! oContract.GetTrump().equals( lCallTrump ) )
        {
          lError.Set( PbnError.BAD_TRUMP );
        }
        else
        if ( ! oContract.GetRisk().equals( lCallRisk ) )
        {
          lError.Set( PbnError.BAD_RISK );
        }
        else
        if ( oContract.GetNrTricks() != lCallNrTricks )
        {
          lError.Set( PbnError.BAD_CONTRACT );
        }
      }
    }
    else
    { /*
       * Auction incomplete.
       */
      if ( lTrump.Is( PbnTrump.NONE ) )
      { /*
         * According to the contract, all players pass.
         */
        if ( lCallDeclarer.IsValid() )
        {
          lError.Set( PbnError.BAD_TRUMP );
        }
      }
      else
      if ( lTrump.IsValid() )
      {
        if ( lDeclarer.IsValid() )
        {
          if ( oContract.GetIrregularDeclarer() )
          { /*
             * The partner of the tag value should have been declarer.
             */
            lDeclarer = lDeclarer.GetPartner();
          }

          lPartner = lDeclarer.GetPartner();
          if ( laaSides[ lPartner.Get()  ][ lTrump.Get() ] <
               laaSides[ lDeclarer.Get() ][ lTrump.Get() ] )
          { /*
             * The partner has bid the denomination earlier.
             */
            lError.Set( PbnError.BAD_DECLARER );
          }
        }
      }
    }

    return lError;
  }
}
