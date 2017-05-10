package com.fnee.pbn;/*
 * File   :     PbnImportState.java
 * Author :     Tis Veugen
 * Date   :     1999-06-06
 * PBN    :     1.4
 *
 * History
 * -------
 * 1999-06-06 Added states for section.
 */

public class PbnImportState
{
  public static final int       IDLE            =  0;
  public static final int       TAG_BEGIN       =  1;
  public static final int       TAG_NAME        =  2;
  public static final int       TAG_EQUAL       =  3;
  public static final int       TAG_VALUE       =  4;
  public static final int       TAG_END         =  5;
  public static final int       AUCTION_IDLE    =  6;
  public static final int       AUCTION_CALL    =  7;
  public static final int       AUCTION_ILL     =  8;
  public static final int       NOTE            =  9;
  public static final int       NAG             = 10;
  public static final int       SUFFIX          = 11;
  public static final int       PLAY_IDLE       = 12;
  public static final int       PLAY_CARD       = 13;
  public static final int       PLAY_ILL        = 14;
  public static final int       ESCAPE          = 15;
  public static final int       COMMENT         = 16;
  public static final int       COMMENT_EOL     = 17;
  public static final int       BACKSLASH       = 18;
  public static final int       SECTION_IDLE    = 19;
  public static final int       SECTION_STRING  = 20;
  public static final int       SECTION_TOKEN   = 21;

  private static final int      NUMBER = 8;

  static PbnCommentAdmin        mCommentAdmin;
  static int                    mIndex;
  static int []                 maStates;
  static int                    mPrevState;
  static int                    mCurrState;
  static int                    mNextState;

  public                        PbnImportState(
  PbnCommentAdmin               oCommentAdmin )
  {
    mCommentAdmin = oCommentAdmin;
    maStates = new int[ NUMBER ];
    mIndex = 0;
    mPrevState =
    mCurrState =
    maStates[ 0 ] = IDLE;
  }

  public int                    Get()
  {
    return maStates[ mIndex ];
  }

  public void                   Inc(
  int                           iNewState )
  {
    mIndex++;
    maStates[ mIndex ] = iNewState;

    switch ( iNewState )
    {
    case AUCTION_CALL:
    case PLAY_CARD:
      mCommentAdmin.SetType( PbnCommentAdmin.TYPE_MOVE, 0 );
      break;
    case NOTE:
      mCommentAdmin.SetType( PbnCommentAdmin.TYPE_NOTE_REF, 0 );
      break;
    case NAG:
      mCommentAdmin.SetType( PbnCommentAdmin.TYPE_NAG, 0 );
      break;
    case SUFFIX:
      mCommentAdmin.SetType( PbnCommentAdmin.TYPE_SUFFIX, 0 );
      break;
    }
  }

  public void                   Dec()
  {
    if ( mIndex > 0 )
    {
      mIndex--;
    }
  }

  public void                   Change(
  int                           iNewState )
  {
    maStates[ mIndex ] = iNewState;
  }

  public void                   SetCurrent()
  {
    mPrevState = mCurrState;
    mCurrState = maStates[ mIndex ];
  }

  public boolean                HasChanged()
  {
    return (mPrevState != mCurrState);
  }

  public boolean                IsCommenting()
  {
    return ( maStates[ mIndex ] == COMMENT );
  }

  public boolean                IsParsing()
  {
    switch( mCurrState )
    {
    case TAG_NAME:
    case TAG_VALUE:
    case AUCTION_CALL:
    case NOTE:
    case NAG:
    case SUFFIX:
    case PLAY_CARD:
      return true;
    default:
      return false;
    }
  }
}
