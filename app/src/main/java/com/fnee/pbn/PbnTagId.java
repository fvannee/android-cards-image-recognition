package com.fnee.pbn;/*
 * File   :     PbnTagId.java
 * Author :     Tis Veugen
 * Date   :     2007-06-24
 * PBN    :     2.1
 *
 * History
 * -------
 * 1999-03-28 Added ScoreTable.
 * 1999-03-28 Added IsSection().
 * 1999-04-19 Added new TagIds; GetNrTags(), GetNrMtsTags(), GetMtsTag()
 * 1999-05-15 Added SetMtsTag()
 * 1999-06-05 Changed to PairEW and PairNS
 * 1999-06-13 Changed BAD_TAG_NAME in NEW_TAG_NAME
 * 1999-06-26 Addged ScoreMP
 * 1999-08-09 Added HistoryRubber tags
 * 1999-08-31 Changed HistoryRubber tags,
 *            added tables 
 * 2007-06-24 Added VERSION_21
 */

import java.util.*;


public class PbnTagId
{
  public static final int       ANNOTATOR          =  0;
  public static final int       ANNOTATORNA        =  1;
  public static final int       APPLICATION        =  2;
  public static final int       BIDSYSTEMEW        =  3;
  public static final int       BIDSYSTEMNS        =  4;
  public static final int       BOARD              =  5;
  public static final int       COMPETITION        =  6;
  public static final int       CONTRACT           =  7;
  public static final int       DATE               =  8;
  public static final int       DEAL               =  9;
  public static final int       DEALER             = 10;
  public static final int       DEALID             = 11;
  public static final int       DECLARER           = 12;
  public static final int       DESCRIPTION        = 13;
  public static final int       EAST               = 14;
  public static final int       EASTNA             = 15;
  public static final int       EASTTYPE           = 16;
  public static final int       EVENT              = 17;
  public static final int       EVENTDATE          = 18;
  public static final int       EVENTSPONSOR       = 19;
  public static final int       FRENCHMP           = 20;
  public static final int       GENERATOR          = 21;
  public static final int       HIDDEN             = 22;
  public static final int       HOMETEAM           = 23;
  public static final int       MODE               = 24;
  public static final int       NORTH              = 25;
  public static final int       NORTHNA            = 26;
  public static final int       NORTHTYPE          = 27;
  public static final int       OPTIMUMSCORE       = 28;
  public static final int       PAIREW             = 29;
  public static final int       PAIRNS             = 30;
  public static final int       RESULT             = 31;
  public static final int       ROOM               = 32;
  public static final int       ROUND              = 33;
  public static final int       SCORE              = 34;
  public static final int       SCOREIMP           = 35;
  public static final int       SCOREMP            = 36;
  public static final int       SCOREPERC          = 37;
  public static final int       SCORERUBBER        = 38;
  public static final int       SCORERUBBERHISTORY = 39;
  public static final int       SCORING            = 40;
  public static final int       SECTION            = 41;
  public static final int       SITE               = 42;
  public static final int       SOUTH              = 43;
  public static final int       SOUTHNA            = 44;
  public static final int       SOUTHTYPE          = 45;
  public static final int       STAGE              = 46;
  public static final int       TABLE              = 47;
  public static final int       TERMINATION        = 48;
  public static final int       TIME               = 49;
  public static final int       TIMECALL           = 50;
  public static final int       TIMECARD           = 51;
  public static final int       TIMECONTROL        = 52;
  public static final int       UTCDATE            = 53;
  public static final int       UTCTIME            = 54;
  public static final int       VISITTEAM          = 55;
  public static final int       VULNERABLE         = 56;
  public static final int       WEST               = 57;
  public static final int       WESTNA             = 58;
  public static final int       WESTTYPE           = 59;

  public static final int       AUCTION            = 60;
  public static final int       PLAY               = 61;
  public static final int       NOTE               = 62;

  public static final int       NUMBER_TAGS        = 63;

  /*
   * Supplemental sections (PBN 2.x)
   */
  public static final int       ACTIONTABLE        = 63;
  public static final int       AUCTIONTIMETABLE   = 64;
  public static final int       INSTANTSCORETABLE  = 65;
  public static final int       OPTIMUMPLAYTABLE   = 66;
  public static final int       OPTIMUMRESULTTABLE = 67;
  public static final int       PLAYTIMETABLE      = 68;
  public static final int       SCORETABLE         = 69;
  public static final int       TOTALSCORETABLE    = 70;

  public static final int       NUMBER_TOTAL       = 71;

  public static final int       NO_TAG_ID          = 71;

  public static final int       NUMBER_MTS_10 = 14;
  public static final int       NUMBER_MTS_20 = 15;

  private static int []         maMtsTags_10 =
  {
    EVENT,
    SITE,
    DATE,
    BOARD,
    WEST,
    NORTH,
    EAST,
    SOUTH,
    DEALER,
    VULNERABLE,
    DEAL,
    DECLARER,
    CONTRACT,
    RESULT,
    AUCTION,
    PLAY,
    NOTE
  };

  private static int []         maMtsTags_20 =
  {
    EVENT,
    SITE,
    DATE,
    BOARD,
    WEST,
    NORTH,
    EAST,
    SOUTH,
    DEALER,
    VULNERABLE,
    DEAL,
    SCORING,
    DECLARER,
    CONTRACT,
    RESULT,
    AUCTION,
    PLAY,
    NOTE
  };

  private static int []         maTags_10 =
  {
    ANNOTATOR,
    ANNOTATORNA,
    BIDSYSTEMEW,
    BIDSYSTEMNS,
    BOARD,
    COMPETITION,
    CONTRACT,
    DATE,
    DEAL,
    DEALER,
    DECLARER,
    DESCRIPTION,
    EAST,
    EASTNA,
    EASTTYPE,
    EVENT,
    EVENTDATE,
    EVENTSPONSOR,
    FRENCHMP,
    GENERATOR,
    HIDDEN,
    HOMETEAM,
    MODE,
    NORTH,
    NORTHNA,
    NORTHTYPE,
    RESULT,
    ROOM,
    ROUND,
    SCORE,
    SCOREIMP,
    SCOREPERC,
    SCORERUBBER,
    SCORING,
    SECTION,
    SITE,
    SOUTH,
    SOUTHNA,
    SOUTHTYPE,
    STAGE,
    TABLE,
    TERMINATION,
    TIME,
    TIMECALL,
    TIMECARD,
    TIMECONTROL,
    UTCDATE,
    UTCTIME,
    VISITTEAM,
    VULNERABLE,
    WEST,
    WESTNA,
    WESTTYPE,
    AUCTION,
    PLAY,
    NOTE
  };

  private static int []         maTags_20 =
  {
    APPLICATION,
    DEALID,
    PAIREW,
    PAIRNS,
    SCOREMP,
    SCORERUBBERHISTORY
  };

  private static int []         maTags_21 =
  {
    OPTIMUMSCORE
  };

  private static int []         maTableTags_20 =
  {
    ACTIONTABLE,
    AUCTIONTIMETABLE,
    INSTANTSCORETABLE,
    PLAYTIMETABLE,
    SCORETABLE,
    TOTALSCORETABLE
  };

  private static int []         maTableTags_21 =
  {
    OPTIMUMPLAYTABLE,
    OPTIMUMRESULTTABLE
  };

  private String []             maTagNames =
  {
    "Annotator",
    "AnnotatorNA",
    "Application",
    "BidSystemEW",
    "BidSystemNS",
    "Board",
    "Competition",
    "Contract",
    "Date",
    "Deal",
    "Dealer",
    "DealId",
    "Declarer",
    "Description",
    "East",
    "EastNA",
    "EastType",
    "Event",
    "EventDate",
    "EventSponsor",
    "FrenchMP",
    "Generator",
    "Hidden",
    "HomeTeam",
    "Mode",
    "North",
    "NorthNA",
    "NorthType",
    "OptimumScore",
    "PairEW",
    "PairNS",
    "Result",
    "Room",
    "Round",
    "Score",
    "ScoreIMP",
    "ScoreMP",
    "ScorePercentage",
    "ScoreRubber",
    "ScoreRubberHistory",
    "Scoring",
    "Section",
    "Site",
    "South",
    "SouthNA",
    "SouthType",
    "Stage",
    "Table",
    "Termination",
    "Time",
    "TimeCall",
    "TimeCard",
    "TimeControl",
    "UTCDate",
    "UTCTime",
    "VisitTeam",
    "Vulnerable",
    "West",
    "WestNA",
    "WestType",

    "Auction",
    "Play",

    "Note",

    "ActionTable",
    "AuctionTimeTable",
    "InstantScoreTable",
    "OptimumPlayTable",
    "OptimumResultTable",
    "PlayTimeTable",
    "ScoreTable",
    "TotalScoreTable",

    "NoTag"
  };

  private static Vector         mvMtsTags10 = new Vector();
  private static Vector         mvMtsTags20 = new Vector();
  private static Vector         mvOptionalTags10 = new Vector();
  private static Vector         mvOptionalTags20 = new Vector();
  private static Vector         mvOptionalTags21 = new Vector();
  private static Vector         mvSupplementalTags10 = new Vector();
  private static Vector         mvSupplementalTags20 = new Vector();
  private static Vector         mvSupplementalTags21 = new Vector();

  private int                   mTagId;

  public                        PbnTagId()
  {
    mTagId = NO_TAG_ID;
  }

  public                        PbnTagId(
  int                           iTagId )
  {
    mTagId = iTagId;
  }

  public int                    Get()
  {
    return mTagId;
  }

  public void                   Set(
  int                           iTagId )
  {
    mTagId = iTagId;
  }

  public void                   Set(
  PbnTagId                      oTagId )
  {
    mTagId = oTagId.mTagId;
  }

  public boolean                Is(
  int                           iTagId )
  {
    return (mTagId == iTagId);
  }

  public PbnError               Search(
  String                        oString )
  {
    for ( int iTagId = 0; iTagId < NUMBER_TOTAL; iTagId++ )
    {
      if ( maTagNames[ iTagId ].equals( oString ) )
      {
        mTagId = iTagId;

        switch ( PbnGen.GetVersion() )
        {
        case PbnGen.VERSION_10:
          if ( ! IsVersion10() )
          {
            return new PbnError( PbnError.NEW_TAG_NAME );
          }
          break;

        case PbnGen.VERSION_20:
          if ( ! ( IsVersion10() || IsVersion20() ) )
          {
            return new PbnError( PbnError.NEW_TAG_NAME );
          }
          break;

        case PbnGen.VERSION_21:
        default:
          if ( ! ( IsVersion10() || IsVersion20() || IsVersion21() ) )
          {
            return new PbnError( PbnError.NEW_TAG_NAME );
          }
          break;
        }

        return new PbnError();
        /********************/
      }
    }

    return new PbnError( PbnError.NEW_TAG_NAME );
  }

  public boolean                IsVersion10()
  {
    return ( PbnU.InArray( maTags_10, mTagId ) );
  }

  public boolean                IsVersion20()
  {
    return ( PbnU.InArray( maTags_20, mTagId )
          || PbnU.InArray( maTableTags_20, mTagId ) );
  }

  public boolean                IsVersion21()
  {
    return ( PbnU.InArray( maTags_21, mTagId )
          || PbnU.InArray( maTableTags_21, mTagId ) );
  }

  public boolean                IsTable20()
  {
    return PbnU.InArray( maTableTags_20, mTagId );
  }

  public boolean                IsTable21()
  {
    return PbnU.InArray( maTableTags_21, mTagId );
  }

  public String                 toName()
  {
    return maTagNames[ mTagId ];
  }


  static
  {
    for ( int i = 0; i < NUMBER_MTS_10; i++ )
    {
      mvMtsTags10.addElement( new PbnTagId( maMtsTags_10[i] ) );
    }

    for ( int i = 0; i < NUMBER_MTS_20; i++ )
    {
      mvMtsTags20.addElement( new PbnTagId( maMtsTags_20[i] ) );
    }


    for ( int i = 0; i < NUMBER_TAGS; i++ )
    {
      if ( PbnU.InArray( maTags_10, i ) )
      {
        if ( ! PbnU.InArray( maMtsTags_10, i ) )
        {
          mvOptionalTags10.addElement( new PbnTagId( i ) );
        }
      }
    }

    for ( int i = 0; i < NUMBER_TAGS; i++ )
    {
      if ( PbnU.InArray( maTags_10, i ) || PbnU.InArray( maTags_20, i ) )
      {
        if ( ! PbnU.InArray( maMtsTags_20, i ) )
        {
          mvOptionalTags20.addElement( new PbnTagId( i ) );
        }
      }
    }

    for ( int i = 0; i < NUMBER_TAGS; i++ )
    {
      if ( PbnU.InArray( maTags_10, i ) || PbnU.InArray( maTags_20, i ) || PbnU.InArray( maTags_21, i ) )
      {
        if ( ! PbnU.InArray( maMtsTags_20, i ) )
        {
          mvOptionalTags21.addElement( new PbnTagId( i ) );
        }
      }
    }


    for ( int i = ACTIONTABLE; i < NUMBER_TOTAL; i++ )
    {
      if ( PbnU.InArray( maTableTags_20, i ) )
      {
        mvSupplementalTags20.addElement( new PbnTagId( i ) );
      }
    }

    for ( int i = ACTIONTABLE; i < NUMBER_TOTAL; i++ )
    {
      if ( PbnU.InArray( maTableTags_20, i ) || PbnU.InArray( maTableTags_21, i ) )
      {
        mvSupplementalTags21.addElement( new PbnTagId( i ) );
      }
    }
  }


  public static Enumeration     GetMtsTags()
  {
    Enumeration e = null;

    switch ( PbnGen.GetVersion() )
    {
    case PbnGen.VERSION_10:
      e = mvMtsTags10.elements();
      break;

    case PbnGen.VERSION_20:
    case PbnGen.VERSION_21:
      e = mvMtsTags20.elements();
      break;

    default:
      break;
    }

    return e;
  }

  public static Enumeration     GetOptionalTags()
  {
    Enumeration e = null;

    switch ( PbnGen.GetVersion() )
    {
    case PbnGen.VERSION_10:
      e = mvOptionalTags10.elements();
      break;

    case PbnGen.VERSION_20:
      e = mvOptionalTags20.elements();
      break;

    case PbnGen.VERSION_21:
      e = mvOptionalTags21.elements();
      break;

    default:
      break;
    }

    return e;
  }

  public static Enumeration     GetSupplementalTags()
  {
    Enumeration e = null;

    switch ( PbnGen.GetVersion() )
    {
    case PbnGen.VERSION_10:
      e = mvSupplementalTags10.elements();
      break;

    case PbnGen.VERSION_20:
      e = mvSupplementalTags20.elements();
      break;

    case PbnGen.VERSION_21:
      e = mvSupplementalTags21.elements();
      break;

    default:
      break;
    }

    return e;
  }
}
