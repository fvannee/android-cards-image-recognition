package com.fnee.pbn;/*
 * File   :     PbnChar.java
 * Author :     Tis Veugen
 * Date   :     1999-10-10
 * PBN    :     2.0
 *
 * History
 * -------
 * 1999-06-06 Added InSection()
 * 1999-10-10 Added FilterBackslash()
 */

public class PbnChar
{
  public static final char HORTAB       = '\u0009';
  public static final char LINEFEED     = '\n';
  public static final char EOL          = '\n';
  public static final char VERTAB       = '\u000B';
  public static final char RETURN       = '\r';     // '\r'
  public static final char SPACE        = '\u0020';
  public static final char DOUBLEQUOTE  = '\u0022';     // '"'
  public static final char ESCAPE       = '\u0025';     // '%'
  public static final char BACKSLASH    = '\\';
  public static final String StringEol  = "\r\n";

  public static boolean         IsPbnChar(
  char                          c )
  {
    return ( ((   0 <= c ) && ( c <= 126 )) ||
             (( 160 <= c ) && ( c <= 255 )) );
  }

  public static boolean         IsDigit(
  char                          cDigit )
  {
    return (( '0' <= cDigit ) && ( cDigit <= '9' ));
  }

  public static boolean         IsDateChar(
  char                          cDate )
  {
    return (( cDate == '?' ) || IsDigit( cDate ));
  }

  public static boolean         IsLetter(
  char                          c )
  {
    return ( (( 'a' <= c ) && ( c <= 'z' )) ||
             (( 'A' <= c ) && ( c <= 'Z' )) );
  }

  public static boolean         IsSpace( char c )
  {
    return ( c <= ' ' );
  }

  public static boolean         InMove(
  char                          c )
  {
    return ( IsLetter( c ) ||
             IsDigit( c )  ||
             ( c == '-' )  );
  }

  public static boolean         InSection(
  char                          c )
  {
    boolean                     bOk = false;

    if ( ( 33 <= c ) && ( c <= 126 ) )
    {
      switch ( c )
      {
      case '[':
      case ']':
      case '{':
      case '}':
      case ';':
      case '%':
      case '"':
        break;
      default:
        bOk = true;
        break;
      }
    }

    return bOk;
  }

  public static boolean         InTagName(
  char                          c )
  {
    return ( IsLetter( c ) ||
             IsDigit( c )  ||
             ( c == '_' )  );
  }

  public static String          SkipSpace(
  String                        oString )
  {
    int                         length = oString.length();
    int                         i;

    for ( i = 0; i < length; i++ )
    {
      if ( ! IsSpace( oString.charAt( i ) ) )
      {
        break;
      }
    }

    if ( i == length )
    {
      return "";
    }

    return oString.substring( i );
  }

  public static String          FilterBackslash(
  String                        oString )
  {
    if ( oString == null )
    {
      return null;
    }

    int                         length = oString.length();
    char                        c;
    boolean                     bBackslash = false;
    StringBuffer                lSb = new StringBuffer( "" );

    for ( int i = 0; i < length; i++ )
    {
      c = oString.charAt( i );
      if ( bBackslash )
      {
        switch ( c )
        {
        case BACKSLASH:
        case DOUBLEQUOTE:
          break;
        default:
          lSb.append( BACKSLASH );
          break;
        }
        lSb.append( c );
        bBackslash = false;
      }
      else
      {
        if ( c == BACKSLASH )
        {
          bBackslash = true;
        }
        else
        {
          lSb.append( c );
        }
      }
    }

    if ( bBackslash )
    {
      lSb.append( BACKSLASH );
    }

    return lSb.toString();
  }
}
