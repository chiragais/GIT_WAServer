package pokerserver.room;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pokerserver.TexassGameManager;
import pokerserver.players.PlayerBean;
import pokerserver.players.Winner;
import pokerserver.rounds.RoundManager;
import pokerserver.turns.TurnManager;
import pokerserver.utils.GameConstants;

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

	private IZone izone;
	private ITurnBasedRoom gameRoom;
	private byte GAME_STATUS;
	TexassGameManager gameManager;
	private boolean isBreakTime = false;
	
	List<String> listRestartGameReq = new ArrayList<String>();
	
	public TexassPokerRoomAdapter(IZone izone, ITurnBasedRoom room) {
		this.izone = izone;
		this.gameRoom = room;
		GAME_STATUS = STOPPED;
		this.gameManager = new TexassGameManager();
		gameManager.initGameRounds();
		System.out.println("Texass Room : " + room.getName()+ " >> "+room.getMaxUsers());
	}

	@Override
	public void onTimerTick(long time) {
		if (GAME_STATUS == STOPPED
				&& gameRoom.getJoinedUsers().size() >= MIN_PLAYER_TO_START_GAME) {
			distributeCarsToPlayerFromDelear();
			GAME_STATUS = RUNNING;
		} else if (GAME_STATUS == RESUMED) {
			GAME_STATUS = RUNNING;
			gameRoom.startGame(TEXASS_SERVER_NAME);
		} else if (GAME_STATUS == RUNNING
				&& gameRoom.getJoinedUsers().size() < MIN_PLAYER_TO_START_GAME) {
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

		managePlayerTurn(gameManager.getPlayersManager().getBigBlindPayer()
				.getPlayerName());
		gameRoom.startGame(TEXASS_SERVER_NAME);
	}
	private void managePlayerTurn(String currentPlayer) {
//		System.out.println(">>Total Players : "
//				+ gameRoom.getJoinedUsers().size());
		RoundManager currentRoundManager = gameManager.getCurrentRoundInfo();

		if (currentRoundManager != null) {
			PlayerBean nextPlayer = getNextPlayerFromCurrentPlayer(currentPlayer);
			if (nextPlayer == null) {
//				System.out.println(" Next turn player : Null");
			} else {
				while (nextPlayer.isFolded() || nextPlayer.isAllIn()) {
					System.out.println(" Next turn player : "
							+ nextPlayer.getPlayerName());
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
			}else if(player.isFolded() ){
				plrStatus = ACTION_FOLDED;
			}
			
			JSONObject cardsObject = new JSONObject();
			try {
				cardsObject.put(TAG_PLAYER_NAME, player.getPlayerName());
				cardsObject.put(TAG_PLAYER_POSITION, player.getPlayerPosition());
				cardsObject.put(TAG_CARD_PLAYER_1, player.getFirstCard()
						.getCardName());
				cardsObject.put(TAG_CARD_PLAYER_2, player.getSecondCard()
						.getCardName());
				cardsObject.put(TAG_PLAYER_BALANCE, player.getBalance());
				cardsObject.put(TAG_GAME_STATUS, GAME_STATUS);
				cardsObject.put(TAG_PLAYER_STATUS, plrStatus);
				cardsObject.put(TAG_CURRENT_ROUND,gameManager.getCurrentRoundIndex());
				gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_PLAYERS_INFO
						+ cardsObject.toString());
				System.out.println("Texass Player Info : " + cardsObject.toString());
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

	public void handleMoveRequest(IUser sender, String moveData,
			HandlingResult result) {
		if (moveData.contains(REQUEST_FOR_ACTION)) {
//			System.out.println("\nTexass : MoveRequest : Sender : " + sender.getName()
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
		}
	}
	public void manageGameFinishEvent() {
		
		gameManager.moveToNextRound();
		// Broad cast game completed to all players
		broadcastRoundCompeleteToAllPlayers();
		broadcastGameCompleteToAllPlayers();
		gameManager.findBestPlayerHand();
		gameManager.findAllWinnerPlayers();
		broadcastWinningPlayer();
		handleFinishGame();
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
	public void handleStartGameRequest(IUser sender, HandlingResult result) {
		System.out.println("Room : handleStartGameRequest : Sender User : "
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
	public void handleStopGameRequest(IUser sender, HandlingResult result) {
		
		System.out.println("Room : handleStopGameRequest : Sender User : "
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
		managePlayerAction(turn.getName(), ACTION_FOLD, 0);

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
	public void handleUserLeavingTurnRoom(IUser user, HandlingResult result) {
		
		System.out.println("Room : handleUserLeavingTurnRoom :  User : "
				+ user.getName());
		gameManager.leavePlayerToGame(gameManager.getPlayersManager().getPlayerByName(user
				.getName()));
		broadcastBlindPlayerDatas();
		// This will be changed.
		if (GAME_STATUS == RUNNING || GAME_STATUS == FINISHED) {
			if(gameRoom.getJoinedUsers().size() == 0){
				System.out.println("Room : Game Over ..... ");
				gameManager.getPlayersManager().removeAllPlayers();
				GAME_STATUS = FINISHED;
			}else{
//				System.out.print("CD:: "+gameRoom.getNextTurnUser().getName());
//				gameRoom.setNextTurn(gameRoom.getNextTurnUser());
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
//			gameRoom.setAdaptor(null);
//			izone.deleteRoom(gameRoom.getId());
			gameRoom.stopGame(TEXASS_SERVER_NAME);
			GAME_STATUS = FINISHED;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void handleRestartGame() {

		System.out.println("--- Restarting Game -------- ");
		listRestartGameReq.clear();
		gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_GAME_START);
//		gameManager.initGameRounds();
		// For Temporary. It will be come from DB
		for(PlayerBean playerBean : gameManager.getPlayersManager().getAllAvailablePlayers()){
			gameManager.addTournamentPlayer(playerBean);
		}
		gameManager.getPlayersManager().removeAllPlayers();
		gameManager.initGameRounds();
		for (IUser user : gameRoom.getJoinedUsers()) {
			addNewPlayerCards(user.getName());
		}
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
				gameManager.startPreFlopRound();
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getSmallBlindPayer().getPlayerName(), ACTION_BET,
						gameManager.getBliendAmount());
				gameManager.managePlayerAction(gameManager.getPlayersManager()
						.getBigBlindPayer().getPlayerName(), ACTION_BET,
						gameManager.getBliendAmount() * 2);
				startGame();
			}
		} else if (message.startsWith(REQUEST_FOR_RESTART_GAME)) {
			listRestartGameReq.add(sender.getName());
			if (isRequestFromAllActivePlayers())
				if(!isBreakTime)
					handleRestartGame();
				else{
					System.out.println("===<><><> Break Time <><><>===");
					startBreakTimer();
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
			System.out.println("<> Request for ReBuy : "+ sender);
//			sender.SendChatNotification(TEXASS_SERVER_NAME, REQUEST_FOR_RE_BUY, gameRoom);
			JSONObject playerObject = new JSONObject();
			try {
				PlayerBean playerBean = gameManager.getPlayersManager().getPlayerByName(sender.getName());
				playerBean.setBalance(playerBean.getBalance()+gameManager.getTournamentEntryFee());
				playerObject.put(TAG_PLAYER_NAME, sender.getName());
				playerObject.put(TAG_PLAYER_BALANCE, playerBean.getBalance());
			}catch(JSONException e){
				e.printStackTrace();
			}
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME,
					REQUEST_FOR_RE_BUY + playerObject.toString());
		}
	}
	private boolean isRequestFromAllActivePlayers() {
		// TODO Auto-generated method stub
		for (PlayerBean playerBean : gameManager.getPlayersManager()
				.getAllAvailablePlayers()) {
			if (!listRestartGameReq.contains(playerBean.getPlayerName())) {
				return false;
			}
		}
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
		if(gameManager.getGameType()==GAME_TYPE_TOURNAMENT_REGULAR || gameManager.getGameType()== GAME_TYPE_TOURNAMENT_SIT_N_GO)
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_RE_BUY_STATUS+gameManager.isReBuyChips);
		System.out.println("Game Status : " + GAME_STATUS +" >> "+gameManager.getGameType());
	}
	
	private void addNewPlayerCards(String userName) {
		int plrPositionOnTable = getPlayerPosition();
		
		PlayerBean player = new PlayerBean(
				gameRoom.getJoinedUsers().size() - 1, userName, plrPositionOnTable);
		int totalPlayers =gameManager.getPlayersManager().getAllAvailablePlayers().size() ; 
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
			System.out.println("<<>>> "+userName +" : "+plrBalance);
			player.setTotalBalance(plrBalance);
		}*/
		int prvBalance =gameManager.getPlayerPreviousBalance(userName);
		int plrBalance = prvBalance!=0 ?prvBalance :gameManager.getTournamentEntryFee();
		System.out.println("<<>>> "+userName +" : "+plrBalance);
		player.setBalance(plrBalance);
		
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
		gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_DESTRIBUTE_CARD);
		System.out.println("Distribute cards...");
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
				
//				System.out.println("Total Players : " + totalPlayerInRoom);

				if (totalPlayerInRoom > 0) {
					cardsObject.put(TAG_PLAYER_DEALER, gameManager
							.getPlayersManager().getDealerPayer().getPlayerName());
					cardsObject.put(TAG_PLAYER_BIG_BLIND, gameManager
							.getPlayersManager().getDealerPayer().getPlayerName());
				} else {
					cardsObject.put(TAG_PLAYER_DEALER, RESPONSE_DATA_SEPRATOR);
					cardsObject.put(TAG_PLAYER_BIG_BLIND,
							RESPONSE_DATA_SEPRATOR);
				}
				if (totalPlayerInRoom > 1) {
					cardsObject.put(TAG_PLAYER_SMALL_BLIND, gameManager
							.getPlayersManager().getSmallBlindPayer().getPlayerName());
				} else {
					cardsObject.put(TAG_PLAYER_SMALL_BLIND,
							RESPONSE_DATA_SEPRATOR);
				}
				if (totalPlayerInRoom > 2) {
					cardsObject.put(TAG_PLAYER_BIG_BLIND, gameManager
							.getPlayersManager().getBigBlindPayer().getPlayerName());
				}
				cardsObject.put(TAG_SMALL_BLIEND_AMOUNT,gameManager.getBliendAmount());
				
				System.out.println("Blind Player Details : "
						+ cardsObject.toString());
				gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_BLIEND_PLAYER
						+ cardsObject.toString());
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
//			System.out.println("Round done " + cardsObject.toString());
			gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_ROUND_COMPLETE
					+ cardsObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	private void broadcastWinningPlayer() {
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

			gameRoom.BroadcastChat(TEXASS_SERVER_NAME, RESPONSE_FOR_WINNIER_INFO
					+ winnerArray.toString());
			System.out.println("<<>> " + winnerArray.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
				gameManager.setNewBliendAmount(gameManager.getBliendAmount() * 2);
				System.out.println("BliendLevel Updated : "+gameManager.getBliendAmount() * 2);
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
				System.out.println("ReBuy Timer Stop");
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
				System.out.println("--------------- Break Time");
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
	int newPlayerPosition = 0;
}
