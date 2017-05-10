package com.fnee.pbn;/*
 * File   :     PbnHand.java
 * Author :     Tis Veugen
 * Date   :     1999-01-03
 * PBN    :     1.0
 */

public class PbnHand
{
  private PbnRanks []           maRanks;

  public                        PbnHand()
  {
    maRanks = new PbnRanks[ PbnSuit.NUMBER ];

    for ( int iSuit = 0; iSuit < PbnSuit.NUMBER; iSuit++ )
    {
      maRanks[ iSuit ] = new PbnRanks();
    }
  }

  public                        PbnHand(
  PbnHand                       oHand )
  {
    maRanks = new PbnRanks[ PbnSuit.NUMBER ];

    for ( int iSuit = 0; iSuit < PbnSuit.NUMBER; iSuit++ )
    {
      maRanks[ iSuit ] = new PbnRanks( oHand.maRanks[ iSuit ] );
    }
  }

  public boolean                HasRank(
  PbnSuit                       oSuit,
  PbnRank                       oRank )
  {
    return maRanks[ oSuit.Get() ].HasRank( oRank );
  }

  public boolean                AddRank(
  PbnSuit                       oSuit,
  PbnRank                       oRank )
  {
    return maRanks[ oSuit.Get() ].AddRank( oRank );
  }

  public boolean                AddRanks(
  PbnSuit                       oSuit,
  PbnRanks                      oRanks )
  {
    return maRanks[ oSuit.Get() ].AddRanks( oRanks );
  }

  public PbnRanks               GetRanks(
  PbnSuit                       oSuit )
  {
    return new PbnRanks( maRanks[ oSuit.Get() ] );
  }

  public boolean                RemoveRank(
  PbnSuit                       oSuit,
  PbnRank                       oRank )
  {
    return maRanks[ oSuit.Get() ].RemoveRank( oRank );
  }

  public void                   RemoveRanks(
  PbnSuit                       oSuit,
  PbnRanks                      oRanks )
  {
    maRanks[ oSuit.Get() ].RemoveRanks( oRanks );
  }

  public void                   SetRanks(
  PbnSuit                       oSuit,
  PbnRanks                      oRanks )
  {
    maRanks[ oSuit.Get() ].Set( oRanks );
  }

  public int                    GetNrRanks(
  int                           iSuit )
  {
    return maRanks[ iSuit ].GetNrRanks();
  }

  public boolean                PlayCard(
  PbnCard                       oCard )
  {
    if ( ! ( oCard.GetSuit().IsValid()
          && oCard.GetRank().IsNormal() ) )
    {
      return false;
    }

    return RemoveRank( oCard.GetSuit(), oCard.GetRank() );
  }

  public boolean                UnplayCard(
  PbnCard                       oCard )
  {
    if ( ! ( oCard.GetSuit().IsValid()
          && oCard.GetRank().IsNormal() ) )
    {
      return false;
    }

    return AddRank( oCard.GetSuit(), oCard.GetRank() );
  }

  public void                   SetRanksAll()
  {
    for ( int iSuit = 0; iSuit < PbnSuit.NUMBER; iSuit++ )
    {
      maRanks[ iSuit ].Set( PbnRank.ALL );
    }
  }
}
