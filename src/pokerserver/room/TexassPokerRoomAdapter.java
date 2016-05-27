package pokerserver.room;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pokerserver.game.TexassGameManager;
import pokerserver.players.PlayerBean;
import pokerserver.rounds.RoundManager;
import pokerserver.turns.TurnManager;
import pokerserver.utils.APIConstants;
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
public class TexassPokerRoomAdapter extends BaseTurnRoomAdaptor implements
		GameConstants {

//	private IZone izone;
	private ITurnBasedRoom gameRoom;
	private byte GAME_STATUS;
	TexassGameManager gameManager;
	int minPlayerToStartGame= MIN_PLAYER_TO_START_GAME;
	private boolean isBreakTime = false;
	
	List<String> listRestartGameReq = new ArrayList<String>();
	
	public TexassPokerRoomAdapter(IZone izone, ITurnBasedRoom room) {
//		this.izone = izone;
		this.gameRoom = room;
		GAME_STATUS = STOPPED;
		this.gameManager = new TexassGameManager();
		gameManager.initGameRounds();
		if (gameRoom.getProperties().containsKey("MinPlayer")) {
			LogUtils.Log("Room : " + gameRoom.getName() + " >> "
					+ gameRoom.getProperties().toString() + " >> "
					+ gameRoom.getProperties().get("MinPlayer"));
			minPlayerToStartGame = Integer.valueOf(room.getProperties()
					.get("MinPlayer").toString());
		}
	}

	@Override
	public void onTimerTick(long time) {
		if (GAME_STATUS == STOPPED &&
				gameRoom.getJoinedUsers().size() >= minPlayerToStartGame &&
				GAME_STATUS !=CARD_DISTRIBUTE) {
			distributeCarsToPlayerFromDelear();
//			GAME_STATUS = RUNNING;
			GAME_STATUS = CARD_DISTRIBUTE;
		} else if (GAME_STATUS == RESUMED) {
			GAME_STATUS = RUNNING;
			gameRoom.startGame(TEXASS_SERVER_NAME);
		} else if (GAME_STATUS == RUNNING
				&& gameRoom.getJoinedUsers().size() < minPlayerToStartGame) {
			GAME_STATUS = STOPPED;
			gameRoom.stopGame(TEXASS_SERVER_NAME);
		}

	}
	private void startGame() {
		if((gameManager.getGameType()== GAME_TYPE_TOURNAMENT_REGULAR ||
				gameManager.getGameType() == GAME_TYPE_TOURNAMENT_SIT_N_GO) &&
				!gameManager.isTournamentStarted()){
			gameManager.setTournamentStarted(true);
			manageBliendLeveAndReBuyOfTournament();
		}
		GAME_STATUS = RUNNING;
		managePlayerTurn(gameManager.getPlayersManager().getBigBlindPayer()
				.getPlayerName());
		gameRoom.startGame(TEXASS_SERVER_NAME);
	}
	private void managePlayerTurn(String currentPlayer) {
		LogUtils.Log(">>Total Players : "
				+ gameRoom.getJoinedUsers().size() +" >> Current Plr : "+ currentPlayer);
		RoundManager currentRoundManager = gameManager.getCurrentRoundInfo();

		if (currentRoundManager != null) {
			PlayerBean nextPlayer = findNextPlayerFromCurrentPlayer(currentPlayer);
			if (nextPlayer != null) {
				while (nextPlayer.isFolded() || nextPlayer.isAllIn()) {
					LogUtils.Log(" Next turn player : "
							+ nextPlayer.getPlayerName());
					nextPlayer = findNextPlayerFromCurrentPlayer(nextPlayer
							.getPlayerName());
				}
				gameRoom.setNextTurn(getUserFromName(nextPlayer.getPlayerName()));
//				LogUtils.Log(currentPlayer
//						+ " >> Next valid turn player : "
//						+ nextPlayer.getPlayerName());
			}
		} 
	}
	public PlayerBean findNextPlayerFromCurrentPlayer(String currentPlayerName) {
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
	private IUser getUserFromName(String name) {
		for (IUser user : gameRoom.getJoinedUsers()) {
			if (user.getName().equals(name)) {
				return user;
			}
		}
		return null;
	}

	private void broadcastPlayerCardsInfo() {

		for (PlayerBean player : gameManager.getPlayersManager()
				.getAllAvailablePlayers()) {
		
			int plrStatus = STATUS_ACTIVE;
			
			if(player.isWaitingForGame()){
				plrStatus=ACTION_WAITING_FOR_GAME;
			}else if(player.isFolded() && GAME_STATUS == RUNNING ){
				plrStatus = ACTION_FOLDED;
			}
			
			JSONObject playerJsonObject = new JSONObject();
			try {
				playerJsonObject.put(TAG_PLAYER_NAME, player.getPlayerName());
				playerJsonObject.put(TAG_PLAYER_POSITION, player.getPlayerPosition());
				playerJsonObject.put(TAG_CARD_PLAYER_1, player.getFirstCard()
						.getCardName());
				playerJsonObject.put(TAG_CARD_PLAYER_2, player.getSecondCard()
						.getCardName());
				playerJsonObject.put(TAG_PLAYER_BALANCE, player.getBalance());
				playerJsonObject.put(TAG_GAME_STATUS, GAME_STATUS);
				playerJsonObject.put(TAG_PLAYER_STATUS, plrStatus);
				playerJsonObject.put(TAG_CURRENT_ROUND,gameManager.getCurrentRoundIndex());
				gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_PLAYERS_INFO
						+ playerJsonObject.toString());
				LogUtils.Log(gameRoom.getName()+" Texass Player Info : " + playerJsonObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
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

	public void handleMoveRequest(IUser sender, String moveData,
			HandlingResult result) {
		
		if (moveData.contains(REQUEST_FOR_ACTION)) {
//			LogUtils.Log("\nTexass : MoveRequest : Sender : " + sender.getName()
//					+ " : Data : " + moveData);
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
//			LogUtils.Log("Next Plr : "+ gameRoom.getNextTurnUser().getName() +" >> "+gameRoom.getTurnUser().getName());
		}
	}
	public void manageGameFinishEvent() {
		
		gameManager.moveToNextRound();
		// Broad cast game completed to all players
//		broadcastRoundCompeleteToAllPlayers();
		broadcastGameCompleteToAllPlayers();
		gameManager.findBestPlayerHand();
		gameManager.findAllWinnerPlayers();
		broadcastWinningPlayer();
		handleFinishGame();
		restartGameRequest();
	}
	public void managePlayerAction(String sender, int playerAction,
			int betAmount) {
		TurnManager turnManager = gameManager.managePlayerAction(sender,
				playerAction, betAmount);

		if (turnManager != null)
			broadcastPlayerActionDoneToOtherPlayers(turnManager);
		if(playerAction==ACTION_FOLD){
			LogUtils.Log("Test 02");
			gameManager.leavePlayerToGame(gameManager.getPlayersManager().getPlayerByName(sender));
		}
		// If all players are folded or all in then declare last player as a
		// winner
		PlayerBean lastActivePlayer = gameManager.checkAllAreFoldOrAllIn();
		if (lastActivePlayer != null) {
			manageGameFinishEvent();
		} else if (playerAction != ACTION_DEALER
				&& gameManager.checkEveryPlayerHaveSameBetAmount()) {
			isRoundCompelete = true;
			if (gameManager.getCurrentRoundInfo().getStatus() == STATUS_ACTIVE
					&& gameManager.getCurrentRoundIndex() == TEXASS_ROUND_RIVER) {
				manageGameFinishEvent();
			} else {
				gameManager.moveToNextRound();
				broadcastRoundCompeleteToAllPlayers();
			}
		}
	}


	public void handleUserLeavingTurnRoom(IUser user, HandlingResult result) {
		
		LogUtils.Log("Room : handleUserLeavingTurnRoom :  User : "
				+ user.getName());
		try {
			sendLeavePlayerDetailsToServer(user.getName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (gameManager.getCurrentRoundInfo() == null) {
			gameManager.leavePlayerToGame(gameManager.getPlayersManager()
					.getPlayerByName(user.getName()));
		} else {
			managePlayerAction(user.getName(), ACTION_FOLD, 0);
		}
		
		broadcastBlindPlayerDatas();
		
		
		// This will be changed.
		if (GAME_STATUS == RUNNING || GAME_STATUS == FINISHED) {
			if(gameRoom.getJoinedUsers().size() == 0){
				LogUtils.Log("Room : Game Over ..... ");
				gameManager.getPlayersManager().removeAllPlayers();
				GAME_STATUS = FINISHED;
				//For PokerUP
				sendGameFinishStatusToServer();
			}
		}
		if(gameRoom.getJoinedUsers().size() == 0 && gameManager.getGameType() != GAME_TYPE_REGULAR){
//			System.out.print("CD:: Bliend Amt : "+SBAmount);
			gameManager.setNewBliendAmount(SBAmount);
		}
	}

	/*
	 * This function stop the game and notify the room players about winning
	 * user and his cards.
	 */
	private void handleFinishGame() {
		try {
			gameRoom.stopGame(TEXASS_SERVER_NAME);
			GAME_STATUS = FINISHED;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void handleRestartGame() {
		LogUtils.Log("--- Restarting Game -------- ");
		listRestartGameReq.clear();
		gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_GAME_START);
		// For Temporary. It will be come from DB
		for(PlayerBean playerBean : gameManager.getPlayersManager().getAllAvailablePlayers()){
			gameManager.addTournamentPlayer(playerBean);
		}
		gameManager.getPlayersManager().removeAllPlayers();
		gameManager.updateTotalGameCntr(gameRoom.getJoinedUsers().size());
		gameManager.initGameRounds();
		
		GAME_STATUS = STOPPED;
		for (IUser user : gameRoom.getJoinedUsers()) {
			addNewPlayerCards(user.getName());
		}
		sendDefaultCards(null, true);
		broadcastPlayerCardsInfo();
		broadcastBlindPlayerDatas();
		
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
		
		// This will be remove
		if ( message.startsWith(RESPONSE_FOR_DESTRIBUTE_CARD)) {
			listRestartGameReq.add(sender.getName());
//			LogUtils.Log("Total Request : "+listRestartGameReq.size());
			if (isRequestFromAllActivePlayers()) {
				listRestartGameReq.clear();
				gameManager.startPreFlopRound();
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getSmallBlindPayer().getPlayerName(), ACTION_BET,
						gameManager.getBliendAmount());
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getBigBlindPayer().getPlayerName(), ACTION_BET,
						gameManager.getBliendAmount() * 2);
				startGame();
				
			}
			// This will be remove
		} else if (message.startsWith(REQUEST_FOR_RESTART_GAME)) {
			listRestartGameReq.add(sender.getName());
			if (isRequestFromAllActivePlayers()){
				listRestartGameReq.clear();
				restartGameRequest();
			}
		}else if(message.startsWith(REQUEST_FOR_BLIEND_AMOUNT)){
			message = message.replace(REQUEST_FOR_BLIEND_AMOUNT, "");
			try {
				JSONObject jsonObject = new JSONObject(message);
				gameManager.setNewBliendAmount(jsonObject.getInt(TAG_SMALL_BLIEND_AMOUNT));
				gameManager.setGameType(jsonObject.getInt(TAG_GAME_TYPE));
				isBreakTime=false;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else if(message.startsWith(REQUEST_FOR_RE_BUY)){
			JSONObject playerObject = new JSONObject();
			try {
				PlayerBean playerBean = gameManager.getPlayersManager().getPlayerByName(sender.getName());
				playerBean.setBalance(playerBean.getBalance()+gameManager.getTournamentEntryFee());
				playerObject.put(TAG_PLAYER_NAME, sender.getName());
				playerObject.put(TAG_PLAYER_BALANCE, playerBean.getBalance());
			}catch(JSONException e){
				e.printStackTrace();
			}
			LogUtils.Log("<> Request for ReBuy : "+ playerObject.toString());
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME,
					REQUEST_FOR_RE_BUY + playerObject.toString());
		}
	}
	private boolean isRequestFromAllActivePlayers() {
		for (PlayerBean playerBean : gameManager.getPlayersManager()
				.getAllAactivePlayersForTurn()) {
			if (!listRestartGameReq.contains(playerBean.getPlayerName())) {
				return false;
			}
		}
		return true;
	}
	
	private void restartGameRequest() {

		long timeLng = 1000 * RESTART_GAME_TIME;
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (!isBreakTime)
					handleRestartGame();
				else {
					LogUtils.Log("===<><><> Break Time <><><>===");
					startBreakTimer();
				}
				timer.cancel();
			}
		}, timeLng, timeLng);

	}

	private void startGameAfterDistributeCardsOnTable(){

		long timeLng = 1000 * (gameManager.getPlayersManager().getTotalActivePlayerCounter() * 2);
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				LogUtils.Log("----------------------------");
				gameManager.startPreFlopRound();
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getSmallBlindPayer().getPlayerName(), ACTION_BET,
						gameManager.getBliendAmount());
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getBigBlindPayer().getPlayerName(), ACTION_BET,
						gameManager.getBliendAmount() * 2);
				startGame();
				timer.cancel();
			}
		}, timeLng, timeLng);

		
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
		if(gameManager.getGameType()==GAME_TYPE_TOURNAMENT_REGULAR ||
				gameManager.getGameType()== GAME_TYPE_TOURNAMENT_SIT_N_GO)
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_RE_BUY_STATUS+gameManager.isReBuyChips);
	}
	
	private void addNewPlayerCards(String userName) {
		int plrPositionOnTable = getPlayerPosition();
		
		PlayerBean player = new PlayerBean(
				gameRoom.getJoinedUsers().size() - 1, userName, plrPositionOnTable);
//		int totalPlayers =gameManager.getPlayersManager().getAllAvailablePlayers().size() ; 
		/*if(gameManager.getGameType()==GAME_TYPE_REGULAR){
			if(totalPlayers== 0){
				player.setTotalBalance(2000);
			} else if (totalPlayers== 1) {
				player.setTotalBalance(1000);
			} else if (totalPlayers == 2) {
				player.setTotalBalance(3000);
			} else {
				player.setTotalBalance(1000);
			}
		}else{
			int plrBalance = 1000;
			plrBalance = gameManager.getPlayerPreviousBalance(userName);
			LogUtils.Log("<<>>> "+userName +" : "+plrBalance);
			player.setTotalBalance(plrBalance);
		}*/
		int prvBalance =gameManager.getPlayerPreviousBalance(userName);
		int plrBalance = prvBalance!=0 ?prvBalance :gameManager.getTournamentEntryFee();
//		LogUtils.Log("<<>>> "+userName +" : "+plrBalance);
		player.setBalance(plrBalance);
		
		player.setCards(gameManager.generatePlayerCards(),
				gameManager.generatePlayerCards(),
				gameManager.generatePlayerCards());
		if (GAME_STATUS == RUNNING || GAME_STATUS == CARD_DISTRIBUTE){
			player.setWaitingForGame(true);
		}
		if(player.getBalance()<=0){
			gameRoom.removeUser(getUserFromName(player.getPlayerName()), true);
		}
		gameManager.addNewPlayerToGame(player);
	}
	public void onUserPaused(IUser user) {
	}

	public void onUserResume(IUser user) {
	}

	private void distributeCarsToPlayerFromDelear() {
		gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_DESTRIBUTE_CARD);
		LogUtils.Log("Distribute cards...");
		startGameAfterDistributeCardsOnTable();
	}

	/** Manage default and player hand cards */
	public void sendDefaultCards(IUser user,boolean isBroadcast) {
		JSONObject cardsObject = new JSONObject();
		try {
			cardsObject.put(TAG_CARD_FLOP_1,
					gameManager.getDefaultCards().get(INDEX_FLOP_1)
							.getCardName());
			cardsObject.put(TAG_CARD_FLOP_2,
					gameManager.getDefaultCards().get(INDEX_FLOP_2)
							.getCardName());
			cardsObject.put(TAG_CARD_FLOP_3,
					gameManager.getDefaultCards().get(INDEX_FLOP_3)
							.getCardName());
			cardsObject
					.put(TAG_CARD_TURN,
							gameManager.getDefaultCards().get(INDEX_TURN)
									.getCardName());
			cardsObject.put(TAG_CARD_RIVER,
					gameManager.getDefaultCards().get(INDEX_RIVER)
							.getCardName());
			if (isBroadcast) {
				gameRoom.BroadcastChat(TEXASS_SERVER_NAME,
						RESPONSE_FOR_DEFAULT_CARDS + cardsObject.toString());
			}else{
				user.SendChatNotification(TEXASS_SERVER_NAME, RESPONSE_FOR_DEFAULT_CARDS
						+ cardsObject.toString(), gameRoom);
			}
			LogUtils.Log("Default Cards Details : "
					+ cardsObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void broadcastBlindPlayerDatas() {
		JSONObject playersObject = new JSONObject();
		try {
			if (!gameManager.getPlayersManager().getAllAactivePlayersForTurn()
					.isEmpty()) {
				int totalPlayerInRoom = gameManager.getPlayersManager()
						.getAllAactivePlayersForTurn().size();
				
				if (totalPlayerInRoom > 0) {
					playersObject.put(TAG_PLAYER_DEALER, gameManager
							.getPlayersManager().getDealerPayer().getPlayerName());
					playersObject.put(TAG_PLAYER_BIG_BLIND, gameManager
							.getPlayersManager().getDealerPayer().getPlayerName());
				} else {
					playersObject.put(TAG_PLAYER_DEALER, RESPONSE_DATA_SEPRATOR);
					playersObject.put(TAG_PLAYER_BIG_BLIND,
							RESPONSE_DATA_SEPRATOR);
				}
				if (totalPlayerInRoom > 1) {
					playersObject.put(TAG_PLAYER_SMALL_BLIND, gameManager
							.getPlayersManager().getSmallBlindPayer().getPlayerName());
				} else {
					playersObject.put(TAG_PLAYER_SMALL_BLIND,
							RESPONSE_DATA_SEPRATOR);
				}
				if (totalPlayerInRoom > 2) {
					playersObject.put(TAG_PLAYER_BIG_BLIND, gameManager
							.getPlayersManager().getBigBlindPayer().getPlayerName());
				}
				playersObject.put(TAG_SMALL_BLIEND_AMOUNT,gameManager.getBliendAmount());
				
				LogUtils.Log("Blind Player Details : "
						+ playersObject.toString());
				gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_BLIEND_PLAYER
						+ playersObject.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void broadcastRoundCompeleteToAllPlayers() {
		JSONObject roundObject = new JSONObject();
		try {
			roundObject.put(TAG_ROUND, gameManager.getCurrentRoundIndex());
			roundObject
					.put(TAG_TABLE_AMOUNT, gameManager.getTotalTableAmount());
//			LogUtils.Log("Round done " + cardsObject.toString());
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_ROUND_COMPLETE
					+ roundObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	private void broadcastWinningPlayer() {
		JSONArray gamePlayerArray = new JSONArray();
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
				JSONObject jsonObject = sendGamePlayerDetailsToServer(gamePlayerArray, winnerPlayer.getPlayer(), true);
				if(jsonObject!=null)
					gamePlayerArray.put(jsonObject);
			}
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_WINNIER_INFO
					+ winnerArray.toString());
			LogUtils.Log("Winner Data : " + winnerArray.toString());
			
			// Get lost player data
			for(PlayerBean playerBean : gameManager.getPlayersManager().getAllAactivePlayersForTurn()){
				JSONObject jsonObject = sendGamePlayerDetailsToServer(gamePlayerArray, playerBean, false);
				if(jsonObject!=null)
					gamePlayerArray.put(jsonObject);
			}
			LogUtils.Log("Game Player status : "+ gamePlayerArray.toString());
			sendGameWinningIfoToServer(gamePlayerArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Send game player data to server
	 * @param winnerArray
	 * @param player
	 * @param isWinner
	 * @return
	 */
	
	public JSONObject sendGamePlayerDetailsToServer(JSONArray winnerArray,PlayerBean player,boolean isWinner){
		// First check is user is already in list of not
		for(int i = 0;i<winnerArray.length();i++){
			try {
				JSONObject jsonObject = winnerArray.getJSONObject(i);
				if(jsonObject.getString(APIConstants.TAG_USER_ID).equals(player.getPlayerName())){
					return null;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(APIConstants.TAG_USER_ID, player.getPlayerName());
			jsonObject.put(APIConstants.TAG_BALANCE, player.getBalance());
			jsonObject.put(APIConstants.TAG_STATUS, isWinner?"win":"lost");
			return jsonObject;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void broadcastGameCompleteToAllPlayers() {
        JSONArray   winnerArray=new JSONArray();
        gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_GAME_COMPLETE
		     + winnerArray.toString());
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
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_ACTION_DONE
					+ cardsObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void manageBliendLeveAndReBuyOfTournament(){
		if(gameManager.getGameType()==GAME_TYPE_TOURNAMENT_SIT_N_GO){
			startBliendLevelTimer(TOURNAMENT_SNG_BLIND_LEVEL_TIMER);
		}else if(gameManager.getGameType()== GAME_TYPE_TOURNAMENT_REGULAR){
			startBliendLevelTimer(TOURNAMENT_REGULAR_LEVEL_TIMER);
			startReBuyChipsTimer();
			startBreakWaitingTimer();
		}
	}
	private void startBliendLevelTimer(int time){
		long timeLng = 1000 * time;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (GAME_STATUS == RUNNING) {
					gameManager.setNewBliendAmount(gameManager
							.getBliendAmount() * 2);
					LogUtils.Log("BliendLevel Updated : "
							+ gameManager.getBliendAmount() * 2);
				}
			}
		}, timeLng, timeLng);
	}
/**
 * When a player has lost all of his/her chips within the first hour of Tournament, this player can
click the "ReBuy" button to rebuy chips equal to the original Entry Fee without 10% house feeas many times as they want within an hour of Tournament and get back into the tournament.
 */
	
	private void startReBuyChipsTimer(){
		long timeLng = 1000 * TOURNAMENT_REBUY_TIMER;
		final Timer timer = new Timer();
		gameManager.isReBuyChips = true;
		gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_RE_BUY_STATUS+gameManager.isReBuyChips);
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				LogUtils.Log("ReBuy Timer Stop");
				gameManager.isReBuyChips = false;
				gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_RE_BUY_STATUS+gameManager.isReBuyChips);
				timer.cancel();
			}
		}, timeLng, timeLng);
	}
	
	private void startBreakTimer(){
		long timeLng = 1000 * TOURNAMENT_BREAK_TIMER;
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				handleRestartGame();
				startBreakWaitingTimer();
				timer.cancel();
			}
		}, timeLng, timeLng);
	}
	private void startBreakWaitingTimer(){
		long timeLng = 1000 * TOURNAMENT_BREAK_WAITING_TIME;
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				isBreakTime=true;
//				LogUtils.Log("--------------- Break Time");
				gameRoom.BroadcastChat(TEXASS_SERVER_NAME,
						RESPONSE_FOR_BREAK_STATUS);
				timer.cancel();
			}
		}, timeLng, timeLng);
	}
	
	private int getPlayerPosition(){
		if(!checkPlayerPositionAlreadyDefine(newPlayerPosition)){
			return newPlayerPosition;
		}else{
			if(newPlayerPosition < gameRoom.getMaxUsers()-1){
				newPlayerPosition++;
			}else{
				newPlayerPosition = 0;
			}
			return getPlayerPosition();
		}
	}
	private boolean checkPlayerPositionAlreadyDefine(int position){
		for(PlayerBean player : gameManager.getPlayersManager().getAllAvailablePlayers()){
			if(player.getPlayerPosition()== position){
				return true;
			}
		}
		return false;
	}
	
	public void sendGameWinningIfoToServer(JSONArray playerArray) throws JSONException{
		JSONObject playObject = new JSONObject();
		playObject.put("player_list", playerArray);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(APIConstants.TAG_GAME_ID, String.valueOf(gameRoom.getProperties().get("RoomName"))));
		params.add(new BasicNameValuePair(APIConstants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis())));
		
		params.add(new BasicNameValuePair("players", playObject.toString()));
		LogUtils.Log("API Winnner Req : "+params.toString());
		sendDataToServer(APIConstants.ACTION_WIN_USER, params);
		
	}
	public void sendGameFinishStatusToServer(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(APIConstants.TAG_GAME_ID, String.valueOf(gameRoom.getProperties().get("RoomName"))));
		params.add(new BasicNameValuePair(APIConstants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis())));
		LogUtils.Log("Exit Game req : "+params.toString());
		sendDataToServer(APIConstants.ACTION_END_GAME, params);
	}
	
	private void sendLeavePlayerDetailsToServer(String user) throws JSONException {
		PlayerBean playerBean = gameManager.getPlayersManager().getPlayerByName(user);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(APIConstants.TAG_GAME_ID, String.valueOf(gameRoom.getProperties().get("RoomName"))));
		params.add(new BasicNameValuePair(APIConstants.TAG_BALANCE, String.valueOf( playerBean.getBalance())));
		params.add(new BasicNameValuePair(APIConstants.TAG_USER_ID, String.valueOf( playerBean.getPlayerName())));
		params.add(new BasicNameValuePair(APIConstants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis())));
		
		LogUtils.Log("Leave Player req : "+params.toString());
		sendDataToServer(APIConstants.ACTION_LEAVE_USER, params);
	}
	
	public void sendDataToServer(String url,List<NameValuePair> params){
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		// Request parameters and other properties.
		try {
		    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		    // writing error to Log
		    e.printStackTrace();
		}
		try {
		    HttpResponse response = httpClient.execute(httpPost);
		    HttpEntity respEntity = response.getEntity();
		    if (respEntity != null) {
		        // EntityUtils to get the response content
		        String content =  EntityUtils.toString(respEntity);
		        System.out.println("Response >:< "+ content);
		    }
		} catch (ClientProtocolException e) {
		    // writing exception to log
		    e.printStackTrace();
		} catch (IOException e) {
		    // writing exception to log
		    e.printStackTrace();
		}
	} 

	int newPlayerPosition = 0;
}
