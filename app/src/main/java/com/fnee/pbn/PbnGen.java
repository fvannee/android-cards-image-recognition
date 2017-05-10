package com.fnee.pbn;/*
 * File   :     PbnGen
 * Author :     Tis Veugen
 * Date   :     2007-06-24
 * PBN    :     2.1
 *
 * History
 * -------
 * 1999-04-19 Added version numbering
 * 1999-08-03 Parsing types
 * 1999-10-08 Added SECTION_SUPPL
 * 2007-06-24 Added VERSION_21
 */

public class PbnGen
{
  public static final int       VERSION_10 = 0;
  public static final int       VERSION_20 = 1;
  public static final int       VERSION_21 = 2;
  
  public static final int       SECTION_IDENT   = 0;
  public static final int       SECTION_AUCTION = 1;
  public static final int       SECTION_PLAY    = 2;
  public static final int       SECTION_SUPPL   = 3;

  public static final int       PARSE_FIRST = 0;
  public static final int       PARSE_NEXT  = 1;
  public static final int       PARSE_COPY  = 2;

  public static int             mVersion = VERSION_21;

  public static int             mParsing = PARSE_FIRST;
  public static boolean         mbVerify = false;
  public static int             mVerbose = 0;

  public static void            SetVerify()
  {
    mbVerify = true;
  }

  public static void            SetNoVerify()
  {
    mbVerify = false;
  }

  public static boolean         IsVerify()
  {
    return mbVerify;
  }

  public static int             GetParsing()
  {
    return mParsing;
  }

  public static void            SetParsing(
  int                           iParsing )
  {
    mParsing = iParsing;
  }

  public static int             GetVerbose()
  {
    return mVerbose;
  }

  public static void            SetVerbose(
  int                           iVerbose )
  {
    mVerbose = iVerbose;
  }

  public static int             GetVersion()
  {
    return mVersion;
  }

  public static void            SetVersion(
  int                           iVersion )
  {
    mVersion = iVersion;
  }
}
