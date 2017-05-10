package com.fnee.pbn;/*
 * File   :     PbnTableColumn.java
 * Author :     Tis Veugen
 * Date   :     2002-02-22
 * PBN    :     2.0
 *
 * History
 * -------
 * 2001-09-07 Added ORDER
 * 2002-02-22 Added table header format
 */

 //import java.util.*;

public class PbnTableColumn
{
  public static final int       ALIGN_NONE  = 0;
  public static final int       ALIGN_LEFT  = 1;
  public static final int       ALIGN_RIGHT = 2;

  public static final int       ORDER_NONE  = 0;
  public static final int       ORDER_DESC  = 1;        // '+'
  public static final int       ORDER_ASC   = 2;        // '-'

  private static final String   mSpaces =
  "                                                                       ";

  private String                mName;
  private int                   mWidth;
  private int                   mAlign;
  private int                   mOrder;
  private float                 mValue;

  public                        PbnTableColumn()
  {
    mName = null;
    mWidth = 0;                 // no minimum width defined
    mAlign = ALIGN_NONE;
    mOrder = ORDER_NONE;
  }

  public                        PbnTableColumn(
  String                        oName,
  int                           iWidth,
  int                           iAlign,
  int                           iOrder )
  {
    mName  = oName;
    mWidth = iWidth;
    mAlign = iAlign;
    mOrder = iOrder;
  }

  private int                   GetNumber(
  String                        oString )
  {
    int                         mNumber = 0;
    int                         n = oString.length();

    for ( int i = 0; i < n; i++ )
    {
      char                      cDigit = oString.charAt( i );

      if ( ! PbnChar.IsDigit( cDigit ) )
      {
        return -1;
      }
      mNumber = 10 * mNumber + ( cDigit - '0' );
    }

    return mNumber;
  }

  public boolean                SetHeader(
  String                        oString )
  {
    mWidth = 0;
    mAlign = ALIGN_NONE;
    mOrder = ORDER_NONE;

    oString.trim();
    if ( oString.length() > 0 )
    {
      switch ( oString.charAt( 0 ) )
      {
      case '+':
        mOrder = ORDER_DESC;
        mValue = Float.MAX_VALUE;
        break;
      case '-':
        mOrder = ORDER_ASC;
        mValue = Float.MIN_VALUE;
        break;
      default:
        break;
      }
    }

    int                         ix = oString.indexOf( '\\' );
    if ( ix < 0 )
    {
      mName = new String( oString );
    }
    else
    {
      mName = oString.substring( 0, ix );
      oString = oString.substring( ix+1 );
      oString = oString.trim();

      int                       length = oString.length();
      if ( length <= 0 )
      {
        return false;
      }

      switch ( oString.charAt( length-1 ) )
      {
      case 'L':
        mAlign = ALIGN_LEFT;
        length--;
        oString = oString.substring( 0, length );
        break;
      case 'R':
        mAlign = ALIGN_RIGHT;
        length--;
        oString = oString.substring( 0, length );
        break;
      default:
        break;
      }

      oString = oString.trim();
      mWidth = GetNumber( oString );
      if ( mWidth <= 0 )
      {
        return false;
      }
    }

    return true;
  }

  public boolean                Order(
  String                        oElement )
  {
    float                       iValue;

    if ( mOrder == ORDER_NONE )
    {
      return true;
    }

    if ( oElement.equals( "-" ) )
    {
      return true;
    }

    try
    {
      iValue = Float.valueOf( oElement ).floatValue();
    }
    catch ( Exception e )
    {
      return false;
    }

    if ( mOrder == ORDER_DESC )
    {
      if ( iValue > mValue )
      {
        return false;
      }
    }
    else
    {
      if ( iValue < mValue )
      {
        return false;
      }
    }

    mValue = iValue;
    return true;
  }

  public String                 toString()
  {
    StringBuffer                lSb = new StringBuffer( mName );

    if ( mWidth > 0 )
    {
      lSb.append( "\\" );
      lSb.append( mWidth );
      switch ( mAlign )
      {
      case ALIGN_LEFT:
        lSb.append( 'L' );
        break;
      case ALIGN_RIGHT:
        lSb.append( 'R' );
        break;
      default:
        break;
      }
    }

    return lSb.toString();
  }

  public String                 Format(
  String                        oString )
  {
    String                      lString = new String( mName );

    if ( mWidth <= 0 )
    {
      return oString;
    }

    int                         lSize = oString.length();
    if ( lSize >= mWidth )
    {
      return oString;
    }

    int                         lAlign = ALIGN_LEFT;

    if ( mAlign == ALIGN_RIGHT )
    {
      lAlign = ALIGN_RIGHT;
    }

    int                         nSpaces = mWidth - lSize;
    int                         maxSpaces = mSpaces.length();

    String                      lSpaces = "";

    while ( nSpaces > 0 )
    {
      if ( nSpaces <= maxSpaces )
      {
        lSpaces += mSpaces.substring( 0, nSpaces );
        break;
      }
      lSpaces += mSpaces;
      nSpaces -= maxSpaces;
    }

    if ( lAlign == ALIGN_RIGHT )
    {
      return lSpaces + oString;
    }

    return oString + lSpaces;
  }

  private String                StripOrder(
  String                        oName )
  {
    if ( ( oName != null ) && ( oName.length() > 0 ) )
    {
      switch ( oName.charAt( 0 ) )
      {
      case '+':
      case '-':
        oName = oName.substring( 1 );
        break;
      }
    }
    return oName;
  }

  public int                    FormatHeaderNr()
  {
    String                      lName = StripOrder( mName );
    int                         n;

    if ( lName == null )
    {
      n = 1;
    }
    else
    if ( mWidth <= 0 )
    {
      n = 1;
    }
    else
    {
      int                       l = lName.length();

      n = ( l + mWidth-1 ) / mWidth;
    }
    return n;
  }

  public String []              FormatHeader()
  {
    String                      lName = StripOrder( mName );
    String []                   laS;

    if ( lName == null )
    {
      laS = new String[1];
      laS[ 0 ] = "?";
    }
    else
    if ( mWidth <= 0 )
    {
      laS = new String[1];
      laS[ 0 ] = lName;
    }
    else
    {
      String                    s = lName;
      int                       l = s.length();
      int                       n = ( l + mWidth-1 ) / mWidth;
      if ( n * mWidth > l )
      {
        s += PbnU.String( ' ', n * mWidth - l );
      }
      laS = new String[n];
      for ( int i = 0; i < n; i++ )
      {
        laS[ i ] = s.substring( i*mWidth, (i+1)*mWidth );
      }
    }
    return laS;
  }

  public String []              FormatHeader(
  int                           n )
  {
    String                      lName = StripOrder( mName );
    String []                   laS = new String[n];

    if ( lName == null )
    {
      laS[ 0 ] = "?";
      for ( int i = 1; i < n; i++ )
      {
        laS[ i ] = " ";
      }
    }
    else
    if ( mWidth <= 0 )
    {
      laS[ 0 ] = lName;
      String                    s = PbnU.String( ' ', lName.length() );
      for ( int i = 1; i < n; i++ )
      {
        laS[ i ] = s;
      }
    }
    else
    {
      String                    s = lName;
      int                       l = s.length();
      if ( n * mWidth > l )
      {
        s += PbnU.String( ' ', n * mWidth - l );
      }
      for ( int i = 0; i < n; i++ )
      {
        laS[ i ] = s.substring( i*mWidth, (i+1)*mWidth );
      }
    }
    return laS;
  }
}
