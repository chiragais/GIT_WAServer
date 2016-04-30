package pokerserver.room;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pokerserver.WAGameManager;
import pokerserver.players.PlayerBean;
import pokerserver.players.WACardPot;
import pokerserver.players.Winner;
import pokerserver.rounds.RoundManager;
import pokerserver.turns.TurnManager;
import pokerserver.utils.GameConstants;

import com.shephertz.app42.server.domain.Room;
import com.shephertz.app42.server.idomain.BaseTurnRoomAdaptor;
import com.shephertz.app42.server.idomain.HandlingResult;
import com.shephertz.app42.server.idomain.ITurnBasedRoom;
import com.shephertz.app42.server.idomain.IUser;
import com.shephertz.app42.server.idomain.IZone;

/**
 * 
 * @author Chirag
 */
public class WAPokerRoomAdapter extends BaseTurnRoomAdaptor implements
		GameConstants {

	private IZone izone;
	private ITurnBasedRoom gameRoom;
	private byte GAME_STATUS;
	WAGameManager gameManager;

	public WAPokerRoomAdapter(IZone izone, ITurnBasedRoom room) {
		this.izone = izone;
		this.gameRoom = room;
		GAME_STATUS = STOPPED;
		this.gameManager = new WAGameManager();
		gameManager.initGameRounds();

		System.out.println("Game Room : " + room.getName());
	}

	@Override
	public void onTimerTick(long time) {
		if (GAME_STATUS == STOPPED
				&& gameRoom.getJoinedUsers().size() >= MIN_PLAYER_TO_START_GAME) {

			distributeCarsToPlayerFromDelear();
			GAME_STATUS = RUNNING;
		} else if (GAME_STATUS == RESUMED) {
			GAME_STATUS = RUNNING;
			gameRoom.startGame(WA_SERVER_NAME);
		} else if (GAME_STATUS == RUNNING
				&& gameRoom.getJoinedUsers().size() < MIN_PLAYER_TO_START_GAME) {
			GAME_STATUS = STOPPED;
			gameRoom.stopGame(WA_SERVER_NAME);
		}

	}

	private void startGame() {
		managePlayerTurn(gameManager.getPlayersManager().getBigBlindPayer()
				.getPlayerName());
		gameRoom.startGame(WA_SERVER_NAME);
	}

	private void broadcastPlayerCardsInfo() {

		for (PlayerBean player : gameManager.getPlayersManager()
				.getAllAvailablePlayers()) {

			JSONObject cardsObject = new JSONObject();

			try {
				cardsObject.put(TAG_PLAYER_NAME, player.getPlayerName());
				cardsObject.put(TAG_CARD_PLAYER_1, player.getFirstCard()
						.getCardName());
				cardsObject.put(TAG_CARD_PLAYER_2, player.getSecondCard()
						.getCardName());
				cardsObject.put(TAG_CARD_WA, player.getWACard().getCardName());
				cardsObject.put(TAG_PLAYER_BALANCE, player.getTotalBalance());

			} catch (JSONException e) {
				e.printStackTrace();
			}
			gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_PLAYERS_INFO
					+ cardsObject.toString());
			System.out.println("Player Info : " + cardsObject.toString());
		}
	}

	/**
	 * Invoked when a move request is received from the client whose turn it is.
	 * 
	 * By default, the sender will be sent back a success response, the turn
	 * user will be updated to the next user in order of joining and a move
	 * notification will be sent to all the subscribers of the room.
	 * 
	 * @param sender
	 *            the user who has sent the move
	 * @param moveData
	 *            the move data sent by the user
	 * @param result
	 *            use this to override the default behavior
	 */
	public boolean isRoundCompelete = false;

	@Override
	public void handleMoveRequest(IUser sender, String moveData,
			HandlingResult result) {
		// result.doDefaultTurnLogic = false;
		if (moveData.contains(REQUEST_FOR_ACTION)) {
			System.out.println("\nMoveRequest : Sender : " + sender.getName()
					+ " : Data : " + moveData);
			int playerAction = 0;
			JSONObject responseJson = null;
			moveData = moveData.replace(REQUEST_FOR_ACTION, "");

			try {
				responseJson = new JSONObject(moveData);
				playerAction = responseJson.getInt(TAG_ACTION);
				if (playerAction != ACTION_NO_TURN) {
					managePlayerAction(sender.getName(), playerAction,
							responseJson.getInt(TAG_BET_AMOUNT));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void managePlayerAction(String sender, int playerAction,
			int betAmount) {
		TurnManager turnManager = gameManager.managePlayerAction(sender,
				playerAction, betAmount);

		if (turnManager != null)
			broadcastPlayerActionDoneToOtherPlayers(turnManager);
		// If all players are folded or all in then declare last player as a
		// winner
		PlayerBean lastActivePlayer = gameManager.checkAllAreFoldOrAllIn();
		// WA Card pot calculation if any player fold in third round
		if(playerAction==ACTION_FOLD && gameManager.getCurrentRoundInfo().getRound()==WA_ROUND_THIRD_FLOP){
			manageWAPotWinnerPlayers();
		}
		
		if (lastActivePlayer != null) {
			if(gameManager.isAllPlayersAreFolded()){
				manageGameFinishEvent();
			}else if (gameManager.getWhoopAssRound().getStatus() == ROUND_STATUS_PENDING) {
				gameManager.calculatePotAmountForAllInMembers();
				gameManager.startWhoopAssRound();
				broadcastRoundCompeleteToAllPlayers();
			} else if ((gameManager.getWhoopAssRound().getStatus() == ROUND_STATUS_ACTIVE || gameManager
					.getCurrentRoundInfo().getStatus() == ROUND_STATUS_ACTIVE)
					&& gameManager.checkEveryPlayerHaveSameBetAmount()) {
				manageGameFinishEvent();
			}

		} else if (playerAction != ACTION_DEALER
				&& gameManager.checkEveryPlayerHaveSameBetAmount()) {
			isRoundCompelete = true;
			if (gameManager.getCurrentRoundInfo().getStatus() == ROUND_STATUS_ACTIVE
					&& gameManager.getCurrentRoundIndex() == WA_ROUND_THIRD_FLOP) {
				manageGameFinishEvent();
			} else {
				System.out.println("Dealer Player : "+ gameManager.getPlayersManager().getDealerPayer()
						.getPlayerName());
//				managePlayerTurn(gameManager.getPlayersManager().getDealerPayer().getPlayerName());
				gameManager.moveToNextRound();
				broadcastRoundCompeleteToAllPlayers();
			}
		} 
	}

	private void manageWAPotWinnerPlayers(){
		List<WACardPot> listWAPots = gameManager.getWinnerManager().getLastPlayerOfWAPotAfterPlayerFold();
		JSONArray waPotArray = new JSONArray();
		
		for(WACardPot waCardPot : listWAPots){
			JSONObject waCardJsonObject = new JSONObject();
			try {
				int totalWinningAmt =  (waCardPot.getPotAmt() * waCardPot.getPlayers().size());
				int winnerPlayerTotalAmt = waCardPot.getWinnerPlayer().getTotalBalance()+totalWinningAmt;
				waCardJsonObject.put(TAG_WINNER_NAME, waCardPot.getWinnerPlayer().getPlayerName());
				waCardJsonObject.put(TAG_WINNERS_WINNING_AMOUNT,totalWinningAmt);
				waCardJsonObject.put(TAG_WINNER_TOTAL_BALENCE, winnerPlayerTotalAmt);
				waPotArray.put(waCardJsonObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(waPotArray.length()>0){
			gameRoom.BroadcastChat(WA_SERVER_NAME, REQUEST_FOR_WA_POT_WINNER
					+ waPotArray.toString());
			System.out.println(">>WA Pot Winner : " +  waPotArray.toString());	
		}
	}
	private void managePlayerTurn(String currentPlayer) {
//		System.out.println(">>Total Players : "
//				+ gameRoom.getJoinedUsers().size());
		RoundManager currentRoundManager = gameManager.getCurrentRoundInfo();

		if (currentRoundManager != null) {
			PlayerBean nextPlayer = getNextPlayerFromCurrentPlayer(currentPlayer);
			if (nextPlayer == null) {
				System.out.println(" Next turn player : Null");
			} else {
				while (nextPlayer.isFolded() || nextPlayer.isAllIn()) {
//					System.out.println(" Next turn player : "
//							+ nextPlayer.getPlayerName());
					nextPlayer = getNextPlayerFromCurrentPlayer(nextPlayer
							.getPlayerName());
				}

				gameRoom.setNextTurn(getUserFromName(nextPlayer.getPlayerName()));
				System.out.println(currentPlayer
						+ " >> Next valid turn player : "
						+ nextPlayer.getPlayerName());
			}
		} else {
			System.out.println("------ Error > Round is not started yet.....");
		}
	}

	public PlayerBean getNextPlayerFromCurrentPlayer(String currentPlayerName) {
		List<PlayerBean> listPlayer = gameManager.getPlayersManager()
				.getAllAactivePlayersForTurn();
		for (int i = 0; i < listPlayer.size(); i++) {
			if (currentPlayerName.equals(listPlayer.get(i).getPlayerName())) {
				if (i == listPlayer.size() - 1) {
					return listPlayer.get(0);
				} else {
					return listPlayer.get(i + 1);
				}
			}
		}
		return null;
	}

	public void manageGameFinishEvent() {
		gameManager.moveToNextRound();
		// Broad cast game completed to all players
		broadcastRoundCompeleteToAllPlayers();
		broadcastGameCompleteToAllPlayers();
//		gameManager.findWAShortPot();
		gameManager.findBestPlayerHand();
		gameManager.findAllWinnerPlayers();
		broadcastWinningPlayer();
		handleFinishGame();
	}

	/**
	 * Invoked when a start game request is received from a client when the room
	 * is in stopped state.
	 * 
	 * By default a success response will be sent back to the client, the game
	 * state will be updated and game started notification is sent to all the
	 * subscribers of the room.
	 * 
	 * @param sender
	 *            the user who has sent the request.
	 * @param result
	 *            use this to override the default behavior
	 */
	@Override
	public void handleStartGameRequest(IUser sender, HandlingResult result) {
		// result.doDefaultTurnLogic = false;
		System.out.println("StartGameRequest : Sender User : "
				+ sender.getName());
	}

	/**
	 * Invoked when a stop game request is received from a client when the room
	 * is in started state.
	 * 
	 * By default a success response will be sent back to the client, the game
	 * state will be updated and game stopped notification is sent to all the
	 * subscribers of the room.
	 * 
	 * @param sender
	 *            the user who has sent the request.
	 * @param result
	 *            use this to override the default behavior
	 */
	@Override
	public void handleStopGameRequest(IUser sender, HandlingResult result) {
		// result.doDefaultTurnLogic = false;
		System.out.println("StopGameRequest : Sender User : "
				+ sender.getName());
	}

	/**
	 * Invoked when the timer expires for the current turn user.
	 * 
	 * By default, the turn user will be updated to the next user in order of
	 * joining and a move notification with empty data will be sent to all the
	 * subscribers of the room.
	 * 
	 * @param turn
	 *            the current turn user whose turn has expired.
	 * @param result
	 *            use this to override the default behavior
	 */
	@Override
	public void handleTurnExpired(IUser turn, HandlingResult result) {
		System.out.println("onTurnExpired : Turn User : " + turn.getName());
		// result.doDefaultTurnLogic = false;
		managePlayerAction(turn.getName(), ACTION_FOLD, 0);
		// managePlayerTurn(turn.getName());
	}

	/**
	 * Invoked when a user leaves the turn based room.
	 * 
	 * By default, the turn user will be updated to the next user in order of
	 * joining if the user who is leaving was the current turn user and a move
	 * notification with empty data will be sent to all the subscribers of the
	 * room.
	 * 
	 * @param user
	 * @param result
	 *            use this to override the default behavior
	 */
	@Override
	public void handleUserLeavingTurnRoom(IUser user, HandlingResult result) {
		System.out.println("UserLeavingTurnRoom :  User : " + user.getName());
		gameManager.leavePlayerToGame(gameManager.getPlayersManager()
				.getPlayerByName(user.getName()));
		broadcastBlindPlayerDatas();
		// This will be changed.
		if (GAME_STATUS == RUNNING || GAME_STATUS == FINISHED
				&& gameRoom.getJoinedUsers().size() == 0) {
			System.out.println("\n\nRoom : Game Over ..... ");
			gameManager.getPlayersManager().removeAllPlayers();
			// handleFinishGame("Chirag", null);
			GAME_STATUS = FINISHED;
		}
	}

	/*
	 * This function stop the game and notify the room players about winning
	 * user and his cards.
	 */
	private void handleFinishGame() {

		try {
			// gameRoom.setAdaptor(null);
			// izone.deleteRoom(gameRoom.getId());
			gameRoom.stopGame(WA_SERVER_NAME);
			GAME_STATUS = FINISHED;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleRestartGame() {

		System.out.println("--- Restarting Game -------- ");
		listRestartGameReq.clear();
		gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_GAME_START);
		gameManager.initGameRounds();
		gameManager.getPlayersManager().removeAllPlayers();
		for (IUser user : gameRoom.getJoinedUsers()) {
			addNewPlayerCards(user.getName());
		}
//		gameManager.initGameRounds();
		sendDefaultCards(null, true);
		broadcastPlayerCardsInfo();
		broadcastBlindPlayerDatas();
		GAME_STATUS = STOPPED;
		System.out.println("Game Status : " + GAME_STATUS);
	}

	/**
	 * Invoked when a chat request is received from the client in the room.
	 * 
	 * By default this will trigger a success response back to the client and
	 * will broadcast a notification message to all the subscribers of the room.
	 * 
	 * 
	 * @param sender
	 *            the user who has sent the request
	 * @param message
	 *            the message that was sent
	 * @param result
	 *            use this to override the default behavior
	 */
	public void handleChatRequest(IUser sender, String message,
			HandlingResult result) {
		System.out.println("ChatRequest :  User : " + sender.getName()
				+ " : Message : " + message);
		if ( message.startsWith(RESPONSE_FOR_DESTRIBUTE_CARD)) {
			listRestartGameReq.add(sender.getName());
			System.out.println("Total Request : "+listRestartGameReq.size());
			if (isRequestFromAllActivePlayers()) {
				System.out.println("Start Game");
				listRestartGameReq.clear();
				gameManager.startFirstRound();
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getSmallBlindPayer().getPlayerName(), ACTION_BET,
						SBAmount);
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getBigBlindPayer().getPlayerName(), ACTION_BET,
						SBAmount * 2);
				startGame();
			}
		} else if (message.startsWith(REQUEST_FOR_RESTART_GAME)) {
			listRestartGameReq.add(sender.getName());
			if (isRequestFromAllActivePlayers())
				handleRestartGame();
		}
	}

	private boolean isRequestFromAllActivePlayers() {
		// TODO Auto-generated method stub
		for (PlayerBean playerBean : gameManager.getPlayersManager()
				.getAllAvailablePlayers()) {
			if (!listRestartGameReq.contains(playerBean.getPlayerName())) {
				System.out.println("Request F : "+listRestartGameReq.size());
				return false;
			}
		}
		System.out.println("Request T : "+listRestartGameReq.size());
		return true;
	}

	/**
	 * Invoked when a join request is received by the room and the number of
	 * joined users is less than the maxUsers allowed.
	 * 
	 * By default this will result in a success response sent back the user, the
	 * user will be added to the list of joined users of the room and a user
	 * joined room notification will be sent back to all the subscribed users of
	 * the room.
	 * 
	 * @param user
	 *            the user who has sent the request
	 * @param result
	 *            use this to override the default behavior
	 */
	public void handleUserJoinRequest(IUser user, HandlingResult result) {
		System.out.println(">>UserJoinRequest :  User : " + user.getName());
		// Handle player request
		if (gameRoom.getJoinedUsers().isEmpty()) {
			GAME_STATUS = STOPPED;
			gameManager.initGameRounds();
		}
		addNewPlayerCards(user.getName());
		sendDefaultCards(user, false);
		broadcastPlayerCardsInfo();
		broadcastBlindPlayerDatas();
		System.out.println("Game Status : " + GAME_STATUS);
	}

	private void addNewPlayerCards(String userName) {
		PlayerBean player = new PlayerBean(
				gameRoom.getJoinedUsers().size() - 1, userName);
//		if (gameRoom.getJoinedUsers().size() == 0) {
		int totalPlayers =gameManager.getPlayersManager().getAllAvailablePlayers().size() ; 
		if(totalPlayers== 0){
			player.setTotalBalance(2000);
		} else if (totalPlayers== 1) {
			player.setTotalBalance(1000);
		} else if (totalPlayers == 2) {
			player.setTotalBalance(3000);
		}
		
		player.setCards(gameManager.generatePlayerCards(),
				gameManager.generatePlayerCards(),
				gameManager.generatePlayerCards());

		gameManager.addNewPlayerToGame(player);
	}

	public void onUserPaused(IUser user) {

	}

	public void onUserResume(IUser user) {

	}

	private void distributeCarsToPlayerFromDelear() {
		/*
		 * int totalPlayerInRoom = gameManager.getPlayersManager()
		 * .getAllAvailablePlayers().size(); if (totalPlayerInRoom > 0) { for
		 * (IUser user : gameRoom.getJoinedUsers()) { if (user.getName().equals(
		 * gameManager.getPlayersManager() .getAllAvailablePlayers().get(0)
		 * .getPlayerName())) { user.SendChatNotification(WA_SERVER_NAME,
		 * RESPONSE_FOR_DESTRIBUTE_CARD, gameRoom); return; } } }
		 */
		gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_DESTRIBUTE_CARD);
		System.out.println("Distribute cards...");
	}

	/** Manage default and player hand cards */
	public void sendDefaultCards(IUser user, boolean isBroadcast) {

		JSONObject cardsObject = new JSONObject();
		try {
			cardsObject.put(TAG_CARD_FIRST_FLOP_1, gameManager
					.getDefaultCards().get(INDEX_FIRST_FLOP_1).getCardName());
			cardsObject.put(TAG_CARD_FIRST_FLOP_2, gameManager
					.getDefaultCards().get(INDEX_FIRST_FLOP_2).getCardName());
			cardsObject.put(TAG_CARD_SECOND_FLOP_1, gameManager
					.getDefaultCards().get(INDEX_SECOND_FLOP_1).getCardName());
			cardsObject.put(TAG_CARD_SECOND_FLOP_2, gameManager
					.getDefaultCards().get(INDEX_SECOND_FLOP_2).getCardName());
			cardsObject.put(TAG_CARD_THIRD_FLOP_1, gameManager
					.getDefaultCards().get(INDEX_THIRD_FLOP_1).getCardName());
			cardsObject.put(TAG_CARD_THIRD_FLOP_2, gameManager
					.getDefaultCards().get(INDEX_THIRD_FLOP_2).getCardName());
			if (isBroadcast) {
				gameRoom.BroadcastChat(WA_SERVER_NAME,
						RESPONSE_FOR_DEFAULT_CARDS + cardsObject.toString());
			} else {
				user.SendChatNotification(WA_SERVER_NAME,
						RESPONSE_FOR_DEFAULT_CARDS + cardsObject.toString(),
						gameRoom);
			}
			System.out.println("Default Cards : " + cardsObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void broadcastBlindPlayerDatas() {
		JSONObject cardsObject = new JSONObject();
		try {
			if (!gameManager.getPlayersManager().getAllAvailablePlayers()
					.isEmpty()) {
				int totalPlayerInRoom = gameManager.getPlayersManager()
						.getAllAvailablePlayers().size();

				if (totalPlayerInRoom > 0) {

					cardsObject.put(TAG_PLAYER_DEALER, gameManager
							.getPlayersManager().getDealerPayer()
							.getPlayerName());
				} else {
					cardsObject.put(TAG_PLAYER_DEALER, RESPONSE_DATA_SEPRATOR);
				}
				if (totalPlayerInRoom > 1) {
					cardsObject.put(TAG_PLAYER_SMALL_BLIND, gameManager
							.getPlayersManager().getSmallBlindPayer()
							.getPlayerName());
				} else {
					cardsObject.put(TAG_PLAYER_SMALL_BLIND,
							RESPONSE_DATA_SEPRATOR);
				}
				if (totalPlayerInRoom > 2) {
					cardsObject.put(TAG_PLAYER_BIG_BLIND, gameManager
							.getPlayersManager().getBigBlindPayer()
							.getPlayerName());
				} else {
					cardsObject.put(TAG_PLAYER_BIG_BLIND,
							RESPONSE_DATA_SEPRATOR);
				}
				cardsObject.put(TAG_SMALL_BLIEND_AMOUNT, SBAmount);
				System.out.println("Blind Player Details : "
						+ cardsObject.toString());
				gameRoom.BroadcastChat(WA_SERVER_NAME,
						RESPONSE_FOR_BLIEND_PLAYER + cardsObject.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void broadcastRoundCompeleteToAllPlayers() {
		JSONObject cardsObject = new JSONObject();
		try {
			cardsObject.put(TAG_ROUND, gameManager.getCurrentRoundIndex());
			cardsObject
					.put(TAG_TABLE_AMOUNT, gameManager.getTotalTableAmount());
			gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_ROUND_COMPLETE
					+ cardsObject.toString());
			System.out.println(">>Round done " + cardsObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void broadcastGameCompleteToAllPlayers() {
		JSONArray winnerArray = new JSONArray();
		gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_GAME_COMPLETE
				+ winnerArray.toString());
		System.out.println("winner array is  " + winnerArray.toString());
	}

	private void broadcastWinningPlayer() {
		JSONObject winningPlayerObject = new JSONObject();
		JSONArray winnerArray = new JSONArray();
		try {
			
			for (Winner winnerPlayer : gameManager.getAllWinnerPlayers()) {
				// Winner winnerPlayer = gameManager.getTopWinner();
				JSONObject winnerObject = new JSONObject();
				winnerObject.put(TAG_ROUND, gameManager.getCurrentRoundIndex());
				winnerObject.put(TAG_TABLE_AMOUNT,
						gameManager.getTotalTableAmount());

				winnerObject.put(TAG_WINNER_TOTAL_BALENCE, winnerPlayer
						.getPlayer().getTotalBalance());
				winnerObject.put(TAG_WINNER_NAME, winnerPlayer.getPlayer()
						.getPlayerName());
				winnerObject.put(TAG_WINNER_RANK, winnerPlayer.getPlayer()
						.getHandRank().ordinal());
				winnerObject.put(TAG_WINNERS_WINNING_AMOUNT,
						winnerPlayer.getWinningAmount());

				winnerObject.put(TAG_WINNER_BEST_CARDS, winnerPlayer
						.getPlayer().getBestHandCardsName());
				winnerArray.put(winnerObject);
			}
			// WA Pot Manage
			JSONArray winnerWAPotArray = new JSONArray();
			for(WACardPot waCardPot : gameManager.getWACardPots()){
				if(waCardPot.getWinnerPlayer()!=null){
					JSONObject waPotObject = new JSONObject();
					waPotObject.put(TAG_WINNERS_WINNING_AMOUNT, waCardPot.getPotAmt());
					waPotObject.put(TAG_WINNER_NAME, waCardPot.getWinnerPlayer().getPlayerName());
					waCardPot.getWinnerPlayer().setTotalBalance(waCardPot.getWinnerPlayer().getTotalBalance()+waCardPot.getPotAmt());
					waPotObject.put(TAG_WINNER_TOTAL_BALENCE, waCardPot.getWinnerPlayer().getTotalBalance());
					winnerWAPotArray.put(waPotObject);
				}
			}
			winningPlayerObject.put("WA_Pot",winnerWAPotArray);
			winningPlayerObject.put("Table_Pot", winnerArray);
			System.out.println("<<WinningPlayers>> " + winningPlayerObject.toString());
			gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_WINNIER_INFO
					+ winningPlayerObject.toString());
//			System.out.println("<<>> " + winnerArray.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	private void broadcastPlayerActionDoneToOtherPlayers(TurnManager turnManager) {

		JSONObject cardsObject = new JSONObject();
		try {
			cardsObject.put(TAG_BET_AMOUNT, turnManager.getBetAmount());
			cardsObject
					.put(TAG_TABLE_AMOUNT, gameManager.getTotalTableAmount());
			cardsObject.put(TAG_ACTION, turnManager.getPlayerAction());
			cardsObject.put(TAG_PLAYER_NAME, turnManager.getPlayer()
					.getPlayerName());
			cardsObject.put(TAG_PLAYER_BALANCE, turnManager.getPlayer()
					.getTotalBalance());
			gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_ACTION_DONE
					+ cardsObject.toString());
			System.out.println("Action<<>> " + cardsObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private IUser getUserFromName(String name) {
		for (IUser user : gameRoom.getJoinedUsers()) {
			if (user.getName().equals(name)) {
				return user;
			}
		}
		return null;
	}

	List<String> listRestartGameReq = new ArrayList<String>();
}
