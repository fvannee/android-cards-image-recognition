package com.fnee.pbn;/*
 * File   :     PbnNote.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

public class PbnNote
{
  public static final int       NONE   = 0;
  public static final int       NUMBER = 32;

  private int                   mNote;

  public                        PbnNote()
  {
    mNote = NONE;
  }

  public                        PbnNote(
  int                           iNote )
  {
    mNote = iNote;
  }

  public                        PbnNote(
  PbnNote                       oNote )
  {
    mNote = oNote.mNote;
  }

  public int                    Get()
  {
    return mNote;
  }

  public void                   Set(
  int                           iNote )
  {
    mNote = iNote;
  }

  public void                   Set(
  PbnNote                       oNote )
  {
    mNote = oNote.mNote;
  }

  public boolean                IsValid()
  {
    return ( (1 <= mNote) && (mNote <= NUMBER) );
  }
}
