package pokerserver.handrank;

import java.util.ArrayList;
import java.util.List;

import pokerserver.cards.Card;
import pokerserver.players.PlayerBean;
import pokerserver.utils.GameConstants;

//http://www.cardplayer.com/rules-of-poker/hand-rankings
public class GeneralHandManager implements GameConstants {
	public HAND_RANK playerHandRank;
	List<Card> defaultCards = new ArrayList<Card>();
	List<Card> playerCards = new ArrayList<Card>();
	List<Card> playerBestHandCard = new ArrayList<Card>();
	List<Card> mainHandCards = new ArrayList<Card>();
	int PLAYER_CARD_LIMIT_FOR_HAND = 0;
	final int BEST_HAND_CARD_LIMIT = 5;

	public GeneralHandManager(int playerCardLimit) {
		this.PLAYER_CARD_LIMIT_FOR_HAND = playerCardLimit;
	}

	public List<Card> setPlayerAndDefaultCards(List<Card> defaultCards,List<Card> playerCards) {
//		this.defaultCards.clear();
//		this.playerCards.clear();
		this.defaultCards = descendingSortedCards(defaultCards);
		this.playerCards = descendingSortedCards(playerCards);
		List<Card> allTableCards = new ArrayList<Card>();
		 allTableCards.addAll(this.defaultCards);
		 allTableCards.addAll(this.playerCards);
		 allTableCards = descendingSortedCards(allTableCards);
		 return allTableCards;
	}
	
	public  void generatePlayerBestRank(List<Card> listDefaultCards,PlayerBean playerBean){
		
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		HAND_RANK handRank = null;
		List<Card> listPlayersCards = new ArrayList<Card>();
		List<Card> listDefaultTableCards = new ArrayList<Card>();
//		List<Card> mainHandCards = new ArrayList<>();
		listPlayersCards.add(playerBean.getFirstCard());
		listPlayersCards.add(playerBean.getSecondCard());
		listDefaultTableCards.addAll(listDefaultCards);
		if(playerBean.getWACardStatus()==ACTION_WA_DOWN){
			System.out.println("=========== WA Down");
			listPlayersCards.add(playerBean.getWACard());
		}else if(playerBean.getWACardStatus()== ACTION_WA_UP){
			System.out.println("=========== WA UP");
			listDefaultTableCards.add(playerBean.getWACard());
		}
		System.out.println("=========== Default Cards >> "+listDefaultTableCards.size());
		for(Card card : listDefaultTableCards){
			System.out.println(card.getCardName());	
		}
		System.out.println("=========== Player Cards");
		for(Card card : listPlayersCards){
			System.out.println(card.getCardName());	
		}
		List<Card> listBestCards = new ArrayList<Card>();
		List<Card> listAllTableCards = setPlayerAndDefaultCards(listDefaultTableCards, listPlayersCards);
		if (isRoyalFlushRank(listAllTableCards)) {
			handRank= HAND_RANK.ROYAL_FLUSH;
//			mainHandCards.addAll(getPlayerBestCards());
		} else if (isStraightFlushRank(listAllTableCards)) {
			handRank= HAND_RANK.STRAIGHT_FLUSH;
//			mainHandCards.addAll(getPlayerBestCards());
		} else if (isFourOfKindRank(listAllTableCards)) {
			handRank= HAND_RANK.FOUR_OF_A_KIND;
//			mainHandCards.addAll(listAllSameRankCards(listAllTableCards).get(0));
		} else if (isFullHouseRank(listAllTableCards)) {
			handRank= HAND_RANK.FULL_HOUSE;
//			mainHandCards.addAll(getPlayerBestCards());
		} else if (isFlushRank(listAllTableCards)) {
			handRank= HAND_RANK.FLUSH;
//			mainHandCards.addAll(getPlayerBestCards());
		} else if (isStraightRank(listAllTableCards)) {
			handRank= HAND_RANK.STRAIGHT;
//			mainHandCards.addAll(getPlayerBestCards());
		} else if(isThreeOfKindRank(listAllTableCards)){
			handRank= HAND_RANK.THREE_OF_A_KIND;
//			mainHandCards.addAll(listAllSameRankCards(listAllTableCards).get(0));
		} else if(isTwoPairRank(listAllTableCards)){
			handRank= HAND_RANK.TWO_PAIR;
//			mainHandCards.addAll(listAllSameRankCards(listAllTableCards).get(0));
//			mainHandCards.addAll(listAllSameRankCards(listAllTableCards).get(1));
		} else if(isPairRank(listAllTableCards)){
			handRank= HAND_RANK.PAIR;
//			mainHandCards.addAll(listAllSameRankCards(listAllTableCards).get(0));
		}  else if(isHighCardRank(listAllTableCards)){
			handRank= HAND_RANK.HIGH_CARD;
//			mainHandCards.addAll(getPlayerBestCards());
		}else {
			handRank= null;
		}
		if (handRank!=null) {
			listBestCards = getPlayerBestCards();
			Card[] bestCard = new Card[listBestCards.size()];
			System.out.println("Player : "+playerBean.getPlayerName()+" >> Rank : "+ handRank);
			for (int i=0;i<listBestCards.size();i++) {
				System.out.println(listBestCards.get(i).getCardName());
				bestCard[i]=listBestCards.get(i);
			}
//			System.out.println("--------------=====");
//			for (int i=0;i<mainHandCards.size();i++) {
//				System.out.println(mainHandCards.get(i).getCardName());
//			}
			playerBean.setPlayersBestHand(handRank, bestCard,mainHandCards);
		}
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}
	public List<Card> getPlayerBestCards() {
		return playerBestHandCard;
	}

	/**
	 * A, K, Q, J, 10, all the same suit.
	 * 
	 * @return boolean
	 */
	public boolean isRoyalFlushRank(List<Card> allCards) {
		// Get all straight cards list from all table cards
		for (List<Card> listStraight : listAllAvailableStraightCards(allCards)) {
			// Check if straight cards start with ace
			if (listStraight.get(0).getRank() == Card.RANKS.ace) {
				// Check list have same suits
				Card diffSuitCard = isSameSuitCards(listStraight);
				if (diffSuitCard == null) {
					// Check list have player limit cards
					if (isValidCardsForPlayerHand(listStraight)) {
						playerBestHandCard.clear();playerBestHandCard.addAll(descendingSortedCards(listStraight));
						mainHandCards.clear();mainHandCards.addAll(listStraight);
						return true;
					}
				} else {
					// Find main suit from list
					Card.SUITS suit = getMainSuitFromList(listStraight);
					Card sameSuitCardFromPlayerHand = getSameRankSameSuitCardFromList(
							allCards, suit, diffSuitCard.getRank());
					if (sameSuitCardFromPlayerHand != null) {
						listStraight.remove(diffSuitCard);
						listStraight.add(sameSuitCardFromPlayerHand);
						listStraight = descendingSortedCards(listStraight);
						// Re-check list for royal flush
						if (isRoyalFlushRank(listStraight)) {
							playerBestHandCard.clear();	playerBestHandCard.addAll(descendingSortedCards(listStraight));
							mainHandCards.clear();mainHandCards.addAll(listStraight);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Five cards in a sequence, all in the same suit.
	 * 
	 * @return boolean
	 */
	public boolean isStraightFlushRank(List<Card> allCards) {
		// Get all straight cards list from all table cards
		for (List<Card> listStraight : listAllAvailableStraightCards(allCards)) {
			// Check list have same suits
			Card diffSuitCard = isSameSuitCards(listStraight);
			if (diffSuitCard == null) {
				// Check list have player limit cards
				if (isValidCardsForPlayerHand(listStraight)) {
					playerBestHandCard.clear();
					playerBestHandCard.addAll(descendingSortedCards(listStraight));
					mainHandCards.clear();mainHandCards.addAll(listStraight);
					return true;
				}
			} else {
				// Find main suit from list
				Card.SUITS suit = getMainSuitFromList(listStraight);
				Card sameSuitCardFromPlayerHand = getSameRankSameSuitCardFromList(
						playerCards, suit, diffSuitCard.getRank());
				if (sameSuitCardFromPlayerHand != null) {
					if (sameSuitCardFromPlayerHand != diffSuitCard) {
						listStraight.remove(diffSuitCard);
						listStraight.add(sameSuitCardFromPlayerHand);
						listStraight = descendingSortedCards(listStraight);
						if (isStraightFlushRank(listStraight)) {
							playerBestHandCard.clear();
							playerBestHandCard.addAll(descendingSortedCards(listStraight));
							mainHandCards.clear();mainHandCards.addAll(listStraight);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * All four cards of the same rank.
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isFourOfKindRank(List<Card> allCards) {
		// Fetch all same ranks cards in list
		for (List<Card> listCards : listAllSameRankCards(allCards)) {
			// Check all ranks are in list
			if (listCards.size() == 4) {
				int totalPlayersCard = getTotalPlayerCards(listCards);
				Card bigCard = null;
				// If player's card is 0 then break
				if (totalPlayersCard == 0) {
					break;
				} else if (totalPlayersCard == PLAYER_CARD_LIMIT_FOR_HAND) {
					// Find maximum cards from default cards which are not list
					bigCard = getBiggestCardFromList(defaultCards, listCards);
				} else {
					// Find maximum cards from player hand which are not list
					bigCard = getBiggestCardFromList(playerCards, listCards);
				}
				// Main hand cards
				mainHandCards.clear();mainHandCards.addAll(listCards);
				listCards.add(bigCard);
				if (isValidCardsForPlayerHand(listCards)) {
					playerBestHandCard.clear();
					playerBestHandCard.addAll(descendingSortedCards(listCards));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Three of a kind with a pair.
	 * 
	 * Pending =========== Default Cards five_club queen_spade queen_diamond
	 * ace_diamond four_club ace_club ace_spade =========== Player Cards
	 * ace_heart queen_club ===================
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isFullHouseRank(List<Card> allCards) {
		List<Card> listFullHouseCards = new ArrayList<Card>();
		List<List<Card>> listOfListSameRankCards = listAllSameRankCards(allCards);
		List<List<Card>> listOfListThreePairCards = new ArrayList<List<Card>>();
		List<List<Card>> listOfListPairCards = new ArrayList<List<Card>>();
		for (List<Card> listCards : listOfListSameRankCards) {
			if (listCards.size() == 3) {
				if (!isListIsAlreadyAdded(listOfListThreePairCards, listCards))
					listOfListThreePairCards.add(listCards);
				// Fetch all pairs from 3 cards
				listOfListPairCards.addAll(listAllPairCards(listCards));
			} else if (listCards.size() == 4) {
				listOfListThreePairCards
						.addAll(listAllThreePairCards(listCards));
				listOfListPairCards.addAll(listAllPairCards(listCards));
			} else if (listCards.size() == 2) {
				if (!isListIsAlreadyAdded(listOfListPairCards, listCards))
					listOfListPairCards.add(listCards);
			}
		}

		// Fetch all same ranks cards in list
		for (List<Card> listThreePairCards : listOfListThreePairCards) {
			for (List<Card> listPairCards : listOfListPairCards) {
				boolean isSameCardInPair = false;
				for (Card pairCard : listPairCards) {
					if (listThreePairCards.contains(pairCard)) {
						isSameCardInPair = true;
						break;
					}
				}
				if (!isSameCardInPair) {
					listFullHouseCards.clear();
					listFullHouseCards.addAll(listThreePairCards);
					listFullHouseCards.addAll(listPairCards);

					if (isValidCardsForPlayerHand(checkBestCardsWithRank(listFullHouseCards))) {
						playerBestHandCard.clear();
						playerBestHandCard.addAll(descendingSortedCards(listFullHouseCards));
						// Main hand cards
						mainHandCards.clear();mainHandCards.addAll(listFullHouseCards);
						return true;
					}

				}
			}
		}
		return false;
	}

	/**
	 * Any five cards of the same suit, but not in a sequence.
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isFlushRank(List<Card> allCards) {
		// Fetch same suits cards
		for (List<Card> listCards : listAllSameSuitCards(allCards)) {
			if (listCards.size() >= BEST_HAND_CARD_LIMIT) {
				if (PLAYER_CARD_LIMIT_FOR_HAND != 0) {
					if (isValidCardsForPlayerHand(checkBestCardsWithSuit(listCards))) {
						playerBestHandCard.clear();
						playerBestHandCard.addAll(descendingSortedCards(listCards));
						mainHandCards.clear();mainHandCards.addAll(listCards);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Five cards in a sequence, but not of the same suit.
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isStraightRank(List<Card> allCards) {
		for (List<Card> listCards : listAllAvailableStraightCards(allCards)) {
			if (listCards.size() >= BEST_HAND_CARD_LIMIT) {
				if (isValidCardsForPlayerHand(checkBestCardsWithRank(listCards))) {
					playerBestHandCard.clear();
					playerBestHandCard.addAll(descendingSortedCards(listCards));
					mainHandCards.clear();mainHandCards.addAll(listCards);
					return true;
				}
				// break;
			}
		}
		return false;
	}

	/**
	 * Three of a kind with a pair.
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isThreeOfKindRank(List<Card> allCards) {
		List<List<Card>> listOfListSameRankCards = listAllSameRankCards(allCards);
		List<List<Card>> listOfListThreePairCards = new ArrayList<List<Card>>();
		for (List<Card> listCards : listOfListSameRankCards) {
			if (listCards.size() == 3) {
				if (!isListIsAlreadyAdded(listOfListThreePairCards, listCards))
					listOfListThreePairCards.add(listCards);
			} else if (listCards.size() == 4) {
				listOfListThreePairCards
						.addAll(listAllThreePairCards(listCards));
			}
		}
		for (List<Card> listCards : listOfListThreePairCards) {
			// If player card = 2 or player card limit =0
			int totalPlayerCards = getTotalPlayerCards(listCards);
			if (PLAYER_CARD_LIMIT_FOR_HAND != 0)
				if (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
					while (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
						for (int i = listCards.size() - 1; i >= 0; i--) {
							if (playerCards.contains(listCards.get(i))) {
								Card sameRankCard = getSameRankDifferentSuitCardFromList(
										defaultCards, listCards.get(i));
								if (sameRankCard != null
										&& !listCards.contains(sameRankCard)) {
									listCards.remove(listCards.get(i));
									listCards.add(sameRankCard);
									totalPlayerCards--;
								}
								break;
							}
						}
						break;
					}
					mainHandCards.clear();mainHandCards.addAll(listCards);
				} else {
					mainHandCards.clear();mainHandCards.addAll(listCards);
					// Add player cards
					for (Card card : playerCards) {
						if (totalPlayerCards < PLAYER_CARD_LIMIT_FOR_HAND
								&& !listCards.contains(card)) {
							listCards.add(card);
							totalPlayerCards++;
						}
					}
				}
			// Add other max card from default
			for (Card card : allCards) {
				if (!listCards.get(0).getRank().equals(card.getRank())
						&& listCards.size() < BEST_HAND_CARD_LIMIT) {
					listCards.add(card);
				}
			}
			if (isValidCardsForPlayerHand(listCards)) {
				playerBestHandCard.clear();
				playerBestHandCard.addAll(descendingSortedCards(listCards));
				return true;
			}
		}
		return false;
	}

	/**
	 * Two different pairs.
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isTwoPairRank(List<Card> allCards) {
		List<Card> listTwoPairCard = new ArrayList<Card>();
		List<List<Card>> listOfListSameRankCards = listAllSameRankCards(allCards);
		List<List<Card>> listOfListPairCards = new ArrayList<List<Card>>();
		for (List<Card> listCards : listOfListSameRankCards) {
			if (listCards.size() == 3) {
				// Fetch all pairs from 3 cards
				listOfListPairCards.addAll(listAllPairCards(listCards));
			} else if (listCards.size() == 4) {
				listOfListPairCards.addAll(listAllPairCards(listCards));
			} else if (listCards.size() == 2) {
				if (!isListIsAlreadyAdded(listOfListPairCards, listCards))
					listOfListPairCards.add(listCards);
			}
		}

		for (int i = 0; i < listOfListPairCards.size(); i++) {
			listTwoPairCard.clear();
			for (int j = i + 1; j < listOfListPairCards.size(); j++) {
				boolean isSameCardInPair = false;
				for (Card pairCard : listOfListPairCards.get(j)) {
					if (listOfListPairCards.get(i).contains(pairCard)) {
						isSameCardInPair = true;
						break;
					}
				}
				if (!isSameCardInPair) {
					listTwoPairCard.clear();
					listTwoPairCard.addAll(listOfListPairCards.get(i));// Add
																		// first
																		// pair
					listTwoPairCard.addAll(listOfListPairCards.get(j));// Add
																		// second
																		// pair

					int totalPlayerCards = getTotalPlayerCards(listTwoPairCard);
					if (PLAYER_CARD_LIMIT_FOR_HAND != 0)
						if (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
							while (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
								for (int k = listTwoPairCard.size() - 1; k >= 0; k--) {
									if (playerCards.contains(listTwoPairCard
											.get(i))) {
										Card sameRankCard = getSameRankDifferentSuitCardFromList(
												defaultCards,
												listTwoPairCard.get(k));
										if (sameRankCard != null
												&& !listTwoPairCard
														.contains(sameRankCard)) {
											listTwoPairCard
													.remove(listTwoPairCard
															.get(k));
											listTwoPairCard.add(sameRankCard);
											totalPlayerCards--;
										}
										break;
									}
								}
								break;
							}
						
						} else {
							// Add player cards
							for (Card card : playerCards) {
								if (totalPlayerCards < PLAYER_CARD_LIMIT_FOR_HAND
										&& !listTwoPairCard.contains(card)) {
									for (int l = listTwoPairCard.size() - 1; l >= 0; l--) {
										if (!playerCards
												.contains(listTwoPairCard
														.get(l))
												&& card.getRank() == listTwoPairCard
														.get(l).getRank()) {
											listTwoPairCard
													.remove(listTwoPairCard
															.get(l));
											listTwoPairCard.add(card);
											totalPlayerCards++;
											break;
										}
									}
								}
							}
						}
					mainHandCards.clear();mainHandCards.addAll(listTwoPairCard);
					// If total player card is 1
					if (totalPlayerCards == 1) {
						for (Card card : playerCards) {
							if (!listTwoPairCard.contains(card)
									&& listTwoPairCard.size() < BEST_HAND_CARD_LIMIT) {
								listTwoPairCard.add(card);
							}
						}
					} else {
						// Add other max card from default
						for (Card card : defaultCards) {
							if (!listTwoPairCard.contains(card)
									&& listTwoPairCard.size() < BEST_HAND_CARD_LIMIT) {
								listTwoPairCard.add(card);
							}
						}
					}
					if (isValidCardsForPlayerHand(listTwoPairCard)) {
						playerBestHandCard.clear();
						playerBestHandCard
								.addAll(descendingSortedCards(listTwoPairCard));
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Two cards of the same rank.
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isPairRank(List<Card> allCards) {
		List<Card> listPairCard = new ArrayList<Card>();
		List<List<Card>> listOfListSameRankCards = listAllSameRankCards(allCards);
		List<List<Card>> listOfListPairCards = new ArrayList<List<Card>>();
		for (List<Card> listCards : listOfListSameRankCards) {
			if (listCards.size() == 3) {
				// Fetch all pairs from 3 cards
				listOfListPairCards.addAll(listAllPairCards(listCards));
			} else if (listCards.size() == 4) {
				listOfListPairCards.addAll(listAllPairCards(listCards));
			} else if (listCards.size() == 2) {
				if (!isListIsAlreadyAdded(listOfListPairCards, listCards))
					listOfListPairCards.add(listCards);
			}
		}

		for (int i = 0; i < listOfListPairCards.size(); i++) {
			listPairCard.clear();
			listPairCard.addAll(listOfListPairCards.get(i));// Add first pair

			int totalPlayerCards = getTotalPlayerCards(listPairCard);

			// Replace same rank card from player hand
			for (Card card : playerCards) {
				if (totalPlayerCards < PLAYER_CARD_LIMIT_FOR_HAND
						&& !listPairCard.contains(card)) {
					for (int l = listPairCard.size() - 1; l >= 0; l--) {
						if (!playerCards.contains(listPairCard.get(l))
								&& card.getRank() == listPairCard.get(l)
										.getRank()) {
							listPairCard.remove(listPairCard.get(l));
							listPairCard.add(card);
							totalPlayerCards++;
							break;
						}
					}
				}
			}
			mainHandCards.clear();mainHandCards.addAll(listPairCard);
			// Add player max cards
			for (Card card : playerCards) {
				if (!listPairCard.contains(card)
						&& totalPlayerCards < PLAYER_CARD_LIMIT_FOR_HAND
						&& listPairCard.size() < BEST_HAND_CARD_LIMIT) {
					listPairCard.add(card);
					totalPlayerCards++;
				}
			}

			// Add other max card from default
			for (Card card : defaultCards) {
				if (!listPairCard.contains(card)
						&& listPairCard.size() < BEST_HAND_CARD_LIMIT) {
					listPairCard.add(card);
				}
			}

			if (isValidCardsForPlayerHand(listPairCard)) {
				playerBestHandCard.clear();
				playerBestHandCard.addAll(descendingSortedCards(listPairCard));
				return true;
			}
		}
		return false;
	}

	/**
	 * When you haven't made any of the hands above, the highest card plays.
	 * 
	 * @param allCards
	 * @return boolean
	 */
	public boolean isHighCardRank(List<Card> allCards) {
		List<Card> listHighCards = new ArrayList<Card>();
		int totalPlayerCards = 0;
		for (Card card : playerCards) {
			if (!listHighCards.contains(card)
					&& totalPlayerCards < PLAYER_CARD_LIMIT_FOR_HAND) {
				listHighCards.add(card);
				totalPlayerCards++;
			}
		}
		for (Card card : defaultCards) {
			if (!listHighCards.contains(card)
					&& listHighCards.size() < BEST_HAND_CARD_LIMIT) {
				listHighCards.add(card);
			}
		}
		if (isValidCardsForPlayerHand(listHighCards)) {
			listHighCards = descendingSortedCards(listHighCards);
			playerBestHandCard.clear();
			playerBestHandCard.addAll(descendingSortedCards(listHighCards));
			mainHandCards.clear();mainHandCards.addAll(listHighCards);
			return true;
		}
		return false;
	}

	/**
	 * It will sort card in descending order. e.g 6,5,4,3,2,1
	 */
	public List<Card> descendingSortedCards(List<Card> allCards) {
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

	/**
	 * Find biggest card from list which is not already added in fixed rank card
	 * list.
	 * 
	 * @param cardList
	 * @param fixedRankCardList
	 * @return biggestCard
	 */
	public Card getBiggestCardFromList(List<Card> cardList,
			List<Card> fixedRankCardList) {
		Card maxValueCard = null;
		for (Card card : cardList) {
			if (maxValueCard == null
					|| card.getValue() > maxValueCard.getValue()) {
				if (!fixedRankCardList.contains(card))
					maxValueCard = card;
			}
		}
		return maxValueCard;
	}

	/**
	 * Fetch all three pairs cards from all same rank cards e.g (j,j,j,j)
	 * 
	 * @param allCards
	 * @return List<List<Card>>
	 */
	public List<List<Card>> listAllThreePairCards(List<Card> allCards) {
		List<List<Card>> listAllSameRankCards = new ArrayList<List<Card>>();
		if (allCards.size() == 4) {
			List<Card> listSameCards = new ArrayList<Card>();
			listSameCards.add(allCards.get(0));
			listSameCards.add(allCards.get(1));
			listSameCards.add(allCards.get(2));
			listAllSameRankCards.add(listSameCards);
			listSameCards = new ArrayList<Card>();
			listSameCards.add(allCards.get(0));
			listSameCards.add(allCards.get(2));
			listSameCards.add(allCards.get(3));
			listAllSameRankCards.add(listSameCards);
			listSameCards = new ArrayList<Card>();
			listSameCards.add(allCards.get(1));
			listSameCards.add(allCards.get(2));
			listSameCards.add(allCards.get(3));
			listAllSameRankCards.add(listSameCards);

		}
		return listAllSameRankCards;
	}

	/**
	 * Fetch all pairs cards from same rank cards e.g j,j,j,j
	 * 
	 * @param allCards
	 * @return List<List<Card>>
	 */
	public List<List<Card>> listAllPairCards(List<Card> allCards) {
		List<List<Card>> listAllSameRankCards = new ArrayList<List<Card>>();
		if (allCards.size() >= 3) {
			List<Card> listSameCards = new ArrayList<Card>();

			listSameCards.add(allCards.get(0));
			listSameCards.add(allCards.get(1));
			listAllSameRankCards.add(listSameCards);
			listSameCards = new ArrayList<Card>();
			listSameCards.add(allCards.get(0));
			listSameCards.add(allCards.get(2));
			listAllSameRankCards.add(listSameCards);
			if (allCards.size() == 4) {
				listSameCards = new ArrayList<Card>();
				listSameCards.add(allCards.get(0));
				listSameCards.add(allCards.get(3));
				listAllSameRankCards.add(listSameCards);
			}
			listSameCards = new ArrayList<Card>();
			listSameCards.add(allCards.get(1));
			listSameCards.add(allCards.get(2));
			listAllSameRankCards.add(listSameCards);
			if (allCards.size() == 4) {
				listSameCards = new ArrayList<Card>();
				listSameCards.add(allCards.get(1));
				listSameCards.add(allCards.get(3));
				listAllSameRankCards.add(listSameCards);
				listSameCards = new ArrayList<Card>();
				listSameCards.add(allCards.get(2));
				listSameCards.add(allCards.get(3));
				listAllSameRankCards.add(listSameCards);
			}

		}
		return listAllSameRankCards;
	}

	/**
	 * Find same rank cards from the all cards e.g 1. two of spade, two of heart
	 * 2. three of heart, three of club, three of spade
	 * 
	 * @param allCards
	 * @return List<List<Card>>
	 */
	public List<List<Card>> listAllSameRankCards(List<Card> allCards) {
		List<List<Card>> listAllSameRankCards = new ArrayList<List<Card>>();
		for (Card currentCard : allCards) {
			List<Card> listSameCards = new ArrayList<Card>();
			for (Card card : allCards) {
				if (card.getRank().equals(currentCard.getRank())
						&& !listSameCards.contains(card)) {
					listSameCards.add(card);
				}
			}
			if (!isListIsAlreadyAdded(listAllSameRankCards, listSameCards)) {
				listAllSameRankCards.add(listSameCards);
			}
		}
		return listAllSameRankCards;
	}

	/**
	 * Check if list already added or not
	 * 
	 * @param listOfListCards
	 * @param listCards
	 * @return boolean
	 */
	public boolean isListIsAlreadyAdded(List<List<Card>> listOfListCards,
			List<Card> listCards) {

		if (listOfListCards.isEmpty()) {
			return false;
		}
		for (List<Card> listCurrentCards : listOfListCards) {
			boolean isSameList = false;
			for (int i = 0; i < listCurrentCards.size(); i++) {
				if (listCurrentCards.size() != listCards.size()) {
					break;
				}
				if (listCurrentCards.get(i) == listCards.get(i)) {
					isSameList = true;
				} else {
					isSameList = false;
				}
			}
			if (isSameList) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find same suit cards from the all cards e.g 1. two of spade, three of
	 * spade 2. three of heart, four of heart, five of heart
	 * 
	 * @param allCards
	 * @return List<List<Card>>
	 */
	public List<List<Card>> listAllSameSuitCards(List<Card> listCards) {
		List<List<Card>> listAllSameSuitCards = new ArrayList<List<Card>>();
		for (Card currentCard : listCards) {
			List<Card> listSameCards = new ArrayList<Card>();
			for (Card card : listCards) {
				if (card.getSuit().equals(currentCard.getSuit())
						&& !listSameCards.contains(card)) {
					listSameCards.add(card);
					if (listSameCards.size() == BEST_HAND_CARD_LIMIT) {
						break;
					}
				}
			}
			if (!isListIsAlreadyAdded(listAllSameSuitCards, listSameCards)) {
				listAllSameSuitCards.add(listSameCards);
			}
		}
		return listAllSameSuitCards;
	}

	/**
	 * Get list of all available straight cards from the all cards . e.g
	 * 10,9,8,7,6,5
	 * 
	 * @param sortedCards
	 * @return List<List<Card>>
	 */
	public List<List<Card>> listAllAvailableStraightCards(List<Card> allCards) {
		List<List<Card>> listAllStraightCards = new ArrayList<List<Card>>();
		Card aceCard = null;
		for (int i = 0; i < allCards.size(); i++) {

			List<Card> listStraightCards = new ArrayList<Card>();
			Card prevCard = allCards.get(i);
			if (prevCard.getRank().equals(Card.RANKS.ace)) {
				aceCard = prevCard;
			}
			listStraightCards.add(prevCard);

			for (int j = i + 1; j < allCards.size(); j++) {
				Card currentCard = allCards.get(j);
				if (currentCard.getRank().equals(Card.RANKS.ace)) {
					aceCard = currentCard;
				}
				if (currentCard.getValue() + 1 == prevCard.getValue()) {
					listStraightCards.add(currentCard);
					prevCard = currentCard;
					if (listStraightCards.size() == BEST_HAND_CARD_LIMIT) {
						break;
					}
				}
			}
			if (aceCard!=null && listStraightCards.size() == 4
					&& listStraightCards.get(listStraightCards.size() - 1)
							.getRank().equals(Card.RANKS.two)) {
				listStraightCards.add(aceCard);
			}
			if (listStraightCards.size() == BEST_HAND_CARD_LIMIT
					&& !isListIsAlreadyAdded(listAllStraightCards,
							listStraightCards)) {
				listAllStraightCards.add(listStraightCards);
			}
		}
		return listAllStraightCards;
	}

	/**
	 * Check is valid cards for player hands.
	 * 
	 * @param listCards
	 * @return boolean
	 */
	public boolean isValidCardsForPlayerHand(List<Card> listCards) {
		if (listCards.size() != BEST_HAND_CARD_LIMIT) {
			return false;
		}
		if (PLAYER_CARD_LIMIT_FOR_HAND == 0) {
			return true;
		}
		if (getTotalPlayerCards(listCards) == PLAYER_CARD_LIMIT_FOR_HAND) {
			return true;
		}
		return false;
	}

	public int getTotalPlayerCards(List<Card> listCards) {
		int totalCards = 0;
		for (Card card : listCards) {
			if (playerCards.contains(card)) {
				totalCards++;
			}
		}
		return totalCards;
	}

	/**
	 * Check all list cards have same suits or not. e.g 1. two of spade, three
	 * of spade 2. three of heart, four of heart, five of heart
	 * 
	 * @param listCards
	 * @return boolean
	 */
	public Card isSameSuitCards(List<Card> listCards) {
		Card card = listCards.get(0);
		for (Card card2 : listCards) {
			if (card.getSuit() != card2.getSuit()) {
				return card2;
			}
		}
		return null;
	}

	/**
	 * Get maximum number of suits from list
	 * 
	 * @param allCards
	 * @return Card.SUITS
	 */
	public Card.SUITS getMainSuitFromList(List<Card> allCards) {
		int suitCntr = 0;
		Card.SUITS mainSuit = null;
		for (List<Card> listCard : listAllSameSuitCards(allCards)) {
			if (suitCntr < listCard.size()) {
				suitCntr = listCard.size();
				mainSuit = listCard.get(0).getSuit();
			}
		}

		return mainSuit;
	}

	public Card getSameSuitDifferentRankCardFromList(List<Card> listCard,
			Card selectedCard) {
		for (Card card : listCard) {
			if (card.getSuit() == selectedCard.getSuit())
				return card;
		}
		return null;
	}

	public Card getSameRankDifferentSuitCardFromList(List<Card> listCard,
			Card selectedCard) {
		for (Card card : listCard) {
			if (card.getRank() == selectedCard.getRank())
				return card;
		}
		return null;
	}

	public Card getSameRankSameSuitCardFromList(List<Card> listCard,
			Card.SUITS suit, Card.RANKS rank) {
		for (Card card : listCard) {
			if (card.getRank() == rank && card.getSuit() == suit)
				return card;
		}
		return null;
	}

	public List<Card> checkBestCardsWithSuit(List<Card> listHandCards) {
		int totalPlayerCards = getTotalPlayerCards(listHandCards);
		if (PLAYER_CARD_LIMIT_FOR_HAND != 0)
			// Check if player cards is higher then limit
			if (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
				System.out.println("Total Player Cards : " + totalPlayerCards);
				while (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
					for (int i = listHandCards.size() - 1; i >= 0; i--) {
						if (playerCards.contains(listHandCards.get(i))) {
							Card sameSuitCard = getSameSuitDifferentRankCardFromList(
									defaultCards, listHandCards.get(i));
							if (sameSuitCard != null
									&& !listHandCards.contains(sameSuitCard)) {
								listHandCards.remove(listHandCards.get(i));
								listHandCards.add(sameSuitCard);
								totalPlayerCards--;
							}
							break;
						}
					}
					break;
				}
			} else {
				for (Card card : playerCards) {
					if (totalPlayerCards < PLAYER_CARD_LIMIT_FOR_HAND
							&& !listHandCards.contains(card)) {
						for (int i = listHandCards.size() - 1; i >= 0; i--) {
							if (!playerCards.contains(listHandCards.get(i))
									&& card.getSuit() == listHandCards.get(i)
											.getSuit()) {
								listHandCards.remove(listHandCards.get(i));
								listHandCards.add(card);
								totalPlayerCards++;
								break;
							}
						}
					}
				}
			}
		return listHandCards;
	}

	public List<Card> checkBestCardsWithRank(List<Card> listHandCards) {
		int totalPlayerCards = getTotalPlayerCards(listHandCards);
		if (PLAYER_CARD_LIMIT_FOR_HAND != 0)
			// Check if player cards is higher then limit
			if (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
				System.out.println("Total Player Cards : " + totalPlayerCards);
				while (totalPlayerCards > PLAYER_CARD_LIMIT_FOR_HAND) {
					for (int i = listHandCards.size() - 1; i >= 0; i--) {
						if (playerCards.contains(listHandCards.get(i))) {
							Card sameRankCard = getSameRankDifferentSuitCardFromList(
									defaultCards, listHandCards.get(i));
							if (sameRankCard != null
									&& !listHandCards.contains(sameRankCard)) {
								listHandCards.remove(listHandCards.get(i));
								listHandCards.add(sameRankCard);
								totalPlayerCards--;
							}
							break;
						}
					}
					break;
				}
			} else {
				for (Card card : playerCards) {
					if (totalPlayerCards < PLAYER_CARD_LIMIT_FOR_HAND
							&& !listHandCards.contains(card)) {
						for (int i = listHandCards.size() - 1; i >= 0; i--) {
							if (!playerCards.contains(listHandCards.get(i))
									&& card.getRank() == listHandCards.get(i)
											.getRank()) {
								listHandCards.remove(listHandCards.get(i));
								listHandCards.add(card);
								totalPlayerCards++;
								break;
							}
						}
					}
				}
			}
		return listHandCards;
	}

	/** 
	 * Manage multiple royal flush winning players. Winning will be decided on joining order 
	 * @param listPlayers
	 * @return listPlayers
	 */
	public List<PlayerBean> manageSameRoyalFlushRank(List<PlayerBean> listPlayers){
		return listPlayers;
	}
	
}
