package com.fnee.pbn;/*
 * File   :     PbnComment.java
 * Author :     Tis Veugen
 * Date   :     1999-01-03
 * PBN    :     1.0
 */

import java.lang.reflect.*;

public class PbnComment
{
  public static final char      BEGIN_END = '\u0000';
  public static final char      BEGIN     = '\u0001';
  public static final char      END       = '\u0002';
  public static final char      NEXT      = '\u0003';
  public static final char      EOL       = '\u0004';

  private String []             maStrings;

  public                        PbnComment()
  {
    maStrings = new String[ 0 ];
  }

/*
 * Possible comment cases:
 * (A)  ; ...
 *      a LINEFEED is stored at the end
 * (B)  { ... }
 *      the comment is stored with '}' + '\0'
 * (C)  { ... } { ... }
 *      the DOS export module prints the comments after eachother
 *      the Windows export module prints the comments at new lines
 * (D)  { ...
 *      ...
 *      ... }
 *      a LINEFEED is stored at the end of the first and second line
 */

  public void                   Put(
  String                        oString )
  {
    int                         iLength = Array.getLength( maStrings );

    maStrings = (String []) PbnU.ArrayInc( maStrings );
    maStrings[ iLength ] = oString;
  }

  public String []              GetStrings()
  {
    return maStrings;
  }
}
