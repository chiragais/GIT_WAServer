package pokerserver.players;

import java.util.ArrayList;
import java.util.List;

import pokerserver.cards.Card;
import pokerserver.cards.PlayerCards;
import pokerserver.utils.GameConstants.HAND_RANK;

/**
 * Player related informations like name, id, cards on hands etc.
 * 
 * @author Chirag
 * 
 */
public class PlayerBean {
	
	
	private String playerName;
	private int playerId;
	private int totalBalance = 1000;
//	private boolean isActivePlayer = true;
	private PlayerCards cards;
	private Card waCard;
	private int wACardStatus;
	private boolean isDealer;
	private boolean isSmalBlind;
	private boolean isBigBlind;
	private boolean isAllInPlayer;
	private boolean isFoldedPlayer;
	private HAND_RANK handRank;
	private int winningIndex = 0;
	private Card[] handBestCards = new Card[5];
	private List<Card> mainHandCards = new ArrayList<Card>();
    
	// private int betAmount;

	public PlayerBean() {
	}

	public PlayerBean(int pId, String pName) {
		this.playerId = pId;
		this.playerName = pName;
	}

	public void deductBetAmount(int betAmount) {
		this.totalBalance -= betAmount;
	}

	public void setSmallBlind(boolean b) {
		this.isSmalBlind = b;
	}

	public boolean isSmallBlind() {
		return this.isSmalBlind;
	}

	public void setDealer(boolean b) {
		this.isDealer = b;
	}

	public boolean isDealer() {
		return this.isDealer;
	}
	
	public void setBigBlind(boolean b) {
		this.isBigBlind = b;
	}

	public boolean isBigBlind() {
		return this.isBigBlind;
	}

	public void setPlayerId(int pId) {
		this.playerId = pId;
	}

	public void setPlayerName(String pName) {
		this.playerName = pName;
	}

	public void setTotalBalance(int playersBal) {
		this.totalBalance = playersBal;
	}

	public void setWinningIndex (int index){
		this.winningIndex=index;
	}
	public int getWinningIndex(){
		return winningIndex;
	}
	public void setWACardStatus(int status){
		this.wACardStatus=status;
	}
	
	public int getWACardStatus(){
		return this.wACardStatus;
	}
	public Card getWACard() {
		return waCard;
	}

	public int getTotalBalance() {
		return this.totalBalance;
	}

	
	public int getBestHandRankTotal(){
		int totalRank =0;
		for(Card card :handBestCards ){
			totalRank += card.getValue();
		}
		return totalRank;
	}
//	public void setPlayerActive(boolean isActive) {
//		this.isActivePlayer = isActive;
//	}
	public void setCards(Card card1, Card card2, Card waCard) {
		this.cards = new PlayerCards(card1, card2);
		this.waCard = waCard;
		System.out.println();
		System.out.println(playerName +" >> Card 1: "+ card1.getCardName()+" >> Card 2: "+ card2.getCardName()+" >> WA Card : " + waCard.getCardName());
	}

	public PlayerCards getPlayerCards() {
		return this.cards;
	}

	public Card getFirstCard() {
		return this.cards.getFirstCard();
	}

	public Card getSecondCard() {
		return this.cards.getSecondCard();
	}

	public int getPlayerId() {
		return this.playerId;
	}

	public String getPlayerName() {
		return this.playerName;
	}

//	public boolean isPlayerActive() {
//		return this.isActivePlayer;
//	}

	public boolean isAllIn() {
		return this.isAllInPlayer;
	}

	public void setPlayerAllIn(boolean b) {
		this.isAllInPlayer = b;
	}

	public boolean isFolded() {
		return this.isFoldedPlayer;
	}

	public void setPlayerFolded(boolean b) {
		this.isFoldedPlayer = b;
	}

	public void setPlayersBestHand(HAND_RANK hand, Card[] listCard, List<Card> listMainHandCards) {
		this.handRank = hand;
		this.handBestCards = listCard;
		this.mainHandCards.clear();
		for(Card card : listMainHandCards){
			this.mainHandCards.add(card);			
		}
	}

	public HAND_RANK getHandRank() {
		return handRank;
	}

	// public Card[] getBestHandCards(){
	// return handBestCards;
	// }
	public ArrayList<Card> getBestHandCards() {
		ArrayList<Card> pokerHands = new ArrayList<Card>();
		for (Card c : handBestCards) {
			pokerHands.add(c);
		}
		return pokerHands;

	}

	public List<Card> getMainHandCards(){
		return mainHandCards;
	}
	
	public Card[] getBestHandCardsArray() {
		
		return handBestCards;

	}
	
	
	public List<String> getBestHandCardsName() {
		List<String> listCard = new ArrayList<String>();
		for (Card card : handBestCards) {
			listCard.add(card.getCardName());
		}
		return listCard;
	}
}
