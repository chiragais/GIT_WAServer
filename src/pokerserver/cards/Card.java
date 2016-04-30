package pokerserver.cards;

import java.util.Random;

import pokerserver.utils.GameConstants;

public class Card implements GameConstants {

	public SUITS suit;
	public RANKS ranks;
	public String cardName = "";

	public Card() {
		generateRandomCard();
	}
	public Card(String suit,String rank){
		this.cardName = suit+"_"+rank;
		SetSuit(suit);
		SetRank(rank);
	}
	public void generateRandomCard() {
		generateRandomSuitIndex();
		generateRandomRankIndex();
	}
	
	public void SetSuit(String suit) {
		switch (suit) {
		case SUIT_HEART:
			this.suit = SUITS.heart;
			break;
		case SUIT_SPADE:
			this.suit = SUITS.spade;
			break;
		case SUIT_DIAMOND:
			this.suit = SUITS.diamond;
			break;
		case SUIT_CLUB:
			this.suit = SUITS.club;
			break;
		}
	}

	public void SetRank(String rank) {
		switch (rank) {
		case RANK_TWO:
			this.ranks = RANKS.two;
			break;
		case RANK_THREE:
			this.ranks = RANKS.three;
			break;
		case RANK_FOUR:
			this.ranks = RANKS.four;
			break;
		case RANK_FIVE:
			this.ranks = RANKS.five;
			break;
		case RANK_SIX:
			this.ranks = RANKS.six;
			break;
		case RANK_SEVEN:
			this.ranks = RANKS.seven;
			break;
		case RANK_EIGHT:
			this.ranks = RANKS.eight;
			break;
		case RANK_NINE:
			this.ranks = RANKS.nine;
			break;
		case RANK_TEN:
			this.ranks = RANKS.ten;
			break;
		case RANK_JACK:
			this.ranks = RANKS.jack;
			break;
		case RANK_QUEEN:
			this.ranks = RANKS.queen;
			break;
		case RANK_KING:
			this.ranks = RANKS.king;
			break;
		case RANK_ACE:
			this.ranks = RANKS.ace;
			break;
		}
	}

	public RANKS getRank() {
		return ranks;
	}

	public SUITS getSuit() {
		return suit;
	}

	public String getCardName() {
		return ranks + "_" + suit;
	}

	public int getValue() {
		switch (ranks.toString()) {
		case RANK_TWO:
			return 2;
		case RANK_THREE:
			return 3;
		case RANK_FOUR:
			return 4;
		case RANK_FIVE:
			return 5;
		case RANK_SIX:
			return 6;
		case RANK_SEVEN:
			return 7;
		case RANK_EIGHT:
			return 8;
		case RANK_NINE:
			return 9;
		case RANK_TEN:
			return 10;
		case RANK_JACK:
			return 11;
		case RANK_QUEEN:
			return 12;
		case RANK_KING:
			return 13;
		case RANK_ACE:
			return 14;
		default:
			return 0;
		}
	}

	public void generateRandomSuitIndex() {
		int min = 0;
		int max = SUITS.values().length - 1;
		Random random = new Random();
		int index = random.nextInt((max - min) + 1) + min;
		this.suit = SUITS.values()[index];
	}

	public void generateRandomRankIndex() {
		int min = 0;
		int max = RANKS.values().length - 1;
		Random random = new Random();
		int index = random.nextInt((max - min) + 1) + min;
		this.ranks = RANKS.values()[index];
	}
}
