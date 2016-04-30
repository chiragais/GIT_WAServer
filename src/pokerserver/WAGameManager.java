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
import pokerserver.players.WACardPot;
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
public class WAGameManager implements GameConstants {

	PlayersManager playersManager;
	GeneralHandManager handManager;
	ArrayList<Card> listDefaultCards = new ArrayList<Card>();
	ArrayList<Card> listTableCards = new ArrayList<Card>();
	RoundManager startRound;
	RoundManager firstFlopRound;
	RoundManager secondFlopRound;
	RoundManager whoopAssRound;
	RoundManager thirdRound;
	int currentRound = 0;
	WinnerManager winnerManager;
	int waCardAmt = 0;
	int totalBBPlayersTurn = 0;
	int totalGameCntr=0;
	
	public WAGameManager() {
		playersManager = new PlayersManager();
	}

	public void initGameRounds() {
		System.out
				.println("================== WA Game started ==================");
		handManager = new GeneralHandManager(WA_PLAYER_CARD_LIMIT_FOR_HAND);
		winnerManager = new WinnerManager(playersManager, handManager);
		generateDefaultCards();
		startRound = new RoundManager(WA_ROUND_START);
		firstFlopRound = new RoundManager(WA_ROUND_FIRST_FLOP);
		secondFlopRound = new RoundManager(WA_ROUND_SECOND_FLOP);
		whoopAssRound = new RoundManager(WA_ROUND_WHOOPASS);
		thirdRound = new RoundManager(WA_ROUND_THIRD_FLOP);
		waCardAmt = 0;
		totalBBPlayersTurn = 0;
		if(totalGameCntr>playersManager.getTotalActivePlayerCounter())
			totalGameCntr=0;
		playersManager.setCurrentGameCntr(totalGameCntr++);
		// startFirstRound();
	}

	public RoundManager getCurrentRoundInfo() {
		if (startRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return startRound;
		} else if (firstFlopRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return firstFlopRound;
		} else if (secondFlopRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return secondFlopRound;
		} else if (whoopAssRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return whoopAssRound;
		} else if (thirdRound.getStatus() == ROUND_STATUS_ACTIVE) {
			return thirdRound;
		}
		return null;
	}

	public void addNewPlayerToGame(PlayerBean player) {
		this.playersManager.addNewPlayerInRoom(player);
	}

	public void findBestPlayerHand() {
		for (PlayerBean player : playersManager.getAllAvailablePlayers()) {
			if (!player.isFolded())
				handManager.generatePlayerBestRank(listDefaultCards, player);
		}
	}

	public void findWAShortPot(){
		List<TurnManager> listTurnManager = whoopAssRound.getAllTurnRecords();

		Collections.sort(listTurnManager,
				new Comparator<TurnManager>() {
					@Override
					public int compare(TurnManager paramT1,
							TurnManager paramT2) {
						return Integer.compare(paramT1.getBetAmount(),
								paramT2.getBetAmount());
					}
				});
		
		try {
			for (TurnManager turnManager : listTurnManager) {
				if (turnManager.getBetAmount() != 0) {
					winnerManager.manageWAAmtInWAPot(turnManager.getPlayer(), turnManager.getBetAmount());
				}
			}
			for(WACardPot waCardPot : winnerManager.getAllWACardPots()){
				System.out.println("WA Pot Amt : "+waCardPot.getPotAmt() +" >> TotalPlayer : "+waCardPot.getPlayers().size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Winner getTopWinner() {
		return winnerManager.getTopWinner();
	}
	public WinnerManager getWinnerManager(){
		return winnerManager;
	}

	public List<WACardPot> getWACardPots(){
		return winnerManager.getAllWACardPots();
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
		// System.out
		// .println("\n ---------calculatePotAmountForAllInMembers------------------------");
		for (int i = 0; i < playersManager.getAllAvailablePlayers().size(); i++) {
			allInBetTotalAmount = 0;
			PlayerBean player = playersManager.getAllAvailablePlayers().get(i);
			boolean isAllIn = player.isAllIn();
			// int lastAction =
			// getCurrentRoundInfo().getPlayerLastAction(player);
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

				if (startRound.getRound() < getCurrentRoundInfo().getRound())
					allInBetTotalAmount += startRound.getTotalRoundBetAmount();
				if (firstFlopRound.getRound() < getCurrentRoundInfo()
						.getRound())
					allInBetTotalAmount += firstFlopRound
							.getTotalRoundBetAmount();
				if (secondFlopRound.getRound() < getCurrentRoundInfo()
						.getRound())
					allInBetTotalAmount += secondFlopRound
							.getTotalRoundBetAmount();
				AllInPlayer allInPlayer = new AllInPlayer(
						player.getPlayerName(), allInBetTotalAmount);
				System.out.println("\n this is all in amount of player  "
						+ allInPlayer.getPlayerName() + "  is =  "
						+ allInPlayer.getTotalAllInPotAmount());
				winnerManager.addAllInTotalPotAmount(allInPlayer);
			}

		}

//		for (AllInPlayer allInPlayer : winnerManager.getAllInPlayers()) {
//			System.out.println("\n All IN Player :  "
//					+ allInPlayer.getPlayerName() + "  Bet Amount =  "
//					+ allInPlayer.getTotalAllInPotAmount());
//		}
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
	 * Start first round and set other round status as a pending
	 */
	public void startFirstRound() {
		currentRound = WA_ROUND_START;
		System.out.println(">>>>>>>>>>> WA start Round started");
		startRound.setStatus(ROUND_STATUS_ACTIVE);
		firstFlopRound.setStatus(ROUND_STATUS_PENDING);
		secondFlopRound.setStatus(ROUND_STATUS_PENDING);
		whoopAssRound.setStatus(ROUND_STATUS_PENDING);
		thirdRound.setStatus(ROUND_STATUS_PENDING);
	}

	public void startFirstFlopRound() {
		currentRound = WA_ROUND_FIRST_FLOP;
		System.out.println(">>>>>>>>>>>WA First Flop Round started  ");
		startRound.setStatus(ROUND_STATUS_FINISH);
		firstFlopRound.setStatus(ROUND_STATUS_ACTIVE);
		secondFlopRound.setStatus(ROUND_STATUS_PENDING);
		whoopAssRound.setStatus(ROUND_STATUS_PENDING);
		thirdRound.setStatus(ROUND_STATUS_PENDING);
	}

	public void startSecondFlopRound() {
		currentRound = WA_ROUND_SECOND_FLOP;
		System.out.println(">>>>>>>>>>> WA Second flop Round started  ");
		startRound.setStatus(ROUND_STATUS_FINISH);
		firstFlopRound.setStatus(ROUND_STATUS_FINISH);
		secondFlopRound.setStatus(ROUND_STATUS_ACTIVE);
		whoopAssRound.setStatus(ROUND_STATUS_PENDING);
		thirdRound.setStatus(ROUND_STATUS_PENDING);
	}

	public void startWhoopAssRound() {
		currentRound = WA_ROUND_WHOOPASS;
		System.out.println(">>>>>>>>>>> WA WhoopAss Round started  ");
		startRound.setStatus(ROUND_STATUS_FINISH);
		firstFlopRound.setStatus(ROUND_STATUS_FINISH);
		secondFlopRound.setStatus(ROUND_STATUS_FINISH);
		whoopAssRound.setStatus(ROUND_STATUS_ACTIVE);
		thirdRound.setStatus(ROUND_STATUS_PENDING);
	}

	public void startThirdFlopRound() {
		currentRound = WA_ROUND_THIRD_FLOP;
		System.out.println(">>>>>>>>>>> WA Third Round started  ");
		startRound.setStatus(ROUND_STATUS_FINISH);
		firstFlopRound.setStatus(ROUND_STATUS_FINISH);
		secondFlopRound.setStatus(ROUND_STATUS_FINISH);
		whoopAssRound.setStatus(ROUND_STATUS_FINISH);
		thirdRound.setStatus(ROUND_STATUS_ACTIVE);
	}

	public RoundManager getStartRound() {
		return startRound;
	}

	public RoundManager getFirstFlopRound() {
		return firstFlopRound;
	}

	public RoundManager getSecondFlopRound() {
		return secondFlopRound;
	}

	public RoundManager getWhoopAssRound() {
		return whoopAssRound;
	}

	public RoundManager getThirdRound() {
		return thirdRound;
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

		if (currentRound.getRound() == WA_ROUND_WHOOPASS) {
			if (currentRound.getAllTurnRecords().size() == playersManager
					.getAllAvailablePlayers().size()) {
				for (PlayerBean player : playersManager
						.getAllAvailablePlayers()) {
					if (!player.isFolded()) {
						player.setWACardStatus(currentRound
								.getPlayerLastAction(player));
					}
				}
				return true;
			}
			return false;
		} else {
			boolean allPlayersAreAllIn = true;
			int maxPlayerBetAmt = 0;
			
			for (PlayerBean player : playersManager.getAllAvailablePlayers()) {
				// Check total BB Player turn
				
				int totalBetAmt = currentRound.getTotalPlayerBetAmount(player);
				if (maxPlayerBetAmt < totalBetAmt) {
					maxPlayerBetAmt = totalBetAmt;
				}
				if (!player.isFolded() && !player.isAllIn()) {
					allPlayersAreAllIn = false;
//					System.out.println("<> Other "+player.getPlayerName()+" >> "+currentRound.getPlayerLastAction(player));
					totalPlayerWiseBetAmount.add(new PlayerBetBean(currentRound
							.getTotalPlayerBetAmount(player), currentRound
							.getPlayerLastAction(player)));
				}
			}

			if(totalPlayerWiseBetAmount.size()==1 && maxPlayerBetAmt==0){
				return true; // All others are folded or All in
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
			// PlayerBetBean lastPlayerBetAmt = totalPlayerWiseBetAmount.get(0);
			// totalPlayerWiseBetAmount.remove(0);
			// Checking all players have same bet amount
			for (PlayerBetBean currentPlayerBetAmt : totalPlayerWiseBetAmount) {
				if (currentPlayerBetAmt.getBetAmount() != maxPlayerBetAmt) {
					return false;
				}
			}
			if (!allPlayerHaveTurn) {
				return false;
			}
			System.out.println("TotalBB Players Turn : "+totalBBPlayersTurn);
			if(totalBBPlayersTurn==1){
				return false;
			}
			return true;

		}
	}

	public void setWhoopAssCardStatus() {

	}

	public int getPlayerTotalBetAmount(String name) {
		PlayerBean player = playersManager.getPlayerByName(name);
		int totalBetAmount = 0;
		totalBetAmount += startRound.getTotalPlayerBetAmount(player);
		totalBetAmount += firstFlopRound.getTotalPlayerBetAmount(player);
		totalBetAmount += secondFlopRound.getTotalPlayerBetAmount(player);
		totalBetAmount += thirdRound.getTotalPlayerBetAmount(player);

		return totalBetAmount;
	}

	public void moveToNextRound() {
		switch (currentRound) {
		case WA_ROUND_START:
			calculatePotAmountForAllInMembers();
			startFirstFlopRound();
			break;
		case WA_ROUND_FIRST_FLOP:
			calculatePotAmountForAllInMembers();
			startSecondFlopRound();
			break;
		case WA_ROUND_SECOND_FLOP:
			calculatePotAmountForAllInMembers();
			startWhoopAssRound();
			break;
		case WA_ROUND_WHOOPASS:
			calculatePotAmountForAllInMembers();
			System.out.println("WA Round done: Calculate WA Pot: ");
			findWAShortPot();
			
			startThirdFlopRound();
			break;
		case WA_ROUND_THIRD_FLOP:
			// startThirdFlopRound();
			calculatePotAmountForAllInMembers();
			getCurrentRoundInfo().setStatus(ROUND_STATUS_FINISH);
			break;
		}

	}

	/** It will generate 1st flop(2), 2nd(2) and 3rd(2) cards. Total 6 cards */
	public void generateDefaultCards() {
		listDefaultCards.clear();
		listTableCards.clear();
		while (listDefaultCards.size() != 6) {
			Card cardBean = new Card();
			if (!isAlreadyDesributedCard(cardBean)) {
				
				listDefaultCards.add(cardBean);
			}
		}
	}

	/** Check card is already on table or not */
	public boolean isAlreadyDesributedCard(Card cardBean) {
		for (Card cardBean2 : listTableCards) {
//			System.out.println(cardBean2.getCardName()+ " == " + cardBean.getCardName()+" : "+cardBean.getCardName().equals(cardBean2.getCardName()));			
			if (cardBean.getCardName().equals(cardBean2.getCardName())) {
				return true;
			}
		}
//		System.out.println("New card added : "+cardBean.getCardName()+" >> "+listTableCards.size());
		listTableCards.add(cardBean);
		return false;
	}

	public Card generatePlayerCards() {
		Card cardBean = new Card();
		while (isAlreadyDesributedCard(cardBean)) {
			cardBean.generateRandomCard();
		}
		// System.out.print("Default card  : " + cardBean.getCardName());
		return cardBean;
	}

	/** Handles player's action taken by him */
	public TurnManager managePlayerAction(String userName, int userAction,
			int betAmount) {
		// gamePlay.executePlayerAction(betAmount, userAction);
		return addCurrentActionToTurnManager(userName, betAmount, userAction);
	}

/*	public void setTotalTableBetAmount() {
		int totalBetAmount = 0;
		totalBetAmount += startRound.getTotalRoundBetAmount();
		totalBetAmount += firstFlopRound.getTotalRoundBetAmount();
		totalBetAmount += secondFlopRound.getTotalRoundBetAmount();
		totalBetAmount += thirdRound.getTotalRoundBetAmount();
		winnerManager.setTotalTableAmount(totalBetAmount);
	}*/

	public int getTotalTableAmount() {
		int totalBetAmount = 0;
		totalBetAmount += startRound.getTotalRoundBetAmount();
		totalBetAmount += firstFlopRound.getTotalRoundBetAmount();
		totalBetAmount += secondFlopRound.getTotalRoundBetAmount();
		totalBetAmount += thirdRound.getTotalRoundBetAmount();
//		totalBetAmount += whoopAssRound.getTotalRoundBetAmount();
		winnerManager.setTotalTableAmount(totalBetAmount);
		return totalBetAmount;
	}

	private TurnManager addCurrentActionToTurnManager(String userName,
			int betAmount, int action) {

		TurnManager turnManager = null;
		PlayerBean currentPlayer = deductPlayerBetAmountFromBalance(userName,
				betAmount, action);
		if(currentRound == WA_ROUND_START&&currentPlayer.isBigBlind()){
			totalBBPlayersTurn++;
		}
		if (currentPlayer != null) {
			RoundManager currentRoundManger = getCurrentRoundInfo();
			if(currentRoundManger.getRound()==WA_ROUND_WHOOPASS && waCardAmt<=betAmount){
				waCardAmt = betAmount;
			}
			if (currentPlayer.getTotalBalance() == 0
					&& currentRoundManger.getRound() != WA_ROUND_WHOOPASS) {
				action = ACTION_ALL_IN;
			}else if(currentPlayer.getTotalBalance() == 0
					&& currentRoundManger.getRound() == WA_ROUND_WHOOPASS) {
				currentPlayer.setPlayerAllIn(true);
			}
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

	public boolean isAllPlayersAreFolded(){
		int foldedCntr = 0;
		for (PlayerBean playerBean : playersManager.getAllAvailablePlayers()) {
			if(playerBean.isFolded()){
				foldedCntr++;
			}
		}
		System.out.println("Number Of players : "+playersManager.getAllAvailablePlayers().size()+" >>Total folded : "+foldedCntr);
		if(playersManager.getAllAvailablePlayers().size()-1 == foldedCntr){
			return true;
		}
		return false;
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
			int betAmt = getCurrentRoundInfo().getTotalPlayerBetAmount(
					playerBean);
			if (maxPlayerAmt < betAmt) {
				maxPlayerAmt = betAmt;
			}
			if (!playerBean.isAllIn()) {
				if (!playerBean.isFolded()) {
					lastPlayer = playerBean;
					totalActivePlayersCnt++;
				}
			} else {
				totalAllInPlayers++;
				lastAllInPlayer = playerBean;
			}
			if (totalActivePlayersCnt == 2) {
				return null;
			}
		}
		if(lastPlayer ==null && lastAllInPlayer != null){
			return lastAllInPlayer;
		}
		if (totalAllInPlayers == playersManager.getAllAvailablePlayers().size() - 1|| totalActivePlayersCnt==1) {

			int activePlrBet = getCurrentRoundInfo().getTotalPlayerBetAmount(
					lastPlayer);
			if (activePlrBet < maxPlayerAmt) {
				return null;
			}
			return lastPlayer;
		} else if (totalAllInPlayers == playersManager.getAllAvailablePlayers()
				.size()) {
			return lastAllInPlayer;
		}
		return lastPlayer;
	}
}
