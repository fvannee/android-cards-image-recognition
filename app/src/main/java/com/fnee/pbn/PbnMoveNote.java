package com.fnee.pbn;/*
 * File   :     PbnMoveNote.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

public class PbnMoveNote
{
  private boolean               mbReference;
  private boolean               mbValueSet;
  private String                mTagValue;

  public                        PbnMoveNote()
  {
    mbReference = false;
    mbValueSet  = false;
    mTagValue = new String();
  }

  public boolean                GetReference()
  {
    return mbReference;
  }

  public void                   SetReference(
  boolean                       bReference )
  {
    mbReference = bReference;
  }

  public String                 GetTagValue()
  {
    return mTagValue;
  }

  public void                   SetTagValue(
  String                        oTagValue )
  {
    mTagValue = oTagValue;
    mbValueSet = true;
  }

  public boolean                IsUsed()
  {
    return mbValueSet;
  }

  public PbnError               Verify()
  {
    PbnError                    lError = new PbnError();

    if ( ! mbValueSet )
    {
      if ( mbReference )
      {
        lError.Set( PbnError.NO_NOTE );
      }
    }
    else
    {
      if ( ! mbReference )
      {
        lError.Set( PbnError.UNUSED_NOTE );
      }
    }

    return lError;
  }
}
