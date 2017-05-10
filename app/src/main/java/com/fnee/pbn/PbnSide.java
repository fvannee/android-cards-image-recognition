package com.fnee.pbn;/*
 * File   :     PbnSide.java
 * Author :     Tis Veugen
 * Date   :     1998-03-28
 * PBN    :     1.0
 *
 * History
 * -------
 * 1999-03-28 Added FromChar.
 */

/**
 * Definition of Side
 */
public class PbnSide
{
  public static final int       IDLE        = -1;
  public static final int       SOUTH       = 0;
  public static final int       WEST        = 1;
  public static final int       NORTH       = 2;
  public static final int       EAST        = 3;
  public static final int       SOUTH_NORTH = 4;
  public static final int       WEST_EAST   = 5;
  public static final int       NONE        = 6;
  public static final int       EMPTY       = 7;
  public static final int       UNKNOWN     = 8;
  public static final int       NUMBER      = 4;
  private static final int      MODULO      = 3;    // NUMBER-1

  private static final String   S_IDLE    = "_";
  private static final String   S_SOUTH   = "South";
  private static final String   S_WEST    = "West";
  private static final String   S_NORTH   = "North";
  private static final String   S_EAST    = "East";
  private static final String   S_NONE    = "";
  private static final String   S_EMPTY   = "";
  private static final String   S_UNKNOWN = "?";

  int                           mSide;

  public                        PbnSide()
  {
    mSide = IDLE;
  }

  /**
   * NOTE: iSide must be a real integer, modulo PbnSide.NUMBER.
   */
  public                        PbnSide(
  int                           iSide )
  {
    mSide = iSide & MODULO;
  }

  public                        PbnSide(
  PbnSide                       oSide )
  {
    mSide = oSide.mSide;
  }

  public int                    Get()
  {
    return mSide;
  }

  public void                   Set(
  int                           iSide )
  {
    mSide = iSide;
  }

  public void                   Set(
  PbnSide                       oSide )
  {
    mSide = oSide.mSide;
  }

  public boolean                equals(
  PbnSide                       oSide )
  {
    return mSide == oSide.mSide;
  }

  public boolean                Is(
  int                           iSide )
  {
    return mSide == iSide;
  }

  public boolean                IsValid()
  {
    return (SOUTH <= mSide) && (mSide <= EAST);
  }

  public int                    Next()
  {
    mSide = ( mSide + 1 ) & MODULO;

    return mSide;
  }

  public int                    Previous()
  {
    mSide = ( mSide + NUMBER - 1 ) & MODULO;

    return mSide;
  }

  public PbnSide                GetPartner()
  {
    return new PbnSide( mSide ^ 2 );
  }

  public PbnSide                GetOpponent()
  {
    return new PbnSide( mSide ^ 1 );
  }

  public boolean                IsEW()
  {
    return ( mSide & 1 ) != 0;
  }

  public boolean                IsNS()
  {
    return ( mSide & 1 ) == 0;
  }

  public boolean                AreOpponents(
  PbnSide                       Side )
  {
    return ( mSide & 1 ) != ( Side.mSide & 1 );
  }

  public PbnSide                toSide(
  int                           number )
  {
    return new PbnSide( mSide + number );
  }

  public String                 toString()
  {
    String                      string = S_IDLE;

    switch ( mSide )
    {
    case SOUTH:
      string = S_SOUTH;
      break;

    case WEST:
      string = S_WEST;
      break;

    case NORTH:
      string = S_NORTH;
      break;

    case EAST:
      string = S_EAST;
      break;

    case NONE:
      string = S_NONE;
      break;

    case EMPTY:
      string = S_EMPTY;
      break;

    case UNKNOWN:
      string = S_UNKNOWN;
      break;
    }

    return string;
  }

  public String                 toCharacter()
  {
    String                      string = this.toString();

    if ( string.length() == 0 )
    {
      return string;
    }
    return string.substring( 0, 1 );
  }

  public static int             FromChar(
  char                          cSide )
  {
    int                         side;

    switch ( cSide )
    {
    case 's':
    case 'S':
      side = PbnSide.SOUTH;
      break;
    case 'w':
    case 'W':
      side = PbnSide.WEST;
      break;
    case 'n':
    case 'N':
      side = PbnSide.NORTH;
      break;
    case 'e':
    case 'E':
      side = PbnSide.EAST;
      break;
    default:
      side = PbnSide.IDLE;
      break;
    }

    return side;
  }
}
