package pokerserver.handrank;

import java.util.ArrayList;
import java.util.List;

import pokerserver.cards.Card;
import pokerserver.cards.PlayerCards;
import pokerserver.utils.GameConstants;

public class WhoopAssHandManager implements GameConstants {

	public HAND_RANK pokerHandRank;
	List<Card> defaultCards = new ArrayList<Card>();
	private Card[] handBestCards = new Card[5];

	int minimumPlayerCards = 2;
	
	public WhoopAssHandManager(List<Card> defaultCards) {
		this.defaultCards.addAll(defaultCards);
	}

	public Card[] getPlayerBestCards() {

		return handBestCards;
	}

	public HAND_RANK findPlayerBestHand(PlayerCards playerCards, Card wACard,
			int wACardStatus) {

		// handBestCards = new Card[5];
		List<Card> allCards = new ArrayList<Card>();
		allCards.addAll(defaultCards);
		allCards.add(playerCards.getFirstCard());
		allCards.add(playerCards.getSecondCard());
		if (wACardStatus != GameConstants.ACTION_WA_NO) {
			allCards.add(wACard);
		}
		allCards = sortingCardsList(allCards);
		List<Card> playerHandList = getPlayerHandList(playerCards, wACard,
				wACardStatus);

		Card[] newCards = new Card[allCards.size()];
		for (int i = 0; i < allCards.size(); i++) {
			newCards[i] = allCards.get(i);
		}
		playerHandList = sortingCardsList(playerHandList);
		
		if (isRoyalFlush(newCards, playerHandList)
				&& isValidHandBestCards(playerHandList)) {
			pokerHandRank = HAND_RANK.ROYAL_FLUSH;
		} else if (isAStraightFlush(newCards, playerHandList)
				&& isValidHandBestCards(playerHandList)) {
			pokerHandRank = HAND_RANK.STRAIGHT_FLUSH;
		} else if (isFourOfKindRank(newCards, playerHandList)) {
			pokerHandRank = HAND_RANK.FOUR_OF_A_KIND;
		} else if (isFullHouseRank(newCards, playerHandList)
				&& isValidHandBestCards(playerHandList)) {
			pokerHandRank = HAND_RANK.FULL_HOUSE;
		} else if (isFlush(newCards, playerHandList)
				&& isValidHandBestCards(playerHandList)) {
			pokerHandRank = HAND_RANK.FLUSH;
		} else if (isStraightRank(newCards, playerHandList)) {
			pokerHandRank = HAND_RANK.STRAIGHT;
		} else if (isAceStraight(newCards, playerHandList)) {
			pokerHandRank = HAND_RANK.STRAIGHT;
		} else if (isThreeOfKindRank(newCards, playerHandList)) {
			pokerHandRank = HAND_RANK.THREE_OF_A_KIND;
		} else if (isTwoPairRank(newCards, playerHandList)) {
			pokerHandRank = HAND_RANK.TWO_PAIR;
		} else if (isPairRank(newCards, playerHandList)) {
			pokerHandRank = HAND_RANK.PAIR;
		} else {
			List<Card> highCardList = getHighCards(playerCards, wACard,
					wACardStatus);
			for (int i = 0; i < highCardList.size(); i++) {
				handBestCards[i] = highCardList.get(i);
			}
			pokerHandRank = HAND_RANK.HIGH_CARD;
		}
		for(Card card : handBestCards){
			System.out.println(card.getCardName());
		}
		return pokerHandRank;
	}

	public List<Card> getHighCards(PlayerCards playerCards, Card wACard,
			int wACardStatus) {

		List<Card> playerHandList = new ArrayList<Card>();
		List<Card> playerDefaultList = new ArrayList<Card>();
		if (wACardStatus == GameConstants.ACTION_WA_UP) {
			playerHandList.add(playerCards.getFirstCard());
			playerHandList.add(playerCards.getSecondCard());
			playerDefaultList.addAll(defaultCards);
			playerDefaultList.add(wACard);
		} else if (wACardStatus == GameConstants.ACTION_WA_DOWN) {
			playerHandList.add(playerCards.getFirstCard());
			playerHandList.add(playerCards.getSecondCard());
			playerHandList.add(wACard);
			playerDefaultList.addAll(defaultCards);
		} else if (wACardStatus == GameConstants.ACTION_WA_NO) {
			playerHandList.add(playerCards.getFirstCard());
			playerHandList.add(playerCards.getSecondCard());
			playerDefaultList.addAll(defaultCards);
		}
		playerHandList = sortingCardsList(playerHandList);
		playerDefaultList = sortingCardsList(playerDefaultList);

		List<Card> playerHighCardList = new ArrayList<Card>();
		playerHighCardList.add(playerHandList.get(0));
		playerHighCardList.add(playerHandList.get(1));
		playerHighCardList.add(playerDefaultList.get(0));
		playerHighCardList.add(playerDefaultList.get(1));
		playerHighCardList.add(playerDefaultList.get(2));
		playerHighCardList = sortingCardsList(playerHighCardList);

		return playerHighCardList;

	}

	public List<Card> getPlayerHandList(PlayerCards playerCards, Card wACard,
			int wACardStatus) {
		List<Card> playerHandList = new ArrayList<Card>();
		if (wACardStatus == GameConstants.ACTION_WA_UP
				|| wACardStatus == GameConstants.ACTION_WA_NO) {
			playerHandList.add(playerCards.getFirstCard());
			playerHandList.add(playerCards.getSecondCard());

		} else if (wACardStatus == GameConstants.ACTION_WA_DOWN) {
			playerHandList.add(playerCards.getFirstCard());
			playerHandList.add(playerCards.getSecondCard());
			playerHandList.add(wACard);
		}

		playerHandList = sortingCardsList(playerHandList);

		return playerHandList;
	}

	public boolean isValidHandBestCards(List<Card> playerHandList) {

		if (checkCardsOccurance(playerHandList)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkCardsOccurance(List<Card> playerHandList) {
		int noOfMatchCount = 0;
		for (Card cH : playerHandList) {
			for (Card cP : handBestCards) {
				if (cH.getCardName().equals(cP.getCardName())) {
					noOfMatchCount++;
				}
			}
		}
		if (noOfMatchCount == 2) {
			handBestCards = sortingCardsArray(handBestCards);
			return true;
		} else {
			return false;
		}

	}

	public List<Card> sortingCardsList(List<Card> allCards) {
		for (int i = 1; i <= allCards.size(); i++) {
			for (int j = 0; j < allCards.size() - i; j++) {
				if (allCards.get(j + 1).getValue() > allCards.get(j).getValue()) {
					Card cTemp = allCards.get(j);
					allCards.set(j, allCards.get(j + 1));
					allCards.set(j + 1, cTemp);
				}
			}
		}

		return allCards;
	}

	public Card[] sortingCardsArray(Card[] cardArray) {
		for (int i = 1; i <= cardArray.length; i++) {
			for (int j = 0; j < cardArray.length - i; j++) {
				if (cardArray[j + 1].getValue() > cardArray[j].getValue()) {
					Card cTemp = cardArray[j];
					cardArray[j] = cardArray[j + 1];
					cardArray[j + 1] = cTemp;
				}
			}
		}
		return cardArray;
	}

	public boolean isRoyalFlush(Card[] allCards, List<Card> playerHandList) {

		if (isFlush(allCards, playerHandList)) {
			if (isStraightRank(handBestCards, playerHandList)) {
				boolean aceExists = false, kingExists = false, queenExists = false, jackExists = false, tenExists = false;
				for (Card card : handBestCards) {
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

				if (isValidHandBestCards(playerHandList)) {
					return (aceExists && kingExists && queenExists
							&& jackExists && tenExists);

				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean isAStraightFlush(Card[] allCards, List<Card> playerHandList) {
		if (isFlush(allCards, playerHandList)) {
			if (isStraightRank(handBestCards, playerHandList)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isFlush(Card[] allCards, List<Card> playerHandList) {

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
					handBestCards[j] = allCards[i];
					j++;
					if (j == handBestCards.length) {
						if (makeFlushWithPlayerHandCards(playerHandList)) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
		} else if (noOfSpades >= 5) {
			int j = 0;
			for (int i = 0; i < allCards.length; i++) {
				if (allCards[i].getSuit() == SUITS.spade) {
					handBestCards[j] = allCards[i];
					j++;
					if (j == handBestCards.length) {
						if (makeFlushWithPlayerHandCards(playerHandList)) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
		} else if (noOfHearts >= 5) {
			int j = 0;
			for (int i = 0; i < allCards.length; i++) {
				if (allCards[i].getSuit() == SUITS.heart) {
					handBestCards[j] = allCards[i];
					j++;
					if (j == handBestCards.length) {
						if (makeFlushWithPlayerHandCards(playerHandList)) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
		} else if (noOfDiamonds >= 5) {
			int j = 0;
			for (int i = 0; i < allCards.length; i++) {
				if (allCards[i].getSuit() == SUITS.diamond) {
					handBestCards[j] = allCards[i];
					j++;
					if (j == handBestCards.length) {
						if (makeFlushWithPlayerHandCards(playerHandList)) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean makeFlushWithPlayerHandCards(List<Card> playerCardList) {
		int count = 0;
		int sizeOfBestHand = handBestCards.length;
		String flushSuit = handBestCards[0].getSuit().toString();
		for (int i = 0; i < playerCardList.size(); i++) {
			if (playerCardList.get(i).getSuit().toString() == flushSuit) {
				handBestCards[--sizeOfBestHand] = playerCardList.get(i);
				count++;
				if (count == 2) {
					handBestCards = sortingCardsArray(handBestCards);
					return true;
				}
			}

		}
		return false;
	}

	public boolean isAceStraight(Card[] allCards, List<Card> playerHandList) {

		if (allCards[0].getRank() == RANKS.ace) {
			int i = 0;
			boolean aceExist = false, twoExist = false, threeExist = false, fourExist = false, fiveExist = false;
			for (Card card : allCards) {

				switch (card.getRank().toString()) {
				case RANK_ACE:
					if (!aceExist) {
						handBestCards[i] = card;
						i++;
						aceExist = true;
					}
					break;
				case RANK_TWO:
					if (!twoExist) {
						handBestCards[i] = card;
						i++;
						twoExist = true;
					}
					break;
				case RANK_THREE:
					if (!threeExist) {
						handBestCards[i] = card;
						i++;
						threeExist = true;
					}
					break;
				case RANK_FOUR:
					if (!fourExist) {
						handBestCards[i] = card;
						i++;
						fourExist = true;
					}
					break;
				case RANK_FIVE:
					if (!fiveExist) {
						handBestCards[i] = card;
						i++;
						fiveExist = true;
					}
					break;

				}
			}
			if (aceExist && twoExist && threeExist && fourExist && fiveExist) {

				if (isValidHandBestCards(playerHandList)) {
					return (aceExist && twoExist && threeExist && fourExist && fiveExist);

				} else {
					return false;
				}
			}
		}
		return false;
	}

	public void checkExplicityCardOccurance(List<Card> playerHandList,
			int threeOfKindValue) {
		int countExist = 2;
		for (Card cH : playerHandList) {
			for (Card cP : handBestCards) {
				if (cH.getCardName().equals(cP.getCardName())) {
					countExist--;
					playerHandList.remove(cH);
				}
			}
		}
		checkExplicitly(countExist, playerHandList, threeOfKindValue);

	}

	public void calculateFinalKindOfArray(int nPlace, Card[] allCards,
			int offset) {
		Card cTemp = handBestCards[0];
		for (int i = 0; i < handBestCards.length; i++) {
			if (i < nPlace) {
				if (cTemp.getValue() < allCards[i].getValue()) {
					handBestCards[i] = allCards[i];
				} else {
					for (int j = 0; j < handBestCards.length; j++) {
						handBestCards[j] = allCards[j];
					}
					break;
				}
			} else {
				handBestCards[i] = allCards[offset--];
			}
		}
	}

	public void checkExplicitly(int nPlace, List<Card> playerHandCard,
			int repeatedCardValue) {
		// Card cTemp = handBestCards[0];
		int count = 0;
		for (int i = 0; i < handBestCards.length; i++) {
			if (count < nPlace) {
				if (repeatedCardValue != handBestCards[i].getValue()) {
					handBestCards[i] = playerHandCard.get(count);
					count++;
				}
			} else {
				break;
			}
		}
		handBestCards = sortingCardsArray(handBestCards);
	}

	public List<Card> sameRankCards(Card[] allCards, Card currentCard) {
		List<Card> listSameCards = new ArrayList<Card>();
		for (Card card : allCards) {
			if (card.getRank().equals(currentCard.getRank())
					&& !listSameCards.contains(card)) {
				listSameCards.add(card);
			}
		}
		return listSameCards;
	}

	public boolean isTwoPairCardsRank(Card[] allCards, List<Card> playerHandList) {
		List<List<Card>> listPairCards = new ArrayList<List<Card>>();
		List<Card> listFixedCards = new ArrayList<Card>();
		for (Card card : allCards) {
			List<Card> sameRankCards = sameRankCards(allCards, card);
			if (sameRankCards.size() == 2) {
				boolean isAlreadyAddedflag = false;
				for (List<Card> listCards : listPairCards) {
					if (listCards.contains(sameRankCards.get(0))) {
						isAlreadyAddedflag = true;
					}
				}
				if (!isAlreadyAddedflag)
					listPairCards.add(sameRankCards);
			}
		}
		for(List<Card> listCard : listPairCards){
			for(Card card : listCard){
				System.out.println(card.getCardName());
			}
		}
		return false;
	}
	// Created by chirag
	public boolean isThreeOfKindRank(Card[] allCards, List<Card> playerHandList) {
		List<List<Card>> listPairCards = new ArrayList<List<Card>>();
		List<Card> listFixedCards = new ArrayList<Card>();
		for (Card card : allCards) {
			// System.out.println("Total same rank Cards : "+sameRankCards(allCards,
			// card).size() +" >> "+card);
			List<Card> sameRankCards = sameRankCards(allCards, card);
			if (sameRankCards.size() >= 3) {
				boolean flag = true;
				for (List<Card> listCards : listPairCards) {
					if (listCards.contains(sameRankCards.get(0))) {
						flag = false;
					}
				}
				if (flag)
					listPairCards.add(sameRankCards);
			}
		}
		if (!listPairCards.isEmpty()) {
			List<Card> listSelectedCards = new ArrayList<Card>();
			boolean hasPlayerCard = false;
			int totalPlayerCards = 0;
			for (List<Card> listCards : listPairCards) {
				// Check with player hand
				hasPlayerCard = false;
				totalPlayerCards = 0;
				for (Card card : listCards) {
					if (playerHandList.contains(card)) {
						hasPlayerCard = true;
						totalPlayerCards++;
//						break;
					}
				}
				if (hasPlayerCard) {
					listSelectedCards.addAll(listCards);
					break;
				}
			}
			if (listSelectedCards.isEmpty()) {
				listSelectedCards.addAll(listPairCards.get(0));
			}
			for(Card card : listSelectedCards){
				if(listFixedCards.size()<3){
					listFixedCards.add(card);
				}
			}
			
			Card maxValuePlayerCard = null;
			while (totalPlayerCards != 2) {
				for (Card card : playerHandList) {
					if (maxValuePlayerCard == null || card.getValue() > maxValuePlayerCard
									.getValue()) {
						boolean isAllReadyAdded = false;
						for(Card card1 : listFixedCards){
							if(card.getCardName().equals(card1.getCardName())){
								isAllReadyAdded=true;
							}
						}
						if (!isAllReadyAdded)
						maxValuePlayerCard = card;
					}
				}
				if (maxValuePlayerCard != null && !listFixedCards.contains(maxValuePlayerCard)){
					listFixedCards.add(maxValuePlayerCard);
					maxValuePlayerCard=null;
					totalPlayerCards++;
				}
			}
			
			for (Card card : allCards) {
				if (listFixedCards.size() != 5 ) {
//					listFixedCards.add(card);
					boolean isAllReadyAdded = false;
					for(Card card1 : listFixedCards){
						if(card.getCardName().equals(card1.getCardName())){
							isAllReadyAdded=true;
						}
					}
					if (!isAllReadyAdded)
						listFixedCards.add(card);
				}
			}

			if (listFixedCards.size() == 5) {
				handBestCards = new Card[5];
				handBestCards[0] = listFixedCards.get(0);
				handBestCards[1] = listFixedCards.get(1);
				handBestCards[2] = listFixedCards.get(2);
				handBestCards[3] = listFixedCards.get(3);
				handBestCards[4] = listFixedCards.get(4);
				return checkCardsOccurance(playerHandList);
			}
		}
		return false;
	}
	// Created by chirag
		public boolean isFourOfKindRank(Card[] allCards, List<Card> playerHandList) {
			List<List<Card>> listPairCards = new ArrayList<List<Card>>();
			List<Card> listFixedCards = new ArrayList<Card>();
			for (Card card : allCards) {
				// System.out.println("Total same rank Cards : "+sameRankCards(allCards,
				// card).size() +" >> "+card);
				List<Card> sameRankCards = sameRankCards(allCards, card);
				if (sameRankCards.size() >= 4) {
					boolean flag = true;
					for (List<Card> listCards : listPairCards) {
						if (listCards.contains(sameRankCards.get(0))) {
							flag = false;
						}
					}
					if (flag)
						listPairCards.add(sameRankCards);
				}
			}
			if (!listPairCards.isEmpty()) {
				List<Card> listSelectedCards = new ArrayList<Card>();
				boolean hasPlayerCard = false;
				int totalPlayerCards = 0;
				for (List<Card> listCards : listPairCards) {
					// Check with player hand
					hasPlayerCard = false;
					totalPlayerCards = 0;
					for (Card card : listCards) {
						if (playerHandList.contains(card)) {
							hasPlayerCard = true;
							totalPlayerCards++;
//							break;
						}
					}
					if (hasPlayerCard) {
						listSelectedCards.addAll(listCards);
						break;
					}
				}
				if (listSelectedCards.isEmpty()) {
					listSelectedCards.addAll(listPairCards.get(0));
				}
				for(Card card : listSelectedCards){
					if(listFixedCards.size()<4){
						listFixedCards.add(card);
					}
				}
				
				if(totalPlayerCards!=0){
					if(totalPlayerCards==1){
						// Add one card from player hand
						for (Card card : playerHandList) {
							if (!listFixedCards.contains(card)) {
								listFixedCards.add(card);
								break;
							}
						}
						
					}else if(totalPlayerCards==2){
						// Add two cards from others
						for (Card card : allCards) {
							if (!listFixedCards.contains(card) && listFixedCards.size()<5) {
								listFixedCards.add(card);
							}
						}
					}
				}else{
					// Add two cards from player
					for (Card card : playerHandList) {
						if (!listFixedCards.contains(card) && listFixedCards.size()<5) {
							listFixedCards.add(card);
						}
					}
				}
				for(Card card: listFixedCards){
					System.out.println(card.getCardName());
				}
				if (listFixedCards.size() == 5) {
					handBestCards = new Card[5];
					handBestCards[0] = listFixedCards.get(0);
					handBestCards[1] = listFixedCards.get(1);
					handBestCards[2] = listFixedCards.get(2);
					handBestCards[3] = listFixedCards.get(3);
					handBestCards[4] = listFixedCards.get(4);
					return checkCardsOccurance(playerHandList);
				}
			}
			return false;
		}
	// Created by chirag
	public boolean isStraightRank(Card[] allCards, List<Card> playerHandList) {
		List<Card> listFixedPair = new ArrayList<Card>();
		List<Card> listStraightPair = new ArrayList<Card>();
		Card[] sortedAllCards = sortingCardsArray(allCards);

		for (int i = 0; i < sortedAllCards.length; i++) {
			if (listFixedPair.isEmpty()) {
				listFixedPair.add(sortedAllCards[i]);
			} else {
				if (sortedAllCards[i - 1].getValue() != sortedAllCards[i]
						.getValue()) {
					listFixedPair.add(sortedAllCards[i]);
				}
			}
		}

		int playerCardsCnr = 0;
		for (int i = 0; i < listFixedPair.size(); i++) {
			listStraightPair.clear();
			Card firstCard = listFixedPair.get(i);
			listStraightPair.add(firstCard);
			for (int j = i + 1; j < listFixedPair.size(); j++) {
				Card secondCard = listFixedPair.get(j);
				if (firstCard.getValue() == secondCard.getValue() + 1) {
					if(playerHandList.contains(secondCard)){
						playerCardsCnr++;
					}
					listStraightPair.add(secondCard);
					firstCard = secondCard;
					if (listStraightPair.size() == 5 && playerCardsCnr!=2) {
						
						for (int l = 0; l < listStraightPair.size(); l++) {
							Card card = listStraightPair.get(l);
							for (Card playerCard : playerHandList) {
								if (playerCard.getRank() == card.getRank()
										&& playerCard.getSuit() != card
												.getSuit()) {
									listStraightPair.set(l, card);
									playerCardsCnr++;
									break;
								}
							}
							if(playerCardsCnr==2){
								break;
							}
						}
						if (playerCardsCnr != 2) {
							if (playerHandList
									.contains(listStraightPair.get(0))) {
								playerCardsCnr--;
							}
							listStraightPair.remove(0);
						}
						if(listStraightPair.size() == 5 && playerCardsCnr==2){
							break;
						}
					}else if(listStraightPair.size() == 5 &&playerCardsCnr==2){
						break;
					}
				} else {
					break;
				}
			}
			if (listStraightPair.size() == 5 &&playerCardsCnr==2) {
				break;
			}
		}
		for (Card card : listStraightPair) {
			System.out.println(card.getValue());
		}
		if (listStraightPair.size() == 5) {
			handBestCards = new Card[5];
			handBestCards[0] = listStraightPair.get(0);
			handBestCards[1] = listStraightPair.get(1);
			handBestCards[2] = listStraightPair.get(2);
			handBestCards[3] = listStraightPair.get(3);
			handBestCards[4] = listStraightPair.get(4);
			boolean isStraight = checkCardsOccurance(playerHandList);
			if (!isStraight) {
				for (int i = 0; i < handBestCards.length; i++) {
					Card card = handBestCards[i];
					for (Card playerCard : playerHandList) {
						if (playerCard.getRank() == card.getRank()
								&& playerCard.getSuit() != card.getSuit()) {
							handBestCards[i] = playerCard;
						}
					}
				}
				isStraight = checkCardsOccurance(playerHandList);
			}
			return isStraight;
		}
		return false;
	}

	// Created by chirag
	public boolean isFullHouseRank(Card[] allCards, List<Card> playerHandList) {

		List<Card> listFixedPair = new ArrayList<Card>();
		boolean isThreeSameRankCards = false;
		boolean isTwoSameRankCards = false;
		for (Card card : allCards) {
			int totalSameRankCards = 0;
			for (Card card1 : allCards) {
				if (card.getRank() == card1.getRank()) {
					totalSameRankCards++;
				}
			}
			if (totalSameRankCards >= 2 && !listFixedPair.contains(card)) {
				if (totalSameRankCards == 2) {
					isTwoSameRankCards = true;
				}
				if (totalSameRankCards == 3) {
					isThreeSameRankCards = true;
				}
				listFixedPair.add(card);
			}
		}

		if (isThreeSameRankCards && isTwoSameRankCards) {
			handBestCards = new Card[5];
			handBestCards[0] = listFixedPair.get(0);
			handBestCards[1] = listFixedPair.get(1);
			handBestCards[2] = listFixedPair.get(2);
			handBestCards[3] = listFixedPair.get(3);
			handBestCards[4] = listFixedPair.get(4);
			return checkCardsOccurance(playerHandList);
		} else {
			return false;
		}
	}

	// Created by chirag
	public boolean isTwoPairRank(Card[] allCards, List<Card> playerHandList) {
		List<Card> listFixedPair = new ArrayList<Card>();
		List<Card> listPairCards = new ArrayList<Card>();
		int playerCardsCntr = 0;
		for (Card card : allCards) {
			if (!listPairCards.contains(card)) {
				for (Card card1 : allCards) {
					if (card.getRank() == card1.getRank()
							&& card.getSuit() != card1.getSuit()) {
						if (playerHandList.contains(card)
								|| playerHandList.contains(card1)) {
							if (!listFixedPair.contains(card1)) {
								listFixedPair.add(card1);
								if (playerHandList.contains(card1))
									playerCardsCntr++;
							}
							if(!listFixedPair.contains(card)){
								listFixedPair.add(card);
								if (playerHandList.contains(card))
									playerCardsCntr++;
							}
						} else {
							listPairCards.add(card1);
							listPairCards.add(card);
						}
					}
				}
			}
		}

		if(playerCardsCntr==3){
			Card minValueCard = null;
			for (Card card : playerHandList) {
				if (minValueCard == null
						|| card.getValue() < minValueCard.getValue()) {
					minValueCard = card;
				}
			}
			List<Card> listTmpCard = new ArrayList<Card>();
			playerCardsCntr = 0;
			for(Card card : listFixedPair){
				if(!minValueCard.getRank().equals(card.getRank())){
					if(playerHandList.contains(card)){
						playerCardsCntr++;
					}
					listTmpCard.add(card);
				}
			}
			listFixedPair.clear();
			listFixedPair.addAll(listTmpCard);
		}
		
		if (listFixedPair.size() == 4) {
			Card maxValueCard = null;
			if(playerCardsCntr==2){
				for (Card card : allCards) {
					if (maxValueCard == null
							|| card.getValue() > maxValueCard.getValue() && !listFixedPair.contains(card) ) {
						if(!playerHandList.contains(card))
						maxValueCard = card;
					}
				}
			}else{
				for (Card card : playerHandList) {
					if (maxValueCard == null
							|| card.getValue() > maxValueCard.getValue()&& !listFixedPair.contains(card)) {
						maxValueCard = card;
					}
				}
			}
			if(maxValueCard!=null)
			listFixedPair.add(maxValueCard);
		} else {
			Card maxValueCard = null;
			for (Card card : listPairCards) {
				if (maxValueCard == null
						|| card.getValue() > maxValueCard.getValue()) {
					maxValueCard = card;
				}
			}
			for (Card card : listPairCards) {
				if (card.getRank() == maxValueCard.getRank()
						&& !listFixedPair.contains(card)) {
					listFixedPair.add(card);
				}
			}
			Card maxValuePlayerCard = null;
			if (playerCardsCntr < 2) {
				for (Card card : playerHandList) {
					if (!listFixedPair.contains(card)
							&& (maxValuePlayerCard == null || card.getValue() > maxValuePlayerCard
									.getValue())) {
						maxValuePlayerCard = card;
					}
				}
			} else {
				for (Card card : allCards) {
					if (!listFixedPair.contains(card)) {
						maxValuePlayerCard = card;
						break;
					}
				}
			}
			if (maxValuePlayerCard != null)
				listFixedPair.add(maxValuePlayerCard);
		}

		if (listFixedPair.size() == 5) {
			handBestCards = new Card[5];
			handBestCards[0] = listFixedPair.get(0);
			handBestCards[1] = listFixedPair.get(1);
			handBestCards[2] = listFixedPair.get(2);
			handBestCards[3] = listFixedPair.get(3);
			handBestCards[4] = listFixedPair.get(4);
			return checkCardsOccurance(playerHandList);
		} else {
			return false;
		}
	}

	// Created by chirag
	public boolean isPairRank(Card[] allCards, List<Card> playerHandList) {
		List<Card> listFixedPair = new ArrayList<Card>();
		List<Card> listPairCards = new ArrayList<Card>();
		int totalPlayerCards = 0;
		for (Card card : allCards) {
			for (Card card1 : allCards) {
				if (card.getRank() == card1.getRank()
						&& card.getSuit() != card1.getSuit()) {
					if (playerHandList.contains(card)
							|| playerHandList.contains(card1)) {
						if (!listFixedPair.contains(card1)) {
							if (playerHandList.contains(card1))
								totalPlayerCards++;
							if (playerHandList.contains(card))
								totalPlayerCards++;
							listFixedPair.add(card1);
							listFixedPair.add(card);
						}
					}else {
						if(!listPairCards.contains(card1))
						listPairCards.add(card1);
						if(!listPairCards.contains(card))
						listPairCards.add(card);
					}
				}
			}
		}
		if (listFixedPair.isEmpty()) {
			if(!listPairCards.isEmpty()){
				Card maxValueCard = null;
				for (Card card : listPairCards) {
					if (maxValueCard == null
							|| card.getValue() > maxValueCard.getValue()) {
						maxValueCard = card;
					}
				}
				for (Card card : listPairCards) {
					if (card.getRank() == maxValueCard.getRank()
							&& !listFixedPair.contains(card)) {
						listFixedPair.add(card);
					}
				}
			}else{
				return false;
			}
		} if(!listFixedPair.isEmpty()) {

			Card maxValuePlayerCard = null;
			while (totalPlayerCards != 2) {
				for (Card card : playerHandList) {
					if (!listFixedPair.contains(card)
							&& (maxValuePlayerCard == null || card.getValue() > maxValuePlayerCard
									.getValue())) {
						maxValuePlayerCard = card;
					}
				}
				if (maxValuePlayerCard != null && !listFixedPair.contains(maxValuePlayerCard)){
					listFixedPair.add(maxValuePlayerCard);
					maxValuePlayerCard=null;
					totalPlayerCards++;
				}
			}
			
			for (Card card : allCards) {
				if (listFixedPair.size() != 5 && !listFixedPair.contains(card)) {
					listFixedPair.add(card);
				}
			}

			if (listFixedPair.size() == 5) {
				handBestCards = new Card[5];
				handBestCards[0] = listFixedPair.get(0);
				handBestCards[1] = listFixedPair.get(1);
				handBestCards[2] = listFixedPair.get(2);
				handBestCards[3] = listFixedPair.get(3);
				handBestCards[4] = listFixedPair.get(4);
				return checkCardsOccurance(playerHandList);
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean checkForSinglePlayerCards(Card[] allCards,
			List<Card> playerHandList) {

		List<Card> listPairCards = new ArrayList<Card>();
		List<Card> listOtherCards = new ArrayList<Card>();
		for (Card card : handBestCards) {
			if (!listPairCards.contains(card)) {
				for (Card card1 : handBestCards) {
					if (card.getRank() == card1.getRank()
							&& card.getSuit() != card1.getSuit()) {
						listPairCards.add(card1);
						listPairCards.add(card);
					}
				}
			}
		}
		for (Card card : allCards) {
			if (!listPairCards.contains(card)) {
				listOtherCards.add(card);
			}
		}
		System.out.println("High Pair Cards===================");
		for (Card card : listPairCards) {
			System.out.println("= " + card.getCardName());
		}
		System.out.println("Other Cards ===================");
		for (Card card : listOtherCards) {
			System.out.println("=> " + card.getCardName());
		}
		System.out.println("Check other card have pair ===================");

		for (Card card : listOtherCards) {

			for (Card card1 : listOtherCards) {
				if (card.getRank() == card1.getRank()
						&& card.getSuit() != card1.getSuit()) {
					System.out.println("Check other card have pair ===>> YES");
					if (playerHandList.contains(card1)
							|| playerHandList.contains(card)) {
						System.out.println("It is Player hand card ===>> YES");
						handBestCards[0] = card;
						handBestCards[1] = card1;
						Card playerExtraCard = null;
						for (Card playerCards : playerHandList) {
							if (!playerCards.getCardName().equals(
									card1.getCardName())
									&& !playerCards.getCardName().equals(
											card.getCardName())) {
								if (playerExtraCard == null
										|| playerExtraCard.getValue() < playerCards
												.getValue()) {
									playerExtraCard = playerCards;
								}
							}
						}
						handBestCards[2] = playerExtraCard;
						for (Card card2 : listPairCards) {
							for (Card card3 : listPairCards) {
								if (card2.getValue() <= card3.getValue()) {
									if (card2.getValue() == card3.getValue()
											&& card2.getSuit() != card3
													.getSuit()) {
										handBestCards[3] = card2;
										handBestCards[4] = card3;
										return true;
									}
								}
							}
						}

					} else {
						System.out.println("It is Player hand card ===>> NO");
					}
				}
			}

		}
		return false;

	}

	public boolean checkPairInPlayerList(List<Card> playerCardList) {
		for (int i = 0; i < playerCardList.size() - 1; i++) {
			if (playerCardList.get(i).getValue() == playerCardList.get(i + 1)
					.getValue()) {
				handBestCards[handBestCards.length - 1] = playerCardList
						.get(i + 1);
				handBestCards[handBestCards.length - 2] = playerCardList.get(i);
				return true;
			}
		}

		return false;

	}

	public boolean makePairWithPlayerCards(List<Card> playerHandList) {
		List<Card> playerDefaultList = sortingCardsList(defaultCards);
		boolean flag = false;
		int k = 0;
		// Card[] bestCardList = new Card[5];
		List<Card> bestCardList = new ArrayList<Card>();
		for (int i = 0; i < playerHandList.size(); i++) {
			for (int j = 0; j < playerDefaultList.size(); j++) {
				if (playerHandList.get(i).getValue() == playerDefaultList
						.get(j).getValue()) {

					bestCardList.add(k, playerHandList.get(i));
					bestCardList.add(++k, playerDefaultList.get(j));

					if (k == handBestCards.length - 3) {
						for (int m = 0; m < handBestCards.length; m++) {
							for (int n = 0; n < bestCardList.size(); n++) {
								if (handBestCards[m].getCardName().equals(
										bestCardList.get(n).cardName)) {
									flag = true;
								}
							}
							if (!flag) {
								bestCardList.set(k, handBestCards[m]);
								flag = false;
								k++;
								if (k == handBestCards.length) {
									break;
								}
							}

						}

						for (int p = 0; p < bestCardList.size(); p++) {
							handBestCards[p] = bestCardList.get(p);
						}
						handBestCards = sortingCardsArray(handBestCards);
						return true;
					}
					// continue;
				}

			}

		}
		return false;

	}


	// public Card[] GetPokerHandArray() {
	public ArrayList<Card> getPokerHandArray() {
		ArrayList<Card> pokerHands = new ArrayList<Card>();
		for (Card c : handBestCards) {
			pokerHands.add(c);
		}
		return pokerHands;
		// return this.PokerHandFinalCards;
	}

	public ArrayList<String> getWinnerHandCardNameList() {
		ArrayList<String> pokerHands = new ArrayList<String>();
		for (Card c : handBestCards) {
			pokerHands.add(c.getCardName());
		}
		return pokerHands;
		// return this.PokerHandFinalCards;
	}
}
