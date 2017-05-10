package com.fnee.pbn;/*
 * File   :     PbnRanks.java
 * Author :     Tis Veugen
 * Date   :     1999-01-30
 * PBN    :     1.0
 *
 * 1999-01-30 Added toString()
 */

public class PbnRanks
{
  private int                   mRanks;

  public                        PbnRanks()
  {
    mRanks = PbnRank.NONE;
  }

  public                        PbnRanks(
  int                           iRanks )
  {
    mRanks = iRanks;
  }

  public                        PbnRanks(
  PbnRank                       oRank )
  {
    mRanks = oRank.Get();
  }

  public                        PbnRanks(
  PbnRanks                      oRanks )
  {
    mRanks = oRanks.mRanks;
  }

  public int                    Get()
  {
    return mRanks;
  }

  public void                   Set(
  int                           iRanks )
  {
    mRanks = iRanks;
  }

  public void                   Set(
  PbnRanks                      oRanks )
  {
    mRanks = oRanks.mRanks;
  }

  public boolean                AddRank(
  PbnRank                       oRank )
  {
    int                         lRank = oRank.Get();

    if ( PbnU.BitsHas( mRanks, lRank ) )
    {
      return false;
    }

    mRanks = PbnU.BitsUp( mRanks, lRank );
    return true;
  }

  public boolean                AddRanks(
  PbnRanks                      oRanks )
  {
    if ( PbnU.BitsHas( mRanks, oRanks.mRanks ) )
    {
      return false;
    }

    mRanks = PbnU.BitsUp( mRanks, oRanks.mRanks );
    return true;
  }

  public boolean                HasRank(
  PbnRank                       oRank )
  {
    return oRank.Has( mRanks );
  }

  public boolean                RemoveRank(
  PbnRank                       oRank )
  {
    int                         lRank = oRank.Get();

    if ( ! PbnU.BitsHas( mRanks, lRank ) )
    {
      return false;
    }

    mRanks = PbnU.BitsNo( mRanks, lRank );
    return true;
  }

  public void                   RemoveRanks(
  PbnRanks                      oRanks )
  {
    mRanks = PbnU.BitsNo( mRanks, oRanks.mRanks );
  }

  public int                    GetNrRanks()
  {
    int                         NrRanks = 0;
    PbnRank                     lRank = new PbnRank( PbnRank.TWO );
    
    for ( int i = 0; i < PbnRank.NUMBER; i++ )
    {
      if ( HasRank( lRank ) )
      {
        NrRanks++;
      }
      lRank.Next();
    }
    
    return NrRanks;
  }

  public String                 toString()
  {
    PbnRank                     lRank = new PbnRank( PbnRank.ACE );
    StringBuffer                lCards = new StringBuffer();

    for ( int i = 0; i < PbnRank.NUMBER; i++ )
    {
      if ( HasRank( lRank ) )
      {
        lCards.append( lRank.toCharacter() );
      }
      lRank.Previous();
    }

    return lCards.toString();
  }
}
