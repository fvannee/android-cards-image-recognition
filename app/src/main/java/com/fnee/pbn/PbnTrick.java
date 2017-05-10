package com.fnee.pbn;/*
 * File   :     PbnTrick.java
 * Author :     Tis Veugen
 * Date   :     1998-10-11
 * PBN    :     1.0
 */

public class PbnTrick
{
  static final int              NUMBER = 13;

  private PbnCard []            maCards;

  public                        PbnTrick()
  {
    maCards = new PbnCard[ PbnSide.NUMBER ];

    for ( int i = 0; i < PbnSide.NUMBER; i++ )
    {
      maCards[ i ] = new PbnCard();
    }
  }

  public PbnCard                Get(
  PbnSide                       oSide )
  {
    return maCards[ oSide.Get() ];
  }

  public void                   Set(
  PbnSide                       oSide,
  PbnCard                       oCard )
  {
    maCards[ oSide.Get() ].Set( oCard );
  }
}
