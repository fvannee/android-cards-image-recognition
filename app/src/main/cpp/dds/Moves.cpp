/*
   DDS, a bridge double dummy solver.

   Copyright (C) 2006-2014 by Bo Haglund /
   2014-2016 by Bo Haglund & Soren Hein.

   See LICENSE and README.
*/


#include <stdexcept>

#include "dds.h"
#include "Moves.h"
#include "ABsearch.h"

#ifdef DDS_MOVES
  #define MG_REGISTER(a, b) lastCall[currTrick][b] = a
#else
  #define MG_REGISTER(a, b) 1;
#endif


int RegisterList[16] =
{
  MG_NT0, MG_TRUMP0,
  -1, -1,

  MG_NT_NOTVOID1, MG_TRUMP_NOTVOID1,
  MG_NT_VOID1, MG_TRUMP_VOID1,

  MG_NT_NOTVOID2, MG_TRUMP_NOTVOID2,
  MG_NT_VOID2, MG_TRUMP_VOID2,

  MG_COMB_NOTVOID3, MG_COMB_NOTVOID3,
  MG_NT_VOID3, MG_TRUMP_VOID3
};


Moves::Moves()
{
  sprintf(funcName[MG_NT0] , "%s", "NT0");
  sprintf(funcName[MG_TRUMP0] , "%s", "Trump0");
  sprintf(funcName[MG_NT_VOID1] , "%s", "NT_Void1");
  sprintf(funcName[MG_TRUMP_VOID1] , "%s", "Trump_Void1");
  sprintf(funcName[MG_NT_NOTVOID1] , "%s", "NT_Notvoid1");
  sprintf(funcName[MG_TRUMP_NOTVOID1], "%s", "Trump_Notvoid1");
  sprintf(funcName[MG_NT_VOID2] , "%s", "NT_Void2");
  sprintf(funcName[MG_TRUMP_VOID2] , "%s", "Trump_Void2");
  sprintf(funcName[MG_NT_NOTVOID2] , "%s", "NT_Notvoid2");
  sprintf(funcName[MG_TRUMP_NOTVOID2], "%s", "Trump_Notvoid2");
  sprintf(funcName[MG_NT_VOID3] , "%s", "NT_Void3");
  sprintf(funcName[MG_TRUMP_VOID3] , "%s", "Trump_Void3");
  sprintf(funcName[MG_COMB_NOTVOID3] , "%s", "Comb_Notvoid3");

  for (int t = 0; t < 13; t++)
  {
    for (int h = 0; h < DDS_HANDS; h++)
    {
      lastCall[t][h] = -1;

      trickTable[t][h].count = 0;
      trickSuitTable[t][h].count = 0;

      trickDetailTable [t][h].nfuncs = 0;
      trickDetailSuitTable[t][h].nfuncs = 0;
      for (int i = 0; i < MG_NUM_FUNCTIONS; i++)
      {
        trickDetailTable [t][h].list[i].count = 0;
        trickDetailSuitTable[t][h].list[i].count = 0;
      }
    }
  }

  trickFuncTable .nfuncs = 0;
  trickFuncSuitTable.nfuncs = 0;
  for (int i = 0; i < MG_NUM_FUNCTIONS; i++)
  {
    trickFuncTable .list[i].count = 0;
    trickFuncSuitTable.list[i].count = 0;
  }

  strcpy(fname, "");
  fp = stdout;

  WeightList[ 4] = &Moves::WeightAllocNTNotvoid1;
  WeightList[ 5] = &Moves::WeightAllocTrumpNotvoid1;
  WeightList[ 6] = &Moves::WeightAllocNTVoid1;
  WeightList[ 7] = &Moves::WeightAllocTrumpVoid1;

  WeightList[ 8] = &Moves::WeightAllocNTNotvoid2;
  WeightList[ 9] = &Moves::WeightAllocTrumpNotvoid2;
  WeightList[10] = &Moves::WeightAllocNTVoid2;
  WeightList[11] = &Moves::WeightAllocTrumpVoid2;

  WeightList[12] = &Moves::WeightAllocCombinedNotvoid3;
  WeightList[13] = &Moves::WeightAllocCombinedNotvoid3;
  WeightList[14] = &Moves::WeightAllocNTVoid3;
  WeightList[15] = &Moves::WeightAllocTrumpVoid3;
}


void Moves::SetFile(char * ourFname)
{
  if (strlen(ourFname) > 80)
    return;

  if (fp != stdout) // Already set
    return;

  strncpy(fname, ourFname, strlen(ourFname));

  fp = fopen(fname, "w");
  if (! fp)
    fp = stdout;
}


Moves::~Moves()
{
  if (fp != stdout && fp != nullptr)
    fclose(fp);
}


void Moves::Init(
  int tricks,
  int relStartHand,
  int initialRanks[],
  int initialSuits[],
  unsigned short int rankInSuit[DDS_HANDS][DDS_SUITS],
  int ourTrump,
  int ourLeadHand)
{
  currTrick = tricks;
  trump = ourTrump;

  if (relStartHand == 0)
    track[tricks].leadHand = ourLeadHand;

  for (int m = 0; m < 13; m++)
  {
    for (int h = 0; h < DDS_HANDS; h++)
    {
      moveList[m][h].current = 0;
      moveList[m][h].last = 0;

    }
  }

  // 0x1ffff would be enough, but this is for compatibility.
  for (int s = 0; s < DDS_SUITS; s++)
    track[tricks].removedRanks[s] = 0xffff;

  for (int h = 0; h < DDS_HANDS; h++)
    for (int s = 0; s < DDS_SUITS; s++)
      track[tricks].removedRanks[s] ^= rankInSuit[h][s];

  for (int n = 0; n < relStartHand; n++)
  {
    int s = initialSuits[n];
    int r = initialRanks[n];

    track[tricks].removedRanks[s] ^= bitMapRank[r];
  }
}


void Moves::Reinit(
  int tricks,
  int ourLeadHand)
{
  track[tricks].leadHand = ourLeadHand;
}


int Moves::MoveGen0(
  int tricks,
  pos * posPoint,
  moveType * bestMove,
  moveType * bestMoveTT,
  relRanksType thrp_rel[])
{
  trackp = &track[tricks];
  leadHand = trackp->leadHand;
  currHand = leadHand;
  currTrick = tricks;

  moveGroupType * mp;
  int removed, g, rank, seq;

  movePlyType * listp = &moveList[tricks][0];
  mply = listp->move;
  for (int s = 0; s < DDS_SUITS; s++)
    trackp->lowestWin[0][s] = 0;
  numMoves = 0;

  bool ftest = ((trump != DDS_NOTRUMP) &&
                (posPoint->winner[trump].rank != 0));

  for (suit = 0; suit < DDS_SUITS; suit++)
  {
    unsigned short ris = posPoint->rankInSuit[leadHand][suit];
    if (ris == 0) continue;

    lastNumMoves = numMoves;
    mp = &groupData[ris];
    g = mp->lastGroup;
    removed = trackp->removedRanks[suit];

    while (g >= 0)
    {
      rank = mp->rank[g];
      seq = mp->sequence[g];

      while (g >= 1 && ((mp->gap[g] & removed) == mp->gap[g]))
        seq |= mp->fullseq[--g];

      mply[numMoves].sequence = seq;
      mply[numMoves].suit = suit;
      mply[numMoves].rank = rank;

      numMoves++;
      g--;
    }

    if (ftest)
      Moves::WeightAllocTrump0(posPoint,
                               bestMove, bestMoveTT, thrp_rel);
    else
      Moves::WeightAllocNT0(posPoint,
                            bestMove, bestMoveTT, thrp_rel);
  }

#ifdef DDS_MOVES
  if (ftest)
    MG_REGISTER(MG_TRUMP0, 0);
  else
    MG_REGISTER(MG_NT0, 0);
#endif

  listp->current = 0;
  listp->last = numMoves - 1;
  if (numMoves != 1)
    Moves::MergeSort();
  return numMoves;
}


int Moves::MoveGen123(
  int tricks,
  int handRel,
  pos * posPoint)
{
  trackp = &track[tricks];
  leadHand = trackp->leadHand;
  currHand = handId(leadHand, handRel);
  currTrick = tricks;
  leadSuit = track[tricks].leadSuit;

  moveGroupType * mp;
  int removed, g, rank, seq;

  movePlyType * listp = &moveList[tricks][handRel];
  mply = listp->move;

  for (int s = 0; s < DDS_SUITS; s++)
    trackp->lowestWin[handRel][s] = 0;
  numMoves = 0;

  WeightPtr WeightFnc;
  int findex;
  int ftest = ((trump != DDS_NOTRUMP) &&
               (posPoint->winner[trump].rank != 0) ? 1 : 0);

  unsigned short ris = posPoint->rankInSuit[currHand][leadSuit];

  if (ris != 0)
  {
    mp = &groupData[ris];
    g = mp->lastGroup;
    removed = trackp->removedRanks[leadSuit];

    while (g >= 0)
    {
      rank = mp->rank[g];
      seq = mp->sequence[g];

      while (g >= 1 && ((mp->gap[g] & removed) == mp->gap[g]))
        seq |= mp->fullseq[--g];

      mply[numMoves].sequence = seq;
      mply[numMoves].suit = leadSuit;
      mply[numMoves].rank = rank;

      numMoves++;
      g--;
    }

    findex = 4 * handRel + ftest;
#ifdef DDS_MOVES
    MG_REGISTER(RegisterList[findex], handRel);
#endif

    listp->current = 0;
    listp->last = numMoves - 1;
    if (numMoves == 1)
      return numMoves;

    // WeightFnc = WeightList[findex];
    // (this->*WeightFnc)(posPoint);
    (this->*WeightList[findex])(posPoint);

    Moves::MergeSort();
    return numMoves;
  }

  findex = 4 * handRel + ftest + 2;

#ifdef DDS_MOVES
  MG_REGISTER(RegisterList[findex], handRel);
#endif
  WeightFnc = WeightList[findex];

  for (suit = 0; suit < DDS_SUITS; suit++)
  {
    ris = posPoint->rankInSuit[currHand][suit];
    if (ris == 0) continue;

    lastNumMoves = numMoves;
    mp = &groupData[ris];
    g = mp->lastGroup;
    removed = trackp->removedRanks[suit];

    while (g >= 0)
    {
      rank = mp->rank[g];
      seq = mp->sequence[g];

      while (g >= 1 && ((mp->gap[g] & removed) == mp->gap[g]))
        seq |= mp->fullseq[--g];

      mply[numMoves].sequence = seq;
      mply[numMoves].suit = suit;
      mply[numMoves].rank = rank;

      numMoves++;
      g--;
    }

    (this->*WeightFnc)(posPoint);
  }

  listp->current = 0;
  listp->last = numMoves - 1;
  if (numMoves != 1)
    Moves::MergeSort();
  return numMoves;
}


void Moves::WeightAllocTrump0(
  pos * posPoint,
  moveType * bestMove,
  moveType * bestMoveTT,
  relRanksType thrp_rel[])
{
  unsigned short suitCount = posPoint->length[leadHand][suit];
  unsigned short suitCountLH = posPoint->length[lho[leadHand]][suit];
  unsigned short suitCountRH = posPoint->length[rho[leadHand]][suit];
  unsigned short aggr = posPoint->aggr[suit];

  // Why?
  int countLH = (suitCountLH == 0 ? currTrick + 1 : suitCountLH) << 2;
  int countRH = (suitCountRH == 0 ? currTrick + 1 : suitCountRH) << 2;

  int suitWeightD = - (((countLH + countRH) << 5) / 13);

  for (int k = lastNumMoves; k < numMoves; k++)
  {
    int suitBonus = 0;
    bool winMove = false;

    int rRank = relRank[aggr][mply[k].rank];

    /* Discourage suit if LHO or RHO can ruff. */
    if ((suit != trump) &&
        (((posPoint->rankInSuit[lho[leadHand]][suit] == 0) &&
          (posPoint->rankInSuit[lho[leadHand]][trump] != 0)) ||
         ((posPoint->rankInSuit[rho[leadHand]][suit] == 0) &&
          (posPoint->rankInSuit[rho[leadHand]][trump] != 0))))
      suitBonus = -12;

    /* Encourage suit if partner can ruff. */
    if ((suit != trump) &&
        (posPoint->length[partner[leadHand]][suit] == 0) &&
        (posPoint->length[partner[leadHand]][trump] > 0) &&
        (suitCountRH > 0))
      suitBonus += 17;

    /* Discourage suit if RHO has high card. */
    if ((posPoint->winner[suit].hand == rho[leadHand]) ||
        (posPoint->secondBest[suit].hand == rho[leadHand]))
    {
      if (suitCountRH != 1)
        suitBonus += -12;
    }

    /* Try suit if LHO has winning card and partner second best.
       Exception: partner has singleton. */

    else if ((posPoint->winner[suit].hand == lho[leadHand]) &&
             (posPoint->secondBest[suit].hand == partner[leadHand]))
    {
      /* This case was suggested by Jo�l Bradmetz. */
      if (posPoint->length[partner[leadHand]][suit] != 1)
        suitBonus += 27;
    }

    /* Encourage play of suit where partner wins and
       returns the suit for a ruff. */
    if ((suit != trump) && (suitCount == 1) &&
        (posPoint->length[leadHand][trump] > 0) &&
        (posPoint->length[partner[leadHand]][suit] > 1) &&
        (posPoint->winner[suit].hand == partner[leadHand]))
      suitBonus += 19;


    /* Discourage a suit selection where the search tree appears larger
       than for the altenative suits: the search is estimated to be
       small when the added number of alternative cards to play for
       the opponents is small. */

    int suitWeightDelta = suitBonus + suitWeightD;

    if (posPoint->winner[suit].rank == mply[k].rank)
    {
      if ((suit != trump))
      {
        if ((posPoint->length[partner[leadHand]][suit] != 0) ||
            (posPoint->length[partner[leadHand]][trump] == 0))
        {
          if (((posPoint->length[lho[leadHand]][suit] != 0) ||
               (posPoint->length[lho[leadHand]][trump] == 0)) &&
              ((posPoint->length[rho[leadHand]][suit] != 0) ||
               (posPoint->length[rho[leadHand]][trump] == 0)))
            winMove = true;
        }
        else if (((posPoint->length[lho[leadHand]][suit] != 0) ||
                  (posPoint->rankInSuit[partner[leadHand]][trump] >
                   posPoint->rankInSuit[lho[leadHand]][trump])) &&
                 ((posPoint->length[rho[leadHand]][suit] != 0) ||
                  (posPoint->rankInSuit[partner[leadHand]][trump] >
                   posPoint->rankInSuit[rho[leadHand]][trump])))
          winMove = true;
      }
      else
        winMove = true;
    }
    else if (posPoint->rankInSuit[partner[leadHand]][suit] >
             (posPoint->rankInSuit[lho[leadHand]][suit] |
              posPoint->rankInSuit[rho[leadHand]][suit]))
    {
      if (suit != trump)
      {
        if (((posPoint->length[lho[leadHand]][suit] != 0) ||
             (posPoint->length[lho[leadHand]][trump] == 0)) &&
            ((posPoint->length[rho[leadHand]][suit] != 0) ||
             (posPoint->length[rho[leadHand]][trump] == 0)))
          winMove = true;
      }
      else
        winMove = true;
    }
    else if (suit != trump)
    {
      if ((posPoint->length[partner[leadHand]][suit] == 0) &&
          (posPoint->length[partner[leadHand]][trump] != 0))
      {
        if ((posPoint->length[lho[leadHand]][suit] == 0) &&
            (posPoint->length[lho[leadHand]][trump] != 0) &&
            (posPoint->length[rho[leadHand]][suit] == 0) &&
            (posPoint->length[rho[leadHand]][trump] != 0))
        {
          if (posPoint->rankInSuit[partner[leadHand]][trump] >
              (posPoint->rankInSuit[lho[leadHand]][trump] |
               posPoint->rankInSuit[rho[leadHand]][trump]))
            winMove = true;
        }
        else if ((posPoint->length[lho[leadHand]][suit] == 0) &&
                 (posPoint->length[lho[leadHand]][trump] != 0))
        {
          if (posPoint->rankInSuit[partner[leadHand]][trump]
              > posPoint->rankInSuit[lho[leadHand]][trump])
            winMove = true;
        }
        else if ((posPoint->length[rho[leadHand]][suit] == 0) &&
                 (posPoint->length[rho[leadHand]][trump] != 0))
        {
          if (posPoint->rankInSuit[partner[leadHand]][trump]
              > posPoint->rankInSuit[rho[leadHand]][trump])
            winMove = true;
        }
        else
          winMove = true;
      }
    }

    if (winMove)
    {
      /* Encourage ruffing LHO or RHO singleton, highest card. */
      if (((suitCountLH == 1) &&
           (posPoint->winner[suit].hand == lho[leadHand]))
          || ((suitCountRH == 1) &&
              (posPoint->winner[suit].hand == rho[leadHand])))
        mply[k].weight = suitWeightDelta + 35 + rRank;

      /* Lead hand has the highest card. */

      else if (posPoint->winner[suit].hand == leadHand)
      {
        /* Also, partner has second highest card. */
        if (posPoint->secondBest[suit].hand == partner[leadHand])
          mply[k].weight = suitWeightDelta + 48 + rRank;
        else if (posPoint->winner[suit].rank == mply[k].rank)
          /* If the current card to play is the highest card. */
          mply[k].weight = suitWeightDelta + 31;
        else
          mply[k].weight = suitWeightDelta - 3 + rRank;
      }
      else if (posPoint->winner[suit].hand == partner[leadHand])
      {
        /* If partner has highest card */
        if (posPoint->secondBest[suit].hand == leadHand)
          mply[k].weight = suitWeightDelta + 42 + rRank;
        else
          mply[k].weight = suitWeightDelta + 28 + rRank;
      }
      /* Encourage playing second highest rank if hand also has
         third highest rank. */
      else if ((mply[k].sequence) &&
               (mply[k].rank == posPoint->secondBest[suit].rank))
        mply[k].weight = suitWeightDelta + 40;
      else if (mply[k].sequence)
        mply[k].weight = suitWeightDelta + 22 + rRank;
      else
        mply[k].weight = suitWeightDelta + 11 + rRank;

      /* playing cards that previously caused search cutoff
         or was stored as the best move in a transposition table entry
         match. */

      if ((bestMove->suit == suit) &&
          (bestMove->rank == mply[k].rank))
        mply[k].weight += 55;
      else if ((bestMoveTT->suit == suit) &&
               (bestMoveTT->rank == mply[k].rank))
        mply[k].weight += 18;
    }
    else
    {
      /* Encourage playing the suit if the hand together with partner
         have both the 2nd highest and the 3rd highest cards such that
         the side of the hand has the highest card in the next round
         playing this suit. */

      int thirdBestHand = thrp_rel[aggr].absRank[3][suit].hand;

      if ((posPoint->secondBest[suit].hand == partner[leadHand]) &&
          (partner[leadHand] == thirdBestHand))
        suitWeightDelta += 20;
      else if (((posPoint->secondBest[suit].hand == leadHand) &&
                (partner[leadHand] == thirdBestHand) &&
                (posPoint->length[partner[leadHand]][suit] > 1)) ||
               ((posPoint->secondBest[suit].hand == partner[leadHand]) &&
                (leadHand == thirdBestHand) &&
                (posPoint->length[partner[leadHand]][suit] > 1)))
        suitWeightDelta += 13;

      /* Higher weight if LHO or RHO has the highest (winning) card as
         a singleton. */

      if (((suitCountLH == 1) &&
           (posPoint->winner[suit].hand == lho[leadHand]))
          || ((suitCountRH == 1) &&
              (posPoint->winner[suit].hand == rho[leadHand])))
        mply[k].weight = suitWeightDelta + rRank + 2;
      else if (posPoint->winner[suit].hand == leadHand)
      {
        if (posPoint->secondBest[suit].hand == partner[leadHand])
          /* Opponents win by ruffing */
          mply[k].weight = suitWeightDelta + 33 + rRank;
        else if (posPoint->winner[suit].rank == mply[k].rank)
          /* Opponents win by ruffing */
          mply[k].weight = suitWeightDelta + 38;
        else
          mply[k].weight = suitWeightDelta - 14 + rRank;
      }
      else if (posPoint->winner[suit].hand == partner[leadHand])
      {
        /* Opponents win by ruffing */
        mply[k].weight = suitWeightDelta + 34 + rRank;
      }
      /* Encourage playing second highest rank if hand also has
         third highest rank. */
      else if ((mply[k].sequence) &&
               (mply[k].rank == posPoint->secondBest[suit].rank))
        mply[k].weight = suitWeightDelta + 35;
      else
        mply[k].weight = suitWeightDelta + 17 - (mply[k].rank);

      /* Encourage playing cards that previously caused search cutoff
         or was stored as the best move in a transposition table
         entry match. */

      if ((bestMove->suit == suit) &&
          (bestMove->rank == mply[k].rank))
        mply[k].weight += 18;
    }
  }
}


void Moves::WeightAllocNT0(
  pos * posPoint,
  moveType * bestMove,
  moveType * bestMoveTT,
  relRanksType thrp_rel[])
{
  int aggr = posPoint->aggr[suit];

  /* Discourage a suit selection where the search tree appears larger
     than for the alternative suits: the search is estimated to be
     small when the added number of alternative cards to play for
     the opponents is small. */

  unsigned short suitCountLH = posPoint->length[lho[leadHand]][suit];
  unsigned short suitCountRH = posPoint->length[rho[leadHand]][suit];

  // Why?
  int countLH = (suitCountLH == 0 ? currTrick + 1 : suitCountLH) << 2;
  int countRH = (suitCountRH == 0 ? currTrick + 1 : suitCountRH) << 2;

  int suitWeightD = - (((countLH + countRH) << 5) / 19);
  if (posPoint->length[partner[leadHand]][suit] == 0)
    suitWeightD += -9;

  for (int k = lastNumMoves; k < numMoves; k++)
  {
    int suitWeightDelta = suitWeightD;
    int rRank = relRank[aggr][mply[k].rank];

    if (posPoint->winner[suit].rank == mply[k].rank ||
        (posPoint->rankInSuit[partner[leadHand]][suit] >
         (posPoint->rankInSuit[lho[leadHand]][suit] |
          posPoint->rankInSuit[rho[leadHand]][suit])))
    {
      // Can win trick, ourselves or partner.
      // FIX: No distinction?
      /* Discourage suit if RHO has second best card.
         Exception: RHO has singleton. */
      if (posPoint->secondBest[suit].hand == rho[leadHand])
      {
        if (suitCountRH != 1)
          suitWeightDelta += -1;
      }
      /* Encourage playing suit if LHO has second highest rank. */
      else if (posPoint->secondBest[suit].hand == lho[leadHand])
      {
        if (suitCountLH != 1)
          suitWeightDelta += 22;
        else
          suitWeightDelta += 16;
      }

      /* Higher weight if also second best rank is present on
         current side to play, or if second best is a singleton
         at LHO or RHO. */

      if (((posPoint->secondBest[suit].hand != lho[leadHand])
           || (suitCountLH == 1)) &&
          ((posPoint->secondBest[suit].hand != rho[leadHand])
           || (suitCountRH == 1)))
        mply[k].weight = suitWeightDelta + 45 + rRank;
      else
        mply[k].weight = suitWeightDelta + 18 + rRank;

      /* Encourage playing cards that previously caused search cutoff
         or was stored as the best move in a transposition table
         entry match. */

      if ((bestMove->suit == suit) &&
          (bestMove->rank == mply[k].rank))
        mply[k].weight += 126;
      else if ((bestMoveTT->suit == suit) &&
               (bestMoveTT->rank == mply[k].rank))
        mply[k].weight += 32;
    }
    else
    {
      /* Discourage suit if RHO has winning or second best card.
         Exception: RHO has singleton. */

      if ((posPoint->winner[suit].hand == rho[leadHand]) ||
          (posPoint->secondBest[suit].hand == rho[leadHand]))
      {
        if (suitCountRH != 1)
          suitWeightDelta += -10;
      }

      /* Try suit if LHO has winning card and partner second best.
         Exception: partner has singleton. */

      else if ((posPoint->winner[suit].hand == lho[leadHand]) &&
               (posPoint->secondBest[suit].hand == partner[leadHand]))
      {
        /* This case was suggested by Jo�l Bradmetz. */
        if (posPoint->length[partner[leadHand]][suit] != 1)
          suitWeightDelta += 31;
      }

      /* Encourage playing the suit if the hand together with partner
         have both the 2nd highest and the 3rd highest cards such
         that the side of the hand has the highest card in the
         next round playing this suit. */

      int thirdBestHand = thrp_rel[aggr].absRank[3][suit].hand;

      if ((posPoint->secondBest[suit].hand == partner[leadHand]) &&
          (partner[leadHand] == thirdBestHand))
        suitWeightDelta += 35;
      else if (((posPoint->secondBest[suit].hand == leadHand) &&
                (partner[leadHand] == thirdBestHand) &&
                (posPoint->length[partner[leadHand]][suit] > 1)) ||
               ((posPoint->secondBest[suit].hand == partner[leadHand]) &&
                (leadHand == thirdBestHand) &&
                (posPoint->length[partner[leadHand]][suit] > 1)))
        suitWeightDelta += 25;

      /* Higher weight if LHO or RHO has the highest (winning) card
         as a singleton. */

      if (((suitCountLH == 1) &&
           (posPoint->winner[suit].hand == lho[leadHand]))
          || ((suitCountRH == 1) &&
              (posPoint->winner[suit].hand == rho[leadHand])))
        mply[k].weight = suitWeightDelta + 28 + rRank;
      else if (posPoint->winner[suit].hand == leadHand)
        mply[k].weight = suitWeightDelta - 17 + rRank;
      else if (! mply[k].sequence)
        mply[k].weight = suitWeightDelta + 12 + rRank;
      else if (mply[k].rank == posPoint->secondBest[suit].rank)
        mply[k].weight = suitWeightDelta + 48;
      else
        mply[k].weight = suitWeightDelta + 29 - rRank;

      /* Encourage playing cards that previously caused search cutoff
         or was stored as the best move in a transposition table
         entry match. */

      if ((bestMove->suit == suit) && (bestMove->rank == mply[k].rank))
        mply[k].weight += 47;
      else if ((bestMoveTT->suit == suit) &&
               (bestMoveTT->rank == mply[k].rank))
        mply[k].weight += 19;
    }
  }
}


void Moves::WeightAllocTrumpNotvoid1(
  pos * posPoint)
{
  int max3rd = highestRank[
                 posPoint->rankInSuit[partner[leadHand]][leadSuit]];
  int maxpd = highestRank[
                 posPoint->rankInSuit[rho[leadHand] ][leadSuit]];
  int min3rd = lowestRank [
                 posPoint->rankInSuit[partner[leadHand]][leadSuit]];
  int minpd = lowestRank [
                 posPoint->rankInSuit[rho[leadHand] ][leadSuit]];

  for (int k = 0; k < numMoves; k++)
  {
    bool winMove = false; /* If true, current move can win trick. */
    int rRank = relRank[ posPoint->aggr[leadSuit] ][mply[k].rank];

    if (leadSuit == trump)
    {
      if (maxpd > trackp->move[0].rank && maxpd > max3rd)
        winMove = true;
      else if (mply[k].rank > trackp->move[0].rank &&
               mply[k].rank > max3rd)
        winMove = true;
    }
    else
    {
      if (mply[k].rank > trackp->move[0].rank && mply[k].rank > max3rd)
      {
        if ((max3rd != 0) ||
            (posPoint->length[partner[leadHand]][trump] == 0))
          winMove = true;
        else if ((maxpd == 0)
                 && (posPoint->length[rho[leadHand]][trump] != 0)
                 && (posPoint->rankInSuit[rho[leadHand]][trump] >
                     posPoint->rankInSuit[partner[leadHand]][trump]))
          winMove = true;
      }
      else if (maxpd > trackp->move[0].rank && maxpd > max3rd)
      {
        if ((max3rd != 0) ||
            (posPoint->length[partner[leadHand]][trump] == 0))
          winMove = true;
      }
      else if (trackp->move[0].rank > maxpd &&
               trackp->move[0].rank > max3rd &&
               trackp->move[0].rank > mply[k].rank)
      {
        if ((maxpd == 0) && (posPoint->length[rho[leadHand]][trump] != 0))
        {
          if ((max3rd != 0) ||
              (posPoint->length[partner[leadHand]][trump] == 0))
            winMove = true;
          else if (posPoint->rankInSuit[rho[leadHand]][trump]
                   > posPoint->rankInSuit[partner[leadHand]][trump])
            winMove = true;
        }
      }
      else if (maxpd == 0 && (posPoint->length[rho[leadHand]][trump] != 0))
        /* winnerHand is partner to first */
        winMove = true;
    }

    if (winMove)
    {
      if (min3rd > mply[k].rank)
        // Partner must be winning -- we can't.
        mply[k].weight = 40 + rRank;
      else if ((maxpd > trackp->move[0].rank) &&
               (posPoint->rankInSuit[leadHand][leadSuit] >
                posPoint->rankInSuit[rho[leadHand]][leadSuit]))
        mply[k].weight = 41 + rRank;

      /* If rho has a card in the leading suit that
         is higher than the trick leading card but lower
         than the highest rank of the leading hand, then
         lho playing the lowest card will be the cheapest win */

      // FIX: Don't follow

      else if (mply[k].rank > trackp->move[0].rank)
      {
        if (mply[k].rank < maxpd)
          mply[k].weight = 78 - (mply[k].rank);
        /* If played card is lower than any of the cards of
           rho, it will be the cheapest win */
        else if (mply[k].rank > max3rd)
          mply[k].weight = 73 - (mply[k].rank);
        /* If played card is higher than any cards at partner
           of the leading hand, rho can play low, under the
           condition that he has a lower card than lho played */
        else if (mply[k].sequence) // May establish a winner
          mply[k].weight = 62 - (mply[k].rank);
        else
          mply[k].weight = 49 - (mply[k].rank);
      }
      else if (maxpd > 0)
        mply[k].weight = 47 - (mply[k].rank);
      else
        mply[k].weight = 40 - (mply[k].rank);
    }
    else if (mply[k].rank < min3rd || mply[k].rank < minpd)
      // Will be beaten anyway.
      mply[k].weight = -9 + rRank;
    else if (mply[k].rank < trackp->move[0].rank)
      // Already beaten.
      mply[k].weight = -16 + rRank;
    else if (mply[k].sequence)
      // May establish a winner
      mply[k].weight = 22 - (mply[k].rank);
    else
      mply[k].weight = 10 - (mply[k].rank);
  }
}


void Moves::WeightAllocNTNotvoid1(
  pos * posPoint)
{
  // FIX: Second test should come first, and outside loop.
  // Why is better not to be able to beat later players than
  // not to be able to beat the lead?
  // Why rRank?

  int max3rd = highestRank[
                 posPoint->rankInSuit[partner[leadHand]][leadSuit]];
  int maxpd = highestRank[
                 posPoint->rankInSuit[rho[leadHand]][leadSuit] ];

  if (maxpd > trackp->move[0].rank && maxpd > max3rd)
  {
    // Partner can beat both opponents.
    for (int k = 0; k < numMoves; k++)
      mply[k].weight = -mply[k].rank;
  }
  else
  {
    int min3rd = lowestRank [
                   posPoint->rankInSuit[partner[leadHand]][leadSuit]];
    int minpd = lowestRank [
                   posPoint->rankInSuit[rho[leadHand]][leadSuit] ];

    for (int k = 0; k < numMoves; k++)
    {
      int rRank = relRank[ posPoint->aggr[leadSuit] ][mply[k].rank];

      if (mply[k].rank > trackp->move[0].rank && mply[k].rank > max3rd)
        // We can beat both opponents.
        mply[k].weight = 81 - mply[k].rank;

      else if ((min3rd > mply[k].rank) || (minpd > mply[k].rank))
        // Card can make no difference, so play very low.
        mply[k].weight = -3 + rRank;

      else if (mply[k].rank < trackp->move[0].rank)
        // Can't beat the card led.
        mply[k].weight = -11 + rRank;

      else if (mply[k].sequence)
        // Some willingness to split.
        mply[k].weight = 10 + rRank;

      else
        mply[k].weight = 13 - mply[k].rank;
    }
  }
}


void Moves::WeightAllocTrumpVoid1(
  pos * posPoint)
{
  // FIX:
  // leadSuit == trump: Why differentiate?
  // suit != trump: Same question.
  // Don't ruff ahead of partner?

  unsigned short suitCount = posPoint->length[currHand][suit];
  int suitAdd;

  if (leadSuit == trump) // We pitch
  {
    if (posPoint->rankInSuit[rho[leadHand]][leadSuit] >
        (posPoint->rankInSuit[partner[leadHand]][leadSuit] |
         bitMapRank[ trackp->move[0].rank ]))
      // Partner can win.
      suitAdd = (suitCount << 6) / 44;
    else
    {
      // Don't pitch from Kx.
      suitAdd = (suitCount << 6) / 36;
      if ((suitCount == 2) &&
          (posPoint->secondBest[suit].hand == currHand))
        suitAdd += -4;
    }

    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = -mply[k].rank + suitAdd;
  }
  else if (suit != trump)
  {
    // We discard on a side suit.

    if (posPoint->length[partner[leadHand]][leadSuit] != 0)
    {
      // 3rd hand will follow.
      if (posPoint->rankInSuit[rho[leadHand]][leadSuit] >
          (posPoint->rankInSuit[partner[leadHand]][leadSuit] |
           bitMapRank[ trackp->move[0].rank ]))
        // Partner has winning card.
        suitAdd = 60 + (suitCount << 6) / 44;
      else if ((posPoint->length[rho[leadHand]][leadSuit] == 0)
               && (posPoint->length[rho[leadHand]][trump] != 0))
        // Partner can ruff.
        suitAdd = 60 + (suitCount << 6) / 44;
      else
      {
        // FIX: No reason to differentiate here?
        suitAdd = -2 + (suitCount << 6) / 36;
        // Don't pitch from Kx.
        if ((suitCount == 2) &&
            (posPoint->secondBest[suit].hand == currHand))
          suitAdd += -4;
      }
    }
    else if ((posPoint->length[rho[leadHand]][leadSuit] == 0)
             && (posPoint->rankInSuit[rho[leadHand]][trump] >
                 posPoint->rankInSuit[partner[leadHand]][trump]))
      // Partner can overruff 3rd hand.
      suitAdd = 60 + (suitCount << 6) / 44;
    else if ((posPoint->length[partner[leadHand]][trump] == 0)
             && (posPoint->rankInSuit[rho[leadHand]][leadSuit] >
                 bitMapRank[ trackp->move[0].rank] ))
      // 3rd hand has no trumps, and partner has suit winner.
      suitAdd = 60 + (suitCount << 6) / 44;
    else
    {
      // FIX: No reason to differentiate here?
      suitAdd = -2 + (suitCount << 6) / 36;
      // Don't pitch from Kx.
      if ((suitCount == 2) &&
          (posPoint->secondBest[suit].hand == currHand))
        suitAdd += -4;
    }
    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = -mply[k].rank + suitAdd;
  }
  else if (posPoint->length[partner[leadHand]][leadSuit] != 0)
  {
    // 3rd hand follows suit while we ruff.
    // Could be ruffing partner's winner!
    suitAdd = (suitCount << 6) / 44;
    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = 24 - (mply[k].rank) + suitAdd;
  }
  else if ((posPoint->length[rho[leadHand]][leadSuit] == 0)
           && (posPoint->length[rho[leadHand]][trump] != 0) &&
           (posPoint->rankInSuit[rho[leadHand]][trump] >
            posPoint->rankInSuit[partner[leadHand]][trump]))
  {
    // Everybody is void, and partner can overruff.
    suitAdd = (suitCount << 6) / 44;
    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = 24 - (mply[k].rank) + suitAdd;
  }
  else
  {
    for (int k = lastNumMoves; k < numMoves; k++)
    {
      if (bitMapRank[mply[k].rank] >
          posPoint->rankInSuit[partner[leadHand]][trump])
      {
        // We can ruff, 3rd hand is void but can't overruff.
        suitAdd = (suitCount << 6) / 44;
        mply[k].weight = 24 - (mply[k].rank) + suitAdd;
      }
      else
      {
        // We're getting overruffed. Make trick costly for opponents.
        suitAdd = (suitCount << 6) / 36;
        // Don't ruff from Kx.
        if ((suitCount == 2) &&
            (posPoint->secondBest[suit].hand == currHand))
          suitAdd += -4;
        mply[k].weight = 15 - (mply[k].rank) + suitAdd;
      }
    }
  }
}


void Moves::WeightAllocNTVoid1(
  pos * posPoint)
{
  // FIX:
  // Why the different penalties depending on partner?

  if (posPoint->rankInSuit[rho[leadHand] ][leadSuit] >
      (posPoint->rankInSuit[partner[leadHand]][leadSuit] |
       bitMapRank[ trackp->move[0].rank ]))
  {
    // Partner can win.
    unsigned short suitCount = posPoint->length[currHand][suit];
    int suitAdd = (suitCount << 6) / 23;
    // Discourage pitch from Kx or A stiff.
    if (suitCount == 2 && posPoint->secondBest[suit].hand == currHand)
      suitAdd += -2;
    else if ((suitCount == 1) &&
             (posPoint->winner[suit].hand == currHand))
      suitAdd += -3;

    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = -mply[k].rank + suitAdd;
  }
  else
  {
    unsigned short suitCount = posPoint->length[currHand][suit];
    int suitAdd = (suitCount << 6) / 33;

    // Discourage pitch from Kx.
    if ((suitCount == 2) &&
        (posPoint->secondBest[suit].hand == currHand))
      suitAdd += -6;

    /* Discourage suit discard of highest card. */
    else if ((suitCount == 1) &&
             (posPoint->winner[suit].hand == currHand))
      suitAdd += -8;

    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = -mply[k].rank + suitAdd;
  }
}


void Moves::WeightAllocTrumpNotvoid2(
  pos * posPoint)
{
  int cards4th = posPoint->rankInSuit[rho[leadHand]][leadSuit];
  int max4th = highestRank[cards4th];
  int min4th = lowestRank [cards4th];
  int max3rd = mply[0].rank;

  if (leadSuit == trump)
  {
    if (trackp->high[1] == 0 && trackp->move[0].rank > max4th)
    {
      // Partner has already beat his LHO and will beat his RHO.
      for (int k = 0; k < numMoves; k++)
        mply[k].weight = -mply[k].rank;
      return;
    }
    else if (max3rd < min4th || max3rd < trackp->move[1].rank)
    {
      // Our cards are too low to matter.
      for (int k = 0; k < numMoves; k++)
        mply[k].weight = -mply[k].rank;
      return;
    }
    else if (max3rd > max4th)
    {
      // We can win the trick.
      for (int k = 0; k < numMoves; k++)
      {
        if (mply[k].rank > max4th &&
            mply[k].rank > trackp->move[1].rank)
          mply[k].weight = 58 - mply[k].rank;
        else
          mply[k].weight = -mply[k].rank;
      }
    }
    else
    {
      // Figure out how high we have to play to force out the top.
      int kBonus = RankForcesAce(cards4th);

      for (int k = 0; k < numMoves; k++)
        mply[k].weight = -mply[k].rank;

      if (kBonus != -1) // Force out ace
        mply[kBonus].weight += 20;
      return;
    }
  }

  else if (trackp->move[1].suit == trump)
  {
    // 2nd hand ruffs, and we must follow suit.
    for (int k = 0; k < numMoves; k++)
      mply[k].weight = -mply[k].rank;
    return;
  }

  // So now leadSuit != trump and second hand didn't ruff.
  else if (trackp->high[1] == 0)
  {
    // Partner is winning so far.
    if (max4th == 0)
    {
      // 4th hand is either ruffing or not -- play low.
      for (int k = 0; k < numMoves; k++)
        mply[k].weight = -mply[k].rank;
      return;
    }

    // So 4th hand follows.
    else if (trackp->move[0].rank > max4th)
    {
      // Partner is already winning.
      for (int k = 0; k < numMoves; k++)
        mply[k].weight = -mply[k].rank;
      return;
    }

    else if (max3rd < min4th || max3rd < trackp->move[1].rank)
    {
      // Our cards are too low to matter.
      for (int k = 0; k < numMoves; k++)
        mply[k].weight = -mply[k].rank;
      return;
    }

    // So 4th hand can beat partner in the suit.
    else if (max3rd > max4th)
    {
      // We can win the trick.
      for (int k = 0; k < numMoves; k++)
      {
        if (mply[k].rank > max4th)
          mply[k].weight = 58 - mply[k].rank;
        else
          mply[k].weight = -mply[k].rank;
      }
    }
    else
    {
      // We can't win the trick.
      // Figure out how high we have to play to force out the top.
      int kBonus = RankForcesAce(cards4th);

      for (int k = 0; k < numMoves; k++)
      {
        if (mply[k].rank > trackp->move[1].rank &&
            mply[k].rank > max4th) // We will win
          mply[k].weight = 60 - mply[k].rank;

        else
          mply[k].weight = -mply[k].rank;
      }

      if (kBonus != -1) // Force out ace
        mply[kBonus].weight += 20;
    }
  }
  else
  {
    // 2nd hand is winning so far. 4th hand is either ruffing
    // or not -- play high enough to beat 2nd hand.
    if (max4th == 0)
    {
      for (int k = 0; k < numMoves; k++)
      {
        if (mply[k].rank > trackp->move[1].rank)
          mply[k].weight = 20 - mply[k].rank;
        else
          mply[k].weight = -mply[k].rank;
      }
      return;
    }

    // Our cards are too low to matter.
    else if (max3rd < min4th || max3rd < trackp->move[1].rank)
    {
      for (int k = 0; k < numMoves; k++)
        mply[k].weight = -mply[k].rank;
      return;
    }

    // We can win the trick.
    else if (max3rd > max4th)
    {
      for (int k = 0; k < numMoves; k++)
      {
        if (mply[k].rank > trackp->move[1].rank &&
            mply[k].rank > max4th)
          mply[k].weight = 58 - mply[k].rank;
        else
          mply[k].weight = -mply[k].rank;
      }
      return;
    }

    // Figure out how high we have to play to force out the top.
    int kBonus = RankForcesAce(cards4th);

    for (int k = 0; k < numMoves; k++)
    {
      if (mply[k].rank > trackp->move[1].rank &&
          mply[k].rank > max4th) // We will win
        mply[k].weight = 60 - mply[k].rank;

      else
        mply[k].weight = -mply[k].rank;
    }

    if (kBonus != -1) // Force out ace
      mply[kBonus].weight += 20;
  }
}


int Moves::RankForcesAce(
  int cards4th)
{
  // Figure out how high we have to play to force out the top.
  moveGroupType * mp = &groupData[cards4th];

  int g = mp->lastGroup;
  int removed = static_cast<int>(trackp->removedRanks[leadSuit]);

  while (g >= 1 && ((mp->gap[g] & removed) == mp->gap[g]))
    g--;

  if (! g)
    return -1;

  // RHO's second-highest rank.
  int secondRHO = (g == 0 ? 0 : mp->rank[g - 1]);

  if (secondRHO > trackp->move[1].rank)
  {
    // Try to force out the top as cheaply as possible.
    int k = 0;
    while (k < numMoves && mply[k].rank > secondRHO)
      k++;

    if (k)
      return k - 1;
  }
  else if (trackp->high[1] == 1)
  {
    // Try to beat 2nd hand as cheaply as possible.
    int k = 0;
    while (k < numMoves && mply[k].rank > trackp->move[1].rank)
      k++;

    if (k)
      return k - 1;
  }

  return -1;
}


void Moves::GetTopNumber(
  int ris,
  int prank,
  int * topNumber,
  int * mno)
{
  * topNumber = -10;

  // Find the lowest move that still overtakes partner's card.
  *mno = 0;
  while (*mno < numMoves - 1 && mply[1 + *mno].rank > prank)
    (*mno)++;

  moveGroupType * mp = &groupData[ris];
  int g = mp->lastGroup;

  // Remove partner's card as well.
  int removed = static_cast<int>(trackp->removedRanks[leadSuit] |
                                 bitMapRank[prank]);

  int fullseq = mp->fullseq[g];

  while (g >= 1 && ((mp->gap[g] & removed) == mp->gap[g]))
    fullseq |= mp->fullseq[--g];

  *topNumber = counttable[fullseq] - 1;
}



void Moves::WeightAllocNTNotvoid2(
  pos * posPoint)
{
  // One of the main remaining issues here is cashing out long
  // suits. Examples:
  // AKJ opposite Q, overtake.
  // KQx opposite Jxxxx, don't block on the ace.
  // KJTx opposite 9 with Qx in dummy, do win the T.

  int cards4th = posPoint->rankInSuit[rho[leadHand]][leadSuit];
  int max4th = highestRank[cards4th];
  int min4th = lowestRank [cards4th];
  int max3rd = mply[0].rank;

  if (trackp->high[1] == 0 && trackp->move[0].rank > max4th)
  {
    // Partner has already beat his LHO and will beat his RHO.
    // Generally we play low and let partner win.
    for (int k = 0; k < numMoves; k++)
      mply[k].weight = -mply[k].rank;

    // This doesn't help much, not sure why. It does work.

    // if (0 && posPoint->length[leadHand][leadSuit] == 0 &&
    if (posPoint->length[leadHand][leadSuit] == 0 &&
        posPoint->winner[leadSuit].hand == currHand)
    {
      // Partner has a singleton, and we have the ace.
      // Maybe we should overtake to run the suit.
      int oppLen = posPoint->length[rho[leadHand]][leadSuit] - 1;
      int lhoLen = posPoint->length[lho[leadHand]][leadSuit];
      if (lhoLen > oppLen)
        oppLen = lhoLen;

      int topNumber, mno;
      GetTopNumber(posPoint->rankInSuit[partner[leadHand]][leadSuit],
                   trackp->move[0].rank, &topNumber, &mno);

      if (oppLen <= topNumber)
        mply[mno].weight += 20;
    }
    return;
  }
  else if (max3rd < min4th || max3rd < trackp->move[1].rank)
  {
    // Our cards are too low to matter.
    for (int k = 0; k < numMoves; k++)
      mply[k].weight = -mply[k].rank;
    return;
  }

  int kBonus = -1;
  if (max4th > max3rd && max4th > trackp->move[1].rank)
    kBonus = RankForcesAce(cards4th);

  for (int k = 0; k < numMoves; k++)
  {
    if (mply[k].rank > trackp->move[1].rank &&
        mply[k].rank > max4th) // We will win
      mply[k].weight = 60 - mply[k].rank;

    else
      mply[k].weight = -mply[k].rank;
  }

  if (kBonus != -1) // Force out ace
    mply[kBonus].weight += 20;
}


void Moves::WeightAllocTrumpVoid2(
  pos * posPoint)
{
  // Compared to "v2.8":
  // Moved a test for partner's win out of the k loop.

  int suitAdd;
  unsigned short suitCount = posPoint->length[currHand][suit];
  int max4th = highestRank[
                 posPoint->rankInSuit[rho[leadHand]][leadSuit] ];

  if (leadSuit == trump || suit != trump)
  {
    // Discard small from a long suit.
    suitAdd = (suitCount << 6) / 40;
    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = -mply[k].rank + suitAdd;
    return;
  }

  else if (trackp->high[1] == 0 && trackp->move[0].rank > max4th &&
           (max4th != 0 || posPoint->length[rho[leadHand]][trump] == 0))
  {
    // Partner already beat 2nd and 4th hands.
    // Don't overruff partner's sure winner.
    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = -mply[k].rank - 50;
    return;
  }

  // So now we're ruffing and partner is not already sure to win.

  for (int k = lastNumMoves; k < numMoves; k++)
  {
    if (trackp->move[1].suit == trump &&
        mply[k].rank < trackp->move[1].rank)
    {
      // Don't underruff.
      int rRank = relRank[ posPoint->aggr[suit] ][mply[k].rank];
      suitAdd = (suitCount << 6) / 40;
      mply[k].weight = -32 + rRank + suitAdd;
    }

    else if (trackp->high[1] == 0)
    {
      // We ruff partner's winner over 2nd hand.
      if (max4th != 0)
      {
        if (posPoint->secondBest[leadSuit].hand == leadHand)
        {
          // We'd like to know whether partner has KQ or just K,
          // but that information takes a bit of diggging. It's
          // easier just not to ruff the king.
          suitAdd = (suitCount << 6) / 50;
          mply[k].weight = 36 - mply[k].rank + suitAdd;
        }
        else
        {
          suitAdd = (suitCount << 6) / 50;
          mply[k].weight = 48 - mply[k].rank + suitAdd;
        }
      }
      else if (bitMapRank[mply[k].rank] >
               posPoint->rankInSuit[rho[leadHand]][trump])
      {
        // We ruff higher than 4th hand.
        suitAdd = (suitCount << 6) / 50;
        mply[k].weight = 48 - mply[k].rank + suitAdd;
      }
      else
      {
        // Force out a higher trump in 4th hand.
        suitAdd = (suitCount << 6) / 50;
        mply[k].weight = -12 - mply[k].rank + suitAdd;
      }
    }

    // 2nd hand was winning before we ruffed.
    else if (max4th != 0)
    {
      // Just ruff low.
      suitAdd = (suitCount << 6) / 50;
      mply[k].weight = 72 - mply[k].rank + suitAdd;
    }

    else if (bitMapRank[mply[k].rank] >
             posPoint->rankInSuit[rho[leadHand]][trump])
    {
      // Ruff higher than 4th hand can.
      suitAdd = (suitCount << 6) / 50;
      mply[k].weight = 48 - mply[k].rank + suitAdd;
    }

    else
    {
      // Force out a higher trump in 4th hand.
      suitAdd = (suitCount << 6) / 50;
      mply[k].weight = 36 - mply[k].rank + suitAdd;
    }
  }
}


void Moves::WeightAllocNTVoid2(
  pos * posPoint)
{
  // Compared to "v2.8":
  // Took only the second branch. The first branch (partner
  // has beat his LHO and will beat his RHO) was a bit different,
  // for no reason that I could see. This is the same or a tiny
  // bit better.

  unsigned short suitCount = posPoint->length[currHand][suit];
  int suitAdd = (suitCount << 6) / 24;

  // Try not to pitch from Kx or stiff ace.
  if ((suitCount == 2) &&
      (posPoint->secondBest[suit].hand == currHand))
    suitAdd -= 4;
  else if ((suitCount == 1) &&
           (posPoint->winner[suit].hand == currHand))
    suitAdd -= 4;

  for (int k = lastNumMoves; k < numMoves; k++)
    mply[k].weight = -(mply[k].rank) + suitAdd;
}


void Moves::WeightAllocCombinedNotvoid3(
  pos * posPoint)
// moveType mply[])
{
  // We're always following suit.
  // This function is very good, but occasionally it is better
  // to beat partner's card in order to cash out a suit in NT.

  if (trackp->high[2] == 1 ||
      (leadSuit != trump && trackp->move[2].suit == trump))
  {
    // Partner is winning the trick so far, or an opponent
    // has ruffed while we must follow. Play low.

    for (int k = 0; k < numMoves; k++)
      mply[k].weight = -mply[k].rank;
  }
  else
  {
    // We're losing so far, and either trumps were led or
    // trumps don't matter in this trick.

    for (int k = 0; k < numMoves; k++)
    {
      if (mply[k].rank > trackp->move[2].rank)
        // Win as cheaply as possible.
        mply[k].weight = 30 - mply[k].rank;
      else
        mply[k].weight = -mply[k].rank;
    }
  }
  UNUSED(posPoint);
}


void Moves::WeightAllocTrumpVoid3(
  pos * posPoint)
// moveType mply[])
{
  // Compared to "v2.8":
  // val removed for trump plays (doesn't really matter, though).

  // To consider:
  // rRank vs rank

  // Don't pitch from Kx or stiff ace.
  int mylen = posPoint->length[currHand][suit];
  int val = (mylen << 6) / 24;
  if ((mylen == 2) && (posPoint->secondBest[suit].hand == currHand))
    val -= 2;
  // else if ((mylen == 1) &&
  // (posPoint->winner[suit].hand == currHand))
  // val -= 4;

  if (leadSuit == trump)
  {
    // We're not following suit, so no hope.
    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = -mply[k].rank + val;
  }
  else if (trackp->high[2] == 1) // Partner is winning so far
  {
    if (suit == trump) // Don't ruff
      for (int k = lastNumMoves; k < numMoves; k++)
        mply[k].weight = 2 - mply[k].rank + val;

    else // Discard from a long suit
      for (int k = lastNumMoves; k < numMoves; k++)
        mply[k].weight = 25 - mply[k].rank + val;
  }
  else if (trackp->move[2].suit == trump) // They've ruffed
  {
    if (suit == trump)
    {
      for (int k = lastNumMoves; k < numMoves; k++)
      {
        int rRank = relRank[ posPoint->aggr[suit] ][mply[k].rank];
        if (mply[k].rank > trackp->move[2].rank)
          mply[k].weight = 33 + rRank; // Overruff
        else
          mply[k].weight = -13 + rRank; // Underruff
      }
    }
    else // We discard
      for (int k = lastNumMoves; k < numMoves; k++)
        mply[k].weight = 14 - (mply[k].rank) + val;
  }
  else if (suit == trump) // We ruff and win
  {
    for (int k = lastNumMoves; k < numMoves; k++)
    {
      int rRank = relRank[ posPoint->aggr[suit] ][mply[k].rank];
      mply[k].weight = 33 + rRank;
    }
  }
  else // We discard and lose
  {
    for (int k = lastNumMoves; k < numMoves; k++)
      mply[k].weight = 14 - mply[k].rank + val;
  }
}


void Moves::WeightAllocNTVoid3(
  pos * posPoint)
// moveType mply[])
{
  int mylen = posPoint->length[currHand][suit];
  int val = (mylen << 6) / 27;
  // Try not to pitch from Kx, or to pitch a singleton winner.
  if ((mylen == 2) && (posPoint->secondBest[suit].hand == currHand))
    val -= 6;
  else if ((mylen == 1) && (posPoint->winner[suit].hand == currHand))
    val -= 8;

  for (int k = lastNumMoves; k < numMoves; k++)
    mply[k].weight = - mply[k].rank + val;
}


inline bool Moves::WinningMove(
  moveType * mvp1,
  extCard * mvp2,
  int ourTrump)
{
  /* Return true if move 1 wins over move 2, with the assumption that
  move 2 is the presently winning card of the trick */

  if (mvp1->suit == mvp2->suit)
  {
    if ((mvp1->rank) > (mvp2->rank))
      return true;
    else
      return false;
  }
  else if ((mvp1->suit) == ourTrump)
    return true;
  else
    return false;
}



int Moves::GetLength(
  int trick,
  int relHand)
{
  return moveList[trick][relHand].last + 1;
}


void Moves::MakeSpecific(
  moveType * ourMply,
  int trick,
  int relHand)
{
  trackp = &track[trick];

  if (relHand == 0)
  {
    trackp->move[0].suit = ourMply->suit;
    trackp->move[0].rank = ourMply->rank;
    trackp->move[0].sequence = ourMply->sequence;
    trackp->high[0] = 0;

    trackp->leadSuit = ourMply->suit;
  }
  else if (ourMply->suit == trackp->move[relHand - 1].suit)
  {
    if (ourMply->rank > trackp->move[relHand - 1].rank)
    {
      trackp->move[relHand].suit = ourMply->suit;
      trackp->move[relHand].rank = ourMply->rank;
      trackp->move[relHand].sequence = ourMply->sequence;
      trackp->high[relHand] = relHand;
    }
    else
    {
      trackp->move[relHand] = trackp->move[relHand - 1];
      trackp->high[relHand] = trackp->high[relHand - 1];
    }
  }
  else if (ourMply->suit == trump)
  {
    trackp->move[relHand].suit = ourMply->suit;
    trackp->move[relHand].rank = ourMply->rank;
    trackp->move[relHand].sequence = ourMply->sequence;
    trackp->high[relHand] = relHand;
  }
  else
  {
    trackp->move[relHand] = trackp->move[relHand - 1];
    trackp->high[relHand] = trackp->high[relHand - 1];
  }

  trackp->playSuits[relHand] = ourMply->suit;
  trackp->playRanks[relHand] = ourMply->rank;

  if (relHand == 3)
  {
    trackType * newp = &track[trick - 1];

    newp->leadHand = (trackp->leadHand + trackp->high[3]) % 4;

    int r, s;
    for (s = 0; s < DDS_SUITS; s++)
      newp->removedRanks[s] = trackp->removedRanks[s];

    for (int h = 0; h < DDS_HANDS; h++)
    {
      r = trackp->playRanks[h];
      s = trackp->playSuits[h];
      newp->removedRanks[s] |= bitMapRank[r];
    }
  }
}


moveType * Moves::MakeNext(
  int trick,
  int relHand,
  unsigned short int ourWinRanks[DDS_SUITS])
{
  // Find moves that are >= ourWinRanks[suit], but allow one
  // "small" move per suit.

  int * lwp = track[trick].lowestWin[relHand];
  movePlyType * listp = &moveList[trick][relHand];
  trackp = &track[trick];

  moveType * currp = nullptr, * prevp;

  bool found = false;
  if (listp->last == -1)
    return NULL;
  else if (listp->current == 0)
  {
    currp = &listp->move[0];
    found = true;
  }
  else
  {
    prevp = &listp->move[ listp->current - 1 ];
    if (lwp[ prevp->suit ] == 0)
    {
      int low = lowestRank[ ourWinRanks[prevp->suit] ];
      if (low == 0)
        low = 15;
      if (prevp->rank < low)
        lwp[ prevp->suit ] = low;
    }

    while (listp->current <= listp->last && ! found)
    {
      currp = &listp->move[ listp->current ];
      if (currp->rank >= lwp[ currp->suit ])
        found = true;
      else
        listp->current++;
    }

    if (! found)
      return NULL;
  }

  if (relHand == 0)
  {
    trackp->move[0].suit = currp->suit;
    trackp->move[0].rank = currp->rank;
    trackp->move[0].sequence = currp->sequence;
    trackp->high[0] = 0;

    trackp->leadSuit = currp->suit;
  }
  else if (currp->suit == trackp->move[relHand - 1].suit)
  {
    if (currp->rank > trackp->move[relHand - 1].rank)
    {
      trackp->move[relHand].suit = currp->suit;
      trackp->move[relHand].rank = currp->rank;
      trackp->move[relHand].sequence = currp->sequence;
      trackp->high[relHand] = relHand;
    }
    else
    {
      trackp->move[relHand] = trackp->move[relHand - 1];
      trackp->high[relHand] = trackp->high[relHand - 1];
    }
  }
  else if (currp->suit == trump)
  {
    trackp->move[relHand].suit = currp->suit;
    trackp->move[relHand].rank = currp->rank;
    trackp->move[relHand].sequence = currp->sequence;
    trackp->high[relHand] = relHand;
  }
  else
  {
    trackp->move[relHand] = trackp->move[relHand - 1];
    trackp->high[relHand] = trackp->high[relHand - 1];
  }

  trackp->playSuits[relHand] = currp->suit;
  trackp->playRanks[relHand] = currp->rank;

  if (relHand == 3)
  {
    trackType * newp = &track[trick - 1];

    newp->leadHand = (trackp->leadHand + trackp->high[3]) % 4;

    int r, s;
    for (s = 0; s < DDS_SUITS; s++)
      newp->removedRanks[s] = trackp->removedRanks[s];

    for (int h = 0; h < DDS_HANDS; h++)
    {
      r = trackp->playRanks[h];
      s = trackp->playSuits[h];
      newp->removedRanks[s] |= bitMapRank[r];
    }
  }

  listp->current++;
  return currp;
}


moveType * Moves::MakeNextSimple(
  int trick,
  int relHand)
{
  // Don't worry about small moves. Why not, actually?

  movePlyType * listp = &moveList[trick][relHand];
  if (listp->current > listp->last)
    return NULL;

  moveType * currp = &listp->move[ listp->current ];

  trackp = &track[trick];

  if (relHand == 0)
  {
    trackp->move[0].suit = currp->suit;
    trackp->move[0].rank = currp->rank;
    trackp->move[0].sequence = currp->sequence;
    trackp->high[0] = 0;

    trackp->leadSuit = currp->suit;
  }
  else if (currp->suit == trackp->move[relHand - 1].suit)
  {
    if (currp->rank > trackp->move[relHand - 1].rank)
    {
      trackp->move[relHand].suit = currp->suit;
      trackp->move[relHand].rank = currp->rank;
      trackp->move[relHand].sequence = currp->sequence;
      trackp->high[relHand] = relHand;
    }
    else
    {
      trackp->move[relHand] = trackp->move[relHand - 1];
      trackp->high[relHand] = trackp->high[relHand - 1];
    }
  }
  else if (currp->suit == trump)
  {
    trackp->move[relHand].suit = currp->suit;
    trackp->move[relHand].rank = currp->rank;
    trackp->move[relHand].sequence = currp->sequence;
    trackp->high[relHand] = relHand;
  }
  else
  {
    trackp->move[relHand] = trackp->move[relHand - 1];
    trackp->high[relHand] = trackp->high[relHand - 1];
  }

  trackp->playSuits[relHand] = currp->suit;
  trackp->playRanks[relHand] = currp->rank;

  if (relHand == 3)
  {
    trackType * newp = &track[trick - 1];
    newp->leadHand = (trackp->leadHand + trackp->high[3]) % 4;
  }

  listp->current++;
  return currp;
}


void Moves::Step(
  int tricks,
  int relHand)
{
  moveList[tricks][relHand].current++;
}


void Moves::Rewind(
  int tricks,
  int relHand)
{
  moveList[tricks][relHand].current = 0;
}


void Moves::Purge(
  int trick,
  int ourLeadHand,
  moveType forbiddenMoves[])
{
  movePlyType * ourMply = &moveList[trick][ourLeadHand];

  for (int k = 1; k <= 13; k++)
  {
    int s = forbiddenMoves[k].suit;
    int rank = forbiddenMoves[k].rank;
    if (rank == 0) continue;

    for (int r = 0; r <= ourMply->last; r++)
    {
      if (s == ourMply->move[r].suit &&
          rank == ourMply->move[r].rank)
      {
        /* For the forbidden move r: */
        for (int n = r; n <= ourMply->last; n++)
          ourMply->move[n] = ourMply->move[n + 1];
        ourMply->last--;
      }
    }
  }
}


void Moves::Reward(
  int tricks,
  int relHand)
{
  moveList[tricks][relHand].
  move[ moveList[tricks][relHand].current - 1 ].weight += 100;
}


trickDataType * Moves::GetTrickData(
  int tricks)
{
  trickDataType * datap = &track[tricks].trickData;
  for (int s = 0; s < DDS_SUITS; s++)
    datap->playCount[s] = 0;
  for (int relh = 0; relh < DDS_HANDS; relh++)
    datap->playCount[ trackp->playSuits[relh] ]++;

  int sum = 0;
  for (int s = 0; s < DDS_SUITS; s++)
    sum += datap->playCount[s];
  if (sum != 4)
  {
    printf("Sum %d is not four\n", sum);
    exit(1);
  }

  datap->bestRank = trackp->move[3].rank;
  datap->bestSuit = trackp->move[3].suit;
  datap->bestSequence = trackp->move[3].sequence;
  datap->relWinner = trackp->high[3];
  return datap;
}


void Moves::Sort(
  int tricks,
  int relHand)
{
  numMoves = moveList[tricks][relHand].last + 1;
  mply = moveList[tricks][relHand].move;
  Moves::MergeSort();
}


void Moves::MergeSort()
{
  moveType tmp;

  switch (numMoves)
  {
    case 12:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(4, 5);
      CMP_SWAP_NEW(6, 7);
      CMP_SWAP_NEW(8, 9);
      CMP_SWAP_NEW(10, 11);

      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(5, 7);
      CMP_SWAP_NEW(9, 11);

      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(4, 6);
      CMP_SWAP_NEW(8, 10);

      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(5, 6);
      CMP_SWAP_NEW(9, 10);

      CMP_SWAP_NEW(1, 5);
      CMP_SWAP_NEW(6, 10);
      CMP_SWAP_NEW(5, 9);
      CMP_SWAP_NEW(2, 6);
      CMP_SWAP_NEW(1, 5);
      CMP_SWAP_NEW(6, 10);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(7, 11);
      CMP_SWAP_NEW(3, 7);
      CMP_SWAP_NEW(4, 8);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(7, 11);
      CMP_SWAP_NEW(1, 4);
      CMP_SWAP_NEW(7, 10);
      CMP_SWAP_NEW(3, 8);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(8, 9);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(7, 9);
      CMP_SWAP_NEW(3, 5);
      CMP_SWAP_NEW(6, 8);
      CMP_SWAP_NEW(3, 4);
      CMP_SWAP_NEW(5, 6);
      CMP_SWAP_NEW(7, 8);
      break;
    case 11:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(4, 5);
      CMP_SWAP_NEW(6, 7);
      CMP_SWAP_NEW(8, 9);

      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(5, 7);
      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(4, 6);
      CMP_SWAP_NEW(8, 10);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(5, 6);
      CMP_SWAP_NEW(9, 10);
      CMP_SWAP_NEW(1, 5);
      CMP_SWAP_NEW(6, 10);
      CMP_SWAP_NEW(5, 9);
      CMP_SWAP_NEW(2, 6);
      CMP_SWAP_NEW(1, 5);
      CMP_SWAP_NEW(6, 10);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(3, 7);
      CMP_SWAP_NEW(4, 8);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(1, 4);
      CMP_SWAP_NEW(7, 10);
      CMP_SWAP_NEW(3, 8);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(8, 9);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(7, 9);
      CMP_SWAP_NEW(3, 5);
      CMP_SWAP_NEW(6, 8);
      CMP_SWAP_NEW(3, 4);
      CMP_SWAP_NEW(5, 6);
      CMP_SWAP_NEW(7, 8);
      break;
    case 10:
      CMP_SWAP_NEW(1, 8);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(5, 9);
      CMP_SWAP_NEW(2, 6);
      CMP_SWAP_NEW(3, 7);
      CMP_SWAP_NEW(0, 3);
      CMP_SWAP_NEW(6, 9);
      CMP_SWAP_NEW(2, 5);
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(3, 6);
      CMP_SWAP_NEW(8, 9);
      CMP_SWAP_NEW(4, 7);
      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(4, 8);
      CMP_SWAP_NEW(1, 5);
      CMP_SWAP_NEW(7, 9);

      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(3, 4);
      CMP_SWAP_NEW(5, 6);
      CMP_SWAP_NEW(7, 8);

      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(6, 8);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(5, 7);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(6, 7);
      CMP_SWAP_NEW(3, 5);
      CMP_SWAP_NEW(4, 6);
      CMP_SWAP_NEW(4, 5);
      break;
    case 9:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(3, 4);
      CMP_SWAP_NEW(6, 7);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(4, 5);
      CMP_SWAP_NEW(7, 8);
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(3, 4);
      CMP_SWAP_NEW(6, 7);
      CMP_SWAP_NEW(0, 3);
      CMP_SWAP_NEW(3, 6);
      CMP_SWAP_NEW(0, 3);
      CMP_SWAP_NEW(1, 4);
      CMP_SWAP_NEW(4, 7);
      CMP_SWAP_NEW(1, 4);
      CMP_SWAP_NEW(2, 5);
      CMP_SWAP_NEW(5, 8);
      CMP_SWAP_NEW(2, 5);
      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(5, 7);
      CMP_SWAP_NEW(2, 6);
      CMP_SWAP_NEW(4, 6);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(5, 6);
      break;
    case 8:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(4, 5);
      CMP_SWAP_NEW(6, 7);

      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(4, 6);
      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(5, 7);

      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(5, 6);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(1, 5);

      CMP_SWAP_NEW(2, 6);
      CMP_SWAP_NEW(3, 7);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(3, 5);

      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(3, 4);
      CMP_SWAP_NEW(5, 6);
      break;
    case 7:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(4, 5);
      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(4, 6);
      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(5, 6);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(1, 5);
      CMP_SWAP_NEW(2, 6);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(3, 5);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(3, 4);
      CMP_SWAP_NEW(5, 6);
      break;
    case 6:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(4, 5);
      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(1, 5);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(3, 5);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(3, 4);
      break;
    case 5:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(0, 4);
      CMP_SWAP_NEW(2, 4);
      CMP_SWAP_NEW(1, 2);
      CMP_SWAP_NEW(3, 4);
      break;
    case 4:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(2, 3);
      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(1, 3);
      CMP_SWAP_NEW(1, 2);
      break;
    case 3:
      CMP_SWAP_NEW(0, 1);
      CMP_SWAP_NEW(0, 2);
      CMP_SWAP_NEW(1, 2);
      break;
    case 2:
      CMP_SWAP_NEW(0, 1);
      break;
    default:
      for (int i = 1; i < numMoves; i++)
      {
        tmp = mply[i];
        int j = i;
        for (; j && tmp.weight > mply[j - 1].weight ; --j)
          mply[j] = mply[j - 1];
        mply[j] = tmp;
      }
  }

  return;
}


void Moves::PrintMove(
  movePlyType * ourMply)
{
  printf("current %d, last %d\n", ourMply->current, ourMply->last);
  printf(" i suit sequence rank wgt\n");
  for (int i = 0; i <= ourMply->last; i++)
  {
    printf("%2d %2c %08x %2c %2d\n",
           i,
           cardSuit[ ourMply->move[i].suit ],
           ourMply->move[i].sequence,
           cardRank[ ourMply->move[i].rank ],
           ourMply->move[i].weight);
  }
}


void Moves::PrintMoves(
  int trick,
  int relHand)
{
  movePlyType * listp = &moveList[trick][relHand];
  printf("trick %d relHand %d last %d current %d\n",
         trick, relHand, listp->last, listp->current);
  Moves::PrintMove(listp);
}


void Moves::TrickToText(
  int trick,
  char line[])
{
  movePlyType * listp0 = &moveList[trick][0],
                * listp1 = &moveList[trick][1],
                  * listp2 = &moveList[trick][2],
                    * listp3 = &moveList[trick][3];

  sprintf(line, "Last trick\t%c: %c%c - %c%c - %c%c - %c%c\n",
          cardHand[ track[trick].leadHand ],
          cardSuit[ listp0->move[listp0->current].suit ],
          cardRank[ listp0->move[listp0->current].rank ],

          cardSuit[ listp1->move[listp1->current].suit ],
          cardRank[ listp1->move[listp1->current].rank ],

          cardSuit[ listp2->move[listp2->current].suit ],
          cardRank[ listp2->move[listp2->current].rank ],

          cardSuit[ listp3->move[listp3->current].suit ],
          cardRank[ listp3->move[listp3->current].rank ]);
}



void Moves::UpdateStatsEntry(
  moveStatsType * statp,
  int findex,
  int hit,
  int len)
{
  bool found = false;
  int fno = 0;
  for (int i = 0; i < statp->nfuncs; i++)
  {
    if (statp->list[i].findex == findex)
    {
      found = true;
      fno = i;
      break;
    }
  }

  moveStatType * funp;
  if (found)
  {
    funp = &statp->list[fno];
    funp->count++;
    funp->sumHits += hit;
    funp->sumLengths += len;
  }
  else
  {
    if (statp->nfuncs >= MG_NUM_FUNCTIONS)
    {
      printf("Shouldn't happen, %d\n", statp->nfuncs);
      for (int i = 0; i < statp->nfuncs; i++)
        printf("%d %d\n", i, statp->list[i].findex);
      exit(1);
    }

    funp = &statp->list[ statp->nfuncs++ ];

    funp->count++;
    funp->findex = findex;
    funp->sumHits += hit;
    funp->sumLengths += len;
  }
}


void Moves::RegisterHit(
  int trick,
  int relHand)
{
  movePlyType * listp = &moveList[trick][relHand];

  int findex = lastCall[trick][relHand];
  int len = listp->last + 1;

  if (findex == -1)
  {
    printf("RegisterHit trick %d relHand %d: findex %d\n",
           trick, relHand, -1);
    exit(1);
  }

  int curr = listp->current;
  if (curr < 1 || curr > len)
  {
    printf("current out of bounds\n");
    exit(1);
  }

  int moveSuit = listp->move[curr - 1].suit;
  int numSuit = 0;
  int numSeen = 0;

  for (int i = 0; i < len; i++)
  {
    if (listp->move[i].suit == moveSuit)
    {
      numSuit++;
      if (i == curr - 1)
        numSeen = numSuit;
    }
  }

// Used for development
#if 0
  if (findex == MG_COMB_NOTVOID3 && numSeen > 1 && trick >= 10)
    // if (0 && findex == MG_TRUMP_VOID3 && numSeen == 1 && curr > 1)
  {
    char text[12][80];
    RankToText(posPoint->rankInSuit, text);

    for (int i = 0; i < 12; i++)
      printf("%s\n", text[i]);
    printf("\n", text);

    trackp = &track[trick];

    printf("Trumps %c, Play %c: %c%c - %c%c - %c%c, best no. %d\n\n",
           // printf("Trumps %c, Play %c: %c%c - %c%c, best no. %d\n\n",
           // printf("Trumps %c, Play %c: %c%c, best no. %d\n\n",
           cardSuit[trump],
           cardHand[leadHand],
           cardSuit[ trackp->playSuits[0] ],
           cardRank[ trackp->playRanks[0] ],
           cardSuit[ trackp->playSuits[1] ],
           cardRank[ trackp->playRanks[1] ],
           cardSuit[ trackp->playSuits[2] ],
           cardRank[ trackp->playRanks[2] ],
           curr - 1);

    Moves::PrintMoves(trick, relHand);
    printf("\n---------------------------------------\n\n");
  }
#endif

  // Now we know enough to update the statistics tables.

  trickTable[trick][relHand].count++;
  trickTable[trick][relHand].sumHits += curr;
  trickTable[trick][relHand].sumLengths += len;

  trickSuitTable[trick][relHand].count++;
  trickSuitTable[trick][relHand].sumHits += numSeen;
  trickSuitTable[trick][relHand].sumLengths += numSuit;

  Moves::UpdateStatsEntry(&trickDetailTable[trick][relHand],
                          findex, curr, len);

  Moves::UpdateStatsEntry(&trickDetailSuitTable[trick][relHand],
                          findex, numSeen, numSuit);

  Moves::UpdateStatsEntry(&trickFuncTable,
                          findex, curr, len);

  Moves::UpdateStatsEntry(&trickFuncSuitTable,
                          findex, numSeen, numSuit);
}


char * Moves::AverageString(
  moveStatType * statp,
  char str[])
{
  if (statp->count == 0)
    sprintf(str, "%5s %4s", "--", "--");
  else
    sprintf(str, "%5.2f %4.1f",
            statp->sumHits / static_cast<double>(statp->count),
            100. * statp->sumHits / static_cast<double>(statp->sumLengths));

  return str;
}


char * Moves::FullAverageString(
  moveStatType * statp,
  char str[])
{
  if (statp->count == 0)
    sprintf(str, "%5s %5s %4s %8s %8s",
            "--", "--", "--", "--", "--");
  else
  {
    double avg = statp->sumHits / static_cast<double>(statp->count);

    sprintf(str, "%5.3f %5.2f %4.1f %8d %8.0f",
            avg,
            statp->sumLengths / static_cast<double>(statp->count),
            100. * statp->sumHits / static_cast<double>(statp->sumLengths),
            statp->count,
            (avg * avg * avg - 1) * statp->count);
  }

  return str;
}


void Moves::PrintTrickTable(
  moveStatType tablep[][DDS_HANDS])
{
  fprintf(fp, "%5s %11s %11s %11s %11s\n",
          "Trick",
          "Hand 0",
          "Hand 1",
          "Hand 2",
          "Hand 3");

  fprintf(fp, "%5s %5s %4s %5s %4s %5s %4s %5s %4s\n",
          "",
          "Avg", "%",
          "Avg", "%",
          "Avg", "%",
          "Avg", "%");

  char str[DDS_HANDS][16];
  for (int t = 12; t >= 0; t--)
  {
    fprintf(fp, "%5d %11s %11s %11s %11s\n",
            t,
            Moves::AverageString(&tablep[t][0], str[0]),
            Moves::AverageString(&tablep[t][1], str[1]),
            Moves::AverageString(&tablep[t][2], str[2]),
            Moves::AverageString(&tablep[t][3], str[3]));
  }
}


void Moves::PrintTrickStats()
{
  fprintf(fp, "Overall statistics\n\n");
  Moves::PrintTrickTable(trickTable);

  fprintf(fp, "\n\nStatistics for winning suit\n\n");
  Moves::PrintTrickTable(trickSuitTable);
  fprintf(fp, "\n\n");
}


void Moves::PrintFunctionTable(
  moveStatsType * statp)
{
  char str[2][40];

  if (statp->nfuncs == 0)
    return;

  fprintf(fp, "%-15s %5s %5s %4s %8s %8s\n",
          "Function", "Avg", "Len", "%", "Count", "Imp");

  for (int fr = 0; fr < MG_NUM_FUNCTIONS; fr++)
  {
    for (int f = 0; f < statp->nfuncs; f++)
    {
      if (statp->list[f].findex != fr)
        continue;

      sprintf(str[0], "%-15s", funcName[fr]);

      fprintf(fp, "%s %34s\n",
              str[0],
              Moves::FullAverageString(&statp->list[f], str[1]));
    }
  }
}

void Moves::PrintTrickDetails()
{
  fprintf(fp, "Trick detail statistics\n\n");

  for (int t = 12; t >= 0; t--)
  {
    for (int h = 0; h < DDS_HANDS; h++)
    {
      fprintf(fp, "Trick %d, relative hand %d\n", t, h);
      Moves::PrintFunctionTable(&trickDetailTable[t][h]);
      fprintf(fp, "\n");
    }
  }

  fprintf(fp, "Suit detail statistics\n\n");

  for (int t = 12; t >= 0; t--)
  {
    for (int h = 0; h < DDS_HANDS; h++)
    {
      fprintf(fp, "Trick %d, relative hand %d\n", t, h);
      Moves::PrintFunctionTable(&trickDetailSuitTable[t][h]);
      fprintf(fp, "\n");
    }
  }

  fprintf(fp, "\n\n");
}


void Moves::PrintFunctionStats()
{
  fprintf(fp, "Function statistics\n\n");
  Moves::PrintFunctionTable(&trickFuncTable);

  fprintf(fp, "\n\nFunction statistics for winning suit\n\n");
  Moves::PrintFunctionTable(&trickFuncSuitTable);
  fprintf(fp, "\n\n");
}


