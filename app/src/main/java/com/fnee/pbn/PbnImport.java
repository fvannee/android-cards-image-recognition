/*
 * File   :     PbnImport.java
 * Author :     Tis Veugen
 * Date   :     2009-10-03
 * PBN    :     2.1
 *
 * History
 * -------
 * 1999-01-07 Added ReadText().
 * 1999-04-19 Using GetVerbose().
 * 1999-04-26 Cleanup.
 * 1999-06-13 Handle SEV_REMARK
 * 1999-07-13 Update Inherit.
 * 1999-08-14 Close log file.
 * 1999-09-29 Store backslash.
 * 2001-09-08 Check table order
 * 2001-09-20 Made uniform files
 * 2002-02-22 Set correct table header
 * 2007-06-24 Added VERSION_21
 * 2009-10-03 Check export rule: begin at new line
 */
package com.fnee.pbn;

import java.io.*;
import java.util.*;


//*************************************************************************/

class PbnInputLine
{
  private PbnInputFile          mInputFile;
  private String                mLineString;
  private int                   mLineIndex;
  private boolean               mbReadLine;

  public                        PbnInputLine(
  PbnInputFile                  pbnInputFile )
  {
    mInputFile = pbnInputFile;
    mbReadLine = true;
    //  mLineString initialised at reading new line
    //  mLineIndex  initialised at reading new line
  }

  public char                   GetChar()
  {
    char                        gotChar;

    if ( mbReadLine )
    {
      // A new line must be read; this happens initially.
      mbReadLine = false;

      mLineString = mInputFile.GetLine();
      mLineIndex = 0;
    }

    if ( mLineIndex == mLineString.length() )
    { /*
       * At the end of the string an end-of-line character is delivered.
       */
      gotChar = PbnChar.LINEFEED;

      mbReadLine = true;
    }
    else
    {
      gotChar = mLineString.charAt( mLineIndex++ );
    }

    return gotChar;
  }

  public char                   Backspace()
  {
    if ( mbReadLine )
    { /*
       * Put back the LINEFEED.
       */
      mbReadLine = false;
    }
    else
    { /*
       * Put back the real character.
       */
      mLineIndex--;
    }

    // A non end-of-line character.
    return '\u0001';
  }
}

//*************************************************************************/

class PbnInputBuffer
{
  public static final int       MAX_LINE = 256;

  private StringBuffer          mInputBuffer;
  private int                   mPrevInputIndex;

  private StringBuffer          mErrorBuffer;
  private int                   mErrorIndex;

  public                        PbnInputBuffer()
  {
    mInputBuffer = new StringBuffer();
    mErrorBuffer = new StringBuffer();

    mPrevInputIndex = 0;
    Reset();
    ErrorReset();
  }

  public void                   append(
  char                          c )
  {
    mInputBuffer.append( c );
  }

  public boolean                equals(
  String                        oString )
  {
    return mInputBuffer.toString().equals( oString );
  }

  public void                   FixLength()
  {
    mPrevInputIndex = mInputBuffer.length();
  }

  public void                   Backspace()
  {
    // Go back one character in the InputBuffer.
    mInputBuffer.setLength( mPrevInputIndex );
  }

  public void                   Reset()
  {
    mInputBuffer.setLength( 0 );

    mErrorIndex = 0;
  }

  public boolean                 AtBegin()
  {
    return (mPrevInputIndex == 0);
  }
  
  private int                   GetInputIndex()
  {
    return mInputBuffer.length();
  }

  public String                 toString()
  {
    return mInputBuffer.toString();
  }

//*************************************************************************/

  public void                   ErrorReset()
  {
    mErrorBuffer.setLength( 0 );
    for ( int i = 0; i < MAX_LINE; i++ )
    {
      mErrorBuffer.append( ' ' );
    }
  }

  public void                   ErrorFlush()
  {
    mErrorIndex = GetInputIndex();
  }

  public void                   ErrorFlush2()
  {
    mErrorIndex = GetInputIndex() + 1;
  }

  public void                   Indicate(
  boolean                       bRange )
  {
    int                         EndIndex = GetInputIndex();

    if ( bRange )
    {
      if ( mErrorIndex > 0 )
      {
        mErrorIndex--;
      }
      for ( ; mErrorIndex < EndIndex; mErrorIndex++ )
      {
          mErrorBuffer.setCharAt( mErrorIndex, '^' );
      }
    }
    else
    {
      mErrorBuffer.setCharAt( mErrorIndex, '^' );
    }
  }

  public String                 toErrorString()
  {
    mErrorBuffer.setLength( GetInputIndex() );
    return mErrorBuffer.toString();
  }

//*************************************************************************/

  public void                   CommentBegin(
  PbnCommentAdmin               oCommentAdmin )
  {
    oCommentAdmin.Begin( GetInputIndex()-1 );
  }

  public void                   CommentEnd(
  PbnCommentAdmin               oCommentAdmin )
  {
    oCommentAdmin.End( mInputBuffer.toString(), GetInputIndex() );
  }

  public void                   CommentEol(
  PbnCommentAdmin               oCommentAdmin )
  {
    oCommentAdmin.Eol( mInputBuffer.toString() );
  }

  public void                   CommentNext(
  PbnCommentAdmin               oCommentAdmin )
  {
    oCommentAdmin.Next( mInputBuffer.toString() );
  }
}

//*************************************************************************/

class PbnInputFile
{
  public static final int       EOL_NONE     = 0;
  public static final int       EOL_CR       = 1;
  public static final int       EOL_LF       = 2;
  public static final int       EOL_EOF      = 3;
  public static final int       EOL_OVERFLOW = 4;
  public static final int       MAX_LINE     = 256;

  private PbnInputStream	mInput;
  private int                   mEolState = EOL_NONE;
  private long                  mPosition;

  public                        PbnInputFile(
  PbnInputStream		oInputStream )
  {
    mInput = oInputStream;
  }

  public void                   SetPosition(
  long                          Position )
  {
    mInput.Seek( Position );
  }

  // This is the position where the last read started.
  public long                   GetReadPosition()
  {
     return mPosition;
  }

  // This is the position where the last read ended.
  public long                   GetLastPosition()
  {
    return mInput.Tell();
  }

  public String                 GetLine()
  {
    StringBuffer                stringBuffer = new StringBuffer();
    int                         i = 0;
    int                         oldEolState = mEolState;

    mEolState = EOL_NONE;

    // Save the current file position.
    mPosition = GetLastPosition();

    while ( mEolState == EOL_NONE )
    {
      try
      {
        int                     inByte;

        inByte = mInput.ReadByte();
        switch ( inByte )
        {
        case -1:
          mEolState = EOL_EOF;
          break;

        case PbnChar.RETURN:
          if ( oldEolState == EOL_LF )
          {
            // Ignore this CR.
            oldEolState = EOL_NONE;
          }
          else
          {
            // Line ends with CR.
            mEolState = EOL_CR;
          }
          break;

        case PbnChar.LINEFEED:
          if ( oldEolState == EOL_CR )
          {
            // Ignore this LF.
            oldEolState = EOL_NONE;
          }
          else
          {
            // Line ends with LF.
            mEolState = EOL_LF;
          }
          break;

        default:
          i++;
          if ( i == MAX_LINE-1 )
          {
            // Too many characters on this line.
            mEolState = EOL_OVERFLOW;
          }
          stringBuffer.append( (char) inByte );
          break;
        }
      }
      catch( Exception e )
      {
        mEolState = EOL_EOF;
      }
    }

    return stringBuffer.toString();
  }

  public boolean                IsEof()
  {
    return (mEolState == EOL_EOF);
  }

  public boolean                IsOverflow()
  {
    return (mEolState == EOL_OVERFLOW);
  }
}

//*************************************************************************/

public class PbnImport
{
  public final char             CHAR_DOUBLE_QUOTE = '\"';
  public final char             CHAR_BACKSLASH    = '\\';
  public final char             CHAR_LINEFEED     = '\n';
  public final char             CHAR_RETURN       = '\r';

  private PbnInherit            mInherit;
  private PbnCommentAdmin       mCommentAdmin;
  private PbnImportState        mState;
  private PbnImportAdmin        mImportAdmin;
  private PbnTagChecker         mTagChecker;

  private PbnInputFile          mInputFile;
  private boolean               mbEndOfFile;
  private DataOutputStream      mLogOs;
  private int                   mLogGameNrPrinted;
  private int                   mLogGameNr;

  public long                   mFilePosBegin;
  public long                   mFilePosOut;
  private String                mStringEol;

  private void                  InformComment(
  int                           iImportState )
  {
    mImportAdmin.mbInformComment = false;

    mState.Dec();
    mState.Inc( iImportState );
  }

  public                        PbnImport()
  {
    mInputFile = null;
    mLogOs = null;
    mInherit = null;
    mStringEol = PbnChar.StringEol;
  }

  private boolean               AddTableElement(
  PbnTable                      oTable,
  String                        oString )
  {
    if ( PbnGen.GetParsing() != PbnGen.PARSE_COPY )
    { /*
       * Make sure that table has a header.
       */
      return oTable.AddElement( oString );
    }

    return true;
  }

// Pre: oGameData and oGameTags are newly created objects
  public PbnError               Read(
  long                          liFilePosIn,
  PbnGameData                   oGameData,
  PbnGameTags                   oGameTags )
  {
    PbnInputLine        lInputLine = new PbnInputLine( mInputFile );
    PbnInputBuffer      lInputBuffer = new PbnInputBuffer();
    StringBuffer        lCheckBuffer = new StringBuffer();

    mImportAdmin  = new PbnImportAdmin();
    mCommentAdmin = new PbnCommentAdmin( mImportAdmin, oGameTags );
    mState        = new PbnImportState( mCommentAdmin );

    mTagChecker = new PbnTagChecker( oGameData
                                   , oGameTags
                                   , mInherit
                                   , mImportAdmin
                                   , mCommentAdmin );

    PbnError                    lGameError = new PbnError();
    PbnError                    lLineError = new PbnError();
    PbnError                    lCharError = new PbnError();
    PbnError                    lCheckError = new PbnError();

    PbnTagId                    lTagId = new PbnTagId();
    PbnNag                      lNag = new PbnNag();
    int                         lNextState = PbnImportState.IDLE;
    boolean                     bReady = false;
    boolean                     bTagFound = false;
    boolean                     bNameFound = false;
    int                         lLineIndex  = 1;
    int                         lNrNonSpace = 0;
    boolean                     bErrorDec;
    char                        c = '\0';
    boolean                     bSectionElement = false;
    boolean                     bCheckLastTrick = false;
    boolean                     bLastTrickChecked = false;
    PbnTable                    lTable = null;
    boolean                     bExport = false;
    boolean                     bCheckNewLine = false;
    boolean                     bCheckNewLineAuction = false;
    boolean                     bCheckNewLinePlay = false;
    boolean                     bFirstAuction = false;
    boolean                     bFirstPlay = false;
    boolean                     bBackspaceError = false;

    // Initialisation before reading
    mFilePosBegin = -1;
    mFilePosOut   = -1;
    mbEndOfFile = false;

    try
    {
      mInputFile.SetPosition( liFilePosIn );
    }
    catch ( Exception e )
    {
      return new PbnError( PbnError.FILE );
      /***********************************/
    }

    while ( ! bReady )
    {
      lCharError.SetOK();
      bErrorDec = true;

      lInputBuffer.FixLength();

      mState.SetCurrent();

      if ( mState.HasChanged() )
      {
        lInputBuffer.ErrorFlush();
      }
      else
      {
        if ( ! mState.IsParsing() )
        {
          // Update the error index to the latest char.
          lInputBuffer.ErrorFlush();
        }
      }

      c = lInputLine.GetChar();

      if ( ! PbnChar.IsPbnChar( c ) )
      {
        lCharError.Set( PbnError.BAD_CHAR );
        lInputBuffer.append( '?' );
      }
      else
      {
        switch ( c )
        {
        case PbnChar.HORTAB:
        case PbnChar.VERTAB:
        case PbnChar.SPACE:
          lInputBuffer.append( ' ' );
          break;

        case PbnChar.LINEFEED:
          if ( mInputFile.IsOverflow() )
          {
            lCharError.Set( PbnError.LINE_TOO_LONG );
          }
          if ( mInputFile.IsEof() )
          {
            mbEndOfFile = true;
            bReady = true;
          }
          break;

        default:
          lNrNonSpace++;
          lInputBuffer.append( c );
          break;
        }
      }

      if ( ( mFilePosBegin == -1 ) && ( lNrNonSpace != 0 ) )
      {
        mFilePosBegin = mInputFile.GetReadPosition();
      }

      if ( ( c == PbnChar.ESCAPE        )
        && ( lInputBuffer.equals( "%" ) ) )
      { /*
         * An escape line has been found.
         */
        mState.Inc( PbnImportState.ESCAPE );
      }
      else
      {
        switch ( mState.Get() )
        {
        case PbnImportState.IDLE:
          switch ( c )
          {
          case '[':
            bCheckNewLine = true;
            mState.Inc( PbnImportState.TAG_BEGIN );
            if ( ! bTagFound )
            { /*
               * The first tag has been found.
               */
              bTagFound = true;
            }
            break;
          case '{':
            mState.Inc( PbnImportState.COMMENT );
            lInputBuffer.CommentBegin( mCommentAdmin );
            break;
          case ';':
            mState.Inc( PbnImportState.COMMENT_EOL );
            lInputBuffer.CommentBegin( mCommentAdmin );
            break;
          default:
            if ( ! PbnChar.IsSpace( c ) )
            { /*
               * Could be a section.
               * Only, if there is a preceding tag.
               */
               if ( bTagFound )
               {
                 lCharError.Set( PbnError.BAD_CHAR );
               }
               else
               {
                 lCharError.Set( PbnError.SEVERE_CHAR );
               }
            }
            break;
          }
          break;

        case PbnImportState.TAG_BEGIN:
          if ( PbnChar.InTagName( c ) )
          {
            mState.Change( PbnImportState.TAG_NAME );
            lCheckBuffer = new StringBuffer();
            lCheckBuffer.append( c );
            bNameFound = false;
            lInputBuffer.ErrorFlush();
          }
          else
          if ( ! PbnChar.IsSpace( c ) )
          {
            lCharError.Set( PbnError.BAD_CHAR );
          }
          break;

        case PbnImportState.TAG_NAME:
          switch ( c )
          {
          case CHAR_DOUBLE_QUOTE:
            mState.Change( PbnImportState.TAG_VALUE );
            bNameFound = true;
            lInputBuffer.ErrorFlush();
            break;
          case ']':
            lCharError.Set( PbnError.NO_TAG_VALUE );
            break;
          default:
            if ( PbnChar.InTagName( c ) )
            {
              lCheckBuffer.append( c );
            }
            else
            if ( PbnChar.IsSpace( c ) )
            {
              mState.Change( PbnImportState.TAG_EQUAL );
              bNameFound = true;
            }
            else
            {
              lCharError.Set( PbnError.BAD_CHAR );
            }
            break;
          }

          if ( bNameFound )
          { /*
             * The tag name has been parsed.
             * Check its tag id.
             */
            lCharError = mTagChecker.TagNameCheck( lCheckBuffer.toString()
                                                 , lTagId );
            if ( ! lCharError.HasSeverity( PbnError.SEV_WARNING ) )
            {
              if ( lTagId.Is( PbnTagId.NOTE ) )
              {
                if ( mImportAdmin.mSection == PbnGen.SECTION_IDENT )
                {
                  lCharError.Set( PbnError.BAD_SECTION );
                }
              }
            }
            else
            {
              lTagId.Set( PbnTagId.NO_TAG_ID );
              bErrorDec = false;
            }

            /*
             * In case of \".
             */
            lCheckBuffer = new StringBuffer();
          }
          break;

        case PbnImportState.TAG_EQUAL:
          switch ( c )
          {
          case CHAR_DOUBLE_QUOTE:
            mState.Change( PbnImportState.TAG_VALUE );
            lCheckBuffer = new StringBuffer();
            lInputBuffer.ErrorFlush();
            break;
          case ']':
            lCharError.Set( PbnError.NO_TAG_VALUE );
            break;
          default:
            if ( ! PbnChar.IsSpace( c ) )
            {
              lCharError.Set( PbnError.BAD_CHAR );
            }
            break;
          }
          break;

        case PbnImportState.TAG_VALUE:
          switch ( c )
          {
          case CHAR_DOUBLE_QUOTE:
            lNextState = PbnImportState.IDLE;
            if ( ! lTagId.Is( PbnTagId.NO_TAG_ID ) )
            {
              lCharError = mTagChecker.TagValueParse( lTagId, lCheckBuffer.toString() );
              switch ( lTagId.Get() )
              {
              case PbnTagId.AUCTION:
                lNextState = PbnImportState.AUCTION_IDLE;
                break;
              case PbnTagId.PLAY:
                lNextState = PbnImportState.PLAY_IDLE;
                bCheckLastTrick = true;
                break;

              default:
                if ( lTagId.IsTable20() || lTagId.IsTable21() )
                {
                  lNextState = PbnImportState.SECTION_IDLE;
                  bSectionElement = false;
                  lTable = oGameTags.GetTable( lTagId );

                  String s = lCheckBuffer.toString();
                  if ( ( s.equals( "#" ) )
                    && ( PbnGen.GetParsing() == PbnGen.PARSE_NEXT ) )
                  { /*
                     * Copy tag from previous game.
                     * It is already stored in oGameTags!
                     */
                    s = oGameTags.GetTagValue( lTagId );
                  }

                  if ( ! lTable.SetTagHeader( s ) )
                  {
                    lCharError.Set( PbnError.BAD_TAG_VALUE );
                  }
                }
                break;
              }
            }
            mState.Change( PbnImportState.TAG_END );
            break;
          case CHAR_BACKSLASH:
            mState.Inc( PbnImportState.BACKSLASH );
            break;
          default:
            if ( c < ' ' )
            {
              lCharError.Set( PbnError.BAD_CHAR );
            }
            else
            {
              lCheckBuffer.append( c );
            }
            break;
          }
          break;

        case PbnImportState.TAG_END:
          if ( c == ']' )
          {
            switch ( lNextState )
            {
            case PbnImportState.AUCTION_IDLE:
              mImportAdmin.mSection = PbnGen.SECTION_AUCTION;
              mState.Change( lNextState );
              bFirstAuction = true;
              break;
            case PbnImportState.PLAY_IDLE:
              mImportAdmin.mSection = PbnGen.SECTION_PLAY;
              mState.Change( lNextState );
              bFirstPlay = true;
              break;
            case PbnImportState.SECTION_IDLE:
              mImportAdmin.mSection = PbnGen.SECTION_IDENT;
              mState.Change( lNextState );
              break;
            default:
              mState.Dec();
              break;
            }
          }
          else
          if ( ! PbnChar.IsSpace( c ) )
          {
            lCharError.Set( PbnError.BAD_CHAR );
          }
          break;

        case PbnImportState.AUCTION_IDLE:
          switch ( c )
          {
          case '*':
            bCheckNewLineAuction = true;
            mState.Dec();
            mImportAdmin.mbIncompleteAuction = true;
            break;
          case '[':
            bCheckNewLine = true;
            mState.Change( PbnImportState.TAG_BEGIN );
            break;
          case '{':
            mState.Inc( PbnImportState.COMMENT );
            lInputBuffer.CommentBegin( mCommentAdmin );
            break;
          case ';':
            mState.Inc( PbnImportState.COMMENT_EOL );
            lInputBuffer.CommentBegin( mCommentAdmin );
            break;
          case '=':
            mState.Inc( PbnImportState.NOTE );
            lCheckBuffer = new StringBuffer();
            lInputBuffer.ErrorFlush();
            break;
          case '$':
            mState.Inc( PbnImportState.NAG );
            lCheckBuffer = new StringBuffer();
            lInputBuffer.ErrorFlush();
            break;
          case '!':
          case '?':
            mState.Inc( PbnImportState.SUFFIX );
            lCheckBuffer = new StringBuffer();
            lCheckBuffer.append( c );
            lInputBuffer.ErrorFlush();
            break;
          case '^':
            bCheckNewLineAuction = true;
            mState.Inc( PbnImportState.AUCTION_ILL );
            lCheckBuffer = new StringBuffer();
            lInputBuffer.ErrorFlush();
            break;
          case '+':
            if ( PbnGen.GetVersion() == PbnGen.VERSION_10 )
            {
              lCharError.Set( PbnError.BAD_CHAR );
            }
            else
            {
              bCheckNewLineAuction = true;
              lCharError = mTagChecker.CallCheck( "+" );
              lInputBuffer.ErrorFlush();
            }
            bErrorDec = false;
            break;
          default:
            if ( PbnChar.InMove( c ) )
            {
              bCheckNewLineAuction = true;
              mState.Inc( PbnImportState.AUCTION_CALL );
              lCheckBuffer = new StringBuffer();
              lCheckBuffer.append( c );
              lInputBuffer.ErrorFlush();
            }
            else
            if ( ! PbnChar.IsSpace( c ) )
            {
              lCharError.Set( PbnError.BAD_CHAR );
              bErrorDec = false;
            }
            break;
          }
          break;

        case PbnImportState.AUCTION_ILL:
          /*
           * An illegal token terminates after 2 characters.
           */
          lCheckBuffer.append( c );
          lCharError = mTagChecker.IllegalCallCheck( lCheckBuffer.toString() );
          mState.Dec();
          bErrorDec = false;
          break;

        case PbnImportState.AUCTION_CALL:
          if ( PbnChar.InMove( c ) )
          {
            lCheckBuffer.append( c );
          }
          else
          {
            c = lInputLine.Backspace();
            lInputBuffer.Backspace();
            lCharError = mTagChecker.CallCheck( lCheckBuffer.toString() );
            mState.Dec();
            bErrorDec = false;
            if ( mImportAdmin.CallsToBeChecked() )
            {
              bFirstAuction = true;
            }
          }
          break;

        case PbnImportState.NOTE:
          switch ( c )
          {
          case '=':
            lCharError = mTagChecker.NoteRefCheck( lCheckBuffer.toString() );
            mState.Dec();
            bErrorDec = false;
            break;
          default:
            if ( PbnChar.IsDigit( c ) )
            {
              lCheckBuffer.append( c );
            }
            else
            {
              lCharError.Set( PbnError.BAD_NOTE );
            }
            break;
          }
          break;

        case PbnImportState.NAG:
          if ( PbnChar.IsDigit( c ) )
          {
            lCheckBuffer.append( c );
          }
          else
          {
            c = lInputLine.Backspace();
            lInputBuffer.Backspace();
            lCharError = mTagChecker.NagCheck( lCheckBuffer.toString()
                                             , lNag );
            if ( lNag.IsSuffix() )
            {
              InformComment( PbnImportState.SUFFIX );
            }
            else
            {
              InformComment( PbnImportState.NAG );
            }
            mState.Dec();
            bErrorDec = false;
          }
          break;

        case PbnImportState.SUFFIX:
          /*
           * An suffix token terminates after 2 characters.
           */
          switch ( c )
          {
          case '!':
          case '?':
            lCheckBuffer.append( c );
            break;
          default:
            c = lInputLine.Backspace();
            lInputBuffer.Backspace();
            break;
          }
          lCharError = mTagChecker.SuffixCheck( lCheckBuffer.toString() );
          InformComment( PbnImportState.SUFFIX );
          mState.Dec();
          bErrorDec = false;
          break;

        case PbnImportState.PLAY_IDLE:
          switch ( c )
          {
          case '*':
            bCheckNewLinePlay = true;
            mState.Dec();
            mImportAdmin.mbIncompletePlay = true;
            break;
          case '[':
            bCheckNewLine = true;
            lCharError = mTagChecker.LastTrickCheck();
            bLastTrickChecked = true;
            bErrorDec = false;
            mState.Change( PbnImportState.TAG_BEGIN );
            break;
          case '{':
            mState.Inc( PbnImportState.COMMENT );
            lInputBuffer.CommentBegin( mCommentAdmin );
            break;
          case ';':
            mState.Inc( PbnImportState.COMMENT_EOL );
            lInputBuffer.CommentBegin( mCommentAdmin );
            break;
          case '=':
            mState.Inc( PbnImportState.NOTE );
            lCheckBuffer = new StringBuffer();
            lInputBuffer.ErrorFlush();
            break;
          case '$':
            mState.Inc( PbnImportState.NAG );
            lCheckBuffer = new StringBuffer();
            lInputBuffer.ErrorFlush();
            break;
          case '!':
          case '?':
            mState.Inc( PbnImportState.SUFFIX );
            lCheckBuffer = new StringBuffer();
            lCheckBuffer.append( c );
            lInputBuffer.ErrorFlush();
            break;
          case '^':
            bCheckNewLinePlay = true;
            mState.Inc( PbnImportState.PLAY_ILL );
            lCheckBuffer = new StringBuffer();
            lInputBuffer.ErrorFlush();
            break;
          case '+':
            if ( PbnGen.GetVersion() == PbnGen.VERSION_10 )
            {
              lCharError.Set( PbnError.BAD_CHAR );
            }
            else
            {
              bCheckNewLinePlay = true;
              lCharError = mTagChecker.CardCheck( "+" );
              lInputBuffer.ErrorFlush();
            }
            bErrorDec = false;
            break;
          default:
            if ( PbnChar.InMove( c ) )
            {
              bCheckNewLinePlay = true;
              mState.Inc( PbnImportState.PLAY_CARD );
              lCheckBuffer = new StringBuffer();
              lCheckBuffer.append( c );
              lInputBuffer.ErrorFlush();
            }
            else
            if ( ! PbnChar.IsSpace( c ) )
            {
              lCharError.Set( PbnError.BAD_CHAR );
              bErrorDec = false;
            }
            break;
          }
          break;

        case PbnImportState.PLAY_ILL:
          /*
           * An illegal token terminates after 2 characters.
           */
          lCheckBuffer.append( c );
          lCharError = mTagChecker.CardIllegalCheck( lCheckBuffer.toString() );
          mState.Dec();
          bErrorDec = false;
          break;

        case PbnImportState.PLAY_CARD:
          if ( PbnChar.InMove( c ) )
          {
            lCheckBuffer.append( c );
          }
          else
          {
            c = lInputLine.Backspace();
            lInputBuffer.Backspace();
            lCharError = mTagChecker.CardCheck( lCheckBuffer.toString() );
            if ( lCharError.IsOK() )
            {
              lInputBuffer.ErrorFlush2();
              if ( mImportAdmin.CardsToBeChecked() )
              {
                lCharError = mTagChecker.TrickCheck();
                bFirstPlay = true;                
              }
            }
            mState.Dec();
            bErrorDec = false;
          }
          break;

        case PbnImportState.SECTION_IDLE:
          switch ( c )
          {
          case CHAR_DOUBLE_QUOTE:
            bSectionElement = true;
            mState.Inc( PbnImportState.SECTION_STRING );
            lCheckBuffer = new StringBuffer();
            lCheckBuffer.append( c );
            lInputBuffer.ErrorFlush();
            break;
          case '[':
            bCheckNewLine = true;
            if ( lTable != null )
            {
              if ( ! lTable.IsFull() )
              {
                lCharError.Set( PbnError.BAD_TABLE );
                bErrorDec = false;
              }
              lTable = null;
            }
            mState.Change( PbnImportState.TAG_BEGIN );
            break;
          case '{':
            mState.Inc( PbnImportState.COMMENT );
            lInputBuffer.CommentBegin( mCommentAdmin );
            if ( bSectionElement )
            {
              lCharError.Set( PbnError.BAD_COMMENT );
            }
            break;
          case ';':
            mState.Inc( PbnImportState.COMMENT_EOL );
            lInputBuffer.CommentBegin( mCommentAdmin );
            if ( bSectionElement )
            {
              lCharError.Set( PbnError.BAD_COMMENT );
            }
            break;
          default:
            if ( PbnChar.InSection( c ) )
            {
              bSectionElement = true;
              mState.Inc( PbnImportState.SECTION_TOKEN );
              lCheckBuffer = new StringBuffer();
              lCheckBuffer.append( c );
              lInputBuffer.ErrorFlush();
            }
            else
            if ( ! PbnChar.IsSpace( c ) )
            {
              lCharError.Set( PbnError.BAD_CHAR );
              bErrorDec = false;
            }
            break;
          }
          break;

        case PbnImportState.SECTION_TOKEN:
          if ( PbnChar.InSection( c ) )
          {
            lCheckBuffer.append( c );
          }
          else
          {
            c = lInputLine.Backspace();
            lInputBuffer.Backspace();
            if ( ! AddTableElement( lTable, lCheckBuffer.toString() ) )
            {
              lCharError.Set( PbnError.BAD_TABLE_ORDER );
            }
            mState.Dec();
            bErrorDec = false;
          }
          break;

        case PbnImportState.SECTION_STRING:
          switch ( c )
          {
          case CHAR_DOUBLE_QUOTE:
            lCheckBuffer.append( c );
            if ( ! AddTableElement( lTable, lCheckBuffer.toString() ) )
            {
              lCharError.Set( PbnError.BAD_TABLE_ORDER );
            }
            mState.Dec();
            bErrorDec = false;
            break;
          case CHAR_BACKSLASH:
            mState.Inc( PbnImportState.BACKSLASH );
            break;
          default:
            if ( c < ' ' )
            {
              lCharError.Set( PbnError.BAD_CHAR );
            }
            else
            {
              lCheckBuffer.append( c );
            }
            break;
          }
          break;

        case PbnImportState.ESCAPE:
          if ( ( c == CHAR_LINEFEED )
            || ( c == CHAR_RETURN   ) )
          {
            String s = lInputBuffer.toString();
            if ( s.startsWith( "% EXPORT" ) )
            {
              bExport = true;
            }
            mState.Dec();
          }
          break;

        case PbnImportState.COMMENT:
          if ( c == '}' )
          {
            lInputBuffer.CommentEnd( mCommentAdmin );
            mState.Dec();
          }
          break;

        case PbnImportState.COMMENT_EOL:
          if ( ( c == CHAR_LINEFEED )
            || ( c == CHAR_RETURN   ) )
          {
            lInputBuffer.CommentEol( mCommentAdmin );
            mState.Dec();
          }
          break;

        case PbnImportState.BACKSLASH:
/*
          if ( ! ( ( c == CHAR_DOUBLE_QUOTE ) ||
                   ( c == CHAR_BACKSLASH    ) ) )
*/
          {
            lCheckBuffer.append( CHAR_BACKSLASH );
          }
          mState.Dec();
          lCheckBuffer.append( c );
          break;
        }
      }

      if ( bExport )
      {
        if ( bCheckNewLine )
        {
          if ( ! lInputBuffer.AtBegin() )
          {
            lCharError.Set( PbnError.NO_NEW_LINE );
          }
          bCheckNewLine = false;
        }

        if ( bCheckNewLineAuction )
        {
          if ( bFirstAuction )
          {
            if ( ! lInputBuffer.AtBegin() )
            {
              lCharError.Set( PbnError.NO_NEW_LINE );
              bBackspaceError = true;
            }
            bFirstAuction = false;
          }
          bCheckNewLineAuction = false;
        }

        if ( bCheckNewLinePlay )
        {
          if ( bFirstPlay )
          {
            if ( ! lInputBuffer.AtBegin() )
            {
              lCharError.Set( PbnError.NO_NEW_LINE );
              bBackspaceError = true;
            }
            bFirstPlay = false;
          }
          bCheckNewLinePlay = false;
        }
      }

      if ( ! lCharError.IsOK() )
      {
        if ( lLineError.IsOK() )
        { /*
           * Copy the first occurring error in the line.
           */
          lLineError.Set( lCharError );
        }
        lGameError.SetWorst( lCharError );

        lInputBuffer.Indicate( mState.IsParsing() || bBackspaceError );
        bBackspaceError = false;

        if ( lCharError.HasSeverity( PbnError.SEV_ERROR )
          && bErrorDec )
        { /*
           * Don't affect the state for SEV_REMARK.
           */
          mState.Dec();
        }
        // Update the states because the error has been handled.
        mState.SetCurrent();
      }

      if ( c == PbnChar.LINEFEED )
      {
        if ( mState.IsCommenting() )
        {
          lInputBuffer.CommentNext( mCommentAdmin );
        }
        else
        if ( ( bTagFound ) && ( lNrNonSpace == 0 ) )
        {
          bReady = true;
          mFilePosOut = mInputFile.GetLastPosition();
        }

        if ( ! lLineError.IsOK() )
        {
          LogPrint( lInputBuffer.toString() );
          LogPrint( lInputBuffer.toErrorString() );
          lInputBuffer.ErrorReset();

          LogPrint( lLineError.toString() );
          lLineError.SetOK();
        }
        else
        if ( PbnGen.GetVerbose() > 0 )
        {
          LogPrint( lInputBuffer.toString() );
        }

        lLineIndex++;
        lNrNonSpace = 0;
        lInputBuffer.Reset();
      }
    }

    if ( lTable != null )
    {
      if ( ! lTable.IsFull() )
      {
        lCheckError.Set( PbnError.BAD_TABLE );
        LogPrint( lCheckError.toString() );
        lGameError.SetWorst( lCheckError );
      }
      lTable = null;
    }

    if ( ( ! bLastTrickChecked )
      && ( bCheckLastTrick     ) )
    {
      lCheckError = mTagChecker.LastTrickCheck();
      if ( ! lCheckError.IsOK() )
      {
        LogPrint( lCheckError.toString() );
        lGameError.SetWorst( lCheckError );
      }
    }

    lCheckError = mTagChecker.VerifyDeclarer();
    if ( ! lCheckError.IsOK() )
    {
      LogPrint( lCheckError.toString() );
      lGameError.SetWorst( lCheckError );
    }

    if ( PbnGen.IsVerify() )
    {
      lCheckError = VerifyNotes( oGameTags );
      lGameError.SetWorst( lCheckError );

      lCheckError = mTagChecker.VerifyResult();
      if ( ! lCheckError.IsOK() )
      {
        LogPrint( lCheckError.toString() );
        lGameError.SetWorst( lCheckError );
      }
      else
      {
        lCheckError = mTagChecker.VerifyScore();
        if ( ! lCheckError.IsOK() )
        {
          LogPrint( lCheckError.toString() );
          lGameError.SetWorst( lCheckError );
        }
      }
    }

    if ( mbEndOfFile )
    {
      mFilePosOut = -1;

      if ( ( lGameError.IsOK() ) && ( ! bTagFound ) )
      {
        lGameError.Set( PbnError.NO_TAG );
      }
    }

    return lGameError;
  }

  public PbnError               VerifyNotes(
  PbnGameTags                   oGameTags )
  {
    PbnMoveNote                 lMoveNote;
    PbnError                    lError = new PbnError();
    PbnError                    lVerifyError;

    for ( int iNote = 1; iNote <= PbnNote.NUMBER; iNote++ )
    {
      lMoveNote = oGameTags.GetCallNote( iNote );
      lVerifyError = lMoveNote.Verify();
      if ( ! lVerifyError.IsOK() )
      {
        lError.SetWorst( lVerifyError );
        LogPrint2( lError.toString(), iNote );
      }
    }

    for ( int iNote = 1; iNote <= PbnNote.NUMBER; iNote++ )
    {
      lMoveNote = oGameTags.GetCardNote( iNote );
      lVerifyError = lMoveNote.Verify();
      if ( ! lVerifyError.IsOK() )
      {
        lError.SetWorst( lVerifyError );
        LogPrint2( lError.toString(), iNote );
      }
    }

    return lError;
  }

  public void                   SetInherit(
  PbnInherit                    oInherit )
  {
    mInherit = oInherit;
  }

  public void                   SetInputFile(
  PbnInputStream		oInputStream )
  {
    mInputFile = new PbnInputFile( oInputStream );
  }

  public void                   SetLogFile(
  FileOutputStream              oFos )
  {
    if ( oFos != null )
    {
      BufferedOutputStream      lBos = new BufferedOutputStream( oFos );
      DataOutputStream          lDos = new DataOutputStream( lBos );

      mLogOs = lDos;
    }

    mLogGameNrPrinted = -2;
    mLogGameNr = -1;
  }

  public void                   LogSetGameNr(
  int                           iGameNr )
  {
    mLogGameNr = iGameNr;
  }

  public void                   CloseLogFile()
  {
    if ( mLogOs != null )
    {
      try
      {
        mLogOs.flush();
      }
      catch ( Exception e )
      {
        System.err.println( "Can't flush logfile" );
      }
      mLogOs = null;
    }
  }

  private void                  LogPrint(
  String                        oString )
  {
    if ( mLogOs != null )
    {
      try
      {
        if ( mLogGameNrPrinted != mLogGameNr )
        {
          mLogGameNrPrinted = mLogGameNr;
          LogPrint( "% ==================== game " + mLogGameNr + " ====================" );
        }

        mLogOs.writeBytes( oString );
        mLogOs.writeBytes( mStringEol );
      }
      catch ( Exception e )
      {
        System.err.println( "Can't write to logfile" );
      }
    }
  }

  private void                  LogPrint2(
  String                        oString,
  int                           iValue )
  {
    int                         iIndex;

    iIndex = oString.lastIndexOf( "%d" );
    if ( iIndex != -1 )
    {
      oString = oString.substring( 0, iIndex )
              + iValue
              + oString.substring( iIndex+2 );
    }

    LogPrint( oString );
  }

  public boolean                IsEof()
  {
    return mbEndOfFile;
  }

  /*
   * Added for PBN Viewer.
   */
  public String []              ReadText(
  long                          liFilePosIn,
  long                          liFilePosEnd )
  {
    try
    {
      mInputFile.SetPosition( liFilePosIn );
    }
    catch ( Exception e )
    {
      return null;
      /**********/
    }

    Vector                      lVector = new Vector();
    String                      lString;

    if ( liFilePosEnd < 0 )
    {
      liFilePosEnd = 0x7FFFFFFF;      // should be large enough
    }

    while ( mInputFile.GetLastPosition() < liFilePosEnd )
    {
      lString = mInputFile.GetLine();
      if ( mInputFile.IsEof() )
      {
        break;
      }
      lVector.addElement( lString );
    }

    /*
     * Copy the Strings into an array.
     */
    String []                   laStrings;

    laStrings = new String[ lVector.size() ];
    lVector.copyInto( laStrings );

    return laStrings;
  }
}
