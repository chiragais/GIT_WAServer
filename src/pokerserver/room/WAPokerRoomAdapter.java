package pokerserver.room;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pokerserver.cards.WACardPot;
import pokerserver.game.WAGameManager;
import pokerserver.players.PlayerBean;
import pokerserver.rounds.RoundManager;
import pokerserver.turns.TurnManager;
import pokerserver.utils.GameConstants;
import pokerserver.utils.LogUtils;
import pokerserver.winner.Winner;

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

		LogUtils.Log("Game Room : " + room.getName());
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

			int plrStatus = STATUS_ACTIVE;
			if(player.isWaitingForGame()){
				plrStatus=ACTION_WAITING_FOR_GAME;
			}else if(player.isFolded() ){
				plrStatus = ACTION_FOLDED;
			}
			JSONObject cardsObject = new JSONObject();

			try {
				cardsObject.put(TAG_PLAYER_NAME, player.getPlayerName());
				cardsObject.put(TAG_CARD_PLAYER_1, player.getFirstCard()
						.getCardName());
				cardsObject.put(TAG_CARD_PLAYER_2, player.getSecondCard()
						.getCardName());
				cardsObject.put(TAG_CARD_WA, player.getWACard().getCardName());
				cardsObject.put(TAG_PLAYER_BALANCE, player.getBalance());
				cardsObject.put(TAG_GAME_STATUS, GAME_STATUS);
				cardsObject.put(TAG_PLAYER_STATUS, plrStatus);

				gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_PLAYERS_INFO
						+ cardsObject.toString());
				LogUtils.Log("Player Info : " + cardsObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
			LogUtils.Log("\nMoveRequest : Sender : " + sender.getName()
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
			}else if (gameManager.getWhoopAssRound().getStatus() == STATUS_PENDING) {
				gameManager.calculatePotAmountForAllInMembers();
				gameManager.startWhoopAssRound();
				broadcastRoundCompeleteToAllPlayers();
			} else if ((gameManager.getWhoopAssRound().getStatus() == STATUS_ACTIVE || gameManager
					.getCurrentRoundInfo().getStatus() == STATUS_ACTIVE)
					&& gameManager.checkEveryPlayerHaveSameBetAmount()) {
				manageGameFinishEvent();
			}

		} else if (playerAction != ACTION_DEALER
				&& gameManager.checkEveryPlayerHaveSameBetAmount()) {
			isRoundCompelete = true;
			if (gameManager.getCurrentRoundInfo().getStatus() == STATUS_ACTIVE
					&& gameManager.getCurrentRoundIndex() == WA_ROUND_THIRD_FLOP) {
				manageGameFinishEvent();
			} else {
				LogUtils.Log("Dealer Player : "+ gameManager.getPlayersManager().getDealerPayer()
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
				int winnerPlayerTotalAmt = waCardPot.getWinnerPlayer().getBalance()+totalWinningAmt;
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
			LogUtils.Log(">>WA Pot Winner : " +  waPotArray.toString());	
		}
	}
	private void managePlayerTurn(String currentPlayer) {
//		LogUtils.Log(">>Total Players : "
//				+ gameRoom.getJoinedUsers().size());
		RoundManager currentRoundManager = gameManager.getCurrentRoundInfo();

		if (currentRoundManager != null) {
			PlayerBean nextPlayer = getNextPlayerFromCurrentPlayer(currentPlayer);
			if (nextPlayer == null) {
				LogUtils.Log(" Next turn player : Null");
			} else {
				while (nextPlayer.isFolded() || nextPlayer.isAllIn()) {
//					LogUtils.Log(" Next turn player : "
//							+ nextPlayer.getPlayerName());
					nextPlayer = getNextPlayerFromCurrentPlayer(nextPlayer
							.getPlayerName());
				}

				gameRoom.setNextTurn(getUserFromName(nextPlayer.getPlayerName()));
				LogUtils.Log(currentPlayer
						+ " >> Next valid turn player : "
						+ nextPlayer.getPlayerName());
			}
		} else {
			LogUtils.Log("------ Error > Round is not started yet.....");
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
		LogUtils.Log("StartGameRequest : Sender User : "
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
		LogUtils.Log("StopGameRequest : Sender User : "
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
		LogUtils.Log("onTurnExpired : Turn User : " + turn.getName());
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
		LogUtils.Log("UserLeavingTurnRoom :  User : " + user.getName());
		gameManager.leavePlayerToGame(gameManager.getPlayersManager()
				.getPlayerByName(user.getName()));
		broadcastBlindPlayerDatas();
		// This will be changed.
		if (GAME_STATUS == RUNNING || GAME_STATUS == FINISHED
				&& gameRoom.getJoinedUsers().size() == 0) {
			LogUtils.Log("\n\nRoom : Game Over ..... ");
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

		LogUtils.Log("--- Restarting Game -------- ");
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
		LogUtils.Log("Game Status : " + GAME_STATUS);
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
		LogUtils.Log("ChatRequest :  User : " + sender.getName()
				+ " : Message : " + message);
		if ( message.startsWith(RESPONSE_FOR_DESTRIBUTE_CARD)) {
			listRestartGameReq.add(sender.getName());
			LogUtils.Log("Total Request : "+listRestartGameReq.size());
			if (isRequestFromAllActivePlayers()) {
				LogUtils.Log("Start Game");
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
				LogUtils.Log("Request F : "+listRestartGameReq.size());
				return false;
			}
		}
		LogUtils.Log("Request T : "+listRestartGameReq.size());
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
		LogUtils.Log(">>UserJoinRequest :  User : " + user.getName());
		// Handle player request
		if (gameRoom.getJoinedUsers().isEmpty()) {
			GAME_STATUS = STOPPED;
			gameManager.initGameRounds();
		}
		addNewPlayerCards(user.getName());
		sendDefaultCards(user, false);
		broadcastPlayerCardsInfo();
		broadcastBlindPlayerDatas();
		LogUtils.Log("Game Status : " + GAME_STATUS);
	}

	private void addNewPlayerCards(String userName) {
		PlayerBean player = new PlayerBean(
				gameRoom.getJoinedUsers().size() - 1, userName,0);
//		if (gameRoom.getJoinedUsers().size() == 0) {
		int totalPlayers =gameManager.getPlayersManager().getAllAvailablePlayers().size() ; 
		if(totalPlayers== 0){
			player.setBalance(2000);
		} else if (totalPlayers== 1) {
			player.setBalance(1000);
		} else if (totalPlayers == 2) {
			player.setBalance(3000);
		}else {
			player.setBalance(1000);
		}
		
		player.setCards(gameManager.generatePlayerCards(),
				gameManager.generatePlayerCards(),
				gameManager.generatePlayerCards());

		if (GAME_STATUS == RUNNING){
			player.setWaitingForGame(true);
		}
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
		LogUtils.Log("Distribute cards...");
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
			LogUtils.Log("Default Cards : " + cardsObject.toString());
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
				LogUtils.Log("Blind Player Details : "
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
			LogUtils.Log(">>Round done " + cardsObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void broadcastGameCompleteToAllPlayers() {
		JSONArray winnerArray = new JSONArray();
		gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_GAME_COMPLETE
				+ winnerArray.toString());
		LogUtils.Log("winner array is  " + winnerArray.toString());
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
						.getPlayer().getBalance());
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
					waCardPot.getWinnerPlayer().setBalance(waCardPot.getWinnerPlayer().getBalance()+waCardPot.getPotAmt());
					waPotObject.put(TAG_WINNER_TOTAL_BALENCE, waCardPot.getWinnerPlayer().getBalance());
					winnerWAPotArray.put(waPotObject);
				}
			}
			winningPlayerObject.put("WA_Pot",winnerWAPotArray);
			winningPlayerObject.put("Table_Pot", winnerArray);
			LogUtils.Log("<<WinningPlayers>> " + winningPlayerObject.toString());
			gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_WINNIER_INFO
					+ winningPlayerObject.toString());
//			LogUtils.Log("<<>> " + winnerArray.toString());
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
					.getBalance());
			gameRoom.BroadcastChat(WA_SERVER_NAME, RESPONSE_FOR_ACTION_DONE
					+ cardsObject.toString());
			LogUtils.Log("Action<<>> " + cardsObject.toString());
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
