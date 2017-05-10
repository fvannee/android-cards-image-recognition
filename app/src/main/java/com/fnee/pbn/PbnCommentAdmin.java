package com.fnee.pbn;/*
 * File   :     PbnCommentAdmin.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

public class PbnCommentAdmin
{
  /*
   * The first 4 enums are used as index in PbnCommentDef.
   */
  public static final int       TYPE_MOVE     = 0;
  public static final int       TYPE_NOTE_REF = 1;
  public static final int       TYPE_SUFFIX   = 2;
  public static final int       TYPE_NAG      = 3;
  public static final int       NUMBER_MOVE   = 4;
  public static final int       TYPE_IDENT    = 4;
  public static final int       TYPE_TAG      = 5;
  public static final int       TYPE_NOTE_TAG = 6;
  public static final int       NUMBER        = 7;

  private PbnImportAdmin        mImportAdmin;
  private PbnGameTags           mGameTags;
  private int                   mType;
  private int                   mTagIndex;
  private int                   mBeginIndex;
  private char                  mKind;

  public                        PbnCommentAdmin(
  PbnImportAdmin                oImportAdmin,
  PbnGameTags                   oGameTags )
  {
    mImportAdmin = oImportAdmin;
    mGameTags    = oGameTags;

    mType       = TYPE_IDENT;
    mTagIndex   = 0;
    mBeginIndex = 0;
    mKind = PbnComment.BEGIN;
  }

  private void                  Put(
  String                        oString )
  {
    mGameTags.PutComment( "" + mKind + oString
                        , mImportAdmin.mSection
                        , mType
                        , mTagIndex
                        , mImportAdmin.mNrCalls
                        , mImportAdmin.mNrTricks
                        , mImportAdmin.mPlaySide );
  }

  public void                   SetType(
  int                           iType,
  int                           iTagIndex )
  {
    mType = iType;
    mTagIndex = iTagIndex;
  }

  public void                   Begin(
  int                           iBeginIndex )
  {
    mKind = PbnComment.BEGIN;
    mBeginIndex = iBeginIndex;
  }

  public void                   End(
  String                        oString,
  int                           iEndIndex )
  {
    if ( mKind == PbnComment.BEGIN )
    {
      mKind = PbnComment.BEGIN_END;
    }
    else
    {
      mKind = PbnComment.END;
    }

    oString = oString.substring( mBeginIndex, iEndIndex );
    Put( oString );
  }

  public void                   Eol(
  String                        oString )
  {
    mKind = PbnComment.EOL;
    oString = oString.substring( mBeginIndex );
    Put( oString );
  }

  public void                   Next(
  String                        oString )
  {
    oString = oString.substring( mBeginIndex );
    Put( oString );
    mKind = PbnComment.NEXT;
    mBeginIndex = 0;
  }
}
