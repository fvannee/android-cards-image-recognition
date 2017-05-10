package com.fnee.pbn;/*
 * File   :     PbnResult.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

public class PbnResult
{
  public static final int       IDLE    = -1;
  public static final int       OK      = 0;
  public static final int       EMPTY   = 1;
  public static final int       UNKNOWN = 2;

  public static final String    S_IDLE    = "_";
  public static final String    S_EMPTY   = "";
  public static final String    S_UNKNOWN = "?";

  private int                   mResult;
  private int                   mNrTricks;
  private boolean               mbIrregular;

  public                        PbnResult()
  {
    mResult     = IDLE;
    mNrTricks   = 0;
    mbIrregular = false;
  }

  public void                   Set(
  int                           iResult )
  {
    mResult = iResult;
  }

  public int                    Get()
  {
    return mResult;
  }

  public boolean                IsIdle()
  {
    return mResult == IDLE;
  }

  public void                   SetTricks(
  int                           iNrTricks )
  {
    mNrTricks = iNrTricks;
    mResult = OK;
  }

  public int                    GetTricks()
  {
    return mNrTricks;
  }

  public void                   SetIrregular(
  boolean                       bIrregular )
  {
    mbIrregular = bIrregular;
  }

  public boolean                IsIrregular()
  {
    return mbIrregular;
  }

/*
 * If Result known, then
 *    NrWon[ N/S ] <= ResultNS
 *    NrWon[ E/W ] <= ResultEW
 */
  public PbnError               Verify(
  PbnContract                   oContract,
  int                           iNrWonNS,
  int                           iNrWonEW )
  {
    PbnError                    lError = new PbnError();
    int                         ResultNS;
    int                         ResultEW;

    if ( mResult == OK )
    {
      if ( ! ( ( oContract.GetDeclarer().IsValid() )
            && ( oContract.GetTrump().IsValid()    ) ) )
      {
        lError.Set( PbnError.BAD_RESULT );
      }
      else
      {
        if ( oContract.GetDeclarer().IsEW() )
        {
          ResultEW = mNrTricks;
          ResultNS = PbnTrick.NUMBER - ResultEW;
        }
        else
        {
          ResultNS = mNrTricks;
          ResultEW = PbnTrick.NUMBER - ResultNS;
        }

        if ( ( ResultNS < iNrWonNS ) ||
             ( ResultEW < iNrWonEW ) )
        { /*
           * Wrong result.
           */
          if ( ! mbIrregular )
          {
            lError.Set( PbnError.BAD_RESULT );
          }
        }
        else
        { /*
           * Correct result.
           */
          if ( mbIrregular )
          {
            lError.Set( PbnError.BAD_RESULT );
          }
        }
      }
    }

    return( lError );
  }
  
  public String                 toString()
  {
    String                      string = S_IDLE;
    
    switch ( mResult )
    {
    case OK:
      string = (( IsIrregular() ) ? "^" : "") + mNrTricks;
      break;
    
    case EMPTY:
      string = S_EMPTY;
      break;

    case UNKNOWN:
      string = S_UNKNOWN;
      break;
    }

    return string;
  }
}
