package com.fnee.pbn;/*
 * File   :     PbnInputStream.java
 * Author :     Tis Veugen
 * Date   :     1999-10-22
 * PBN    :     2.0
 *
 * History
 * -------
 */

import java.io.*;
import java.util.*;

//*************************************************************************/


public class PbnInputStream
{
  private static final int      IS_RAF = 0;
  private static final int      IS_SEQ = 1;

  private int                   mType;
  private RandomAccessFile      mRaf;
  private long                  mIsIndex = 0;
  private long                  mIsMaxIndex = 0;
  private byte []               maBytes;

  public                        PbnInputStream(
  RandomAccessFile              oRaf )
  {
    mType = IS_RAF;
    mRaf = oRaf;
  }

  public                        PbnInputStream(
  InputStream                   oSeq )
  {
    mType = IS_SEQ;
    BufferedInputStream         lBos = new BufferedInputStream( oSeq );
    DataInputStream             lDos = new DataInputStream( lBos );
    ReadSeqFile( lDos );
  }

  private void                  ReadSeqFile(
  DataInputStream               oDos )
  {
    maBytes = PbnU.Stream2Bytes( oDos );
    if ( maBytes != null )
  	{
      mIsMaxIndex = maBytes.length;
  	}
  }
 
  public void                   Seek(
  long                          Position )
  {
    switch ( mType )
    {
    case IS_RAF:
      try
      {
        mRaf.seek( Position );
      }
      catch ( Exception e )
      {
        System.err.println( "Exception PbnInputFile.SetPosition" + Position );
      }
      break;
    case IS_SEQ:
      mIsIndex = Position;
      break;
    }
  }

  public long                   Tell()
  {
    long                        Position = 0;

    switch ( mType )
    {
    case IS_RAF:
      try
      {
        Position = mRaf.getFilePointer();
      }
      catch ( Exception e )
      {
        System.err.println( "Exception PbnInputFile.Tell" );
      }
      break;
    case IS_SEQ:
      Position = mIsIndex;
      break;
    }
    return Position;
  }

  int                           GetInt(
  long                          Index )
  {
    if ( maBytes == null )
  	{
      return -1;
  	}

    int                         inByte = (int) maBytes[ (int) Index ];
    if ( inByte < 0 )
    {
      inByte += 256;
    }
    return inByte;
  }

  public int               	ReadByte()
  {
    int      			inByte = -1;

    switch ( mType )
    {
    case IS_RAF:
      try
      {
        inByte = mRaf.readUnsignedByte();
      }
      catch ( Exception e )
      {
        inByte = -1;
      }
      break;
    case IS_SEQ:
      if ( mIsIndex == mIsMaxIndex )
      {
        inByte = -1;
      }
      else
      {
        inByte = GetInt( mIsIndex++ );
      }
      break;
    }
    return inByte;
  }
}
