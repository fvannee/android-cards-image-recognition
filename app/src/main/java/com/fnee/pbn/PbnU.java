package com.fnee.pbn;/*
 * File   :     PbnU.java
 * Author :     Tis Veugen
 * Date   :     2002-02-17
 * PBN    :     2.0
 *
 * History
 * -------
 * 1999-09-10 Added InArray()
 * 1999-09-22 Added InStringArray(), ParseString()
 * 1999-11-28 Added Stream2Bytes()
 * 2002-02-17 Added String()
 */

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

public class PbnU
{
  public static int             BitsUp(
  int                           Value,
  int                           Mask )
  {
    return ( Value | Mask );
  }

  public static boolean         BitsHas(
  int                           Value,
  int                           Mask )
  {
    return ( (Value & Mask) != 0 );
  }

  public static int             BitsNo(
  int                           Value,
  int                           Mask )
  {
    return ( Value & ~Mask );
  }

  public static Object          ArrayInc(
  Object                        aoArray )
  {
    Class                       lClass = aoArray.getClass();
    if ( ! lClass.isArray() )
    {
      // Not an array.
      return null;
    }

    int                         iOldLength = Array.getLength( aoArray );
    int                         iNewLength = iOldLength + 1;
    Class                       lComponentType = lClass.getComponentType();
    Object                      alNewArray;

    alNewArray = Array.newInstance( lComponentType, iNewLength );
    System.arraycopy( aoArray, 0, alNewArray, 0, iOldLength );

    return alNewArray;
  }

  public static Object          ArrayGrow(
  Object                        aoArray,
  int                           iPercentage )
  {
    Class                       lClass = aoArray.getClass();
    if ( ! lClass.isArray() )
    {
      // Not an array.
      return null;
    }

    int                         iOldLength = Array.getLength( aoArray );
    int                         iNewLength;
    Class                       lComponentType = lClass.getComponentType();
    Object                      alNewArray;

    iNewLength = iOldLength * ( 100 + iPercentage ) / 100;
    if ( iNewLength == iOldLength )
    {
      iNewLength += iPercentage;
    }

    alNewArray = Array.newInstance( lComponentType, iNewLength );
    System.arraycopy( aoArray, 0, alNewArray, 0, iOldLength );

    return alNewArray;
  }

  public static boolean         InArray(
  int []                        oaInts,
  int                           iValue )
  {
    for ( int i = 0; i < oaInts.length; i++ )
    {
      if ( iValue == oaInts[ i ] )
      {
        return true;
      }
    }
    return false;
  }

  public static int             InStringArray(
  String []                     oaStrings,
  String                        oString )
  {
    for ( int i = 0; i < oaStrings.length; i++ )
    {
      if ( oString.equalsIgnoreCase( oaStrings[ i ] ) )
      {
        return i;
      }
    }
    return -1;
  }

  public static Vector          ParseString(
  String                        oString,
  String                        oPattern )
  {
    Vector                      lV = new Vector();
    int                         ix;

    for ( ; ; )
    {
      ix = oString.indexOf( oPattern );
      if ( ix < 0 )
      {
        lV.addElement( oString );
        break;
      }
      lV.addElement( oString.substring( 0, ix ) );
      oString = oString.substring( ix+1 );
    }
    return lV;
  }

  public static byte []         Stream2Bytes(
  InputStream                   oInputStream )
  {
    ByteArrayOutputStream       lBaos = new ByteArrayOutputStream();
    int                         c;

    try
    {
      while ( (c=oInputStream.read()) >= 0 )
      {
        lBaos.write( c );
      }
    }
    catch ( IOException e )
    {
      return null;
    }
    return lBaos.toByteArray();
  }

  public static String          String(
  char                          c,
  int                           n )
  {
    StringBuffer                sb = new StringBuffer();
    for ( int i = 0; i < n; i++ )
    {
      sb.append( c );
    }
    return sb.toString();
  }
}
