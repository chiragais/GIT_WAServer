package pokerserver.turns;

import pokerserver.players.PlayerBean;
import pokerserver.utils.GameConstants;

/**
 * Manage player's turn in every rounds
 * 
 * @author Chirag
 *
 */
public class TurnManager implements GameConstants{

	
	/** Player info regarding current turn*/
	PlayerBean player;
	/** Call,Fold etc...*/
	int playerAction;
	int betAmount;
	
	
	public TurnManager(PlayerBean player,int playerAction,int betAmount) {
		// TODO Auto-generated constructor stub
		this.player=player;
		this.playerAction = playerAction;
		this.betAmount = betAmount;
	}

	
	public PlayerBean getPlayer() {
		return player;
	}

	public void setPlayer(PlayerBean player) {
		this.player = player;
	}

	public int getPlayerAction() {
		return playerAction;
	}

	public void setPlayerAction(int playerAction) {
		this.playerAction = playerAction;
	}

	public int getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(int betAmount) {
		this.betAmount = betAmount;
	}
	
	
	
}
