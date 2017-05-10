package com.fnee.pbn;/*
 * File   :     PbnUseTagIds.java
 * Author :     Tis Veugen
 * Date   :     1999-08-10
 * PBN    :     1.6
 *
 * History
 * -------
 * 1999-08-10 Store game nrs.
 */

public class PbnUseTagIds
{
  private int []                maGameNrs;
  private int                   mGameNr = -1;
  private int                   mTagId  = -1;

  public                        PbnUseTagIds()
  {
    maGameNrs = new int[ PbnTagId.NUMBER_TOTAL ];

    for ( int iTag = 0; iTag < PbnTagId.NUMBER_TOTAL; iTag++ )
    {
      maGameNrs[ iTag ] = -1;
    }
  }

  public void                   SetGameNr(
  int                           iTag,
  int                           iGameNr )
  {
    maGameNrs[ iTag ] = iGameNr;
  }

  public void                   Reset()
  {
    mGameNr = -1;
  }

  /*
   * Compute the smallest game number, bigger than mGameNr.
   */
  public int                    GetGameNr()
  {
    int                         lGameNr = -1;
    int                         iGameNr;

    for ( int iTag = 0; iTag < PbnTagId.NUMBER_TOTAL; iTag++ )
    {
      iGameNr = maGameNrs[ iTag ];
      if ( iGameNr > mGameNr )
      {
        if ( ( lGameNr < 0       )
          || ( iGameNr < lGameNr ) )
        {
          lGameNr = iGameNr;
        }
      }
    }

    mGameNr = lGameNr;
    mTagId  = 0;

    return mGameNr;
  }

  /*
   * Compute the next TagId with reference to game mGameNr.
   */
  public int                    GetTagId()
  {
    for ( int iTag = mTagId; iTag < PbnTagId.NUMBER_TOTAL; iTag++ )
    {
      if ( maGameNrs[ iTag ] == mGameNr )
      {
        mTagId = iTag + 1;
        return iTag;
      }
    }

    /*
     * No tag found.
     */
    return -1;
  }
}
