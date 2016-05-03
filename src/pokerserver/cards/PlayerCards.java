package pokerserver.cards;

public class PlayerCards {

	public Card cardOne;
	public Card cardTwo;

	public PlayerCards(Card cardOne, Card cardTwo) {
		this.cardOne = cardOne;
		this.cardTwo = cardTwo;
	}

	public Card getFirstCard() {
		return cardOne;
	}

	public Card getSecondCard() {
		return cardTwo;
	}
}