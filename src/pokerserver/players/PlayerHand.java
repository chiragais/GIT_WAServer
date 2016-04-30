package pokerserver.players;

import java.util.ArrayList;

import pokerserver.cards.Card;
import pokerserver.cards.PlayerCards;
import pokerserver.utils.GameConstants;
/**
 * Manage player cards and user hands
 * @author Chirag
 *
 */
public class PlayerHand implements GameConstants {

	
	public HAND_RANK pokerHandRank;

	public int getValue() {
		return pokerHandRank.ordinal();
	}

	private Card[] hand = new Card[2];
	private Card[] PokerHandFinalCards = new Card[5];

	public PlayerHand(PlayerCards plrCards) {
		hand[0]=plrCards.getFirstCard();
		hand[1]=plrCards.getSecondCard();
	}

	public PlayerHand(Card[] hand) {
		this.hand = hand;
	}

	public Card[] getHand() {
		return hand;
	}

	public void setHand(Card[] hand) {
		this.hand = hand;
	}

	/**
	 * It will check best rank for user
	 * @param flop
	 * @return
	 */
	public HAND_RANK determineHandRank(Card[] flop) {

		Card[] allCards = new Card[hand.length + flop.length];

		for (int i = 0; i < hand.length; i++) {
			allCards[i] = hand[i];
		}

		for (int i = 0; i < flop.length; i++) {
			allCards[hand.length + i] = flop[i];
		}

		allCards = sortingCards(allCards);

		for (int j = 0; j < allCards.length; j++) {
			System.out.println("this is list of cards " + j + " = "
					+ allCards[j].getValue());
		}

		System.out.println("___________________________________________________");

		if (isRoyalFlush(allCards)) {
			pokerHandRank = HAND_RANK.ROYAL_FLUSH;
			return HAND_RANK.ROYAL_FLUSH;
		} else if (isAStraightFlush(allCards)) {
			pokerHandRank = HAND_RANK.STRAIGHT_FLUSH;
			return HAND_RANK.STRAIGHT_FLUSH;
		} else if (isFourOfAKind(allCards)) {
			pokerHandRank = HAND_RANK.FOUR_OF_A_KIND;
			return HAND_RANK.FOUR_OF_A_KIND;
		} else if (isFullHouse(allCards)) {
			pokerHandRank = HAND_RANK.FULL_HOUSE;
			return HAND_RANK.FULL_HOUSE;
		} else if (isFlush(allCards)) {
			pokerHandRank = HAND_RANK.FLUSH;
			return HAND_RANK.FLUSH;
		} else if (isStraight(allCards)) {
			pokerHandRank = HAND_RANK.STRAIGHT;
			return HAND_RANK.STRAIGHT;
		} else if (isAceStraight(allCards)) {
			pokerHandRank = HAND_RANK.STRAIGHT;
			return HAND_RANK.STRAIGHT;
		} else if (isThreeOfAKind(allCards)) {
			pokerHandRank = HAND_RANK.THREE_OF_A_KIND;
			return HAND_RANK.THREE_OF_A_KIND;
		} else if (isTwoPair(allCards)) {
			pokerHandRank = HAND_RANK.TWO_PAIR;
			return HAND_RANK.TWO_PAIR;
		} else if (isPair(allCards)) {
			pokerHandRank = HAND_RANK.PAIR;
			return HAND_RANK.PAIR;
		} else {
			for (int i = 0; i < PokerHandFinalCards.length; i++) {
				PokerHandFinalCards[i] = allCards[i];
			}
			pokerHandRank = HAND_RANK.HIGH_CARD;
			return HAND_RANK.HIGH_CARD;
		}

	}

	public Card[] sortingCards(Card[] cardArray) {
		for (int i = 1; i <= cardArray.length; i++) {
			for (int j = 0; j < cardArray.length - i; j++) {
				if (cardArray[j + 1].getValue() > cardArray[j].getValue()) {
					Card cTemp = cardArray[j];
					cardArray[j] = cardArray[j + 1];
					cardArray[j + 1] = cTemp;
				}
			}
		}
		for (int i = 0; i < cardArray.length; i++) {
			System.out.println("this is order  = " + cardArray[i].getValue());
		}
		return cardArray;
	}

	public boolean isRoyalFlush(Card[] allCards) {

		if (isFlush(allCards)) {
			if (isStraight(PokerHandFinalCards)) {
				boolean aceExists = false, kingExists = false, queenExists = false, jackExists = false, tenExists = false;
				for (Card card : PokerHandFinalCards) {
					switch (card.getRank().toString()) {
					case RANK_ACE:
						aceExists = true;
						break;
					case RANK_KING:
						kingExists = true;
						break;
					case RANK_QUEEN:
						queenExists = true;
						break;
					case RANK_JACK:
						jackExists = true;
						break;
					case RANK_TEN:
						tenExists = true;
						break;
					}
				}
				return (aceExists && kingExists && queenExists && jackExists && tenExists);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isStraight(Card[] allCards) {
		int lastPos = 0;
		int noOfCardsInARow = 0;
		int pos = 0;
		boolean isAStraight = false;
		while (pos < allCards.length - 1 && !isAStraight) {
			if (allCards[pos].getValue() - allCards[pos + 1].getValue() == 1) {
				noOfCardsInARow++;
				if (noOfCardsInARow == 4) {
					isAStraight = true;
					lastPos = pos + 1;

				} else {
					pos++;
				}
			} else if (allCards[pos].getValue() - allCards[pos + 1].getValue() == 0) {

				pos++;
			} else {
				noOfCardsInARow = 0;
				pos++;
			}
		}
		if (isAStraight) {
			System.out.println("this is pokerhand    "+"  "+allCards.length);
			System.out.println("this is pokerhand    "+"  "+PokerHandFinalCards.length);
			
			for (int i=0,j=0; i < allCards.length - 1; i++) {

				if ((allCards[i].getValue() - allCards[i + 1].getValue()) == 1) {
					PokerHandFinalCards[j] = allCards[i];
					
				    System.out.println("this is pokerhand    "+j+"  "+PokerHandFinalCards[j]);
					j++;
					if(j==PokerHandFinalCards.length-1){
						PokerHandFinalCards[j]=allCards[i+1];
						 System.out.println("this is pokerhand    "+j+"  "+PokerHandFinalCards[j]);
						break;
					}
				}

			}
		}

		return isAStraight;
	}

	public boolean isAceStraight(Card[] allCards) {
		if (allCards[0].getRank() == RANKS.ace) {
			boolean aceExist = false, twoExist = false, threeExist = false, fourExist = false, fiveExist = false;
			for (Card card : allCards) {

				switch (card.getRank().toString()) {
				case RANK_ACE:
					aceExist = true;
					break;
				case RANK_TWO:
					twoExist = true;
					break;
				case RANK_THREE:
					threeExist = true;
					break;
				case RANK_FOUR:
					fourExist = true;
					break;
				case RANK_FIVE:
					fiveExist = true;
					break;

				}
			}
			if (aceExist && twoExist && threeExist && fourExist && fiveExist) {
				for (int i = 1; i < PokerHandFinalCards.length; i++) {
					PokerHandFinalCards[i - 1] = allCards[i];
				}
				PokerHandFinalCards[4] = allCards[0];
			}
			return (aceExist && twoExist && threeExist && fourExist && fiveExist);
		} else {
			return false;
		}
	}

	public boolean isFlush(Card[] allCards) {

		int noOfClubs = 0;
		int noOfSpades = 0;
		int noOfHearts = 0;
		int noOfDiamonds = 0;
		for (Card c : allCards) {
			switch (c.getSuit().toString()) {
			case SUIT_HEART:
				noOfHearts++;
				break;
			case SUIT_SPADE:
				noOfSpades++;
				break;
			case SUIT_CLUB:
				noOfClubs++;
				break;
			case SUIT_DIAMOND:
				noOfDiamonds++;
				break;
			}
		}

		if (noOfClubs >= 5) {
			int j = 0;
			for (int i = 0; i < allCards.length; i++) {
				if (allCards[i].getSuit() == SUITS.club) {
					PokerHandFinalCards[j] = allCards[i];
					j++;
					if (j == PokerHandFinalCards.length) {
						return true;
					}
				}
			}
		} else if (noOfSpades >= 5) {
			int j = 0;
			for (int i = 0; i < allCards.length; i++) {
				if (allCards[i].getSuit() == SUITS.spade) {
					PokerHandFinalCards[j] = allCards[i];
					j++;
					if (j == PokerHandFinalCards.length) {
						return true;
					}
				}
			}
		} else if (noOfHearts >= 5) {
			int j = 0;
			for (int i = 0; i < allCards.length; i++) {
				if (allCards[i].getSuit() == SUITS.heart) {
					PokerHandFinalCards[j] = allCards[i];
					j++;
					if (j == PokerHandFinalCards.length) {
						return true;
					}
				}
			}
		} else if (noOfDiamonds >= 5) {
			int j = 0;
			for (int i = 0; i < allCards.length; i++) {
				if (allCards[i].getSuit() == SUITS.diamond) {
					PokerHandFinalCards[j] = allCards[i];
					j++;
					if (j == PokerHandFinalCards.length) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isThreeOfAKind(Card[] allCards) {

		int cardRepeats = 1;
		boolean isThreeOfAKind = false;
		int m = 0;
		int n = m + 1;
		while (m < allCards.length && !isThreeOfAKind) {
			cardRepeats = 1;
			n = m + 1;
			while (n < allCards.length && !isThreeOfAKind) {
				if (allCards[m].getValue() == allCards[n].getValue()) {
					cardRepeats++;
					if (cardRepeats == 3) {
						PokerHandFinalCards[0] = allCards[n];
						isThreeOfAKind = true;
					}
				}
				n++;
			}
			m++;
		}
		if (isThreeOfAKind) {
			calculateFinalKindOfArray(2, allCards);
		}
		return isThreeOfAKind;
	}

	public void calculateFinalKindOfArray(int nPlace, Card[] allCards) {
		Card cTemp = PokerHandFinalCards[0];
		for (int i = 0; i < PokerHandFinalCards.length; i++) {
			if (i < nPlace) {
				if (cTemp.getValue() < allCards[i].getValue()) {
					PokerHandFinalCards[i] = allCards[i];
				} else {
					for (int j = 0; j < PokerHandFinalCards.length; j++) {
						PokerHandFinalCards[j] = allCards[j];
					}
					break;
				}
			} else {
				PokerHandFinalCards[i] = cTemp;
			}
		}
	}

	private boolean isTwoPair(Card[] allCards) {

		Card cTemp1 = null, cTemp2 = null;
		int cardRepeats = 1;
		int noOfCardRepeats = 0;
		boolean isTwoPair = false;
		int m = 0;
		int n = m + 1;
		while (m < allCards.length && !isTwoPair) {
			cardRepeats = 1;
			n = m + 1;
			while (n < allCards.length && !isTwoPair) {
				if (allCards[m].getValue() == allCards[n].getValue()) {
					cardRepeats++;
					if (cardRepeats == 2) {
						cardRepeats = 1;
						noOfCardRepeats++;
						if (noOfCardRepeats == 1) {
							cTemp1 = allCards[m];
						}
						if (noOfCardRepeats == 2) {
							cTemp2 = allCards[m];
							isTwoPair = true;

						}
					}

				}
				n++;
			}
			m++;
		}
		if (isTwoPair) {
			if (allCards[0].getValue() > cTemp1.getValue()) {
				PokerHandFinalCards[0] = allCards[0];
				PokerHandFinalCards[1] = cTemp1;
				PokerHandFinalCards[2] = cTemp1;
				PokerHandFinalCards[3] = cTemp2;
				PokerHandFinalCards[4] = cTemp2;
			} else {
				PokerHandFinalCards[0] = cTemp1;
				PokerHandFinalCards[1] = cTemp1;
				if (allCards[2].getValue() > cTemp2.getValue()) {
					PokerHandFinalCards[2] = allCards[2];
					PokerHandFinalCards[3] = cTemp2;
					PokerHandFinalCards[4] = cTemp2;
				} else {
					PokerHandFinalCards[2] = cTemp2;
					PokerHandFinalCards[3] = cTemp2;
					PokerHandFinalCards[4] = allCards[4];
				}
			}

		}

		return isTwoPair;
	}

	private boolean isPair(Card[] allCards) {

		int cardRepeats = 1;
		boolean isPair = false;
		int m = 0;
		int n = m + 1;
		while (m < allCards.length && !isPair) {
			cardRepeats = 1;
			n = m + 1;
			while (n < allCards.length && !isPair) {
				if (allCards[m].getValue() == allCards[n].getValue()) {
					cardRepeats++;
					if (cardRepeats == 2) {
						isPair = true;
						PokerHandFinalCards[0] = allCards[m];
						break;
					}
				}
				n++;
			}

			m++;
		}
		if (isPair) {
			calculateFinalKindOfArray(3, allCards);
		}
		return isPair;
	}

	private boolean isFullHouse(Card[] allCards) {

		Card cTemp = null;
		int ThreeOfKindCardValue = 0;
		int noOfRepeats = 1;
		int j = 0;
		boolean isThreeOfAKind = false;
		boolean isTwoOfAKind = false;
		for (int i = 0; i < allCards.length; i++) {
			j = i + 1;
			noOfRepeats = 1;
			while (j < allCards.length) {
				if (allCards[i].getValue() == allCards[j].getValue()) {
					noOfRepeats++;
					if (noOfRepeats == 3) {
						ThreeOfKindCardValue = allCards[i].getValue();
						isThreeOfAKind = true;
						cTemp = allCards[j];
						// PokerHandFinalCards[0]=allCards[j];
						noOfRepeats = 1;
						break;
					}
				}
				j++;
			}
			if (isThreeOfAKind) {
				for (int k = 0; k < 3; k++) {
					PokerHandFinalCards[k] = cTemp;
				}
				break;
			}
		}
		if (isThreeOfAKind) {
			// noOfRepeats=1;
			for (int i = 0; i < allCards.length - 1; i++) {
				j = i + 1;
				noOfRepeats = 1;
				if (allCards[i].getValue() != ThreeOfKindCardValue) {
					while (j < allCards.length) {
						if (allCards[i].getValue() == allCards[j].getValue()) {
							noOfRepeats++;
							if (noOfRepeats == 2) {
								ThreeOfKindCardValue = allCards[j].getValue();
								isTwoOfAKind = true;
								cTemp = allCards[j];
								noOfRepeats = 1;
								break;
							}
						}
						j++;
					}
				}
				if (isTwoOfAKind) {
					for (int k = 3; k < PokerHandFinalCards.length; k++) {
						PokerHandFinalCards[k] = cTemp;
					}
					PokerHandFinalCards = sortingCards(PokerHandFinalCards);
					break;
				}
			}
		}
		return (isTwoOfAKind && isThreeOfAKind);

	}

	public boolean isFourOfAKind(Card[] allCards) {

		int cardRepeats = 1;
		boolean isFourOfAKind = false;
		int m = 0;
		int n = m + 1;
		while (m < allCards.length && !isFourOfAKind) {
			cardRepeats = 1;
			n = m + 1;
			while (n < allCards.length && !isFourOfAKind) {
				if (allCards[m].getValue() == allCards[n].getValue()) {
					cardRepeats++;
					if (cardRepeats == 4) {
						PokerHandFinalCards[0] = allCards[n];
						isFourOfAKind = true;
					}
				}
				n++;
			}
			m++;
		}
		if (isFourOfAKind) {
			calculateFinalKindOfArray(1, allCards);

		}

		return isFourOfAKind;
	}

	private boolean isAStraightFlush(Card[] flop) {
		if (isFlush(flop)) {
			if (isStraight(PokerHandFinalCards)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

//	public Card[] GetPokerHandArray() {
	public ArrayList<Card> getPokerHandArray() {
		ArrayList< Card> pokerHands=  new ArrayList<Card>();
		for(Card c : PokerHandFinalCards){
			pokerHands.add(c);
		}
		return pokerHands;
	//	return this.PokerHandFinalCards;
	}
	
	public ArrayList<String> getWinnerHandCardNameList() {
		ArrayList< String> pokerHands=  new ArrayList<String>();
		for(Card c : PokerHandFinalCards){
			pokerHands.add(c.getCardName());
		}
		return pokerHands;
	//	return this.PokerHandFinalCards;
	}
	
}
