package pokerserver.rounds;

import java.util.ArrayList;

import pokerserver.players.PlayerBean;
import pokerserver.turns.TurnManager;
import pokerserver.utils.GameConstants;

public class RoundManager implements GameConstants {
	/** Current status of round. e.g active, finish, pending */
	int status;
	/** Track all player action in round */
	ArrayList<TurnManager> listTurn;
	int currentRound;

	public RoundManager(int currentRound) {
		// TODO Auto-generated constructor stub
		this.currentRound = currentRound;
		this.listTurn = new ArrayList<TurnManager>();
	}

	/**
	 * Add turn record when player performed his action
	 */
	public void addTurnRecord(TurnManager turnManager) {
		this.listTurn.add(turnManager);
	}

	/**
	 * Fetch all turn's data in current round
	 * 
	 * @return ArrayList<TurnManager>
	 */
	public ArrayList<TurnManager> getAllTurnRecords() {
		return listTurn;
	}

	/**
	 * Get current status of round it may be ROUND_STATUS_ACTIVE,
	 * ROUND_STATUS_PENDING, ROUND_STATUS_FINISH
	 * 
	 * @return index
	 */
	public int getStatus() {
		return status;
	}

	public int getRound() {
		return this.currentRound;
	}

	/**
	 * Update current status of round it may be ROUND_STATUS_ACTIVE,
	 * ROUND_STATUS_PENDING, ROUND_STATUS_FINISH
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	public int getTotalPlayerBetAmount(PlayerBean player) {
		int totalBet = 0;
		for (TurnManager turnManager : listTurn) {
			if (turnManager.getPlayer().getPlayerName()
					.equals(player.getPlayerName())) {
				totalBet += turnManager.getBetAmount();
			}
		}
		return totalBet;
	}

	public int getPlayerLastAction(PlayerBean player) {
		int lastAction = ACTION_PENDING;

		for (TurnManager turnManager : listTurn) {
			if (turnManager.getPlayer().getPlayerName()
					.equals(player.getPlayerName())) {
				lastAction = turnManager.getPlayerAction();
			}
		}
		return lastAction;
	}

	public int getTotalRoundBetAmount() {
		int totalBet = 0;
		for (TurnManager turnManager : listTurn) {
			totalBet += turnManager.getBetAmount();
		}
		return totalBet;
	}

	public int getPlayerBetAmountAtActionAllIn(PlayerBean player) {
		int action = ACTION_PENDING;
		int lastBetAmount = 0;
		for (TurnManager turnManager : listTurn) {
			if (turnManager.getPlayer().getPlayerName()
					.equals(player.getPlayerName())) {
				action = turnManager.getPlayerAction();
				if (action == ACTION_ALL_IN) {
					lastBetAmount = turnManager.getBetAmount();
				}
			}
		}
		return lastBetAmount;
	}
}
