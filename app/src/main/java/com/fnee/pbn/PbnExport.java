package com.fnee.pbn;/*
 * File   :     PbnExport.java
 * Author :     Tis Veugen
 * Date   :     1999-10-02
 * PBN    :     2.0
 *
 * History
 * -------
 * 1999-03-28 Added ExportScoreTable().
 * 1999-03-28 Added ExportTotalScoreTable().
 * 1999-05-15 Changed GetMtsTag() in SetMtsTag().
 * 1999-05-17 Don't export Auction tag without calls.
 * 1999-07-05 Added export other tables.
 * 1999-08-10 Changed SetTagValue.
 * 1999-10-02 Fixed end position.
 */

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

class PbnExportAdmin
{
  public PbnDeal                mDeal;
  public PbnHand                mUnknownHand;
  public boolean []             mabUnknownSides;
  public char [][]              maacIllegals;

  public                        PbnExportAdmin(
  PbnDeal                       oDeal )
  {
    mDeal = new PbnDeal( oDeal );
    mUnknownHand = new PbnHand();

    mabUnknownSides = new boolean[ PbnSide.NUMBER ];
    maacIllegals = new char[ PbnTrick.NUMBER ][ PbnSide.NUMBER ];

    /*
     * Hands with no cards are considered as unknown hands.
     */
    PbnSide                     lSide = new PbnSide( PbnSide.SOUTH );
    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      mabUnknownSides[ iSide ] = ( mDeal.GetNrRanks( lSide ) == 0 );
      lSide.Next();

      for ( int iTrick = 0; iTrick < PbnTrick.NUMBER; iTrick++ )
      {
        maacIllegals[ iTrick ][ iSide ] = ' ';
      }
    }
  }
}

/**************************************************************************/

class PbnExportBuffer
{
  static final int              MAX_LINE_LENGTH = 80;

  private PbnExport             mExport;
  private StringBuffer          mOutputLine;

  public                        PbnExportBuffer(
  PbnExport                     oExport )
  {
    mExport = oExport;

    mOutputLine = new StringBuffer();
  }

  public void                   Flush()
  {
    if ( mOutputLine.length() > 0 )
    {
      Write();
    }
  }

  public void                   Reset()
  {
    mOutputLine.setLength( 0 );
  }

  public void                   Text(
  String                        oString )
  {
    int                         BufferLength = mOutputLine.length();
    int                         TextLength   = oString.length();
    int                         SpaceLength  = ( BufferLength == 0 ) ? 0 : 1;

    if ( TextLength == 0 )
    {
      return;
    }

    if ( ( BufferLength > 0 ) &&
         ( BufferLength + SpaceLength + TextLength >= MAX_LINE_LENGTH ) )
    {
      Write();
      SpaceLength = 0;
    }

    if ( SpaceLength != 0 )
    {
      mOutputLine.append( " " );
    }
    mOutputLine.append( oString );

    if ( oString.charAt( TextLength - 1 ) == PbnChar.EOL )
    { /*
       * The line has an 'eol' :
       * Remove the 'eol', and print it immediately.
       */
      mOutputLine.setLength( mOutputLine.length() - 1 );
      Write();
    }
  }

  public void                   Write()
  {
    mExport.ExportLine( mOutputLine.toString() );
    Reset();
  }
}

public class PbnExport
{
  private PbnExportBuffer       mExportBuffer;
  private boolean               mbNoCR;
  private DataOutputStream      mExpOs;
  private PbnGameData           mGameData;
  private PbnGameTags           mGameTags;
  private PbnExportAdmin        mExAdmin;

  public                        PbnExport()
  {
    mExportBuffer = new PbnExportBuffer( this );
    mExpOs = null;
    mbNoCR = false;
  }

  public void                   ExportLine(
  String                        oString )
  {
    if ( mExpOs != null )
    {
      try
      {
        mExpOs.writeBytes( oString );
        if ( ! mbNoCR )
        {
          mExpOs.writeByte( '\r' );
        }
        mExpOs.writeByte( '\n' );
      }
      catch ( Exception e )
      {
        System.err.println( "Can't write to export file" );
      }
    }
  }

  private PbnSide               GetLeader()
  {
    PbnSide                     lLeader;

    lLeader = new PbnSide( mGameData.GetEndPosSide() );
    if ( lLeader.Is( PbnSide.IDLE ) )
    {
      lLeader = new PbnSide( mGameData.GetContract().GetDeclarer() );
      lLeader.Next();
    }

    return lLeader;
  }

  private void                  ExportTag(
  PbnTagId                      oTagId,
  String                        oString )
  {
    StringBuffer                lStringBuffer;
    int                         Length = oString.length();

    lStringBuffer = new StringBuffer( "[" + oTagId.toName() + " \"" );

/*
    for ( int i = 0; i < Length; i++ )
    {
      char                        c = oString.charAt( i );

      switch ( c )
      {
      case PbnChar.DOUBLEQUOTE:
      case PbnChar.BACKSLASH:
        lStringBuffer.append( PbnChar.BACKSLASH );
        break;
      default:
        break;
      }
      lStringBuffer.append( c );
    }
*/
    lStringBuffer.append( oString );
    lStringBuffer.append( "\"]" );

    if ( lStringBuffer.length() >= 255 )    // PBN_TAG_VALUE_LENGTH
    { /*
       * The line is too long.
       * This has occurred by introducing the extra space after the tag name.
       */
      int                       SpacePos;

      oString = lStringBuffer.toString();
      SpacePos = oString.indexOf( ' ' );
      lStringBuffer.setLength( SpacePos );
      lStringBuffer.append( oString.substring( SpacePos + 1 ) );
    }

    ExportLine( lStringBuffer.toString() );
  }

  private String                ExportDeal()
  {
    int                         NrUnknownSides = 0;
    PbnDeal                     lDeal = mGameData.GetDeal();
    PbnSide                     lSide;
    StringBuffer                lSB;

    lSide = new PbnSide( mGameData.GetSituation().GetDealer() );
    lSB = new StringBuffer( lSide.toCharacter() + ":" );

    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      if ( mExAdmin.mabUnknownSides[ lSide.Get() ] )
      {
        lSB.append( "-" );
        NrUnknownSides++;
      }
      else
      {
        PbnSuit                 lSuit = new PbnSuit( PbnSuit.SPADES );
        PbnRanks                lRanks;
        PbnRank                 lRank = new PbnRank();

        for ( int iSuit = 0; iSuit < PbnSuit.NUMBER; iSuit++ )
        {
          lRanks = lDeal.GetRanks( lSide, lSuit );
          lRank.Set( PbnRank.ACE );
          for ( int k = PbnRank.NUMBER-1; k >= 0; k-- )
          {
            if ( lRanks.HasRank( lRank ) )
            {
              lSB.append( lRank.toCharacter() );
            }
            lRank.Previous();
          }

          if ( iSuit < PbnSuit.NUMBER-1)
          {
            lSB.append( ".");
          }
          lSuit.Previous();
        }
      }

      if ( iSide < PbnSide.NUMBER-1 )
      {
        lSB.append( " ");
      }
      lSide.Next();
    }

    if ( NrUnknownSides == PbnSide.NUMBER )
    { /*
       * No cards at all.
       */
      return "?";
    }

    return lSB.toString();
  }

  private void                  ExportComment(
  PbnComment                    oComment )
  {
    String []                   laStrings = oComment.GetStrings();
    int                         iLength = Array.getLength( laStrings );
    String                      lString;

    for ( int i = 0; i < iLength; i++ )
    {
      lString = laStrings[ i ];
      mExportBuffer.Text( lString.substring( 1 ) );

      switch ( lString.charAt( 0 ) )
      {
      case PbnComment.BEGIN:
      case PbnComment.NEXT:
      case PbnComment.EOL:
        mExportBuffer.Flush();
        break;
      }
    }
  }

  private void                  ExportCommentTag(
  PbnComment                    oComment )
  {
    ExportComment( oComment );
    mExportBuffer.Flush();
  }

  private void                  ExportCall(
  PbnCall                       oCall )
  {
    mExportBuffer.Text( oCall.toString() );
  }

  private void                  ExportCard(
  PbnCard                       oCard,
  char                          cIllegal )
  {
    String                      lString = oCard.toString();

    if ( cIllegal != ' ')
    {
      lString = "^" + cIllegal + " " + lString;
    }

    mExportBuffer.Text( lString );
  }

  private void                  ExportNoteRef(
  PbnNote                       oNote )
  {
    if ( oNote.IsValid() )
    {
      mExportBuffer.Text( "=" + oNote.Get() + "=" );
    }
  }

  private void                  ExportSuffix(
  PbnNag []                     aoNags )
  {
    int                         iLength = Array.getLength( aoNags );
    PbnNag                      lNag;

    for ( int i = 0; i < iLength; i++ )
    {
      lNag = aoNags[ i ];
      if ( lNag.IsSuffix() )
      {
        mExportBuffer.Text( "$" + lNag.Get() );
      }
    }
  }

  private void                  ExportNags(
  PbnNag []                     aoNags )
  {
    int                         iLength = Array.getLength( aoNags );
    PbnNag                      lNag;

    for ( int i = 0; i < iLength; i++ )
    {
      lNag = aoNags[ i ];
      if ( ! lNag.IsSuffix() )
      {
        mExportBuffer.Text( "$" + lNag.Get() );
      }
    }
  }

  private void                  ExportMoveAnno(
  PbnMoveAnno                   oMoveAnno )
  {
    ExportComment( oMoveAnno.GetComment( PbnCommentAdmin.TYPE_MOVE ) );

    ExportNoteRef( oMoveAnno.GetNote() );
    ExportComment( oMoveAnno.GetComment( PbnCommentAdmin.TYPE_NOTE_REF ) );

    ExportSuffix( oMoveAnno.GetNags() );
    ExportComment( oMoveAnno.GetComment( PbnCommentAdmin.TYPE_SUFFIX ) );

    ExportNags( oMoveAnno.GetNags() );
    ExportComment( oMoveAnno.GetComment( PbnCommentAdmin.TYPE_NAG ) );
  }

  private void                  ExportNote(
  int                           iNote,
  PbnMoveNote                   lMoveNote,
  PbnComment                    lComment )
  {
    ExportTag( new PbnTagId( PbnTagId.NOTE )
             , "" + iNote + ":" + lMoveNote.GetTagValue() );
    ExportCommentTag( lComment );
  }

  private void                  ExportCallNotes()
  {
    PbnMoveNote                 lMoveNote;
    PbnComment                  lComment;

    for ( int iNote = 1; iNote <= PbnNote.NUMBER; iNote++ )
    {
      lMoveNote = mGameTags.GetCallNote( iNote );
      if ( lMoveNote.IsUsed() )
      {
        lComment = mGameTags.GetCallNoteComment( iNote );
        ExportNote( iNote, lMoveNote, lComment );
      }
    }
  }

  private void                  ExportCardNotes()
  {
    PbnMoveNote                 lMoveNote;
    PbnComment                  lComment;

    for ( int iNote = 1; iNote <= PbnNote.NUMBER; iNote++ )
    {
      lMoveNote = mGameTags.GetCardNote( iNote );
      if ( lMoveNote.IsUsed() )
      {
        lComment = mGameTags.GetCardNoteComment( iNote );
        ExportNote( iNote, lMoveNote, lComment );
      }
    }
  }

  private void                  ExportAuction()
  {
    PbnAuction                  lAuction = mGameData.GetAuction();
    PbnTagId                    lTagId = new PbnTagId( PbnTagId.AUCTION );

    if ( ! mGameTags.UsedTagValue( lTagId ) )
    {
      return;
      /*****/
    }

    int                         iNrCalls = lAuction.GetNrCalls();
    if ( iNrCalls == 0 )
    {
      return;
      /*****/
    }

    PbnSide                     lDealer;
    lDealer = mGameData.GetSituation().GetDealer();

    ExportTag( lTagId, lDealer.toCharacter() );
    ExportCommentTag( mGameTags.GetTagComment( lTagId ) );

    int                         iNrSides = PbnSide.NUMBER;
    /*
     * First bid starts with 'Dealer'.
     * So, no initial '-'.
     */

    for ( int iCall = 0; iCall < iNrCalls; iCall++ )
    {
      ExportCall( lAuction.GetCall( iCall ) );
      ExportMoveAnno( mGameTags.GetCallAnno( iCall ) );

      if ( --iNrSides == 0 )
      {
        iNrSides = PbnSide.NUMBER;
        mExportBuffer.Flush();
      }
    }

    /*
     * If the auction ends prematurely, then add '*'.
     */
    if ( lAuction.GetNrToPass() > 0 )
    {
      mExportBuffer.Text( "*" );
    }
    mExportBuffer.Flush();
    ExportCallNotes();
  }

  private void		            PrepareExportCard(
  PbnSide   	            	oSide,
  PbnCard     		            oCard )
  {
    if ( ! mExAdmin.mDeal.PlayCard( oSide, oCard ) )
    {
      if ( ! mExAdmin.mUnknownHand.PlayCard( oCard ) )
      { /*
         * Internal error: a card is played but it's not in the hand anymore.
         */
        System.err.println( "Export: Card not in hand"
                          + oSide.toString()
                          + oCard.GetSuit().toString()
                          + oCard.GetRank().toString() );
      }
    }
  }

  private int 		            PrepareExportPlay()
  {
    PbnDeal                     lExportDeal  = mExAdmin.mDeal;
    PbnHand                     lUnknownHand = mExAdmin.mUnknownHand;
    PbnSuit                     lSuit = new PbnSuit( PbnSuit.CLUBS );
    PbnSide                     lSide = new PbnSide();
    PbnRanks                    lRanks;
    PbnPlay                     lPlay = mGameData.GetPlay();

    int                         iTrick = 0;
    PbnSide                     lPlaySide;
    int                         NrPlayed;
    PbnCard                     lLeadCard;
    PbnCard                     lPlayCard;
    PbnCard                     lWinCard = new PbnCard();
    PbnSide                     lWinnerSide = new PbnSide();
    PbnSuit                     lTrumpSuit;

    lUnknownHand.SetRanksAll();
    for ( int iSuit = 0; iSuit < PbnSuit.NUMBER; iSuit++ )
    {
      lSide.Set( PbnSide.SOUTH );
      for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
      {
        lRanks = mExAdmin.mDeal.GetRanks( lSide, lSuit );
        lUnknownHand.RemoveRanks( lSuit, lRanks );
        lSide.Next();
      }
      lSuit.Next();
    }

    lPlaySide = GetLeader();

    for ( iTrick = 0; iTrick < PbnTrick.NUMBER; iTrick++ )
    { /*
       * Check if any cards are played.
       */
      NrPlayed = 0;
      lSide.Set( PbnSide.SOUTH );
      for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
      {
        if ( lPlay.GetCard( iTrick, lSide ).IsUsed() )
        {
          NrPlayed++;
        }
        lSide.Next();
      }

      if ( NrPlayed == 0 )
      {
        break;
      }

      lSide = lPlay.GetFirst( iTrick );
      if ( lSide.IsValid() )
      {
        lPlaySide.Set( lSide );
        mExAdmin.maacIllegals[ iTrick ][ lPlaySide.Get() ] = 'L';
      }

      lLeadCard = lPlay.GetCard( iTrick, lPlaySide );

      if ( ( lLeadCard.GetSuit().Is( PbnSuit.UNKNOWN ) )
        || ( lLeadCard.GetRank().IsNone()              ) )
      { /*
         * When the leader doesn't play, then a revoke can't be tested.
         */
        continue;
        /*******/
      }

      PrepareExportCard( lPlaySide, lLeadCard );

      /*
       * Determine the winner side.
       */
      lWinnerSide.Set( lPlaySide );
      lWinCard.Set( lLeadCard );
      lTrumpSuit = new PbnSuit( mGameData.GetContract().GetTrump() );

      for ( int iSide = 1; iSide < PbnSide.NUMBER; iSide++ )
      {
        lPlaySide.Next();
        lPlayCard = lPlay.GetCard( iTrick, lPlaySide );

        if ( ( lPlayCard.GetSuit().Is( PbnSuit.UNKNOWN ) )
          || ( lPlayCard.GetRank().IsNone()              ) )
        {
          continue;
          /*******/
        }

        if ( ! lLeadCard.GetSuit().equals( lPlayCard.GetSuit() ) )
        {
          if ( ( ! mExAdmin.mabUnknownSides[ lPlaySide.Get() ] )
            && ( lExportDeal.GetNrRanks( lPlaySide, lLeadCard.GetSuit() ) > 0 ) )
          { /*
             * Wrong suit played.
             */
            mExAdmin.maacIllegals[ iTrick ][ lPlaySide.Get() ] = 'R';
          }
        }

        if ( ( lPlayCard.GetSuit().equals( lTrumpSuit )  )
          && ( ! lWinCard.GetSuit().equals( lTrumpSuit ) ) )
        { /*
           * A trump card has been played for the first time.
           */
          lWinCard.GetSuit().Set( lTrumpSuit );
          lWinCard.GetRank().Set( PbnRank.NONE );
        }

        if ( ( lWinCard.GetSuit().equals( lPlayCard.GetSuit() ) )
          && ( lWinCard.GetRank().LT( lPlayCard.GetRank() )     ) )
        {
          lWinCard.GetRank().Set( lPlayCard.GetRank() );
          lWinnerSide.Set( lPlaySide );
        }

        PrepareExportCard( lPlaySide, lPlayCard );
      }

      lPlaySide.Set( lWinnerSide );
    }

    return iTrick;
  }

  private void		            ExportPlay()
  {
    PbnPlay                     lPlay = mGameData.GetPlay();
    PbnTagId                    lTagId = new PbnTagId( PbnTagId.PLAY );
    int                         iNrTricks;
    PbnSide                     lPlayFirst;
    PbnSide                     lPlaySide;
    int                         iNrLastSides;
    int                         iNrSides;
    PbnCard                     lPlayCard;

    iNrTricks = PrepareExportPlay();

    if ( iNrTricks == 0 )
    {
      return;
      /*****/
    }

    lPlayFirst = GetLeader();

    ExportTag( lTagId, lPlayFirst.toCharacter() );
    ExportCommentTag( mGameTags.GetTagComment( lTagId ) );

    iNrLastSides = 0;
    lPlaySide = new PbnSide( lPlayFirst );
    for ( int iSide = 0; iSide < PbnSide.NUMBER; iSide++ )
    {
      lPlayCard = lPlay.GetCard( iNrTricks-1, lPlaySide );
      if ( ! lPlayCard.GetRank().IsUnknown() )
      {
        iNrLastSides = iSide+1;
      }
      lPlaySide.Next();
    }

    for ( int iTrick = 0; iTrick < iNrTricks; iTrick++ )
    {
      lPlaySide.Set( lPlayFirst );
      if ( iTrick == iNrTricks-1 )
      {
        iNrSides = iNrLastSides;
      }
      else
      {
        iNrSides = PbnSide.NUMBER;
      }

      for ( int iSide = 0; iSide < iNrSides; iSide++ )
      {
        lPlayCard = lPlay.GetCard( iTrick, lPlaySide );
        ExportCard( lPlayCard
                  , mExAdmin.maacIllegals[ iTrick ][ lPlaySide.Get() ] );
        ExportMoveAnno( mGameTags.GetCardAnno( iTrick, lPlaySide ) );

        lPlaySide.Next();
      }

      mExportBuffer.Flush();
    }

    /*
     * If the played cards end prematurely, then add '*'.
     */
    if ( ( iNrTricks < PbnTrick.NUMBER   )
      || ( iNrLastSides < PbnSide.NUMBER ) )
    {
      mExportBuffer.Text( "*" );
    }

    mExportBuffer.Flush();

    ExportCardNotes();
  }

  private void		            ExportGameTag(
  boolean                       bMandatory,
  PbnTagId                      oTagId )
  {
    String                      lTagValue = null;
    PbnContract                 lContract  = mGameData.GetContract();
    PbnSituation                lSituation = mGameData.GetSituation();
    boolean                     bEmpty = false;
    boolean                     bCopy  = false;

    switch ( oTagId.Get() )
    {
    case PbnTagId.AUCTION:
      // Dealer must be present
      lTagValue = lSituation.GetDealer().toCharacter();
      break;

    case PbnTagId.CONTRACT:
      if ( lContract.GetTrump().Is( PbnTrump.NONE ) )
      {
        lTagValue = "Pass";
      }
      else
      if ( lContract.GetTrump().IsValid() )
      {
        if ( mGameData.GetEndPosSide().Is( PbnSide.IDLE ) )
        {
          lTagValue = "" + lContract.GetNrTricks()
                         + lContract.GetTrump().toString()
                         + lContract.GetRisk().toString();
        }
        else
        {
          lTagValue = "" + lContract.GetTrump().toString();
        }
      }
      break;

    case PbnTagId.DEAL:
      if ( mGameData.GetSituation().GetDealer().IsValid() )
      {
        lTagValue = ExportDeal();
      }
      else
      {
        lTagValue = mGameTags.GetTagValue( oTagId );
      }
      break;

    case PbnTagId.DEALER:
      if ( ! lSituation.GetDealer().Is( PbnSide.IDLE ) )
      {
        lTagValue = lSituation.GetDealer().toCharacter();
      }
      break;

    case PbnTagId.DECLARER:
      if ( lContract.GetTrump().Is( PbnTrump.NONE ) )
      {
        bEmpty = true;
      }
      else
      if ( ! lContract.GetDeclarer().Is( PbnSide.IDLE ) )
      {
        lTagValue = (( lContract.GetIrregularDeclarer() ) ? "^" : "" )
                  + lContract.GetDeclarer().toCharacter();
      }
      break;

    case PbnTagId.RESULT:
      if ( lContract.GetTrump().Is( PbnTrump.NONE ) )
      {
        lTagValue = "";
      }
      else
      if ( mGameData.GetResult().IsIdle() )
      {
        lTagValue = mGameTags.GetTagValue( oTagId );
      }
      else
      if ( ! lContract.GetDeclarer().Is( PbnSide.IDLE ) )
      {
        lTagValue = mGameData.GetResult().toString();
      }
      break;

    case PbnTagId.VULNERABLE:
      if ( ! lSituation.GetVulner().IsIdle() )
      {
        lTagValue = lSituation.GetVulner().toString();
      }
      break;

    default:
      switch ( mGameTags.GetTagUse( oTagId ) )
      {
      case PbnTagUse.USED:
      case PbnTagUse.PREV:
      case PbnTagUse.HSHS:
        lTagValue = mGameTags.GetTagValue( oTagId );
        break;
      case PbnTagUse.COPY:
        bCopy = true;
        break;
      default:
      case PbnTagUse.NONE:
        break;
      }
      break;
    }

    if ( ( lTagValue == null ) && ( bMandatory ) && ( ! bCopy ) )
    {
      lTagValue = "?";
    }

    if ( bEmpty )
    {
      ExportTag( oTagId, "" );
      ExportCommentTag( mGameTags.GetTagComment( oTagId ) );
    }
    else
    if ( lTagValue != null)
    {
      ExportTag( oTagId, lTagValue );
      ExportCommentTag( mGameTags.GetTagComment( oTagId ) );
    }
  }

  public void			        NoCR(
  boolean                       bNoCR )
  {
    mbNoCR = bNoCR;
  }

  private void                  ExportTable(
  PbnTable                      oTable,
  PbnTagId                      oTagId )
  {
    if ( oTable == null )
    {
      return;
    }

    PbnTableColumn []           lHeader = oTable.GetHeader();

    if ( lHeader == null )
    {
      return;
    }

    int                         n = lHeader.length;
    String                      lString = "";
    for ( int i = 0; i < n; i++ )
    {
      lString = lString + lHeader[ i ].toString();
      if ( i < n-1 )
      {
        lString = lString + ";";
      }
    }

    switch ( mGameTags.GetTagUse( oTagId ) )
    {
    case PbnTagUse.HSHS:        // remove useless ##
      mGameTags.SetTagValue( oTagId
                           , PbnTagUse.USED
                           , lString );
      break;
    case PbnTagUse.USED:
    case PbnTagUse.PREV:
    case PbnTagUse.COPY:        // does not happen
    case PbnTagUse.NONE:
    default:
      break;
    }

    ExportGameTag( false, oTagId );

    Enumeration         eRows = oTable.GetRows();
    String []           lRow;
    while ( eRows.hasMoreElements() )
    {
      lRow = (String []) eRows.nextElement();
      lString = "";
      for ( int i = 0; i < n; i++ )
      {
        lString = lString + lHeader[ i ].Format( lRow[ i ] );
        if ( i < n-1 )
        {
          lString = lString + " ";
        }
      }

      ExportLine( lString );
    }
  }

  public void                   SetExportFile(
  FileOutputStream              oFos )
  {
    BufferedOutputStream        lBos = new BufferedOutputStream( oFos );
    DataOutputStream            lDos = new DataOutputStream( lBos );

    mExpOs = lDos;
  }

  public void                   Flush()
  {
    try
    {
      mExpOs.flush();
    }
    catch ( Exception e )
    {
      System.err.println( "Can't flush exportfile" );
    }
  }

  public void                   Write(
  PbnGameData                   oGameData,
  PbnGameTags                   oGameTags )
  {
    mGameData = oGameData;
    mGameTags = oGameTags;
    mExAdmin = new PbnExportAdmin( mGameData.GetDeal() );

    PbnTagId                    lTagId = new PbnTagId();
    Enumeration                 leTags;

    ExportCommentTag( oGameTags.GetGameComment() );

    leTags = PbnTagId.GetMtsTags();
    while ( leTags.hasMoreElements()  )
    {
      lTagId = (PbnTagId) leTags.nextElement();
      ExportGameTag( true, lTagId );
    }

    leTags = PbnTagId.GetOptionalTags();
    while ( leTags.hasMoreElements()  )
    {
      lTagId = (PbnTagId) leTags.nextElement();
      ExportGameTag( false, lTagId );
    }

    ExportAuction();
    ExportPlay();

    leTags = PbnTagId.GetSupplementalTags();
    while ( leTags.hasMoreElements()  )
    {
      lTagId = (PbnTagId) leTags.nextElement();
      ExportTable( mGameTags.GetTable( lTagId ), lTagId );
    }

    // Write an extra empty line to end the game.
    ExportLine( "" );
  }
}
