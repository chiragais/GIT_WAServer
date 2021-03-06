package pokerserver.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pokerserver.cards.Card;
import pokerserver.handrank.GeneralHandManager;
import pokerserver.players.AllInPlayer;
import pokerserver.players.PlayerBean;
import pokerserver.players.PlayersManager;
import pokerserver.rounds.RoundManager;
import pokerserver.turns.TurnManager;
import pokerserver.utils.GameConstants;
import pokerserver.utils.LogUtils;
import pokerserver.winner.Winner;
import pokerserver.winner.WinnerManager;

/**
 * Manage player, round and all other tasks
 * 
 * @author Chirag
 * 
 */
public class TexassGameManager implements GameConstants {

	PlayersManager playersManager;
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
	int bliendAmount = SBAmount;
	int tournamentEntryFee = 1000;
	boolean isTournamentStarted = false;
	public boolean isReBuyChips = false;
	/**
	 * Game type : Regular WA/TH or Tournament
	 */
	int gameType = GAME_TYPE_REGULAR;
	/**
	 * For tournament only
	 */
	int newBliendAmt = SBAmount;
	/**
	 * This is for temporary base. This list is for store player balance in last game. 
	 */
	ArrayList<PlayerBean> listTournamentPlayers = new ArrayList<PlayerBean>();
	
	public TexassGameManager() {
		playersManager = new PlayersManager();
	}

	public void initGameRounds() {
		System.out
				.println("\n================== Texass Game started ==================");
		this.bliendAmount = newBliendAmt;
		currentRound=0;
		handManager = new GeneralHandManager(TEXASS_PLAYER_CARD_LIMIT_FOR_HAND);
		winnerManager = new WinnerManager(playersManager, handManager);
		generateDefaultCards();
		preflopRound = new RoundManager(TEXASS_ROUND_PREFLOP);
		flopRound = new RoundManager(TEXASS_ROUND_FLOP);
		turnRound = new RoundManager(TEXASS_ROUND_TURN);
		riverRound = new RoundManager(TEXASS_ROUND_RIVER);
		totalBBPlayersTurn = 0;
		
	}

	public RoundManager getCurrentRoundInfo() {
		if (preflopRound.getStatus() == STATUS_ACTIVE) {
			return preflopRound;
		} else if (flopRound.getStatus() == STATUS_ACTIVE) {
			return flopRound;
		} else if (turnRound.getStatus() == STATUS_ACTIVE) {
			return turnRound;
		} else if (riverRound.getStatus() == STATUS_ACTIVE) {
			return riverRound;
		}
		return null;
	}

	public void addNewPlayerToGame(PlayerBean player) {
		//handManager.findPlayerBestHand(player.getPlayerCards());
		//handManager.generatePlayerBestRank(listDefaultCards, player);
//		for (Card card : player.getBestHandCards()) {		
		//LogUtils.Log("Player Best Cards : " + card.getCardName());
//		}
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

	public boolean isTournamentStarted() {
		return isTournamentStarted;
	}

	public void setTournamentStarted(boolean isTournamentStarted) {
		this.isTournamentStarted = isTournamentStarted;
	}

	public void setGameType(int gameType){
		this.gameType = gameType;
	}
	public int getGameType (){
		return gameType;
	}
	public int getBliendAmount(){
		return bliendAmount;
	}
	public void setNewBliendAmount(int amt){
		this.newBliendAmt = amt;
	}
	/**
	 * Start pre flop round and set other round status as a pending
	 */
	public void startPreFlopRound() {
		currentRound = TEXASS_ROUND_PREFLOP;
		LogUtils.Log(">>>>>>>>>>> Preflop Round started");
		preflopRound.setStatus(STATUS_ACTIVE);
		flopRound.setStatus(STATUS_PENDING);
		turnRound.setStatus(STATUS_PENDING);
		riverRound.setStatus(STATUS_PENDING);
	}

	public void startFlopRound() {
		currentRound = TEXASS_ROUND_FLOP;
		LogUtils.Log(">>>>>>>>>>> Flop Round started  ");
		preflopRound.setStatus(STATUS_FINISH);
		flopRound.setStatus(STATUS_ACTIVE);
		turnRound.setStatus(STATUS_PENDING);
		riverRound.setStatus(STATUS_PENDING);
	}

	public void startTurnRound() {
		currentRound = TEXASS_ROUND_TURN;
		LogUtils.Log(">>>>>>>>>>> Turn Round started  ");
		preflopRound.setStatus(STATUS_FINISH);
		flopRound.setStatus(STATUS_FINISH);
		turnRound.setStatus(STATUS_ACTIVE);
		riverRound.setStatus(STATUS_PENDING);
	}

	public void startRiverRound() {
		currentRound = TEXASS_ROUND_RIVER;
		LogUtils.Log(">>>>>>>>>>> River Round started  ");
		preflopRound.setStatus(STATUS_FINISH);
		flopRound.setStatus(STATUS_FINISH);
		turnRound.setStatus(STATUS_FINISH);
		riverRound.setStatus(STATUS_ACTIVE);
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

	public void updateTotalGameCntr(int totalPlr){
		if(totalGameCntr>totalPlr)
			totalGameCntr=0;
		playersManager.setDealerPosition(totalGameCntr++);
	}
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
		for (PlayerBetBean currentPlayer : totalPlayerWiseBetAmount) {
				if(currentPlayer.getBetAmount()!=maxPlayerBetAmt){
					return false;
				}
		}
		if (!allPlayerHaveTurn) {
			return false;
		}
		LogUtils.Log("Total BB Turn : "+ totalBBPlayersTurn);
		if(totalBBPlayersTurn==1){
			return false;
		}
		return true;
	}

	public int getPlayerTotalBetAmountInAllRounds(String name) {
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
			getCurrentRoundInfo().setStatus(STATUS_FINISH);
			break;
		}

	}

	/** It will generate flop(3), turn(1) and river(1) cards. Total 5 cards */
	public void generateDefaultCards() {
//		LogUtils.Log("Generate Default Cards " );
		listDefaultCards.clear();
		listTableCards.clear();
		while (listDefaultCards.size() != 5) {
			Card cardBean = new Card();
			if (!isAlreadyDesributedCard(cardBean)) {
				
//				LogUtils.Log("Default card : " + cardBean.getCardName());
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
//		LogUtils.Log("Total Cards on table : " + listTableCards.size());
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
	public TurnManager managePlayerAction(String userName, int action,
			int betAmount) {
		TurnManager turnManager = null;
		PlayerBean currentPlayer = deductPlayerBetAmountFromBalance(userName,
				betAmount, action);
		PlayerBean bbPlayer = getPlayersManager()
				.getBigBlindPayer();
		boolean isBB = bbPlayer.getPlayerName().equals(currentPlayer.getPlayerName());
//		LogUtils.Log("<><> "+currentPlayer.getPlayerName()+" >>BB : "+ isBB+" >>DLR : "+currentPlayer.isDealer());
		if(currentRound == TEXASS_ROUND_PREFLOP&& isBB){
			totalBBPlayersTurn++;
		}
		if (currentPlayer != null) {
			if(currentPlayer.getBalance()==0){
				action = ACTION_ALL_IN;
			}
			RoundManager currentRoundManger = getCurrentRoundInfo();
			turnManager = new TurnManager(currentPlayer, action, betAmount);
			currentRoundManger.addTurnRecord(turnManager);
			LogUtils.Log("Turn Manager # User: "
					+ currentPlayer.getPlayerName() + " # Action: " + action
					+ " # Bet: " + betAmount + " # Round: "
					+ currentRoundManger.getRound()+" # Total Bet : "+currentRoundManger.getTotalRoundBetAmount());
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

	public int getTournamentEntryFee(){
		return tournamentEntryFee;
	}
	public ArrayList<Winner> getAllWinnerPlayers() {
		return winnerManager.getWinnerList();
	}

	public void findAllWinnerPlayers() {
		winnerManager.findWinnerPlayers(gameType);
	}

	public List<PlayerBean> generateWinnerPlayers() {
		return winnerManager.generateWinnerPlayers();
	}

	public void calculatePotAmountForAllInMembers() {
		int allInBetTotalAmount = 0;
//		int tempCurrentRound = 0;
		for (int i = 0; i < playersManager.getAllAvailablePlayers().size(); i++) {
			PlayerBean player = playersManager.getAllAvailablePlayers().get(i);
			boolean isAllIn = player.isAllIn();
//			int lastAction = getCurrentRoundInfo().getPlayerLastAction(player);
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

	public void addTournamentPlayer(PlayerBean playerBean){
		listTournamentPlayers.add(playerBean);
	}
	public int getPlayerPreviousBalance(String plrName){
		int balanace = 0;
		
		for(PlayerBean playerBean : listTournamentPlayers){
			if(playerBean.getPlayerName().equals(plrName)){
				balanace = playerBean.getBalance();
				listTournamentPlayers.remove(playerBean);
				break;
			}
		}
		return balanace;
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
		return totalBetAmount;
	}
}
