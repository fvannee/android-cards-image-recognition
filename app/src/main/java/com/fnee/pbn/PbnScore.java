package com.fnee.pbn;/*
 * File   :     PbnScore.java
 * Author :     Tis Veugen
 * Date   :     1999-05-13
 * PBN    :     1.0
 */

public class PbnScore
{
  public static final int       UNKNOWN = -1;

  public static int             Compute(
  PbnVulner                     oVulner,
  PbnContract                   oContract,
  PbnResult                     oResult )
  {
    int                         lScore = UNKNOWN;
    int                         lContractTricks = oContract.GetNrTricks();

    if ( ( lContractTricks > 0 )
      && ( oResult.Get() == PbnResult.OK ) )
    {
      PbnSide                   lDeclarer = oContract.GetDeclarer();
      PbnTrump                  lTrump = oContract.GetTrump();
      PbnRisk                   lRisk  = oContract.GetRisk();

      int                       lNeedTricks;
      int                       lMadeTricks;
      int                       lGameTricks;
      int                       lExtraTricks;
      int                       lNegTricks;
      int                       lGameScore;
      int                       lTrickScore;
      int                       lExtraScore;
      int                       lNegScore;
      boolean                   bVulner;

      bVulner = ( ( lDeclarer.IsNS() && oVulner.IsNS() )
               || ( lDeclarer.IsEW() && oVulner.IsEW() ) );

      lNeedTricks = 6 + lContractTricks;
      lMadeTricks = oResult.GetTricks();
      if ( lMadeTricks >= lNeedTricks )
      { /*
         * Positive score.
         */
        lExtraTricks = lMadeTricks - lNeedTricks;

        switch ( lRisk.Get() )
        {
        case PbnRisk.NONE:
          lScore = 0;
          lGameTricks = lContractTricks;
          break;
        case PbnRisk.DOUBLE:
          lScore = 50;
          lGameTricks = 2 * lContractTricks;
          break;
        case PbnRisk.REDOUBLE:
          lScore = 100;
          lGameTricks = 4 * lContractTricks;
          break;
        default:
          lScore = 0;
          lGameTricks = 0;
          break;
        }

        lGameScore = ( bVulner ) ? 500 : 300;

        if ( lContractTricks == 7 )
        { /*
           * Grand slam
           */
          lScore += ( bVulner ) ? 2000 : 1300;
        }
        else
        if ( lContractTricks == 6 )
        { /*
           * slam
           */
          lScore += ( bVulner ) ? 1250 : 800;
        }
        else
        if ( ( lGameTricks >= 5 )
          && ( lTrump.Is( PbnTrump.CLUBS )
            || lTrump.Is( PbnTrump.DIAMONDS ) ) )
        { /*
           * game C/D
           */
          lScore += lGameScore;
        }
        else
        if ( ( lGameTricks >= 4 )
          && ( lTrump.Is( PbnTrump.HEARTS )
            || lTrump.Is( PbnTrump.SPADES ) ) )
        { /*
           * game H/S
           */
          lScore += lGameScore;
        }
        else
        if ( ( lGameTricks >= 3 )
          && ( lTrump.Is( PbnTrump.NOTRUMP ) ) )
        { /*
           * game NT
           */
          lScore += lGameScore;
        }
        else
        {
          lScore += 50;
        }

        switch ( lTrump.Get() )
        {
        case PbnTrump.CLUBS:
        case PbnTrump.DIAMONDS:
          lTrickScore = 20 * lContractTricks;
          lExtraScore = 20;
          break;
        case PbnTrump.HEARTS:
        case PbnTrump.SPADES:
          lTrickScore = 30 * lContractTricks;
          lExtraScore = 30;
          break;
        case PbnTrump.NOTRUMP:
          lTrickScore = 10 + 30 * lContractTricks;
          lExtraScore = 30;
          break;
        default:
          lTrickScore = 0;
          lExtraScore = 0;
          break;
        }

        switch ( lRisk.Get() )
        {
        case PbnRisk.NONE:
          break;
        case PbnRisk.DOUBLE:
          lTrickScore *= 2;
          lExtraScore = ( bVulner ) ? 200 : 100;
          break;
        case PbnRisk.REDOUBLE:
          lTrickScore *= 4;
          lExtraScore = ( bVulner ) ? 400 : 200;
          break;
        }

        lScore += lTrickScore;
        lScore += lExtraTricks * lExtraScore;
      }
      else
      { /*
         * Negative score.
         */
        lNegTricks = lNeedTricks - lMadeTricks;
        if ( lRisk.Is( PbnRisk.NONE ) )
        {
          lNegScore = ( bVulner ) ? 100 : 50;
          lScore = lNegTricks * lNegScore;
        }
        else
        {
          if ( bVulner )
          {
            lScore = 200;
            lNegTricks--;
          }
          else
          {
            lScore = 100;
            lNegTricks--;
            if ( lNegTricks > 0 )
            {
              lScore += 200;
              lNegTricks--;
            }
            if ( lNegTricks > 0 )
            {
              lScore += 200;
              lNegTricks--;
            }
          }

          lScore += 300 * lNegTricks;
        }
        if ( lRisk.Is( PbnRisk.REDOUBLE ) )
        {
          lScore *= 2;
        }
        lScore = -lScore;
      }
    }

    return lScore;
  }
}
