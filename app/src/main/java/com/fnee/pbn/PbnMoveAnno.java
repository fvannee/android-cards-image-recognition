package com.fnee.pbn;/*
 * File   :     PbnMoveAnno.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

import java.lang.reflect.*;

class PbnMoveComment
{
  private PbnComment []         maComments;

/* Comments are located:
 *   after the move
 *   after the note reference
 *   after the suffix
 *   after the nags (collected)
 */

  public                        PbnMoveComment()
  {
    maComments = new PbnComment[ PbnCommentAdmin.NUMBER_MOVE ];
    
    for ( int i = 0; i < PbnCommentAdmin.NUMBER_MOVE; i++ )
    {
      maComments[ i ] = new PbnComment();
    }
  }

  public PbnComment             Get(
  int                           iType )
  {
    return maComments[ iType ];
  }

  public void                   Put(
  String                        oString,
  int                           iType )
  {
    maComments[ iType ].Put( oString );
  }
}

public class PbnMoveAnno
{
  private PbnMoveComment        mMoveComment;
  private PbnNote               mNote;
  private PbnSuffix             mSuffix;
  private PbnNag []             maNags;

  public                        PbnMoveAnno()
  {
    mMoveComment = new PbnMoveComment();
    mNote = new PbnNote();
    mSuffix = new PbnSuffix();
    maNags = new PbnNag[ 0 ];
  }

  public PbnComment             GetComment(
  int                           iType )
  {
    return mMoveComment.Get( iType );
  }

  public PbnNote                GetNote()
  {
    return mNote;
  }

  public PbnSuffix              GetSuffix()
  {
    return mSuffix;
  }

  public PbnNag []              GetNags()
  {
    return maNags;
  }

  /*
   * Check if aoNags contains another suffix-nag.
   */
  public boolean                ContainsSuffix()
  {
    int                         iLength = Array.getLength( maNags );

    for ( int i = 0; i < iLength; i++ )
    {
      if ( maNags[ i ].IsSuffix() )
      {
        return true;
      }
    }
    return false;
  }

  /*
   * Check if the nag has already been stored.
   */
  public boolean                ContainsNag(
  PbnNag                        oNag )
  {
    int                         iLength = Array.getLength( maNags );

    for ( int i = 0; i < iLength; i++ )
    {
      if ( maNags[ i ].equals( oNag ) )
      {
        return true;
      }
    }
    return false;
  }

  public void                   PutNag(
  PbnNag                        oNag )
  {
    int                         iLength = Array.getLength( maNags );

    maNags = (PbnNag []) PbnU.ArrayInc( maNags );
    maNags[ iLength ] = new PbnNag( oNag );
  }

  public void                   PutComment(
  String                        oString,
  int                           iType )
  {
    mMoveComment.Put( oString, iType );
  }
}
