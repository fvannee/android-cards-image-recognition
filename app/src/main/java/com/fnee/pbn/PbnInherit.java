package com.fnee.pbn;/*
 * File   :     PbnInherit.java
 * Author :     Tis Veugen
 * Date   :     1999-07-12
 * PBN    :     1.5
 *
 * History
 * -------
 */

public class PbnInherit
{
  private int                   mGame;
  private int []                maCopyTagIds;
  private int []                maPrevTagIds;
  private PbnGameTags           mSaveGameTags;
  private int []                maPrevGameNrs;

  public                        PbnInherit()
  {
    mGame = -1;
    maCopyTagIds = new int[ PbnTagId.NUMBER_TOTAL ];
    maPrevTagIds = new int[ PbnTagId.NUMBER_TOTAL ];
    mSaveGameTags = new PbnGameTags();

    ClearPrevGameNrs();

    for ( int i = 0; i < PbnTagId.NUMBER_TOTAL; i++ )
    {
      maCopyTagIds[ i ] = -1;
      maPrevTagIds[ i ] = -1;
    }
  }

  public void                   SetGame(
  int                           iGame )
  {
    mGame = iGame;
  }

  /*
   * Store tag value starting with "##".
   */
  public void                   SetHshs(
  PbnTagId                      oTagId,
  String                        oString )
  {
    int                         iTagIndex = oTagId.Get();

    maCopyTagIds[ iTagIndex ] = mGame;
    maPrevTagIds[ iTagIndex ] = -1;
    mSaveGameTags.SetTagValue( oTagId, PbnTagUse.HSHS, oString );
  }

  /*
   * Store normal tag value.
   */
  public void                   SetUsed(
  PbnTagId                      oTagId,
  String                        oString )
  {
    int                         iTagIndex = oTagId.Get();

    maCopyTagIds[ iTagIndex ] = -1;
    maPrevTagIds[ iTagIndex ] = mGame;
    mSaveGameTags.SetTagValue( oTagId, PbnTagUse.USED, oString );
  }

  /*
   * Set tag value starting with "#".
   */
  public String                 SetPrev(
  PbnTagId                      oTagId )
  {
    int                         iTagIndex = oTagId.Get();

    if ( maPrevTagIds[ iTagIndex ] >= 0 )
    {
      // Use previous tag value.
    }

    if ( maCopyTagIds[ iTagIndex ] >= 0 )
    {
      maPrevTagIds[ iTagIndex ] = maCopyTagIds[ iTagIndex ];
      maCopyTagIds[ iTagIndex ] = -1;
    }

    maPrevGameNrs[ iTagIndex ] = maPrevTagIds[ iTagIndex ];

    return mSaveGameTags.GetTagValue( oTagId );
  }

  public void                   ClearPrevGameNrs()
  {
    maPrevGameNrs = new int[ PbnTagId.NUMBER_TOTAL ];

    for ( int i = 0; i < PbnTagId.NUMBER_TOTAL; i++ )
    {
      maPrevGameNrs[ i ] = -1;
    }
  }

  private void                  GetGameNrs(
  int []                        oaGameNrs,
  PbnUseTagIds                  oUseTagIds )
  {
    int                         iGameNr;

    for ( int i = 0; i < PbnTagId.NUMBER_TOTAL; i++ )
    {
      iGameNr = oaGameNrs[ i ];
      if ( iGameNr >= 0 )
      {
        oUseTagIds.SetGameNr( i, iGameNr );
      }
    }
  }

  public void                   GetPrevGameNrs(
  PbnUseTagIds                  oUseTagIds )
  {
    GetGameNrs( maPrevGameNrs, oUseTagIds );
  }

  public void                   GetCopyGameNrs(
  PbnUseTagIds                  oUseTagIds )
  {
    GetGameNrs( maCopyTagIds, oUseTagIds );
  }

  /*
   * Copy game tags of '##' tags in case of PARSE_FIRST.
   */
  public void                   GetCopyTags(
  PbnGameTags                   oGameTags )
  {
    int                         iGameNr;

    for ( int i = 0; i < PbnTagId.NUMBER_TOTAL; i++ )
    {
      iGameNr = maCopyTagIds[ i ];
      if ( iGameNr >= 0 )
      {
        oGameTags.InheritCopy( mSaveGameTags
                             , new PbnTagId( i ) );
      }
    }
  }
}
