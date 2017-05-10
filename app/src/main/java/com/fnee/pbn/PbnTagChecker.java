package com.fnee.pbn;/*
 * File   :     PbnTagChecker.java
 * Author :     Tis Veugen
 * Date   :     2009-10-03
 * PBN    :     2.1
 *
 * History
 * -------
 * 1999-01-03 use PbnRank.FromChar().
 * 1999-04-04 First tag value "#" caused NullPointer
 * 1999-05-12 add VerifyScore()
 * 1999-06-05 added new TagIds
 * 1999-06-13 changed error values
 * 1999-06-26 check last call and last card
 * 1999-08-02 contract of end position
 * 1999-08-09 check history rubber
 *            changed ParseNumber (no error after '-' or '+')
 * 1999-08-31 check history rubber
 * 1999-10-02 changed end position
 * 2002-08-25 check play conditions
 * 2007-06-24 Added VERSION_21
 * 2009-10-03 check unknown cards
 *            check new lines for export format
 */

import java.util.*;


class PbnDupScore
{
  public int                    Above;
  public int                    Below;

  public                        PbnDupScore()
  {
    Above = 0;
    Below = 0;
  }
}

class PbnDecScore
{
  public int                    High;
  public int                    Low;

  public                        PbnDecScore()
  {
    High = 0;
    Low  = 0;
  }
}

class PbnRGames
{
  public int                    NrGames;
  public int                    TrickPoints;

  public                        PbnRGames()
  {
    NrGames = 0;
    TrickPoints = 0;
  }
}

class PbnPScore
{
  public int                    Side1;
  public int                    Side2;

  public                        PbnPScore()
  {
    Side1 = 0;
    Side2 = 0;
  }
}

class Pbn2Results
{
  public int                    Result1;
  public int                    Result2;

  public                        Pbn2Results()
  {
    Result1 = 0;
    Result2 = 0;
  }
}

class VulnerPair
{
  public static final int       NUMBER = 9;

  static int []                 maVulners =
  { PbnVulner.NONE    ,
    PbnVulner.NONE    ,
    PbnVulner.NONE    ,
    PbnVulner.NS      ,
    PbnVulner.EW      ,
    PbnVulner.ALL     ,
    PbnVulner.ALL     ,
    PbnVulner.EMPTY   ,
    PbnVulner.UNKNOWN };

  static String []              maStrings =
  { PbnVulner.S_NONE    ,
    PbnVulner.S_LOVE    ,
    PbnVulner.S_DASH    ,
    PbnVulner.S_NS      ,
    PbnVulner.S_EW      ,
    PbnVulner.S_ALL     ,
    PbnVulner.S_BOTH    ,
    PbnVulner.S_EMPTY   ,
    PbnVulner.S_UNKNOWN };

  public static int             GetVulner(
  int                           i )
  {
    return maVulners[ i ];
  }

  public static String          GetString(
  int                           i )
  {
    return maStrings[ i ];
  }
}

class PbnParser
{
  private int                   mLength;        // #checked characters
  private int                   mNumber;        // parsed number

  private PbnError              mPbnError;

  public                        PbnParser()
  {
    mLength = 0;
  }

  public PbnError               CheckRank(
  String                        oString,
  PbnRank                       oRank )
  {
    char                        cRank = oString.charAt( 0 );

    return CheckRank( cRank, oRank );
  }

  public PbnError               CheckRank(
  char                          cRank,
  PbnRank                       oRank )
  {
    int                         rank;
    int                         error = PbnError.OK;

    mLength = 1;
    rank = PbnRank.FromChar( cRank );
    oRank.Set( rank );

    if ( ! oRank.IsNormal() )
    {
      error = PbnError.BAD_RANK;
    }

    return new PbnError( error );
  }

  public PbnError               CheckSide(
  String                        oString,
  PbnSide                       oSide )
  {
    int                         side  = PbnSide.IDLE;

    if ( oString.length() > 0 )
    {
      char                      cSide = oString.toUpperCase().charAt( 0 );

      mLength = 1;
      switch ( cSide )
      {
      case 'W':
        side = PbnSide.WEST;
        break;
      case 'N':
        side = PbnSide.NORTH;
        break;
      case 'E':
        side = PbnSide.EAST;
        break;
      case 'S':
        side = PbnSide.SOUTH;
        break;
      default:
        break;
      }
    }
    oSide.Set( side );

    if ( side == PbnSide.IDLE )
    {
      return new PbnError( PbnError.BAD_SIDE );
    }

    return new PbnError();
  }

  public PbnError               CheckSuit(
  String                        oString,
  PbnSuit                       oSuit )
  {
    int                         suit = PbnSuit.IDLE;

    if ( oString.length() > 0 )
    {
      char                      cSuit = oString.charAt( 0 );

      mLength = 1;
      suit = PbnSuit.FromChar( cSuit );
    }
    oSuit.Set( suit );

    if ( suit == PbnSuit.IDLE )
    {
      return new PbnError( PbnError.BAD_SUIT );
    }

    return new PbnError();
  }

  public PbnError               CheckTrump(
  String                        oString,
  PbnTrump                      oTrump )
  {
    int                         trump = PbnTrump.IDLE;

    if ( oString.length() > 0 )
    {
      oString = oString.toUpperCase();

      char                      cTrump = oString.charAt( 0 );

      mLength = 1;
      switch ( cTrump )
      {
      case 'S':
        trump = PbnTrump.SPADES;
        break;
      case 'H':
        trump = PbnTrump.HEARTS;
        break;
      case 'D':
        trump = PbnTrump.DIAMONDS;
        break;
      case 'C':
        trump = PbnTrump.CLUBS;
        break;
      case 'N':
        if ( oString.length() > 1 )
        {
          mLength = 2;
          if ( oString.charAt( 1 ) == 'T' )
          {
            trump = PbnTrump.NOTRUMP;
          }
        }
        break;
      default:
        break;
      }
    }
    oTrump.Set( trump );

    if ( trump == PbnTrump.IDLE )
    {
      return new PbnError( PbnError.BAD_TRUMP );
    }

    return new PbnError();
  }

  public String                 GetDScore(
  String                        oString,
  PbnDupScore                   oDScore,
  PbnError                      oError )
  {
    PbnError                    lError;

    lError = ParseNumber( oString );
    oDScore.Above = mNumber;
    oString = oString.substring( mLength );

    if ( lError.IsOK() )
    {
      oString = PbnChar.SkipSpace( oString );
      if ( ! oString.startsWith( "/" ) )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }
      else
      {
        oString = oString.substring( 1 );
        oString = PbnChar.SkipSpace( oString );

        lError = ParseNumber( oString );
        oDScore.Below = mNumber;
        oString = oString.substring( mLength );
      }
    }
    oError.Set( lError );

    return oString;
  }

  public String                 GetDScores(
  String                        oString,
  String                        oSide,
  PbnDupScore                   oDScore1,
  PbnDupScore                   oDScore2,
  PbnError                      oError )
  {
    oString = oString.substring( 2 );
    oString = PbnChar.SkipSpace( oString );

    oString = GetDScore( oString, oDScore1, oError );
    if ( oError.IsOK() )
    {
      oString = PbnChar.SkipSpace( oString );
      if ( oString.startsWith( oSide ) )
      {
        oString = oString.substring( 2 );
        oString = PbnChar.SkipSpace( oString );

        oString = GetDScore( oString, oDScore2, oError );
      }
    }

    return oString;
  }

  public String                 GetFScore(
  String                        oString,
  PbnDecScore                   oFScore,
  PbnError                      oError )
  {
    PbnError                    lError = new PbnError();

    if ( oString.startsWith( "." ) )
    {
      oFScore.High = 0;
    }
    else
    {
      lError = ParseNumber( oString );
      oFScore.High = mNumber;
      oString = oString.substring( mLength );
    }

    if ( lError.IsOK() )
    {
      if ( oString.startsWith( "." ) )
      {
        oString = oString.substring( 1 );

        if ( ( oString.length() > 0 )
          && ( PbnChar.IsDigit( oString.charAt( 0 ) ) ) )
        {
          lError = ParseNumber( oString );
          oFScore.Low = mNumber;
          oString = oString.substring( mLength );
        }
        else
        {
          oFScore.Low = 0;
          oString = PbnChar.SkipSpace( oString );
        }
      }
    }
    oError.Set( lError );

    return oString;
  }

  public String                 GetFScores(
  String                        oString,
  String                        oSide,
  PbnDecScore                   oFScore1,
  PbnDecScore                   oFScore2,
  PbnError                      oError )
  {
    oString = oString.substring( 2 );
    oString = PbnChar.SkipSpace( oString );

    oString = GetFScore( oString, oFScore1, oError );
    if ( oError.IsOK() )
    {
      oString = PbnChar.SkipSpace( oString );
      if ( oString.startsWith( oSide ) )
      {
        oString = oString.substring( 2 );
        oString = PbnChar.SkipSpace( oString );

        oString = GetFScore( oString, oFScore2, oError );
      }
    }

    return oString;
  }

  public String                 GetPScores(
  String                        oString,
  String                        oSide,
  PbnPScore                     oPScores,
  PbnError                      oError )
  {
    PbnError                    lError;

    oString = oString.substring( 2 );
    oString = PbnChar.SkipSpace( oString );

    lError = ParseNumber( oString );
    oPScores.Side1 = mNumber;
    oPScores.Side2 = -mNumber;
    oString = oString.substring( mLength );
    if ( lError.IsOK() )
    {
      oString = PbnChar.SkipSpace( oString );
      if ( oString.startsWith( oSide ) )
      {
        oString = oString.substring( 2 );
        oString = PbnChar.SkipSpace( oString );

        lError = ParseNumber( oString );
        oPScores.Side2 = mNumber;
        oString = oString.substring( mLength );
      }
    }
    oError.Set( lError );

    return oString;
  }

  public String                 GetRScore(
  String                        oString,
  String                        oSide,
  int []                        oaScores,
  PbnError                      oError )
  {
    PbnError                    lError;

    for ( int i = 0; i < 5 ; i++ )
    {
      oaScores[ i ] = -1;
    }

    oString = PbnChar.SkipSpace( oString );
    if ( ! oString.startsWith( oSide ) )
    {
      oError.Set( PbnError.BAD_TAG_VALUE );
    }
    else
    {
      oString = oString.substring( 2 );

      for ( int i = 0; i < 5 ; i++ )
      {
        if ( i == 2 )
        {
          oString = PbnChar.SkipSpace( oString );
          if ( ! oString.startsWith( "/" ) )
          {
            oError.Set( PbnError.BAD_TAG_VALUE );
            break;
          }
          oString = oString.substring( 1 );
        }
        oString = PbnChar.SkipSpace( oString );
        lError = ParseNumberPos( oString );
        oString = oString.substring( mLength );
        if ( ! lError.IsOK() )
        {
          break;
        }
        oaScores[ i ] = mNumber;
      }
    }

    return oString;
  }

  public String                 GetResults(
  String                        oString,
  String                        oSide,
  Pbn2Results                   oResults,
  PbnError                      oError )
  {
    PbnError                    lError;

    oString = oString.substring( 2 );
    oString = PbnChar.SkipSpace( oString );

    lError = ParseNumberPos( oString );
    oResults.Result1 = mNumber;
    oString = oString.substring( mLength );
    if ( lError.IsOK() )
    {
      oString = PbnChar.SkipSpace( oString );
      if ( oString.startsWith( oSide ) )
      {
        oString = oString.substring( 2 );
        oString = PbnChar.SkipSpace( oString );

        lError = ParseNumberPos( oString );
        oResults.Result2 = mNumber;
        oString = oString.substring( mLength );
      }
      else
      {
        oResults.Result2 = PbnTrick.NUMBER - oResults.Result1;
      }
    }
    oError.Set( lError );

    return oString;
  }

  public int                    GetLength()
  {
    return mLength;
  }

  public int                    GetNumber()
  {
    return mNumber;
  }

  public PbnError               ParseNumber(
  String                        oString )
  {
    int                         Length;
    int                         StringLength = oString.length();
    boolean                     bDigit = false;
    boolean                     bNegative = false;
    int                         error = PbnError.OK;

    mNumber = 0;
    for ( mLength = 0; mLength < StringLength; mLength++ )
    {
      char                      cDigit = oString.charAt( mLength );

      if ( cDigit == '-' )
      {
        if ( mLength > 0 )
        {
          break;
        }
        bNegative = true;
      }
      else
      if ( cDigit == '+' )
      {
        if ( mLength > 0 )
        {
          break;
        }
      }
      else
      if ( PbnChar.IsDigit( cDigit ) )
      {
        mNumber = 10 * mNumber + ( cDigit - '0' );
        bDigit = true;
      }
      else
      {
        break;
      }
    }

    if ( ! bDigit )
    {
      error = PbnError.BAD_NUMBER;
    }
    else
    if ( bNegative )
    {
      mNumber = -mNumber;
    }

    return new PbnError( error );
  }

  public PbnError               ParseNumberPos(
  String                        oString )
  {
    PbnError                    lError = ParseNumber( oString );

    if ( lError.IsOK() )
    {
      if ( mNumber < 0 )
      {
        lError.Set( PbnError.BAD_NUMBER );
      }
    }

    return( lError );
  }

  public int                    toNumber(
  String                        oString )
  {
    PbnError                    lError = ParseNumber( oString );

    return mNumber;
  }
}


public class PbnTagChecker
{
  private PbnGameData           mGameData;
  private PbnGameTags           mGameTags;
  private PbnInherit            mInheritTags;
  private PbnImportAdmin        mImportAdmin;
  private PbnCommentAdmin       mCommentAdmin;
  private PbnParser             mParser;
  private PbnError              mCheckError;
  private boolean               mbUsePrevTag;
  private boolean               mbTagValueEmpty;

  private int                   mScoreNS;
  private int                   mScoreEW;
  private boolean               mbScoreDeclarer;

  private PbnNote               mNote;

  public                        PbnTagChecker(
  PbnGameData                   oGameData,
  PbnGameTags                   oGameTags,
  PbnInherit                    oInheritTags,
  PbnImportAdmin                oImportAdmin,
  PbnCommentAdmin               oCommentAdmin )
  {
    mGameData = oGameData;
    mGameTags = oGameTags;
    mInheritTags  = oInheritTags;
    mImportAdmin  = oImportAdmin;
    mCommentAdmin = oCommentAdmin;

    mParser         = new PbnParser();
    mCheckError     = new PbnError();
    mbUsePrevTag    = false;
    mbTagValueEmpty = false;

    mScoreNS = PbnScore.UNKNOWN;
    mScoreEW = PbnScore.UNKNOWN;
    mbScoreDeclarer = false;
  }

  private boolean               CheckGenIdentical(
  String                        oString )
  {
    return oString.equals( "#" );
  }

  private boolean               CheckGenCopy(
  String                        oString )
  {
    if ( oString.length() < 2 )
    {
      return false;
    }

    return oString.substring( 0, 2 ).equals( "##" );
  }

  private boolean               CheckGenUnknown(
  String                        oString )
  {
    return oString.equals( "?" );
  }

  private boolean               CheckGenEmpty(
  String                        oString )
  {
    mbTagValueEmpty = false;
    if ( oString.length() == 0 )
    {
      mbTagValueEmpty = true;
      return true;              // IsOK
    }

    return false;
  }

  /*
   * Used for:
   *    Dealer
   *    Declarer
   *    Vulnerable
   */
  private boolean               CheckNoInherit()
  {
    mCheckError.SetOK();

    // Check tag value identical to previous.
    if ( mbUsePrevTag )
    {
      mCheckError.Set( PbnError.BAD_INHERITANCE );
      return true;              // is ready
    }

    return false;
  }

  private boolean               CheckGeneral1(
  String                        oString )
  {
    mCheckError.SetOK();

    // Check tag value unknown.
    if ( CheckGenUnknown( oString ) )
    {
      return true;              // IsOK
    }

    // Check tag value empty: set mbTagValueEmpty.
    if ( CheckGenEmpty( oString ) )
    {
      return true;              // IsOK
    }

    return false;
  }

  /*
   * Used for:
   *    CheckNoInherit()
   *    Auction
   *    Contract
   *    Deal
   *    Play
   */
  private boolean               CheckGeneral2(
  String                        oString )
  {
    // Check tag value identical to previous.
    if ( CheckNoInherit() )
    {
      return true;              // is ready
    }

    // Check tag value unknown.
    if ( CheckGenUnknown( oString ) )
    {
      return true;              // IsOK
    }

    // Check tag value empty: set mbTagValueEmpty.
    if ( CheckGenEmpty( oString ) )
    {
      return true;              // IsOK
    }

    return false;
  }

  public PbnError               CheckSide(
  String                        oString,
  PbnSide                       oSide )
  {
    PbnError                    lError;

    lError = mParser.CheckSide( oString, oSide );
    if ( lError.IsOK() )
    {
      if ( oString.length() != 1 )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }
    }

    return lError;
  }

  public PbnError               CheckSideType(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    if ( ! ( oString.equalsIgnoreCase( "human"   ) ||
             oString.equalsIgnoreCase( "program" ) ) )
    {
      lError.Set( PbnError.NEW_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckAuction(
  String                        oString )
  {
    if ( CheckGeneral2( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    lError = CheckSide( oString, mImportAdmin.mAuctionSide );
    if ( lError.IsOK() )
    {
      if ( ! mGameTags.TagIdExist( new PbnTagId ( PbnTagId.DEALER ) ) )
      {
        lError.Set( PbnError.NO_DEALER );
      }
    }

    return lError;
  }

  public PbnError               CheckBoard(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    int                         iBoard;

    lError = mParser.ParseNumber( oString );
    iBoard = mParser.GetNumber();

    if ( lError.IsOK() )
    {
      if ( mParser.GetLength() != oString.length() )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }
    }

    return lError;
  }

  public PbnError               CheckCompetition(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    if ( ! ( oString.equalsIgnoreCase( "Chicago"     ) ||
             oString.equalsIgnoreCase( "Individuals" ) ||
             oString.equalsIgnoreCase( "Pairs"       ) ||
             oString.equalsIgnoreCase( "Rubber"      ) ||
             oString.equalsIgnoreCase( "Teams"       ) ) )
    {
      lError.Set( PbnError.NEW_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckContract(
  String                        oString )
  {
    if ( CheckGeneral2( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnContract                 lContract = mGameData.GetContract();
    PbnTrump                    lTrump = new PbnTrump();
    char                        cNumber = oString.charAt( 0 );

    if ( ( PbnGen.GetVersion() != PbnGen.VERSION_10       )
      && ( ! mGameData.GetEndPosSide().Is( PbnSide.IDLE ) ) )
    { /*
       * Check trump for end position: hands with < 13 cards
       */
      lError = mParser.CheckTrump( oString, lTrump );

      if ( lError.IsOK() )
      {
        lContract.GetTrump().Set( lTrump );
        lContract.GetRisk().Set( PbnRisk.NONE );

        oString = oString.substring( mParser.GetLength() );
        lError = CheckEndOfString( oString );
      }
    }
    else
    if ( oString.equalsIgnoreCase( "Pass" ) )
    {
      // All pass.
      lContract.GetTrump().Set( PbnTrump.NONE );
    }
    else
    if ( ! ( ( '1' <= cNumber ) && ( cNumber <= '7' ) ) )
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
    }
    else
    {
      lContract.SetNrTricks( cNumber - '0' );

      oString = oString.substring( 1 );
      lError = mParser.CheckTrump( oString, lTrump );

      if ( lError.IsOK() )
      {
        lContract.GetTrump().Set( lTrump );

        int                     risk = PbnRisk.IDLE;

        oString = oString.substring( mParser.GetLength() );

        if ( oString.equalsIgnoreCase( PbnRisk.S_NONE ) )
        {
          risk = PbnRisk.NONE;
        }
        else
        if ( oString.equalsIgnoreCase( PbnRisk.S_DOUBLE ) )
        {
          risk = PbnRisk.DOUBLE;
        }
        else
        if ( oString.equalsIgnoreCase( PbnRisk.S_REDOUBLE ) )
        {
          risk = PbnRisk.REDOUBLE;
        }
        else
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }

        if ( lError.IsOK() )
        {
          lContract.GetRisk().Set( risk );
        }
      }
    }

    return lError;
  }

  public PbnError               CheckDate(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    int                         Year;
    int                         Month;
    int                         Day;

    if ( ( oString.length() == 10                  ) &&
         ( PbnChar.IsDateChar( oString.charAt(0) ) ) &&
         ( PbnChar.IsDateChar( oString.charAt(1) ) ) &&
         ( PbnChar.IsDateChar( oString.charAt(2) ) ) &&
         ( PbnChar.IsDateChar( oString.charAt(3) ) ) &&
         ( oString.charAt(4) == '.'                ) &&
         ( PbnChar.IsDateChar( oString.charAt(5) ) ) &&
         ( PbnChar.IsDateChar( oString.charAt(6) ) ) &&
         ( oString.charAt(7) == '.'                ) &&
         ( PbnChar.IsDateChar( oString.charAt(8) ) ) &&
         ( PbnChar.IsDateChar( oString.charAt(9) ) ) )
    {
      if ( oString.charAt(0) != '?' )
      {
        Year = mParser.toNumber( oString.substring( 0, 4 ) );

        if ( Year < 1900 )
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }
      }

      if ( oString.charAt(5) != '?' )
      {
        Month = mParser.toNumber( oString.substring( 5, 7 ) );

        if ( ! ( 1 <= Month ) && ( Month <= 12 ) )
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }
      }

      if ( oString.charAt(8) != '?' )
      {
        Day = mParser.toNumber( oString.substring( 8, 10 ) );

        if ( ! ( 1 <= Day ) && ( Day <= 31 ) )
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }
      }
    }
    else
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckDeal(
  String                        oString )
  {
    if ( CheckGeneral2( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnDeal                     lDeal = mGameData.GetDeal();
    PbnSide                     lSide = new PbnSide();

    lError = mParser.CheckSide( oString, lSide );
    if ( lError.IsOK() )
    {
      oString = oString.substring( 1 );
      oString = PbnChar.SkipSpace( oString );
      if ( ! oString.startsWith( ":" ) )
      {
        lError.Set( PbnError.SEVERE_TAG_VALUE );
      }
      else
      {
        oString = oString.substring( 1 );
      }
    }

    if ( lError.IsOK() )
    {
      PbnHand                   lHands = new PbnHand();
      int                       nSides = 1;
      PbnSuit                   lSuit = new PbnSuit( PbnSuit.SPADES );
      PbnRanks                  lRanks = new PbnRanks();
      PbnRank                   lRank = new PbnRank();
      int                       iChar;
      int                       nChar = oString.length();
      boolean                   bReady = false;

      for ( iChar = 0; iChar < nChar; iChar++ )
      {
        char                    cRank = oString.charAt( iChar );

        lRank.Set( PbnRank.NONE );
        switch ( cRank )
        {
        case ' ':
          if ( ! ( ( nSides != PbnSide.NUMBER  ) &&
                   ( lSuit.Is( PbnSuit.CLUBS ) ) ) )
          {
            lError.Set( PbnError.SEVERE_TAG_VALUE );
            bReady = true;
          }
          else
          {
            // Hand completed.
            // All ranks of side-suit found.
            lDeal.SetRanks( lSide, lSuit, lRanks );
            lRanks.Set( PbnRank.NONE );
            lSide.Next();
            nSides++;
            lSuit.Set( PbnSuit.SPADES );
          }
          break;

        case '-':
          lSuit.Set( PbnSuit.CLUBS );
          mImportAdmin.UnknownSideSet( lSide );
          break;

        case '.':
          if ( lSuit.Is( PbnSuit.CLUBS ) )
          {
            lError.Set( PbnError.SEVERE_TAG_VALUE );
            bReady = true;
          }
          else
          {
            // Suit completed.
            // All ranks of side-suit found.
            lDeal.SetRanks( lSide, lSuit, lRanks );
            lRanks.Set( PbnRank.NONE );
            lSuit.Previous();
          }
          break;

        default:
          lError = mParser.CheckRank( cRank, lRank );
          if ( ! lError.IsOK() )
          {
            bReady = true;
          }
          else
          if ( ! lRank.IsNormal() )
          {
            // Not a real rank.
            lError.Set( PbnError.BAD_RANK );
            bReady = true;
          }
          else
          if ( ! lHands.AddRank( lSuit, lRank ) )
          {
            // Rank already exists.
            lError.Set( PbnError.SEVERE_TAG_VALUE );
            bReady = true;
          }
          else
          {
            lRanks.AddRank( lRank );
          }
          break;
        }

        if ( bReady )
        {
          break;
        }
      }

      if ( lError.IsOK() )
      {
        if ( ! ( ( nSides == PbnSide.NUMBER  ) &&
                 ( lSuit.Is( PbnSuit.CLUBS ) ) ) )
        {
          lError.Set( PbnError.SEVERE_TAG_VALUE );
        }
        else
        {
          // Deal completed. All ranks of side-suit found.
          lDeal.SetRanks( lSide, lSuit, lRanks );
        }
      }
    }

    if ( lError.IsOK() )
    {
      mImportAdmin.DealSet( lDeal );

      lError = CheckDealCards( lDeal );
    }

    return lError;
  }

  /*
   * Check the number of cards in the deal.
   * 1. Each side has <= 13 cards.
   * 2. All sides have the same number of cards.
   * 3. Retrieve the unknown cards
   */
  private PbnError              CheckDealCards(
  PbnDeal                       oDeal )
  {
    PbnError                    lError = new PbnError();
    int []                      NrHandCards = new int[ PbnSide.NUMBER ];
    int                         TotalNrCards = 0;
    int                         NrSides = 0;
    PbnSide                     oSide = new PbnSide( PbnSide.SOUTH );

    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      if ( ! mImportAdmin.UnknownSideIs( oSide ) )
      {
        PbnSuit                 oSuit = new PbnSuit( PbnSuit.CLUBS );
        int                     NrCards = 0;

        for ( int iSuit = 0; iSuit < PbnSuit.NUMBER; iSuit++ )
        {
          NrCards += oDeal.GetNrRanks( oSide, oSuit );
          mImportAdmin.UnknownHandRemoveRanks( oSuit,
                oDeal.GetRanks( oSide, oSuit ) );
          oSuit.Next();
        }

        NrHandCards[ iSide ] = NrCards;
        if ( NrCards > PbnTrick.NUMBER )
        {
          lError.Set( PbnError.BAD_DEAL );
        }

        TotalNrCards += NrCards;
        NrSides++;
      }
      oSide.Next();
    }

    if ( NrSides == 0 )
    {
      lError.Set( PbnError.BAD_DEAL );
    }
    else
    {
      // The number of cards of each side must equal the average number
      // of cards per side.
      int                       NrAvCards = TotalNrCards / NrSides;

      oSide.Set( PbnSide.SOUTH );

      for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
      {
        if ( ! mImportAdmin.UnknownSideIs( oSide ) )
        {
          if ( NrHandCards[ iSide ] != NrAvCards )
          {
            lError.Set( PbnError.BAD_DEAL );
          }
        }
        oSide.Next();
      }

      if ( lError.IsOK() )
      {
        if ( NrAvCards < PbnTrick.NUMBER )
        {
          mGameData.GetEndPosSide().Set( PbnSide.UNKNOWN );
        }
      }
    }

    return lError;
  }

  public PbnError               CheckDealer(
  String                        oString )
  {
    if ( CheckNoInherit() ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnSide                     lDealer = mGameData.GetSituation().GetDealer();

    if ( CheckGenUnknown( oString ) )
    {
      lDealer.Set( PbnSide.UNKNOWN );
      return lError;
    }

    if ( CheckGenEmpty( oString ) )
    {
      lDealer.Set ( PbnSide.EMPTY );
      return lError;
    }

    lError = CheckSide( oString, lDealer );

    return lError;
  }

  public PbnError               CheckDeclarer(
  String                        oString )
  {
    if ( CheckNoInherit() ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnContract                 lContract = mGameData.GetContract();
    PbnSide                     lDeclarer = lContract.GetDeclarer();

    if ( CheckGenUnknown( oString ) )
    {
      lDeclarer.Set ( PbnSide.UNKNOWN );
      return lError;
    }

    if ( CheckGenEmpty( oString ) )
    {
      lDeclarer.Set ( PbnSide.EMPTY );
      return lError;
    }

    if ( oString.startsWith( "^" ) )
    {
      lContract.SetIrregularDeclarer( true );
      oString = oString.substring( 1 );
    }

    lError = CheckSide( oString, lDeclarer );
    if ( lError.IsOK() )
    {
      mImportAdmin.mLeader.Set( lDeclarer );
      mImportAdmin.mLeader.Next();
    }

    return lError;
  }

  public PbnError               CheckFrenchMP(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    if ( ! ( oString.equalsIgnoreCase( "Yes" ) ||
             oString.equalsIgnoreCase( "No"  ) ) )
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckHidden(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnSide                     lSide  = new PbnSide();
    int                         Length = oString.length();

    for ( int i = 0; i < Length; i++ )
    {
      lError = mParser.CheckSide( oString, lSide );
      if ( ! lError.IsOK() )
      {
        break;
      }
      mGameTags.SetHiddenSide( lSide, true );

      oString = oString.substring( 1 );
    }

    return lError;
  }

  private PbnError              CheckSectionTable(
  PbnTagId                      oTagId,
  String                        oString )
  {
    PbnTable                    lTable = null;

    lTable = mGameTags.GetTable( oTagId );
    if ( ! lTable.SetTagHeader( oString ) )
    {
      return new PbnError( PbnError.BAD_TAG_VALUE );
    }

    return new PbnError();
  }

  private PbnError              CheckEndOfString(
  String                        oString )
  {
    return CheckEndOfString( oString, PbnError.BAD_TAG_VALUE );
  }

  private PbnError              CheckEndOfString(
  String                        oString,
  int                           iError )
  {
    oString = PbnChar.SkipSpace( oString );
    if ( oString.length() == 0 )
    {
      iError = PbnError.OK;
    }

    return new PbnError( iError );
  }

  public PbnError               CheckScore(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnPScore                   oPScore = new PbnPScore();

    oString = PbnChar.SkipSpace( oString );
    if ( oString.startsWith( "EW" ) )
    {
      oString = mParser.GetPScores( oString, "NS", oPScore, lError );
      mScoreEW = oPScore.Side1;
      mScoreNS = oPScore.Side2;
    }
    else
    if ( oString.startsWith( "NS" ) )
    {
      oString = mParser.GetPScores( oString, "EW", oPScore, lError );
      mScoreNS = oPScore.Side1;
      mScoreEW = oPScore.Side2;
    }
    else
    {
      lError = mParser.ParseNumber( oString );
      mbScoreDeclarer = true;
      mScoreNS = mParser.GetNumber();
      oString = oString.substring( mParser.GetLength() );
    }

    if ( lError.IsOK() )
    {
      lError = CheckEndOfString( oString );
    }

    return lError;
  }

  public PbnError               CheckScoreOptimum(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnPScore                   oPScore = new PbnPScore();

    oString = PbnChar.SkipSpace( oString );
    if ( oString.startsWith( "EW" ) )
    {
      oString = mParser.GetPScores( oString, "NS", oPScore, lError );
      mScoreEW = oPScore.Side1;
      mScoreNS = oPScore.Side2;
    }
    else
    if ( oString.startsWith( "NS" ) )
    {
      oString = mParser.GetPScores( oString, "EW", oPScore, lError );
      mScoreNS = oPScore.Side1;
      mScoreEW = oPScore.Side2;
    }
    else
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
    }

    if ( lError.IsOK() )
    {
      lError = CheckEndOfString( oString );
    }

    return lError;
  }

  /*
   * Return score of declarer.
   */
  public int                    GetScore(
  PbnSide                       oDeclarer )
  {
    int                         lScore;

    if ( mbScoreDeclarer )
    {
      lScore = mScoreNS;
    }
    else
    {
      if ( oDeclarer.IsNS() )
      {
        lScore = mScoreNS;
      }
      else
      {
        lScore = mScoreEW;
      }
    }

    return lScore;
  }

  public PbnError               CheckScoreIMP(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnDecScore                 oFScoreEW = new PbnDecScore();
    PbnDecScore                 oFScoreNS = new PbnDecScore();

    oString = PbnChar.SkipSpace( oString );

    if ( oString.startsWith( "EW" ) )
    {
      oString = mParser.GetFScores( oString, "NS"
                                  , oFScoreEW, oFScoreNS, lError );
    }
    else
    if ( oString.startsWith( "NS" ) )
    {
      oString = mParser.GetFScores( oString, "EW"
                                  , oFScoreNS, oFScoreEW, lError );
    }
    else
    {
      oString = mParser.GetFScore( oString, oFScoreNS, lError );
    }

    if ( lError.IsOK() )
    {
      lError = CheckEndOfString( oString );
    }

    return lError;
  }

  private PbnError              RangeScorePerc(
  PbnDecScore                   oFScore )
  {
    PbnError                    lError = new PbnError();

    if ( ( oFScore.Low  < 0   ) ||
         ( oFScore.High < 0   ) ||
         ( oFScore.High > 100 ) ||
         ( ( oFScore.High == 100 ) && ( oFScore.Low != 0 ) ) )
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckScorePerc(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnDecScore                 oFScoreEW = new PbnDecScore();
    PbnDecScore                 oFScoreNS = new PbnDecScore();

    oString = PbnChar.SkipSpace( oString );

    if ( oString.startsWith( "EW" ) )
    {
      oString = mParser.GetFScores( oString, "NS"
                                  , oFScoreEW, oFScoreNS, lError );
    }
    else
    if ( oString.startsWith( "NS" ) )
    {
      oString = mParser.GetFScores( oString, "EW"
                                  , oFScoreNS, oFScoreEW, lError );
    }
    else
    {
      oString = mParser.GetFScore( oString, oFScoreNS, lError );
    }

    if ( lError.IsOK() )
    {
      lError = CheckEndOfString( oString );
    }

    if ( lError.IsOK() )
    {
      // Check range 0 - 100 %
      lError = RangeScorePerc( oFScoreEW );
      if ( lError.IsOK() )
      {
        lError = RangeScorePerc( oFScoreNS );
      }
    }

    return lError;
  }

  public PbnError               CheckScoreRubber(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    PbnDupScore                 oDScoreEW = new PbnDupScore();
    PbnDupScore                 oDScoreNS = new PbnDupScore();

    oString = PbnChar.SkipSpace( oString );

    if ( oString.startsWith( "EW" ) )
    {
      oString = mParser.GetDScores( oString, "NS"
                                  , oDScoreEW, oDScoreNS, lError );
    }
    else
    if ( oString.startsWith( "NS" ) )
    {
      oString = mParser.GetDScores( oString, "EW"
                                  , oDScoreNS, oDScoreEW, lError );
    }
    else
    {
      oString = mParser.GetDScore( oString, oDScoreNS, lError );
    }

    if ( lError.IsOK() )
    {
      lError = CheckEndOfString( oString );
    }

    return lError;
  }

  private boolean               CheckPosNeg(
  int                           i1,
  int                           i2 )
  {
    return ( (( i1 >= 0 ) && ( i2 < 0 ))
          || (( i2 >= 0 ) && ( i1 < 0 )) );
  }

  private boolean               CheckPosPos(
  int                           i1,
  int                           i2 )
  {
    return ( ( i1 >= 0 ) && ( i2 >= 0 ) );
  }

  private boolean               CheckTrickPoints(
  int                           iTp )
  {
    if ( iTp < 0 )
    {
      return false;
    }

    int v = iTp / 10;
    return ( 10 * v == iTp );
  }

  private boolean               CheckTrickPointsGame(
  int                           iTp1,
  int                           iTp2 )
  {
    if ( ! CheckTrickPoints( iTp1 ) ) return false;
    if ( ! CheckTrickPoints( iTp2 ) ) return false;
    if ( iTp1 >= 100 )
    {
      return iTp2 < 100;
    }
    else
    {
      return iTp2 >= 100;
    }
  }

  public PbnError               CheckScoreRubberHistory(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    int []                      laScoresNS = new int[ 5 ];
    int []                      laScoresEW = new int[ 5 ];

    oString = mParser.GetRScore( oString, "NS", laScoresNS, lError );
    if ( lError.IsOK() )
    {
      oString = mParser.GetRScore( oString, "EW", laScoresEW, lError );
    }

    if ( lError.IsOK() )
    {
      lError = CheckEndOfString( oString );
    }

    if ( lError.IsOK() )
    { /*
       * Check trick point scores.
       */
      if ( CheckPosNeg( laScoresNS[4], laScoresEW[4] )
        || CheckPosNeg( laScoresNS[3], laScoresEW[3] )
        || CheckPosNeg( laScoresNS[2], laScoresEW[2] ) )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }
      else
      if ( CheckPosPos( laScoresNS[4], laScoresEW[4] ) )
      {
        mImportAdmin.RubberVulner.Set( PbnVulner.ALL );

        if ( ! ( CheckTrickPointsGame( laScoresNS[3], laScoresEW[3] )
              && CheckTrickPointsGame( laScoresNS[2], laScoresEW[2] ) ) )
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }
      }
      else
      if ( CheckPosPos( laScoresNS[3], laScoresEW[3] ) )
      {
        if ( ! CheckTrickPointsGame( laScoresNS[2], laScoresEW[2] ) )
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }
        else
        if ( laScoresNS[2] > laScoresEW[2] )
        {
          mImportAdmin.RubberVulner.Set( PbnVulner.NS );
        }
        else
        {
          mImportAdmin.RubberVulner.Set( PbnVulner.EW );
        }
      }
      else
      {
        mImportAdmin.RubberVulner.Set( PbnVulner.NONE );
      }
    }

    if ( lError.IsOK() )
    {
      PbnVulner             lVulner = mGameData.GetSituation().GetVulner();

      if ( ! lVulner.Is( PbnVulner.UNKNOWN ) )
      {
        if ( ! lVulner.equals( mImportAdmin.RubberVulner ) )
        {
          lError.Set( PbnError.BAD_VULNERABLE );
        }
      }
    }

    return lError;
  }

  private String [] laScoringSystems =
  {
    "MP",
    "MatchPoints",
    "IMP",
    "Cavendish",
    "Chicago",
    "Rubber",
    "BAM",
    "Instant"
  };

  private int [] laScorings =
  {
    PbnImportAdmin.SCORING_MP,
    PbnImportAdmin.SCORING_MP,
    PbnImportAdmin.SCORING_IMP,
    PbnImportAdmin.SCORING_CAVENDISH,
    PbnImportAdmin.SCORING_CHICAGO,
    PbnImportAdmin.SCORING_RUBBER,
    PbnImportAdmin.SCORING_BAM,
    PbnImportAdmin.SCORING_MP
  };

  private String [] laScoringModifiers =
  {
    "Butler",
    "Butler-2",
    "Experts",
    "Cross",
    "Cross1",
    "Cross2",
    "Mean",
    "Median",
    "MP1",
    "MP2",
    "OldMP",
    "Mitchell2",
    "Mitchell3",
    "Mitchell4",
    "Ascherman",
    "Bastille",
    "EMP",
    "IMP_1948",
    "IMP_1961"
  };

  public PbnError               CheckScoring(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    Vector                      lVector = PbnU.ParseString( oString, ";" );
    Enumeration                 lEnum = lVector.elements(); 
    String                      lField;
    int                         index;

    while ( lEnum.hasMoreElements() )
    {
      lField = (String) lEnum.nextElement();

      index = PbnU.InStringArray( laScoringSystems, lField );
      if ( index >= 0 )
      {
        if ( mImportAdmin.mScoring == PbnImportAdmin.SCORING_NONE )
        {
          mImportAdmin.mScoring = laScorings[ index ];
        }
        else
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }
      }
      else
      {
        if ( PbnU.InStringArray( laScoringModifiers, lField ) < 0 )
        {
          lError.Set( PbnError.NEW_TAG_VALUE );
        }
      }
    }

    return lError;
  }

  public PbnError               CheckMode(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    if ( ! ( ( oString.equalsIgnoreCase( "EMB"   ) ) ||
             ( oString.equalsIgnoreCase( "IBS"   ) ) ||
             ( oString.equalsIgnoreCase( "OKB"   ) ) ||
             ( oString.equalsIgnoreCase( "TABLE" ) ) ||
             ( oString.equalsIgnoreCase( "TC"    ) ) ) )
    {
      lError.Set( PbnError.NEW_TAG_VALUE );
    }

    return lError;
  }

  /*
   * Check note tag.
   */
  public PbnError               CheckNote(
  String                        oString,
  PbnNote                       oNote )
  {
    if ( CheckGeneral2( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    lError = mParser.ParseNumberPos( oString );
    if ( lError.IsOK() )
    {
      oNote.Set( mParser.GetNumber() );

      oString = oString.substring( mParser.GetLength() );
      oString = PbnChar.SkipSpace( oString );
      if ( ! oNote.IsValid() )
      {
        lError.Set( PbnError.BAD_NOTE );
      }
      else
      if ( ! oString.startsWith( ":" ) )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }
      else
      {
        mCommentAdmin.SetType( PbnCommentAdmin.TYPE_NOTE_TAG, oNote.Get()-1 );
      }
    }

    return lError;
  }

  public PbnError               CheckPlay(
  String                        oString )
  {
    if ( CheckGeneral2( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    lError = CheckSide( oString, mImportAdmin.mTagPlaySide );

    if ( lError.IsOK() )
    {
      if ( ! mGameTags.TagIdExist( new PbnTagId ( PbnTagId.CONTRACT ) ) )
      {
        lError.Set( PbnError.NO_CONTRACT );
      }

      if ( ! mGameTags.TagIdExist( new PbnTagId ( PbnTagId.DEAL ) ) )
      {
        lError.Set( PbnError.NO_DEAL );
      }

      if ( ! mGameData.GetEndPosSide().Is( PbnSide.IDLE ) )
      {
        mImportAdmin.mLeader.Set( mImportAdmin.mTagPlaySide );
        /*
         * For export:
         */
        mGameData.GetEndPosSide().Set( mImportAdmin.mTagPlaySide );
      }
      else
      {
        if ( ! mGameTags.TagIdExist( new PbnTagId ( PbnTagId.DECLARER ) ) )
        {
          lError.Set( PbnError.NO_DECLARER );
        }
      }
    }

    return lError;
  }

  public PbnError               CheckResult(
  String                        oString )
  {
    PbnError                    lError = new PbnError();
    PbnResult                   lResult;
    PbnSide                     lDeclarer;
    Pbn2Results                 lResults = new Pbn2Results();
    boolean                     bUseDeclarer = false;
    int                         NrTricks;
    int                         NrTricksEW = 0;
    int                         NrTricksNS = 0;

    lResult = mGameData.GetResult();
    lDeclarer = mGameData.GetContract().GetDeclarer();

    if ( oString.length() == 0 )
    { /*
       * Inappropriate, or
       * All pass
       */
      lResult.Set( PbnResult.EMPTY );
      return lError;
      /************/
    }

    if ( CheckGeneral1( oString ) ) return mCheckError;

    if ( oString.startsWith( "^" ) )
    {
      lResult.SetIrregular( true );
      oString = oString.substring( 1 );
    }

    if ( oString.startsWith( "EW" ) )
    {
      oString = mParser.GetResults( oString, "NS", lResults, lError );
      NrTricksEW = lResults.Result1;
      NrTricksNS = lResults.Result2;
    }
    else
    if ( oString.startsWith( "NS" ) )
    {
      oString = mParser.GetResults( oString, "EW", lResults, lError );
      NrTricksNS = lResults.Result1;
      NrTricksEW = lResults.Result2;
    }
    else
    {
      lError = mParser.ParseNumberPos( oString );
      if ( lError.IsOK() )
      {
        NrTricks = mParser.GetNumber();
        lResult.SetTricks( NrTricks );

        if ( ! lDeclarer.IsValid() )
        {
          lError.Set( PbnError.NO_DECLARER );
        }
        else
        {
          bUseDeclarer = true;

          if ( lDeclarer.IsEW() )
          {
            NrTricksEW = NrTricks;
            NrTricksNS = PbnTrick.NUMBER - NrTricksEW;
          }
          else
          {
            NrTricksNS = NrTricks;
            NrTricksEW = PbnTrick.NUMBER - NrTricksNS;
          }
        }
      }
    }

    if ( lError.IsOK() )
    {
      if ( bUseDeclarer )
      {
        oString = oString.substring( mParser.GetLength() );
      }
      else
      if ( lDeclarer.IsValid() )
      {
        if ( lDeclarer.IsEW() )
        {
          lResult.SetTricks( NrTricksEW );
        }
        else
        {
          lResult.SetTricks( NrTricksNS );
        }
      }

      if ( ( NrTricksEW > PbnTrick.NUMBER ) ||
           ( NrTricksNS > PbnTrick.NUMBER ) ||
           ( NrTricksEW + NrTricksNS > PbnTrick.NUMBER ) )
      {
        lError.Set( PbnError.BAD_NUMBER );
      }
      else
      if ( ( PbnGen.GetVersion() == PbnGen.VERSION_10   ) &&
           ( NrTricksEW + NrTricksNS != PbnTrick.NUMBER ) )
      {
        lError.Set( PbnError.BAD_NUMBER );
      }        
    }

    if ( lError.IsOK() )
    {
      lError = CheckEndOfString( oString );
    }

    return lError;
  }

  public PbnError               CheckRoom(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();

    if ( ! ( oString.equalsIgnoreCase( "Open"   )
          || oString.equalsIgnoreCase( "Closed" ) ) )
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckRound(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    int                         StringLength = oString.length();

    for ( int index = 0; index < StringLength; index++ )
    {
      char                      cRound = oString.charAt( index );

      // Check the digits.
      if ( ! ( PbnChar.IsLetter( cRound ) ||
               PbnChar.IsDigit( cRound )  ||
               ( cRound == '_' )          ||
               ( cRound == '.' )          ) )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
        break;
        /****/
      }
    }

    return lError;
  }

  public PbnError               CheckTable(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    int                         Table;

    lError = mParser.ParseNumberPos( oString );
    Table = mParser.GetNumber();

    if ( lError.IsOK() )
    {
      oString = oString.substring( mParser.GetLength() );
      lError = CheckEndOfString( oString );
    }

    if ( ! lError.IsOK() )
    {
      lError.Set( PbnError.NEW_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckTime(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    int                         Hours;
    int                         Minutes;
    int                         Seconds;

    if ( ( oString.length() == 8                  ) &&
         ( PbnChar.IsDigit( oString.charAt( 0 ) ) ) &&
         ( PbnChar.IsDigit( oString.charAt( 1 ) ) ) &&
         ( oString.charAt( 2 ) == ':'             ) &&
         ( PbnChar.IsDigit( oString.charAt( 3 ) ) ) &&
         ( PbnChar.IsDigit( oString.charAt( 4 ) ) ) &&
         ( oString.charAt( 5 ) == ':'             ) &&
         ( PbnChar.IsDigit( oString.charAt( 6 ) ) ) &&
         ( PbnChar.IsDigit( oString.charAt( 7 ) ) ) )
    {
      Hours   = mParser.toNumber( oString.substring( 0, 2 ) );
      Minutes = mParser.toNumber( oString.substring( 3, 5 ) );
      Seconds = mParser.toNumber( oString.substring( 6, 8 ) );

      if ( ! ( 0 <= Hours ) && ( Hours <= 23 ) )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }

      if ( ! ( 0 <= Minutes ) && ( Minutes <= 59 ) )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }

      if ( ! ( 0 <= Seconds ) && ( Seconds <= 59 ) )
      {
        lError.Set( PbnError.BAD_TAG_VALUE );
      }
    }
    else
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
    }

    return lError;
  }

  public PbnError               CheckTimeMove(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    int                         NrSeconds;

    lError = mParser.ParseNumberPos( oString );
    NrSeconds = mParser.GetNumber();

    if ( lError.IsOK() )
    {
      oString = oString.substring( mParser.GetLength() );
      lError = CheckEndOfString( oString );
    }

    return lError;
  }

  public PbnError               CheckTimeControl(
  String                        oString )
  {
    if ( CheckGeneral1( oString ) ) return mCheckError;

    PbnError                    lError = new PbnError();
    int                         Games;
    int                         Minutes;

    if ( ! oString.equals( "-" ) )
    {
      lError = mParser.ParseNumberPos( oString );
      Games = mParser.GetNumber();

      if ( lError.IsOK() )
      {
        oString = oString.substring( mParser.GetLength() );
        if ( ! oString.startsWith( "/" ) )
        {
          lError.Set( PbnError.BAD_TAG_VALUE );
        }
        else
        {
          oString = oString.substring( 1 );
        }
      }

      if ( lError.IsOK() )
      {
        lError = mParser.ParseNumberPos( oString );
        Minutes = mParser.GetNumber();

        if ( lError.IsOK() )
        {
          oString = oString.substring( mParser.GetLength() );
          lError = CheckEndOfString( oString );
        }
      }
    }

    return lError;
  }

  public PbnError               CheckVulnerable(
  String                        oString )
  {
    if ( CheckNoInherit() ) return mCheckError;

    for ( int i = 0; i < VulnerPair.NUMBER; i++ )
    {
      if ( oString.equalsIgnoreCase( VulnerPair.GetString(i) ) )
      {
        PbnVulner   lVulner = mGameData.GetSituation().GetVulner();

        lVulner.Set( VulnerPair.GetVulner(i) );

        if ( ! mImportAdmin.RubberVulner.Is( PbnVulner.UNKNOWN ) )
        {
          if ( ! lVulner.equals( mImportAdmin.RubberVulner ) )
          {
            return new PbnError( PbnError.BAD_VULNERABLE );
          }
        }

        return new PbnError();
        /********************/
      }
    }

    return new PbnError( PbnError.SEVERE_TAG_VALUE );
  }

  public PbnError               CheckEmpty(
  String                        oString )
  {
    CheckGenEmpty( oString );

    return new PbnError();
  }

  private PbnError              GameNoteCopy(
  PbnNote                       oNote,
  String                        oString )
  {
    PbnError                    lError = new PbnError();
    PbnMoveNote                 lPbnMoveNote = new PbnMoveNote();
    String                      lTagValue;

    switch ( mImportAdmin.mSection )
    {
    case PbnGen.SECTION_AUCTION:
      lPbnMoveNote = mGameTags.GetCallNote( oNote.Get() );
      break;

    case PbnGen.SECTION_PLAY:
      lPbnMoveNote = mGameTags.GetCardNote( oNote.Get() );
      break;
    }

    lTagValue = lPbnMoveNote.GetTagValue();
    if ( lTagValue.length() > 0 )
    {
      lError.Set( PbnError.TAG_EXISTS );
      return lError;
      /************/
    }

    int iColon = oString.indexOf( ":" );
    if ( iColon == -1 )
    {
      lError.Set( PbnError.BAD_TAG_VALUE );
      return lError;
      /************/
    }

    oString = oString.substring( iColon+1 );
    lPbnMoveNote.SetTagValue( oString );

    return lError;
  }

  private PbnError              GameTagCopy(
  PbnTagId                      oTagId,
  int                           iTagType,
  String                        oString )
  {
    PbnError                    lError = new PbnError();

    if ( mGameTags.TagIdExist( oTagId ) )
    {
      lError.Set( PbnError.TAG_EXISTS );
      return lError;
      /************/
    }

    mGameTags.SetTagValue( oTagId, iTagType, oString );

    return lError;
  }

/*
 * Check the syntax of the tag values.
 */
  public PbnError               TagValueParse(
  PbnTagId                      oTagId,
  String                        oString )
  {
    PbnError                    lError = new PbnError();
    String                      lValueBuffer;
    int                         lTagType = PbnTagUse.NONE;

    mbUsePrevTag = false;
    switch ( PbnGen.GetParsing() )
    {
    case PbnGen.PARSE_FIRST:
      if ( CheckGenIdentical( oString ) )
      { /*
         * Use previous game's tag.
         */
        mbUsePrevTag = true;
        lTagType = PbnTagUse.PREV;

        lValueBuffer = mInheritTags.SetPrev( oTagId );
        if ( lValueBuffer == null )
        {
          lValueBuffer = "";
        }
      }
      else
      if ( ( PbnGen.GetVersion() != PbnGen.VERSION_10 )
        && ( CheckGenCopy( oString )                  ) )
      { /*
         * case "##" : Use previous game's tag.
         */
        mbUsePrevTag = true;
        lTagType = PbnTagUse.HSHS;
        lValueBuffer = oString.substring( 2 );

        mInheritTags.SetHshs( oTagId, lValueBuffer );
      }
      else
      {
        lTagType = PbnTagUse.USED;
        mInheritTags.SetUsed( oTagId, oString );
        lValueBuffer = oString;
      }

      lError = TagValueCheck( oTagId, lValueBuffer );
    
      if ( ! lError.HasSeverity( PbnError.SEV_WARNING ) )
      {
        PbnError                lCopyError;

        lCopyError = TagValueCopy( oTagId, lTagType, oString );

        lError.SetWorst( lCopyError );
      }
      break;

    case PbnGen.PARSE_NEXT:
      if ( CheckGenIdentical( oString ) )
      { /*
         * Skip tag.
         */
      }
      else
      {
        if ( ( PbnGen.GetVersion() != PbnGen.VERSION_10 )
          && ( CheckGenCopy( oString )                  ) )
        { /*
           * case "##" : Use previous game's tag.
           */
          lTagType = PbnTagUse.HSHS;
          lValueBuffer = oString.substring( 2 );
          lError = TagValueCopy( oTagId, lTagType, lValueBuffer );
        }
        else
        {
          lError = TagValueCheck( oTagId, oString );
    
          if ( ! lError.HasSeverity( PbnError.SEV_WARNING ) )
          {
            PbnError            lCopyError;

            lTagType = PbnTagUse.USED;
            lCopyError = TagValueCopy( oTagId, lTagType, oString );

            lError.SetWorst( lCopyError );
          }
        }
      }
      break;

    case PbnGen.PARSE_COPY:
      if ( CheckGenIdentical( oString ) )
      { /*
         * Skip tag.
         */
      }
      else
      {
        if ( ( PbnGen.GetVersion() != PbnGen.VERSION_10 )
          && ( CheckGenCopy( oString )                  ) )
        { /*
           * case "##" : Use previous game's tag.
           */
          lTagType = PbnTagUse.COPY;
          lValueBuffer = oString.substring( 2 );
          lError = TagValueCopy( oTagId, lTagType, lValueBuffer );
        }
        else
        {
          lError = TagValueCheck( oTagId, oString );
    
          if ( ! lError.HasSeverity( PbnError.SEV_WARNING ) )
          {
            PbnError            lCopyError;

            lTagType = PbnTagUse.PREV;
            lCopyError = TagValueCopy( oTagId, lTagType, oString );

            lError.SetWorst( lCopyError );
          }
        }
      }
      break;
    }

    return lError;
  }

  private PbnError              TagValueCheck(
  PbnTagId                      oTagId,
  String                        lValueBuffer )
  {
    PbnError                    lError = new PbnError();

    switch ( oTagId.Get() )
    {
    case PbnTagId.ANNOTATOR:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.ANNOTATORNA:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.AUCTION:
      lError = CheckAuction( lValueBuffer );
      break;
    case PbnTagId.BIDSYSTEMEW:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.BIDSYSTEMNS:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.BOARD:
      lError = CheckBoard( lValueBuffer );
      break;
    case PbnTagId.COMPETITION:
      lError = CheckCompetition( lValueBuffer );
      break;
    case PbnTagId.CONTRACT:
      lError = CheckContract( lValueBuffer );
      break;
    case PbnTagId.DATE:
      lError = CheckDate( lValueBuffer );
      break;
    case PbnTagId.DEALER:
      lError = CheckDealer( lValueBuffer );
      break;
    case PbnTagId.DEAL:
      lError = CheckDeal( lValueBuffer );
      break;
    case PbnTagId.DECLARER:
      lError = CheckDeclarer( lValueBuffer );
      break;
    case PbnTagId.DESCRIPTION:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.EAST:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.EASTNA:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.EASTTYPE:
      lError = CheckSideType( lValueBuffer );
      break;
    case PbnTagId.EVENT:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.EVENTDATE:
      lError = CheckDate( lValueBuffer );
      break;
    case PbnTagId.EVENTSPONSOR:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.FRENCHMP:
      lError = CheckFrenchMP( lValueBuffer );
      break;
    case PbnTagId.GENERATOR:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.HIDDEN:
      lError = CheckHidden( lValueBuffer );
      break;
    case PbnTagId.HOMETEAM:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.MODE:
      lError = CheckMode( lValueBuffer );
      break;
    case PbnTagId.NORTH:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.NORTHNA:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.NORTHTYPE:
      lError = CheckSideType( lValueBuffer );
      break;
    case PbnTagId.NOTE:
      lError = CheckNote( lValueBuffer, mNote );
      break;
    case PbnTagId.PLAY:
      lError = CheckPlay( lValueBuffer );
      break;
    case PbnTagId.RESULT:
      lError = CheckResult( lValueBuffer );
      break;
    case PbnTagId.ROOM:
      lError = CheckRoom( lValueBuffer );
      break;
    case PbnTagId.ROUND:
      lError = CheckRound( lValueBuffer );
      break;
    case PbnTagId.SCORE:
      lError = CheckScore( lValueBuffer );
      break;
    case PbnTagId.SCOREIMP:
      lError = CheckScoreIMP( lValueBuffer );
      break;
    case PbnTagId.SCOREPERC:
      lError = CheckScorePerc( lValueBuffer );
      break;
    case PbnTagId.SCORERUBBER:
      lError = CheckScoreRubber( lValueBuffer );
      break;
    case PbnTagId.SCORING:
      lError = CheckScoring( lValueBuffer );
      break;
    case PbnTagId.SECTION:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.SITE:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.SOUTH:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.SOUTHNA:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.SOUTHTYPE:
      lError = CheckSideType( lValueBuffer );
      break;
    case PbnTagId.STAGE:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.TABLE:
      lError = CheckTable( lValueBuffer );
      break;
    case PbnTagId.TERMINATION:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.TIME:
      lError = CheckTime( lValueBuffer );
      break;
    case PbnTagId.TIMECALL:
      lError = CheckTimeMove( lValueBuffer );
      break;
    case PbnTagId.TIMECARD:
      lError = CheckTimeMove( lValueBuffer );
      break;
    case PbnTagId.TIMECONTROL:
      lError = CheckTimeControl( lValueBuffer );
      break;
    case PbnTagId.UTCDATE:
      lError = CheckDate( lValueBuffer );
      break;
    case PbnTagId.UTCTIME:
      lError = CheckTime( lValueBuffer );
      break;
    case PbnTagId.VISITTEAM:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.VULNERABLE:
      lError = CheckVulnerable( lValueBuffer );
      break;
    case PbnTagId.WEST:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.WESTNA:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.WESTTYPE:
      lError = CheckSideType( lValueBuffer );
      break;

    /*
     * PBN 2.0 tags
     */
    case PbnTagId.APPLICATION:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.DEALID:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.PAIREW:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.PAIRNS:
      lError = CheckEmpty( lValueBuffer );
      break;
    case PbnTagId.SCOREMP:
      lError = CheckScoreIMP( lValueBuffer );
      break;
    case PbnTagId.SCORERUBBERHISTORY:
      lError = CheckScoreRubberHistory( lValueBuffer );
      break;

    /*
     * PBN 2.0 section tags
     */
    case PbnTagId.ACTIONTABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;
    case PbnTagId.AUCTIONTIMETABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;
    case PbnTagId.INSTANTSCORETABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;
    case PbnTagId.PLAYTIMETABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;
    case PbnTagId.SCORETABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;
    case PbnTagId.TOTALSCORETABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;

    /*
     * PBN 2.1 tags
     */
    case PbnTagId.OPTIMUMSCORE:
      lError = CheckScoreOptimum( lValueBuffer );
      break;

    /*
     * PBN 2.1 section tags
     */
    case PbnTagId.OPTIMUMPLAYTABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;
    case PbnTagId.OPTIMUMRESULTTABLE:
      lError = CheckSectionTable( oTagId, lValueBuffer );
      break;
    }

    return lError;
  }

  private PbnError              TagValueCopy(
  PbnTagId                      oTagId,
  int                           iTagType,
  String                        oString )
  {
    PbnError                    lError = new PbnError();

    if ( oTagId.Is( PbnTagId.NOTE ) )
    {
      lError = GameNoteCopy( mNote, oString );
    }
    else
    {
      lError = GameTagCopy( oTagId, iTagType, oString );
    }

    return lError;
  }

  public PbnError               TagNameCheck(
  String                        oString,
  PbnTagId                      oTagId )
  {
    PbnError                    lError;

    lError = oTagId.Search( oString );
    if ( lError.IsOK() )
    {
      if ( oTagId.Is( PbnTagId.NOTE ) )
      { /*
         * Initialise the mNote to prepare rest of handling.
         */
        mNote = new PbnNote();
      }
      else
      {
        if ( mGameTags.TagIdExist( oTagId ) )
        {
          lError.Set( PbnError.TAG_EXISTS );
        }
        mCommentAdmin.SetType( PbnCommentAdmin.TYPE_TAG, oTagId.Get() );
      }
    }

    return lError;
  }


  public PbnError               CallCheck(
  String                        oString )
  {
    PbnError                    lError = new PbnError();
    boolean                     bAllPass = false;
    PbnCall                     lCall = new PbnCall();

    if ( mImportAdmin.IsLastCall() )
    {
      lError.Set( PbnError.BAD_LAST_CALL );
    }
    else
    if ( oString.equals( "+" ) )
    {
      mImportAdmin.SetLastCall();
      lCall.SetType( PbnCall.TYPE_NONE );
    }
    else
    if ( oString.equals( "-" ) )
    {
      lCall.SetType( PbnCall.TYPE_NONE );
    }
    else
    if ( oString.equalsIgnoreCase( "AP" ) )
    {
      bAllPass = true;
    }
    else
    if ( oString.equalsIgnoreCase( "Pass" ) )
    {
      lCall.SetType( PbnCall.TYPE_PASS );
    }
    else
    if ( oString.equalsIgnoreCase( PbnRisk.S_DOUBLE ) )
    {
      lCall.SetType( PbnCall.TYPE_DBL );
    }
    else
    if ( oString.equalsIgnoreCase( PbnRisk.S_REDOUBLE ) )
    {
      lCall.SetType( PbnCall.TYPE_RDB );
    }
    else
    {
      if ( mImportAdmin.IllegalMoveHas( PbnImportAdmin.ILLEGAL_INSUFF ) )
      {
        lCall.SetType( PbnCall.TYPE_INSUFF );
      }
      else
      {
        lCall.SetType( PbnCall.TYPE_REAL );
      }

      char                      cNumber = oString.charAt( 0 );

      if ( ( '1' <= cNumber ) && ( cNumber <= '7' ) )
      {
        lCall.SetNrTricks( cNumber - '0' );
        oString = oString.substring( 1 );

        PbnTrump                lTrump = new PbnTrump();

        lError = mParser.CheckTrump( oString, lTrump );
        if ( lError.IsOK() )
        {
          lCall.GetTrump().Set( lTrump );

          oString = oString.substring( mParser.GetLength() );
          lError = CheckEndOfString( oString, PbnError.BAD_CALL );
        }
      }
      else
      {
        lError.Set( PbnError.BAD_CALL );
      }
    }

    if ( lError.IsOK() )
    {
      if ( bAllPass )
      {
        lError = CallStorePasses();
      }
      else
      {
        lError = CallStore( lCall );
      }
    }

    return lError;
  }

  public PbnError               IllegalCallCheck(
  String                        oString )
  {
    PbnError                    lError = new PbnError();

    if ( oString.equalsIgnoreCase( "I" ) )
    {
      if ( mImportAdmin.IllegalMoveHas( PbnImportAdmin.ILLEGAL_INSUFF ) )
      {
        lError.Set( PbnError.DOUBLE_ILLEGAL );
      }
      else
      {
        mImportAdmin.IllegalMoveAdd( PbnImportAdmin.ILLEGAL_INSUFF );
      }
    }
    else
    if ( oString.equalsIgnoreCase( "S" ) )
    {
      mImportAdmin.IllegalMoveSet( PbnImportAdmin.ILLEGAL_SKIP );

      PbnCall                   lCall = new PbnCall();

      lCall.SetType( PbnCall.TYPE_SKIP );
      lError = CallStore( lCall );
    }
    else
    {
      lError.Set( PbnError.BAD_ILLEGAL );
    }

    return lError;
  }

  private PbnError              CallStore(
  PbnCall                       oCall )
  {
    PbnError                    lError = new PbnError();

    if ( oCall.GetType() != PbnCall.TYPE_NONE )
    {
      mImportAdmin.CallInc();
      mGameTags.IncCallAnno();

      if ( ! mImportAdmin.mbFirstCall )
      {
        mImportAdmin.mbFirstCall = true;
        if ( ! mImportAdmin.mAuctionSide.equals(
                mGameData.GetSituation().GetDealer() ) )
        {
          lError.Set( PbnError.BAD_SIDE );
        }
      }

      if ( lError.IsOK() )
      {
        if ( mImportAdmin.IsLastCall() )
        {
          lError.Set( PbnError.BAD_LAST_CALL );
        }
      }

      if ( lError.IsOK() )
      {
        lError = mGameData.GetAuction().StoreCall( oCall );
      }
    }
    mImportAdmin.mAuctionSide.Next();

    mImportAdmin.IllegalMoveClear();

    return lError;
  }

  private PbnError              CallStorePasses()
  {
    PbnError                    lError = new PbnError();
    PbnAuction                  lAuction = mGameData.GetAuction();
    int                         iNrPasses = lAuction.GetNrToPass();
    for ( int i = 0; i < iNrPasses; i++ )
    {
      PbnCall                   lCall = new PbnCall();

      lCall.SetType( PbnCall.TYPE_PASS );
      lError = CallStore( lCall );
    }

    return lError;
  }

  public PbnError               NagCheck(
  String                        oString,
  PbnNag                        oNag )
  {
    PbnError                    lError = new PbnError();

    lError = mParser.ParseNumberPos( oString );
    if ( lError.IsOK() )
    {
      oString = oString.substring( mParser.GetLength() );
      lError = CheckEndOfString( oString, PbnError.BAD_NAG );
    }

    if ( lError.IsOK() )
    {
      oNag.Set( mParser.GetNumber() );
      if ( ! oNag.IsValid() )
      {
        lError.Set( PbnError.BAD_NAG );
      }
      else
      {
        switch ( mImportAdmin.mSection )
        {
        case PbnGen.SECTION_AUCTION:
          if ( oNag.IsPlayNag() )
          {
            lError.Set( PbnError.BAD_NAG );
          }
          break;
        case PbnGen.SECTION_PLAY:
          if ( oNag.IsAuctionNag() )
          {
            lError.Set( PbnError.BAD_NAG );
          }
          break;
        }

        if ( lError.IsOK() )
        {
          lError = NagStore( oNag );
        }
      }
    }

    return lError;
  }

  private PbnError              NagStore(
  PbnNag                        oNag )
  {
    PbnError                    lError = new PbnError( PbnError.BAD_NAG );
    PbnMoveAnno                 lMoveAnno = new PbnMoveAnno();

    switch ( mImportAdmin.mSection )
    {
    case PbnGen.SECTION_AUCTION:
      if ( mImportAdmin.mNrCalls >= 0 )
      {
        lMoveAnno = mGameTags.GetCallAnno( mImportAdmin.mNrCalls );
        lError.SetOK();
      }
      break;

    case PbnGen.SECTION_PLAY:
      if ( mImportAdmin.mNrTricks >= 0 )
      {
        lMoveAnno = mGameTags.GetCardAnno( mImportAdmin.mNrTricks
                                         , mImportAdmin.mPlaySide );
        lError.SetOK();
      }
      break;
    }

    if ( lError.IsOK() )
    {
      if ( oNag.IsSuffix() )
      { /*
         * Change state to inform the comment administration.
         */
        mImportAdmin.mbInformComment = true;

        if ( lMoveAnno.ContainsSuffix() )
        {
          lError.Set( PbnError.DOUBLE_NAG );
        }
      }
      else
      {
        if ( lMoveAnno.ContainsNag( oNag ) )
        {
          lError.Set( PbnError.DOUBLE_NAG );
        }
      }
    }

    if ( lError.IsOK() )
    {
      lMoveAnno.PutNag( oNag );
    }

    return lError;
  }

  public PbnError               NoteRefCheck(
  String                        oString )
  {
    PbnError                    lError = new PbnError();
    PbnNote                     lNote;

    lError = mParser.ParseNumberPos( oString );
    if ( lError.IsOK() )
    {
      oString = oString.substring( mParser.GetLength() );
      lError = CheckEndOfString( oString, PbnError.BAD_NOTE );
    }

    if ( lError.IsOK() )
    {
      lNote = new PbnNote( mParser.GetNumber() );
      if ( ! lNote.IsValid() )
      {
        lError.Set( PbnError.BAD_NOTE );
      }
      else
      {
        lError = NoteRefStore( lNote );
      }
    }

    return lError;
  }

  private PbnError              NoteRefStore(
  PbnNote                       oNote )
  {
    PbnError                    lError = new PbnError( PbnError.BAD_NOTE );
    PbnMoveAnno                 lMoveAnno = new PbnMoveAnno();
    PbnMoveNote                 lMoveNote = new PbnMoveNote();

    switch ( mImportAdmin.mSection )
    {
    case PbnGen.SECTION_AUCTION:
      if ( mImportAdmin.mNrCalls >= 0 )
      {
        lMoveNote = mGameTags.GetCallNote( oNote.Get() );
        lMoveAnno = mGameTags.GetCallAnno( mImportAdmin.mNrCalls );
        lError.SetOK();
      }
      break;

    case PbnGen.SECTION_PLAY:
      if ( mImportAdmin.mNrTricks >= 0 )
      {
        lMoveNote = mGameTags.GetCardNote( oNote.Get() );
        lMoveAnno = mGameTags.GetCardAnno( mImportAdmin.mNrTricks, mImportAdmin.mPlaySide );
        lError.SetOK();
      }
      break;
    }

    if ( lError.IsOK() )
    {
      lMoveNote.SetReference( true );

      PbnNote                   lNote = lMoveAnno.GetNote();

      if ( lNote.IsValid() )
      {
        lError.Set( PbnError.DOUBLE_NOTE );
      }
      else
      {
        lNote.Set( oNote );
      }
    }

    return lError;
  }


  public PbnError               SuffixCheck(
  String                        oString )
  {
    PbnError                    lError = new PbnError();
    int                         iSuffix = 0;
    int                         iLength = oString.length();

    if ( oString.charAt( 0 ) == '!' )
    {
      if ( iLength == 1 )
      {
        iSuffix = PbnSuffix.GOOD;
      }
      else
      switch ( oString.charAt( 1 ) )
      {
      case '!':
        iSuffix = PbnSuffix.VERY_GOOD;
        break;
      case '?':
        iSuffix = PbnSuffix.SPECULATIVE;
        break;
      default:
        lError.Set( PbnError.BAD_SUFFIX );
        break;
      }
    }
    else
    { /*
       * oString.charAt( 0 ) == '?'
       */
      if ( iLength == 1 )
      {
        iSuffix = PbnSuffix.POOR;
      }
      else
      switch ( oString.charAt( 1 ) )
      {
      case '!':
        iSuffix = PbnSuffix.QUESTIONABLE;
        break;
      case '?':
        iSuffix = PbnSuffix.VERY_POOR;
        break;
      default:
        lError.Set( PbnError.BAD_SUFFIX );
        break;
      }
    }

    if ( lError.IsOK() )
    {
      if ( iLength > 2 )
      {
        lError.Set( PbnError.BAD_SUFFIX );
      }

      if ( lError.IsOK() )
      {
        lError = SuffixStore( new PbnSuffix( iSuffix ) );
      }
    }

    return( lError );
  }

  private PbnError              SuffixStore(
  PbnSuffix                     oSuffix )
  {
    PbnError                    lError = new PbnError( PbnError.BAD_SUFFIX );
    PbnMoveAnno                 lMoveAnno = new PbnMoveAnno();
    boolean                     bAuction = false;

    switch ( mImportAdmin.mSection )
    {
    case PbnGen.SECTION_AUCTION:
      bAuction = true;
      if ( mImportAdmin.mNrCalls >= 0 )
      {
        lMoveAnno = mGameTags.GetCallAnno( mImportAdmin.mNrCalls );
        lError.SetOK();
      }
      break;

    case PbnGen.SECTION_PLAY:
      if ( mImportAdmin.mNrTricks >= 0 )
      {
        lMoveAnno = mGameTags.GetCardAnno( mImportAdmin.mNrTricks
                                         , mImportAdmin.mPlaySide );
        lError.SetOK();
      }
      break;
    }

    if ( lError.IsOK() )
    {
      PbnSuffix                 lSuffix = lMoveAnno.GetSuffix();

      if ( lSuffix.IsValid() )
      {
        lError.Set( PbnError.DOUBLE_SUFFIX );
      }
      else
      {
        lSuffix.Set( oSuffix );

        PbnNag                  lNag = oSuffix.Convert( bAuction );
        lError = NagStore( lNag );
      }
    }

    return( lError );
  }

  public PbnError               CardCheck(
  String                        oString )
  {
    PbnError                    lError;
    PbnSuit                     lSuit = new PbnSuit();
    PbnRank                     lRank = new PbnRank();
    PbnCard                     lCard = new PbnCard();

    mImportAdmin.CardInc();

    PbnDeal                     lDeal = mGameData.GetDeal();
    PbnSide                     lSide = new PbnSide( PbnSide.SOUTH );
    int                         NrCards = 0;

    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      NrCards += lDeal.GetNrRanks( lSide );
      lSide.Next();
    }
    NrCards /= PbnSide.NUMBER;

    lError = mGameData.GetContract().IsPlayable( NrCards == PbnTrick.NUMBER );
    if ( ! lError.IsOK() )
    {
      return lError;
    }

    lError = mParser.CheckSuit( oString, lSuit );
    if ( lError.IsOK() )
    {
      oString = oString.substring( mParser.GetLength() );
      lError = mParser.CheckRank( oString, lRank );

      if ( lError.IsOK() )
      {
        oString = oString.substring( mParser.GetLength() );
      }
    }
    else
    if ( oString.startsWith( "-" ) )
    {
      lSuit.Set( PbnSuit.UNKNOWN );
      lRank.Set( PbnRank.NONE );
      lError.SetOK();
      oString = oString.substring( 1 );
    }
    else
    if ( oString.startsWith( "+" ) )
    {
      lSuit.Set( PbnSuit.UNKNOWN );
      lRank.Set( PbnRank.NONE );
      if ( mImportAdmin.SetLastCard() )
      {
        lError.SetOK();
      }
      else
      {
        lError.Set( PbnError.BAD_LAST_CARD );
      }
      oString = oString.substring( 1 );
    }

    if ( lError.IsOK() )
    {
      lCard = new PbnCard( lSuit, lRank );

      if ( oString.length() != 0 )
      {
        lError.Set( PbnError.BAD_CARD );
      }
    }

    if ( lError.IsOK() )
    { /*
       * Check if player owns card.
       */
      if ( lSuit.IsValid() )
      {
        if ( mImportAdmin.UnknownSideIs( mImportAdmin.mPlaySide ) )
        {
          if ( ! mImportAdmin.UnknownHandRemoveCard( lCard ) )
          {
            lError.Set( PbnError.BAD_CARD );
          }
        }
        else
        if ( ! mImportAdmin.mDeal.PlayCard( mImportAdmin.mPlaySide, lCard ) )
        {
          lError.Set( PbnError.BAD_CARD );
        }
      }

      mImportAdmin.IllegalMoveCopy();
      mImportAdmin.IllegalMoveClear();

      mGameData.GetPlay().SetCard( mImportAdmin.mNrTricks
                                 , mImportAdmin.mPlaySide
                                 , lCard );
    }

    return lError;
  }

  public PbnError               TrickCheck()
  {
    PbnError                    lError = new PbnError();
    PbnPlay                     lPlay;
    PbnContract                 lContract;
    boolean                     bIllegalLead;
    PbnSide                     lPlaySide = new PbnSide();
    PbnCard                     lLeadCard;
    PbnCard                     lPlayCard;
    PbnCard                     lWinCard;
    PbnSide                     lWinnerSide = new PbnSide();
    PbnSuit                     lTrumpSuit;
    int                         NrUnknown = 0;

    if ( ! mImportAdmin.CardsToBeChecked() )
    {
      // No need to check trick.
      return lError;
    }

    lPlay     = mGameData.GetPlay();
    lContract = mGameData.GetContract();

    /*
     * Check if suit of played cards is the same as the lead suit.
     */
    bIllegalLead = mImportAdmin.IllegalLeadGet( lPlaySide );
    if ( bIllegalLead )
    { /*
       * Administer an illegal lead play.
       */
      lPlay.SetFirst( mImportAdmin.mNrTricks, lPlaySide );
    }
    else
    {
      lPlaySide.Set( mImportAdmin.mLeader );
    }

    /*
     * 2009-10-02 Bug report
     * When more than 1 trick contains unknown cards
     * indicated by '-', then no lead side can be determined.
     */
    if ( lPlaySide.mSide < 0 )
    {
      return lError;
    }

    // Real reference, no copy
    lLeadCard = lPlay.GetCard( mImportAdmin.mNrTricks
                             , lPlaySide );

    if ( bIllegalLead && ( lPlaySide.equals( mImportAdmin.mLeader ) ) )
    {
      lError.Set( PbnError.BAD_LEAD );
    }
    else
    if ( mImportAdmin.IllegalPlayHasRevoke( lPlaySide ) )
    {
      lError.Set( PbnError.BAD_REVOKE );
    }
    else
    if ( lLeadCard.GetSuit().Is( PbnSuit.UNKNOWN ) )
    { /*
       * Accept this error, when the opponents have e.g. Trump Ace
       */
      lError.Set( PbnError.UNKNOWN_LEAD );
      lError.SetOK();
    }
    else
    { /*
       * Also, determine the winner side.
       */
      lWinnerSide = new PbnSide( lPlaySide );
      lWinCard    = new PbnCard( lLeadCard );
      lTrumpSuit  = new PbnSuit( lContract.GetTrump() );

      boolean           bCheckLastCard;
      boolean           bLastCardPlayed = false;
      bCheckLastCard = mImportAdmin.IsLastCardTrick();

      for ( int iSide = 1; iSide < PbnSide.NUMBER; iSide++ )
      {
        lPlaySide.Next();
        lPlayCard = lPlay.GetCard( mImportAdmin.mNrTricks
                                 , lPlaySide );

        if ( lPlayCard.GetSuit().Is( PbnSuit.UNKNOWN ) )
        {
          if ( bCheckLastCard )
          {
            if ( mImportAdmin.IsLastCardSide( lPlaySide ) )
            {
              bLastCardPlayed = true;
            }
          }

          NrUnknown++;
          continue;
          /*******/
        }

        if ( bLastCardPlayed )
        { /*
           * A card has been played after the 'last card'.
           */
          lError.Set( PbnError.BAD_LAST_CARD );
        }

        if ( lLeadCard.GetSuit().equals( lPlayCard.GetSuit() ) )
        { /*
           * A revoke has been reported in a non-discard situation.
           */
          if ( mImportAdmin.IllegalPlayHasRevoke( lPlaySide ) )
          {
            lError.Set( PbnError.BAD_REVOKE );
          }
        }
        else
        if ( ! mImportAdmin.UnknownSideIs( lPlaySide ) )
        { /*
           * Check for (in)valid revokes.
           */
          if ( mImportAdmin.mDeal.GetNrRanks( lPlaySide, lLeadCard.GetSuit() ) > 0 )
          { /*
             * Wrong suit played.
             */
            if ( ! mImportAdmin.IllegalPlayHasRevoke( lPlaySide ) )
            { /*
               * No revoke reported.
               */
              lError.Set( PbnError.REVOKE );
            }
          }
          else
          {
            if ( mImportAdmin.IllegalPlayHasRevoke( lPlaySide ) )
            { /*
               * A revoke has been reported in a discard situation.
               */
              lError.Set( PbnError.BAD_REVOKE );
            }
          }
        }

        if ( ( lPlayCard.GetSuit().equals( lTrumpSuit )  ) &&
             ( ! lWinCard.GetSuit().equals( lTrumpSuit ) ) )
        { /*
           * A trump card has been played for the first time.
           */
          lWinCard = new PbnCard( lTrumpSuit, new PbnRank( PbnRank.NONE ) );
        }

        if ( ( lWinCard.GetSuit().equals( lPlayCard.GetSuit() ) ) &&
             ( lWinCard.GetRank().LT( lPlayCard.GetRank() )     ) )
        {
          lWinCard.Set( lPlayCard );
          lWinnerSide.Set( lPlaySide );
        }
      }
      if ( NrUnknown == 0 )
      { /*
         * Only update NrWon when all sides have played.
         */
        mImportAdmin.NrWonInc( lWinnerSide );
      }
    }
    mImportAdmin.mLeader.Set( lWinnerSide );
    mImportAdmin.IllegalTrickClear();

    return lError;
  }

  public PbnError               LastTrickCheck()
  {
    PbnError                    lError = new PbnError();

    if ( mImportAdmin.mNrPlayed >= 2 )
    { /*
       * It makes only sense to compare cards, when at least
       * 2 cards have been played.
       */
      for ( int i = mImportAdmin.mNrPlayed; i < PbnSide.NUMBER; i++ )
      {
        CardCheck( "-" );
      }

      lError = TrickCheck();
    }

    return lError;
  }

  public PbnError               CardIllegalCheck(
  String                        oString )
  {
    PbnError                    lError = new PbnError();

    if ( oString.equals( "R" ) )
    {
      if ( mImportAdmin.IllegalMoveHas( PbnImportAdmin.ILLEGAL_REVOKE ) )
      {
        lError.Set( PbnError.DOUBLE_ILLEGAL );
      }
      else
      if ( mImportAdmin.IllegalMoveHas( PbnImportAdmin.ILLEGAL_LEAD ) )
      { /*
         * A (irregular) lead can not be a revoke.
         */
        lError.Set( PbnError.BAD_ILLEGAL );
      }
      else
      {
        mImportAdmin.IllegalMoveAdd( PbnImportAdmin.ILLEGAL_REVOKE );
      }
    }
    else
      if ( oString.equals( "L" ) )
    {
      if ( mImportAdmin.IllegalMoveHas( PbnImportAdmin.ILLEGAL_LEAD ) )
      {
        lError.Set( PbnError.DOUBLE_ILLEGAL );
      }
      else
      if ( mImportAdmin.IllegalMoveHas( PbnImportAdmin.ILLEGAL_REVOKE ) )
      { /*
         * A revoke can not be a (irregular) lead.
         */
        lError.Set( PbnError.BAD_ILLEGAL );
      }
      else
      if ( mImportAdmin.IllegalTrickHas( PbnImportAdmin.ILLEGAL_LEAD ) )
      {
        lError.Set( PbnError.DOUBLE_ILLEGAL );
      }
      else
      {
        mImportAdmin.IllegalMoveAdd( PbnImportAdmin.ILLEGAL_LEAD );
      }
    }
    else
    {
      lError.Set( PbnError.BAD_ILLEGAL );
    }

    return lError;
  }

  public PbnError               VerifyDeclarer()
  {
    PbnSide                     lDealer = mGameData.GetSituation().GetDealer();

    if ( ! lDealer.IsValid() )
    {
      return new PbnError();
    }

    PbnAuction                  lAuction  = mGameData.GetAuction();
    PbnContract                 lContract = mGameData.GetContract();

    return lAuction.Verify( lDealer
                          , lContract );
  }

  public PbnError               VerifyResult()
  {
    PbnResult                   lResult   = mGameData.GetResult();
    PbnContract                 lContract = mGameData.GetContract();

    return lResult.Verify( lContract
                         , mImportAdmin.NrWonGetNS()
                         , mImportAdmin.NrWonGetEW() );
  }

  public PbnError               VerifyScore()
  {
    PbnError                    lError = new PbnError();
    PbnContract                 lContract = mGameData.GetContract();
    PbnSide                     lDeclarer = lContract.GetDeclarer();
    int                         lGameScore;
    int                         lCompScore;

    lGameScore = GetScore( lDeclarer );
    if ( lGameScore != PbnScore.UNKNOWN )
    {
      lCompScore = PbnScore.Compute( mGameData.GetSituation().GetVulner()
                                   , lContract
                                   , mGameData.GetResult() );

      if ( lCompScore != PbnScore.UNKNOWN )
      {
        if ( lGameScore != lCompScore )
        {
          lError.Set( PbnError.BAD_SCORE );
        }
      }
    }

    return lError;
  }
}
