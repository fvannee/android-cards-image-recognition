package com.fnee.pbn;/*
 * File   :     PbnError.java
 * Author :     Tis Veugen
 * Date   :     2009-10-03
 * PBN    :     2.1
 *
 * History
 * -------
 * 1999-03-01 Added severity levels
 * 1999-05-14 Added BAD_CONTRACT
 * 1999-06-06 Added BAD_COMMENT
 * 1999-06-12 Added HasSeverity()
 * 1999-06-13 Updated messages
 * 1999-06-26 Updated messages
 * 1999-07-05 Updated messages
 * 2001-09-08 Check table order
 * 2002-08-25 Updated messages
 * 2009-10-03 Updated messages
 */

// Reminder: PbnErrDef is the index for PbnErrors[].
public class PbnError
{
  /*
   * A PBN game can be exported when it has no errors or warnings. Remarks are allowed.
   */
  public static final int       SEV_FATAL       = 0;    // Fatal error
  public static final int       SEV_SEVERE      = 1;    // PBN error; Game not playable
  public static final int       SEV_ERROR       = 2;    // PBN error; Game playable
  public static final int       SEV_WARNING     = 3;    // PBN warning; Game playable
  public static final int       SEV_REMARK      = 4;    // PBN remark;  Game playable
  public static final int       SEV_INFO        = 5;    // Informational
  public static final int       SEV_NUMBER      = 6;    // Number of severities

  public static final int       OK              =  0;
  public static final int       ERROR           =  1;
  public static final int       BAD_CALL        =  2;
  public static final int       BAD_CARD        =  3;
  public static final int       BAD_CHAR        =  4;
  public static final int       BAD_COMMENT     =  5;
  public static final int       BAD_CONTRACT    =  6;
  public static final int       BAD_DEAL        =  7;
  public static final int       BAD_DECLARER    =  8;
  public static final int       BAD_ILLEGAL     =  9;
  public static final int       BAD_INHERITANCE = 10;
  public static final int       BAD_LEAD        = 11;
  public static final int       BAD_NAG         = 12;
  public static final int       BAD_NOTE        = 13;
  public static final int       BAD_NUMBER      = 14;
  public static final int       BAD_RANK        = 15;
  public static final int       BAD_RESULT      = 16;
  public static final int       BAD_REVOKE      = 17;
  public static final int       BAD_RISK        = 18;
  public static final int       BAD_SCORE       = 19;
  public static final int       BAD_SECTION     = 20;
  public static final int       BAD_SIDE        = 21;
  public static final int       BAD_SUFFIX      = 22;
  public static final int       BAD_SUIT        = 23;
  public static final int       BAD_TAG_VALUE   = 24;
  public static final int       BAD_TRUMP       = 25;
  public static final int       DOUBLE_ILLEGAL  = 26;
  public static final int       DOUBLE_NAG      = 27;
  public static final int       DOUBLE_NOTE     = 28;
  public static final int       DOUBLE_SUFFIX   = 29;
  public static final int       FILE            = 30;
  public static final int       LINE_TOO_LONG   = 31;
  public static final int       NEW_TAG_NAME    = 32;
  public static final int       NEW_TAG_VALUE   = 33;
  public static final int       NO_CONTRACT     = 34;
  public static final int       NO_DEAL         = 35;
  public static final int       NO_DEALER       = 36;
  public static final int       NO_DECLARER     = 37;
  public static final int       NO_NOTE         = 38;
  public static final int       NO_TAG          = 39;
  public static final int       NO_TAG_VALUE    = 40;
  public static final int       RESOURCES       = 41;
  public static final int       REVOKE          = 42;
  public static final int       SEVERE_CHAR     = 43;
  public static final int       SEVERE_TAG_VALUE= 44;
  public static final int       SUFFICIENT      = 45;
  public static final int       TAG_EXISTS      = 46;
  public static final int       UNKNOWN_LEAD    = 47;
  public static final int       UNUSED_NOTE     = 48;

  public static final int       BAD_LAST_CALL   = 49;
  public static final int       BAD_LAST_CARD   = 50;
  public static final int       BAD_TABLE       = 51;
  public static final int       BAD_TABLE_ORDER = 52;
  public static final int       BAD_VULNERABLE  = 53;
  public static final int       BAD_PLAY        = 54;
  public static final int       NO_NEW_LINE     = 55;

  private int                   mError;

  public                        PbnError()
  {
    mError = OK;
  }

  public                        PbnError(
  int                           iError )
  {
    mError = iError;
  }

  public                        PbnError(
  PbnError                      oPbnError )
  {
    mError = oPbnError.mError;
  }

  public int                    Get()
  {
    return mError;
  }

  public void                   Set(
  int                           iError )
  {
    mError = iError;
  }

  public void                   Set(
  PbnError                      oError )
  {
    mError = oError.mError;
  }

  public void                   SetWorst(
  PbnError                      oError )
  {
    if ( oError.GetSeverity() < GetSeverity() )
    {
      mError = oError.mError;
    }
  }

  public void                   SetOK()
  {
    mError = OK;
  }

  public boolean                Is(
  int                           iError )
  {
    return (mError == iError);
  }

  public boolean                IsOK()
  {
    return (mError == OK);
  }

  public int                    GetSeverity()
  {
    int                         lSeverity;

    switch ( maTexts[ mError ].charAt( 0 ) )
    {
    case 'F':
      lSeverity = SEV_FATAL;
      break;
    case 'S':
      lSeverity = SEV_SEVERE;
      break;
    case 'E':
      lSeverity = SEV_ERROR;
      break;
    case 'W':
      lSeverity = SEV_WARNING;
      break;
    case 'R':
      lSeverity = SEV_REMARK;
      break;
    case 'I':
    default:
      lSeverity = SEV_INFO;
      break;
    } 

    return lSeverity;
  }

  public boolean                HasSeverity(
  int                           iSeverity )
  {
    return GetSeverity() <= iSeverity;
  }

  public String                 toString()
  {
    return maTexts[ mError ];
  }

/*
 * F: Fatal error
 * S: Severe error
 * E: Error
 * W: Warning
 * R: Remark
 * I: Info
 */
  private String []             maTexts =
  {
  "I: No error",
  "E: General error",
  "S: Bad call",
  "S: Bad card",
  "E: Bad character",
  "S: Bad comment",
  "S: Bad contract",
  "S: Bad deal",
  "S: Bad declarer",
  "S: Bad illegal",
  "S: Bad inheritance",
  "S: Bad lead",
  "E: Bad nag",
  "E: Bad note",
  "E: Bad number",
  "S: Bad rank",
  "E: Bad result",
  "S: Bad revoke",
  "S: Bad risk",
  "E: Bad score",
  "E: Bad section",
  "S: Bad side",
  "E: Bad suffix",
  "S: Bad suit",
  "E: Bad tag value",
  "S: Bad trump",
  "S: Double illegal",
  "W: Double nag",
  "E: Double note",
  "E: Double suffix",
  "F: File error",
  "S: Line too long",
  "W: New tag name",
  "R: New tag value",
  "S: No contract",
  "S: No deal",
  "S: No dealer",
  "S: No declarer",
  "E: No note for =%d=",
  "I: No tag",
  "E: No tag value",
  "F: No resources",
  "S: Revoke",
  "S: Wrong character",
  "S: Wrong tag value",
  "S: Sufficient",
  "W: Tag exists already",
  "W: Unknown lead",
  "W: Unused note [%d]",
  "E: Bad last call",
  "E: Bad last card",
  "E: Bad table",
  "E: Bad table order",
  "E: Bad vulnerable",
  "W: Bad play",
  "W: No new line"
  };
}
