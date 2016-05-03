package pokerserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pokerserver.cards.Card;
import pokerserver.handrank.GeneralHandManager;
import pokerserver.players.AllInPlayer;
import pokerserver.players.PlayerBean;
import pokerserver.players.PlayersManager;
import pokerserver.players.Winner;
import pokerserver.players.WinnerManager;
import pokerserver.rounds.RoundManager;
import pokerserver.turns.TurnManager;
import pokerserver.utils.GameConstants;

/**
 * Manage player, round and all other tasks
 * 
 * @author Chirag
 * 
 */
public class TexassGameManager implements GameConstants {

	PlayersManager playersManager;
	// HandManager handManager;
	GeneralHandManager handManager;
	ArrayList<Card> listDefaultCards = new ArrayList<Card>();
	ArrayList<Card> listTableCards = new ArrayList<Card>();
	RoundManager preflopRound;
	RoundManager flopRound;
	RoundManager turnRound;
	RoundManager riverRound;
	int currentRound = 0;
	WinnerManager winnerManager;
	int totalBBPlayersTurn = 0;
	int totalGameCntr=0;
	
	public TexassGameManager() {
		playersManager = new PlayersManager();
	}

	public void initGameRounds() {
		System.out
				.println("================== Texass Game started ==================");
		handManager = new GeneralHandManager(TEXASS_PLAYER_CARD_LIMIT_FOR_HAND);
		winnerManager = new WinnerManager(playersManager, handManager);
		generateDefaultCards();
		preflopRound = new RoundManager(TEXASS_ROUND_PREFLOP);
		flopRound = new RoundManager(TEXASS_ROUND_FLOP);
		turnRound = new RoundManager(TEXASS_ROUND_TURN);
		riverRound = new RoundManager(TEXASS_ROUND_RIVER);
		totalBBPlayersTurn = 0;
		if(totalGameCntr>playersManager.getTotalActivePlayerCounter())
			totalGameCntr=0;
		playersManager.setCurrentGameCntr(totalGameCntr++);
		// handManager = new HandManager(listDefaultCards);
		// startPreFlopRound();
	}

	public RoundManager getCurrentRoundInfo() {
		if (preflopRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return preflopRound;
		} else if (flopRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return flopRound;
		} else if (turnRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return turnRound;
		} else if (riverRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return riverRound;
		}
		return null;
	}

	public void addNewPlayerToGame(PlayerBean player) {
		// handManager.findPlayerBestHand(player.getPlayerCards());
		handManager.generatePlayerBestRank(listDefaultCards, player);
		for (Card card : player.getBestHandCards()) {
			
			System.out.println("Player Best Cards : " + card.getCardName());
		}
		this.playersManager.addNewPlayerInRoom(player);
	}

	public void leavePlayerToGame(PlayerBean player) {
		this.playersManager.removePlayerFromRoom(player);
	}

	public PlayerBean getPlayerFromPosition(int position) {
		return this.playersManager.getPlayer(position);
	}

	public PlayersManager getPlayersManager() {
		return playersManager;
	}

	public ArrayList<Card> getDefaultCards() {
		return listDefaultCards;
	}

	public void setPlayersManager(PlayersManager playersManager) {
		this.playersManager = playersManager;
	}

	public int getCurrentRoundIndex() {
		return currentRound;
	}

	/**
	 * Start pre flop round and set other round status as a pending
	 */
	public void startPreFlopRound() {
		currentRound = TEXASS_ROUND_PREFLOP;
		System.out.println(">>>>>>>>>>> Preflop Round started");
		preflopRound.setStatus(ROUND_STATUS_ACTIVE);
		flopRound.setStatus(ROUND_STATUS_PENDING);
		turnRound.setStatus(ROUND_STATUS_PENDING);
		riverRound.setStatus(ROUND_STATUS_PENDING);
	}

	public void startFlopRound() {
		currentRound = TEXASS_ROUND_FLOP;
		System.out.println(">>>>>>>>>>> Flop Round started  ");
		preflopRound.setStatus(ROUND_STATUS_FINISH);
		flopRound.setStatus(ROUND_STATUS_ACTIVE);
		turnRound.setStatus(ROUND_STATUS_PENDING);
		riverRound.setStatus(ROUND_STATUS_PENDING);
	}

	public void startTurnRound() {
		currentRound = TEXASS_ROUND_TURN;
		System.out.println(">>>>>>>>>>> Turn Round started  ");
		preflopRound.setStatus(ROUND_STATUS_FINISH);
		flopRound.setStatus(ROUND_STATUS_FINISH);
		turnRound.setStatus(ROUND_STATUS_ACTIVE);
		riverRound.setStatus(ROUND_STATUS_PENDING);
	}

	public void startRiverRound() {
		currentRound = TEXASS_ROUND_RIVER;
		System.out.println(">>>>>>>>>>> River Round started  ");
		preflopRound.setStatus(ROUND_STATUS_FINISH);
		flopRound.setStatus(ROUND_STATUS_FINISH);
		turnRound.setStatus(ROUND_STATUS_FINISH);
		riverRound.setStatus(ROUND_STATUS_ACTIVE);
	}

	public RoundManager getPreflopRound() {
		return preflopRound;
	}

	public RoundManager getFlopRound() {
		return flopRound;
	}

	public RoundManager getTurnRound() {
		return turnRound;
	}

	public RoundManager getRiverRound() {
		return riverRound;
	}

	// public ArrayList<String> getWinnerCards() {
	// return gamePlay.getWinnerCards();
	// }

	/**
	 * Return last active player.
	 * 
	 * @return PlayerBean
	 */
	public PlayerBean checkAllAreFoldOrAllIn() {
		PlayerBean lastPlayer = null;
		int totalActivePlayersCnt = 0;
		int totalAllInPlayers = 0;
		int maxPlayerAmt = 0;
		PlayerBean lastAllInPlayer = null;
		for (PlayerBean playerBean : playersManager.getAllAvailablePlayers()) {
			int betAmt = getCurrentRoundInfo().getTotalPlayerBetAmount(playerBean);
			if(maxPlayerAmt< betAmt){
				maxPlayerAmt=betAmt;
			}
			if (!playerBean.isAllIn()){
				if (!playerBean.isFolded()) {
					lastPlayer = playerBean;
					totalActivePlayersCnt++;
				}
			}else{
				totalAllInPlayers++;
				lastAllInPlayer=playerBean;
			}
			if (totalActivePlayersCnt == 2) {
				return null;
			}
		}
		if(lastPlayer ==null && lastAllInPlayer != null){
			return lastAllInPlayer;
		}
		if(totalAllInPlayers == playersManager.getAllAvailablePlayers().size()-1 || totalActivePlayersCnt==1){
			int activePlrBet = getCurrentRoundInfo().getTotalPlayerBetAmount(
					lastPlayer);
			if (activePlrBet < maxPlayerAmt) {
				return null;
			}
			return lastPlayer;
		}else if(totalAllInPlayers == playersManager.getAllAvailablePlayers().size()){
			return lastAllInPlayer;
		}
		return lastPlayer;
	}

	public void findBestPlayerHand() {
		for (PlayerBean player : playersManager.getAllAvailablePlayers()) {
			if (!player.isFolded())
				handManager.generatePlayerBestRank(listDefaultCards, player);
		}
	}

	public PlayerBean deductPlayerBetAmountFromBalance(String name, int amount,
			int action) {
		for (PlayerBean player : playersManager.getAllAvailablePlayers()) {
			if (player.getPlayerName().equals(name)) {
				if (action == ACTION_ALL_IN) {
					player.setPlayerAllIn(true);
				}
				if (action == ACTION_FOLD) {
					player.setPlayerFolded(true);
				} else if (!player.isFolded()) {
					player.deductBetAmount(amount);
				}
				return player;
			}
		}
		return null;
	}

	/**
	 * In this function, It will checking all player have equal bet amount on
	 * table. If yes then you have to start new round.
	 * 
	 * @return
	 */
	public boolean checkEveryPlayerHaveSameBetAmount() {

		ArrayList<PlayerBetBean> totalPlayerWiseBetAmount = new ArrayList<PlayerBetBean>();
		RoundManager currentRound = getCurrentRoundInfo();
		int maxPlayerBetAmt = 0;
		boolean allPlayersAreAllIn = true;
		for (PlayerBean player : playersManager.getAllAvailablePlayers()) {
			int totalBetAmt =currentRound.getTotalPlayerBetAmount(player);
			if(maxPlayerBetAmt<totalBetAmt){
				maxPlayerBetAmt= totalBetAmt;
			}
			if (!player.isFolded() && !player.isAllIn()) {
				allPlayersAreAllIn = false;
				totalPlayerWiseBetAmount.add(new PlayerBetBean(currentRound
						.getTotalPlayerBetAmount(player), currentRound
						.getPlayerLastAction(player)));
			}
		}
		Collections.sort(totalPlayerWiseBetAmount,
				new Comparator<PlayerBetBean>() {
					@Override
					public int compare(PlayerBetBean paramT1,
							PlayerBetBean paramT2) {
						return Integer.compare(paramT1.getBetAmount(),
								paramT2.getBetAmount());
					}
				});

		boolean allPlayerHaveTurn = true;
		// Checking all players checked
		for (PlayerBetBean c : totalPlayerWiseBetAmount) {
			if (c.getLastAction() == ACTION_PENDING) {
				allPlayerHaveTurn = false;
			}
		}
		if (allPlayerHaveTurn && allPlayersAreAllIn) {
			return true;
		}
//		PlayerBetBean lastPlayerBetAmt = totalPlayerWiseBetAmount.get(0);
//		totalPlayerWiseBetAmount.remove(0);
		// Checking all players have same bet amount
		for (PlayerBetBean currentPlayer : totalPlayerWiseBetAmount) {
//			if (currentPlayerBetAmt.getBetAmount() != lastPlayerBetAmt
//					.getBetAmount()) {
				if(currentPlayer.getBetAmount()!=maxPlayerBetAmt){
					return false;
				}
//			}
		}
		if (!allPlayerHaveTurn) {
			return false;
		}
		if(totalBBPlayersTurn==1){
			return false;
		}
		return true;
	}

	public int getPlayerTotalBetAmount(String name) {
		PlayerBean player = playersManager.getPlayerByName(name);
		int totalBetAmount = 0;
		totalBetAmount += preflopRound.getTotalPlayerBetAmount(player);
		totalBetAmount += flopRound.getTotalPlayerBetAmount(player);
		totalBetAmount += turnRound.getTotalPlayerBetAmount(player);
		totalBetAmount += riverRound.getTotalPlayerBetAmount(player);
		return totalBetAmount;
	}

	public void moveToNextRound() {
		switch (currentRound) {
		case TEXASS_ROUND_PREFLOP:
			calculatePotAmountForAllInMembers();
			startFlopRound();
			break;
		case TEXASS_ROUND_FLOP:
			calculatePotAmountForAllInMembers();
			startTurnRound();
			break;
		case TEXASS_ROUND_TURN:
			calculatePotAmountForAllInMembers();
			startRiverRound();
			break;
		case TEXASS_ROUND_RIVER:
			calculatePotAmountForAllInMembers();
			getCurrentRoundInfo().setStatus(ROUND_STATUS_FINISH);
			break;
		}

	}

	/** It will generate flop(3), turn(1) and river(1) cards. Total 5 cards */
	public void generateDefaultCards() {
		listDefaultCards.clear();
		listTableCards.clear();
		while (listDefaultCards.size() != 5) {
			Card cardBean = new Card();
			if (!isAlreadyDesributedCard(cardBean)) {
				
				System.out.println("Default card : " + cardBean.getCardName());
				listDefaultCards.add(cardBean);
			}
		}
	}

	/** Check card is already on table or not */
	public boolean isAlreadyDesributedCard(Card cardBean) {
		for (Card cardBean2 : listTableCards) {
			if (cardBean.getCardName().equals(cardBean2.getCardName())) {
				return true;
			}
		}
		listTableCards.add(cardBean);
		return false;
	}

	public Card generatePlayerCards() {
		Card cardBean = new Card();
		while (isAlreadyDesributedCard(cardBean)) {
			cardBean.generateRandomCard();
		}
		return cardBean;
	}

	/** Handles player's action taken by him */
	public TurnManager managePlayerAction(String userName, int userAction,
			int betAmount) {
		// gamePlay.executePlayerAction(betAmount, userAction);
		return addCurrentActionToTurnManager(userName, betAmount, userAction);
	}

	private TurnManager addCurrentActionToTurnManager(String userName,
			int betAmount, int action) {

		TurnManager turnManager = null;
		PlayerBean currentPlayer = deductPlayerBetAmountFromBalance(userName,
				betAmount, action);
		if(currentRound == TEXASS_ROUND_PREFLOP&&currentPlayer.isBigBlind()){
			totalBBPlayersTurn++;
		}
		if (currentPlayer != null) {
			if(currentPlayer.getTotalBalance()==0){
				action = ACTION_ALL_IN;
			}
			RoundManager currentRoundManger = getCurrentRoundInfo();
			turnManager = new TurnManager(currentPlayer, action, betAmount);
			currentRoundManger.addTurnRecord(turnManager);
			
			System.out.println("Turn Manager # User: "
					+ currentPlayer.getPlayerName() + " # Action: " + action
					+ " # Bet: " + betAmount + " # Round: "
					+ currentRoundManger.getRound());
		}
		return turnManager;
	}

	class PlayerBetBean {
		int betAmount = 0;
		int lastAction = ACTION_PENDING;

		public PlayerBetBean(int totalBet, int lastAction) {
			this.betAmount = totalBet;
			this.lastAction = lastAction;
		}

		public int getBetAmount() {
			return betAmount;
		}

		public int getLastAction() {
			return lastAction;
		}
	}

	public Winner getTopWinner() {
		return winnerManager.getTopWinner();
	}

	public ArrayList<String> getAllWinnerName() {
		ArrayList<String> listWinners = new ArrayList<String>();
		for (Winner winner : winnerManager.getWinnerList()) {
			listWinners.add(winner.getPlayer().getPlayerName());
		}
		return listWinners;
	}

	public ArrayList<Winner> getAllWinnerPlayers() {
		return winnerManager.getWinnerList();
	}

	public void findAllWinnerPlayers() {
		winnerManager.findWinnerPlayers();
	}

	public List<PlayerBean> generateWinnerPlayers() {
		return winnerManager.generateWinnerPlayers();
	}

	public void calculatePotAmountForAllInMembers() {
		int allInBetTotalAmount = 0;
		int tempCurrentRound = 0;
		for (int i = 0; i < playersManager.getAllAvailablePlayers().size(); i++) {
			PlayerBean player = playersManager.getAllAvailablePlayers().get(i);
			boolean isAllIn = player.isAllIn();
			int lastAction = getCurrentRoundInfo().getPlayerLastAction(player);
			if (isAllIn
					&& winnerManager.getAllInPotAmount(player.getPlayerName()) == 0) {

				int allInBetAmt = getCurrentRoundInfo()
						.getPlayerBetAmountAtActionAllIn(player);
				for (int j = 0; j < playersManager.getAllAvailablePlayers()
						.size(); j++) {
					if (allInBetAmt < getCurrentRoundInfo()
							.getTotalPlayerBetAmount(
									playersManager.getAllAvailablePlayers()
											.get(j))) {

						allInBetTotalAmount = allInBetTotalAmount + allInBetAmt;

					} else {
						allInBetTotalAmount = allInBetTotalAmount
								+ getCurrentRoundInfo()
										.getTotalPlayerBetAmount(
												playersManager
														.getAllAvailablePlayers()
														.get(j));

					}
				}

				if (preflopRound.getRound() < getCurrentRoundInfo().getRound())
					allInBetTotalAmount += preflopRound
							.getTotalRoundBetAmount();
				if (flopRound.getRound() < getCurrentRoundInfo().getRound())
					allInBetTotalAmount += flopRound.getTotalRoundBetAmount();
				if (turnRound.getRound() < getCurrentRoundInfo().getRound())
					allInBetTotalAmount += turnRound.getTotalRoundBetAmount();
				AllInPlayer allInPlayer = new AllInPlayer(
						player.getPlayerName(), allInBetTotalAmount);
				winnerManager.addAllInTotalPotAmount(allInPlayer);

			}

		}

	}

	public void setTotalTableBetAmount() {
		int totalBetAmount = 0;
		totalBetAmount += preflopRound.getTotalRoundBetAmount();
		totalBetAmount += flopRound.getTotalRoundBetAmount();
		totalBetAmount += turnRound.getTotalRoundBetAmount();
		totalBetAmount += riverRound.getTotalRoundBetAmount();
		winnerManager.setTotalTableAmount(totalBetAmount);
	}

	public int getTotalTableAmount() {
		int totalBetAmount = 0;
		totalBetAmount += preflopRound.getTotalRoundBetAmount();
		totalBetAmount += flopRound.getTotalRoundBetAmount();
		totalBetAmount += turnRound.getTotalRoundBetAmount();
		totalBetAmount += riverRound.getTotalRoundBetAmount();

		winnerManager.setTotalTableAmount(totalBetAmount);
		
		System.out.println("Total Bet Amount : " + totalBetAmount);
		return totalBetAmount;
	}
}
