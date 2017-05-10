package com.fnee.pbn;/*
 * File   :     PbnGameTags.java
 * Author :     Tis Veugen
 * Date   :     2007-06-24
 * PBN    :     2.1
 *
 * History
 * -------
 * 1999-03-28 Added GetScoreTable().
 * 1999-03-28 Added GetTotalScoreTable().
 * 1999-04-19 Changed PbnTagID.NUMBER_20.
 * 1999-07-05 Added other tables.
 * 1999-08-22 Added GetTagUse() and SetTagUse().
 * 1999-09-29 Added GetTagString().
 * 2002-02-17 Added tables.
 * 2007-06-24 Added VERSION_21
 */

import java.lang.reflect.*;

public class PbnGameTags
{
  String []                     maTagValues;
  int []                        maUsedTags;

  private PbnMoveNote []        maCallNote;
  private PbnMoveNote []        maCardNote;

  private PbnComment            mGameComment;
  private PbnComment []         maTagComments;
  private PbnComment []         maCallNoteComments;
  private PbnComment []         maCardNoteComments;

  private PbnMoveAnno []        maCallAnno;
  private PbnMoveAnno [][]      maaCardAnno;

  private PbnTable              mActionTable;
  private PbnTable              mAuctionTimeTable;
  private PbnTable              mInstantScoreTable;
  private PbnTable              mPlayTimeTable;
  private PbnTable              mScoreTable;
  private PbnTable              mTotalScoreTable;
  private PbnTable              mOptimumPlayTable;
  private PbnTable              mOptimumResultTable;

  public boolean []             mabHiddenSide;

  public                        PbnGameTags()
  {
    maTagValues = new String[ PbnTagId.NUMBER_TOTAL ];
    maUsedTags = new int[ PbnTagId.NUMBER_TOTAL ];

    maCallNote  = new PbnMoveNote[ PbnNote.NUMBER ];
    maCardNote  = new PbnMoveNote[ PbnNote.NUMBER ];

    mabHiddenSide = new boolean[ PbnSide.NUMBER ];

    mGameComment = new PbnComment();
    maTagComments = new PbnComment[ PbnTagId.NUMBER_TOTAL ];
    maCallNoteComments = new PbnComment[ PbnNote.NUMBER ];
    maCardNoteComments = new PbnComment[ PbnNote.NUMBER ];

    maCallAnno  = new PbnMoveAnno[ 0 ];
    maaCardAnno = new PbnMoveAnno[ PbnTrick.NUMBER ][ PbnSide.NUMBER ];

    mActionTable        = new PbnTable();
    mAuctionTimeTable   = new PbnTable();
    mInstantScoreTable  = new PbnTable();
    mPlayTimeTable      = new PbnTable();
    mScoreTable         = new PbnTable();
    mTotalScoreTable    = new PbnTable();
    mOptimumPlayTable   = new PbnTable();
    mOptimumResultTable = new PbnTable();

    for ( int iNote = 0; iNote < PbnNote.NUMBER; iNote++ )
    {
      maCallNote[ iNote ] = new PbnMoveNote();
      maCardNote[ iNote ] = new PbnMoveNote();

      maCallNoteComments[ iNote ] = new PbnComment();
      maCardNoteComments[ iNote ] = new PbnComment();
    }

    for ( int iTag = 0; iTag < PbnTagId.NUMBER_TOTAL; iTag++ )
    {
      maUsedTags[ iTag ] = PbnTagUse.NONE;
      maTagComments[ iTag ] = new PbnComment();
    }

    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      mabHiddenSide[ iSide ] = false;

      for ( int iTrick = 0; iTrick < PbnTrick.NUMBER; iTrick++ )
      {
        maaCardAnno[ iTrick][ iSide ] = new PbnMoveAnno();
      }
    }
  }

  public boolean                TagIdExist(
  PbnTagId                      oTagId )
  {
    switch ( maUsedTags[ oTagId.Get() ] )
    {
    case PbnTagUse.NONE:
    case PbnTagUse.COPY:
      return false;
    default:
      break;
    }

    return true;
  }

  public String                 GetTagValue(
  PbnTagId                      oTagId )
  {
    return maTagValues[ oTagId.Get() ];
  }

  public String                 GetTagString(
  PbnTagId                      oTagId )
  {
    return PbnChar.FilterBackslash( GetTagValue( oTagId ) );
  }

  public void                   SetTagValue(
  PbnTagId                      oTagId,
  String                        oString )
  {
    SetTagValue( oTagId, PbnTagUse.USED, oString );
  }

  public void                   SetTagValue(
  PbnTagId                      oTagId,
  int                           iTagType,
  String                        oString )
  {
    int                         index = oTagId.Get();

    maTagValues[ index ] = oString;  //.clone();
    maUsedTags[ index ] = iTagType;
  }

  public int                    GetTagUse(
  PbnTagId                      oTagId )
  {
    return maUsedTags[ oTagId.Get() ];
  }

  public void                   SetTagUse(
  PbnTagId                      oTagId,
  int                           iTagType )
  {
    maUsedTags[ oTagId.Get() ] = iTagType;
  }

  public boolean                UsedTagValue(
  PbnTagId                      oTagId )
  {
    return UsedTagValue( oTagId.Get() );
  }

  public boolean                UsedTagValue(
  int                           iTag )
  {
    return (maUsedTags[ iTag ] != PbnTagUse.NONE);
  }

  public void                   CopyTags(
  PbnGameTags                   oGameTags )
  {
    for ( int iTag = 0; iTag < PbnTagId.NUMBER_TOTAL; iTag++ )
    {
      maUsedTags[ iTag ] = oGameTags.maUsedTags[ iTag ];
      if ( UsedTagValue( iTag ) )
      {
        maTagValues[ iTag ] = new String( oGameTags.maTagValues[ iTag ] );
      }
    }
  }

  public void                   IncCallAnno()
  {
    maCallAnno = (PbnMoveAnno []) PbnU.ArrayInc( maCallAnno );

    maCallAnno[ Array.getLength(maCallAnno)-1 ] = new PbnMoveAnno();
  }

  public PbnMoveAnno            GetCallAnno(
  int                           ixCall )
  {
    return maCallAnno[ ixCall ];
  }

  public PbnMoveAnno            GetCardAnno(
  int                           ixTrick,
  PbnSide                       oSide )
  {
    return maaCardAnno[ ixTrick ][ oSide.Get() ];
  }

  public PbnMoveNote            GetCallNote(
  int                           iNote )
  {
    return maCallNote[ iNote-1 ];
  }

  public PbnMoveNote            GetCardNote(
  int                           iNote )
  {
    return maCardNote[ iNote-1 ];
  }

  public PbnComment             GetGameComment()
  {
    return mGameComment;
  }

  public PbnComment             GetTagComment(
  PbnTagId                      oTagId )
  {
    return maTagComments[ oTagId.Get() ];
  }

  public PbnComment             GetCallNoteComment(
  int                           iNote )
  {
    return maCallNoteComments[ iNote-1 ];
  }

  public PbnComment             GetCardNoteComment(
  int                           iNote )
  {
    return maCardNoteComments[ iNote-1 ];
  }

  public void                   SetHiddenSide(
  PbnSide                       oSide,
  boolean                       bHidden )
  {
    mabHiddenSide[ oSide.Get() ] = bHidden;
  }

  public void                   PutComment(
  String                        oString,
  int                           iSection,
  int                           iType,
  int                           iTagIndex,
  int                           iNrCalls,
  int                           iNrTricks,
  PbnSide                       oPlaySide )
  {
    switch ( iSection )
    {
    case PbnGen.SECTION_IDENT:
      switch ( iType )
      {
      case PbnCommentAdmin.TYPE_IDENT:
        mGameComment.Put( oString );
        break;
      case PbnCommentAdmin.TYPE_TAG:
        maTagComments[ iTagIndex ].Put( oString );
        break;
      }
      break;

    case PbnGen.SECTION_AUCTION:
      switch ( iType )
      {
      case PbnCommentAdmin.TYPE_TAG:
        maTagComments[ iTagIndex ].Put( oString );
        break;
      case PbnCommentAdmin.TYPE_NOTE_TAG:
        maCallNoteComments[ iTagIndex ].Put( oString );
        break;
      default:
        maCallAnno[ iNrCalls ].PutComment( oString, iType );
        break;
      }
      break;

    case PbnGen.SECTION_PLAY:
      switch ( iType )
      {
      case PbnCommentAdmin.TYPE_TAG:
        maTagComments[ iTagIndex ].Put( oString );
        break;
      case PbnCommentAdmin.TYPE_NOTE_TAG:
        maCardNoteComments[ iTagIndex ].Put( oString );
        break;
      default:
        maaCardAnno[iNrTricks][oPlaySide.Get()].PutComment( oString, iType );
        break;
      }
      break;
    }
  }

  public PbnTable               GetInstantScoreTable()
  {
    return mInstantScoreTable;
  }

  public PbnTable               GetActionTable()
  {
    return mActionTable;
  }

  public PbnTable               GetScoreTable()
  {
    return mScoreTable;
  }

  public PbnTable               GetTotalScoreTable()
  {
    return mTotalScoreTable;
  }

  public PbnTable               GetOptimumPlayTable()
  {
    return mOptimumPlayTable;
  }

  public PbnTable               GetOptimumResultTable()
  {
    return mOptimumResultTable;
  }

  public PbnTable               GetTable(
  PbnTagId                      oTagId )
  {
    switch ( oTagId.Get() )
    {
    case PbnTagId.ACTIONTABLE:
      return mActionTable;

    case PbnTagId.AUCTIONTIMETABLE:
      return mAuctionTimeTable;

    case PbnTagId.INSTANTSCORETABLE:
      return mInstantScoreTable;

    case PbnTagId.PLAYTIMETABLE:
      return mPlayTimeTable;

    case PbnTagId.SCORETABLE:
      return mScoreTable;

    case PbnTagId.TOTALSCORETABLE:
      return mTotalScoreTable;

    case PbnTagId.OPTIMUMPLAYTABLE:
      return mOptimumPlayTable;

    case PbnTagId.OPTIMUMRESULTTABLE:
      return mOptimumResultTable;

    default:
      return null;
    }
  }

  public void                   InheritCopy(
  PbnGameTags                   oGameTags,
  PbnTagId                      oTagId )
  {
    int                         iTag = oTagId.Get();

    maUsedTags[ iTag ] = PbnTagUse.COPY;
    if ( oGameTags.UsedTagValue( iTag ) )
    {
      maTagValues[ iTag ] = new String( oGameTags.maTagValues[ iTag ] );
    }
    else
    {
      maTagValues[ iTag ] = "";
    }
  }
}
