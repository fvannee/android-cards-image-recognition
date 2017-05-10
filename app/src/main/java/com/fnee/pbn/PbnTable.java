package com.fnee.pbn;/*
 * File   :     PbnTable.java
 * Author :     Tis Veugen
 * Date   :     2002-02-22
 * PBN    :     2.0
 *
 * History
 * -------
 * 1999-07-05 Added AddElement().
 * 2002-02-22 Added table header format
 */

import java.util.*;

public class PbnTable
{
  private Vector                mVector;
  private int                   mNrColumns;
  private PbnTableColumn []     maColumns;

  private String []             maRow;
  private int                   mColumnIndex;

  public                        PbnTable()
  {
    mVector = new Vector();
    maColumns = null;
    mNrColumns = 0;

    maRow = null;
    mColumnIndex = 0;
  }

  public void                   AddEntry(
  String []                     oaElements )
  {
    mVector.addElement( oaElements );
  }

  public PbnTableColumn []      GetHeader()
  {
    return maColumns;
  }

  public boolean                SetTagHeader(
  String                        oTagValue )
  {
    Vector                      lVector = PbnU.ParseString( oTagValue, ";" );
    String []                   laColumns = new String[ lVector.size() ];

    lVector.copyInto( laColumns );
    return SetHeader( laColumns );
  }


  public boolean                SetHeader(
  String []                     oaColumns )
  {
    boolean                     bOk = true;

    mNrColumns = oaColumns.length;
    maColumns = new PbnTableColumn[ mNrColumns ];
    for ( int i = 0; i < mNrColumns; i++ )
    {
      maColumns[ i ] = new PbnTableColumn();
      bOk &= maColumns[ i ].SetHeader( oaColumns[ i ] );
    }
    
    NewRow();

    return bOk;
  }

  public Enumeration            GetRows()
  {
    return mVector.elements();
  }

  private void                  NewRow()
  {
    maRow = new String[ mNrColumns ];
    mColumnIndex = 0;
  }

  public boolean                AddElement(
  String                        oElement )
  {
    boolean                     bOk = maColumns[ mColumnIndex ].Order( oElement );

    maRow[ mColumnIndex ] = oElement;
    if ( ++mColumnIndex >= mNrColumns )
    {
      AddEntry( maRow );
      NewRow();
    }

    return bOk;
  }

  public boolean                IsFull()
  {
    return mColumnIndex == 0;
  }

  public String []              toStrings(
  PbnTagId                      oTagId )
  {
    if ( maColumns == null )
    {
      return null;
    }

    int                         hHeader = 0;
    int                         h;
    for ( int i = 0; i < mNrColumns; i++ )
    {
      h = maColumns[ i ].FormatHeaderNr();
      if ( h > hHeader )
      {
        hHeader = h;
      }
    }

    StringA []                  lasa = new StringA[ mNrColumns ];
    for ( int i = 0; i < mNrColumns; i++ )
    {
      lasa[ i ] = new StringA( maColumns[ i ].FormatHeader( hHeader ) );
    }

    StringBuffer                sb;
    boolean                     bFirst;
    int                         nRows = mVector.size();
    String []                   laS = new String[ 1 + 1 + hHeader + 1 + nRows + 1 ];
    laS[ 0 ] = oTagId.toName();
    laS[ 1 ] = PbnU.String( '=', laS[0].length() );

    for ( h = 0; h < hHeader; h++ )
    {
      bFirst = false;
      sb = new StringBuffer();
      for ( int i = 0; i < mNrColumns; i++ )
      {
        if ( bFirst )
        {
          sb.append( " " );
        }
        bFirst = true;
        sb.append( lasa[ i ].maSa[ h ] );
      }
      laS[ 2 + h ] = sb.toString();
    }

    bFirst = false;
    sb = new StringBuffer();
    for ( int i = 0; i < mNrColumns; i++ )
    {
      if ( bFirst )
      {
        sb.append( " " );
      }
      bFirst = true;
      sb.append( PbnU.String( '-', lasa[ i ].maSa[ 0 ].length() ) );
    }
    laS[ 2 + hHeader ] = sb .toString();

    Enumeration                 eRows = GetRows();
    String []                   lRow;
    int                         j = 2 + hHeader + 1;
    while ( eRows.hasMoreElements() )
    {
      bFirst = false;
      sb = new StringBuffer();

      lRow = (String []) eRows.nextElement();
      for ( int i = 0; i < mNrColumns; i++ )
      {
        if ( bFirst )
        {
          sb.append( " " );
        }
        bFirst = true;
        sb.append( maColumns[ i ].Format( lRow[ i ] ) );
      }
      laS[ j++ ] = sb.toString();
    }
    laS[ j ] = "";
    return laS;
  }
}

class StringA
{
  public String []              maSa = null;

  public                        StringA(
  String []                     oaSa )
  {
    maSa = oaSa;
  }
}
