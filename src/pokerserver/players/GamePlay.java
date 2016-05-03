package pokerserver.players;

import java.util.ArrayList;
import java.util.List;

import pokerserver.cards.Card;
import pokerserver.cards.PlayerCards;
import pokerserver.utils.GameConstants;

public class GamePlay implements GameConstants {

	public ArrayList<PlayerCards> userData;
	public Card[] tableCards; // Array of table cards to be displayed on table
								// for all the users
	public int[] betText; // Array of all Bet String of players on table
	public String[] playerNameText; // Array of all player Names
	public String[] playerBalanceText; // Array of all Player Balance
	public int callAmt;
	public String raiseAmt;
	public int betAmount = 0;
	private int sbBet = 0; // Small Blind Bet
	private int bbBet = 0; // Big Blind Bet
	private int lastTurnAmt = 0; // Last total bet amount of a player on table
	public int curPlayerId = 0; // Current player Id
	private int TotalTableAmt = 0;
	public String TotalTableText;
	public PlayersManager playerManager;
	private int curCallAmt = 0;
	private int curRaiseAmt = 0;
	private boolean isActiveCall = true;
	private boolean isActiveBet;
	private boolean isActiveFold;
	private boolean isActiveRaise;
	private boolean isActiveCheck;
	private boolean isActiveAllIn;
	public int curBetAmount = 0;
	private int curPlayerBalance;
	private int myPlayerId = 0;
	private boolean turnExecuted;
	private int curTurnNo = 1; // turn counter
	private int[] betAmtIntArray; // all betting amt integer array
	// public Button CallBtn;
	// public Button BetBtn;
	int nPlayer = 3;
	int curRound = -1; // Current round player, playing in.
	public boolean roundComplete = false;
	private int[] allInAmountArray; // it contains the valid table amount for
									// all player at time of AllIn
	private Card[] allFinalTableCards; // it contains all seven cards at the end
										// of River round.
	private ArrayList<PlayerCards> UserCardsList;
	private int lastRoundTableAmt = 0; // total pot amount of last round on
										// table

	private ArrayList<PlayerHand> AllPokerHandsList;
	private ArrayList<HAND_RANK> AllPlayersRankArray;
	// private PokerHandRank[] AllPlayersRankArray;
	private ArrayList<Integer> ActivePlayerList;
	private ArrayList<Integer> WinnerList;
	public String txtAllText;
	// int intPlayerCounter = 0;

	private int TopWinnerRank = 0;

	// public Slider sliderObj;
	private int maxValue = 100;

	public GamePlay(PlayersManager plraMgr) {

		// intializtion
		betText = new int[nPlayer];
		// intializtion
		allInAmountArray = new int[nPlayer];
		for (int i = 0; i < allInAmountArray.length; i++) {
			allInAmountArray[i] = 0;
		}

		// intializtion
		betAmtIntArray = new int[nPlayer];
		for (int i = 0; i < betAmtIntArray.length; i++) {
			betAmtIntArray[i] = 0;
		}

		this.playerManager = plraMgr;

		userData = new ArrayList<PlayerCards>();

		for (int i = 0; i < nPlayer; i++) {
			userData.add(playerManager.getPlayer(i).getPlayerCards());
		}

		// curRound = ROUND_PREFLOP;

		for (int i = 0; i < betText.length; i++) {
			betText[i] = 0;
		}

		// setting perticuler player as small blind and big blind
		playerManager.getPlayer(0).setSmallBlind(true);
		playerManager.getPlayer(1).setBigBlind(true);
		OnStartGame();
	}

	public void OnStartGame() {

		if (playerManager.getPlayer(curPlayerId).isSmallBlind()) {

			callAmt = sbBet;
			curCallAmt = sbBet;
			playerManager.getPlayer(curPlayerId).setSmallBlind(false);

			curPlayerBalance = playerManager.getPlayer(curPlayerId)
					.getTotalBalance();
			curPlayerBalance = curPlayerBalance - curCallAmt;

			lastTurnAmt = sbBet;

			betText[curPlayerId] = sbBet;// String.valueOf(sbBet);

			playerManager.getPlayer(curPlayerId).setTotalBalance(
					curPlayerBalance);
			ChangePlayerTurn();
			if (playerManager.getPlayer(curPlayerId).isBigBlind()) {

				callAmt = bbBet;

				curCallAmt = bbBet;
				playerManager.getPlayer(curPlayerId).setBigBlind(false);

				curPlayerBalance = playerManager.getPlayer(curPlayerId)
						.getTotalBalance();

				curPlayerBalance = curPlayerBalance - curCallAmt;

				lastTurnAmt = bbBet;
				betText[curPlayerId] = bbBet;// String.valueOf(bbBet);
				// playerManager.updatePlayerBalance(
				// playerManager.getPlayer(curPlayerId).getPlayeName(),
				// curPlayerId, curPlayerBalance);
				playerManager.getPlayer(curPlayerId).setTotalBalance(
						curPlayerBalance);

				ChangePlayerTurn();
			}
		}
	}

	public void setTableCards(List<Card> crdlist) {
		tableCards = new Card[5];
		for (int i = 0; i < tableCards.length; i++) {
			tableCards[i] = crdlist.get(i);
		}
	}

	public void setCurrentRoundIndex(int curRound) {
		this.curRound = curRound;
	}

	public void setCurrentPlayerId(int curId) {
		this.curPlayerId = curId;
	}

	/** Executes player's current Action */
	public void executePlayerAction(int curPutAmt, int curAction) {

		switch (curAction) {
		case ACTION_CALL:
			curCallAmt = curPutAmt;
			OnCallButtonPress();
			break;
		case ACTION_RAISE:
			curRaiseAmt = curPutAmt;
			OnRaiseButtonPress();
			break;
		case ACTION_FOLD:
			OnFoldButtonPress();
			break;
		case ACTION_BET:
			curCallAmt = curPutAmt;
			OnBetBtnPressed();
			break;
		case ACTION_CHECK:
			OnCheckButtonPress();
			break;
		case ACTION_ALL_IN:
			OnAllInButtonPress();
			break;
		}
	}

	// TODO : Need to change logic
	// calculates call amount , player turn, position etc.
	public void update() {

		CheckRoundComplete();
	}

	// method to check wether round is completed or not
	public void CheckRoundComplete() {
		// if (!flag && maxAmt != 0) {
		OnRoundCompeleted();
		CountAmountForAllInMember();
		nextRoundPreset();
		// }

	}

	public boolean CheckSameBetAmount() {

		for (int i = 0; i < betAmtIntArray.length; i++) {

			System.out.println("betAmt  " + i + " = " + betAmtIntArray[i]);
		}
		boolean flag = false;
		int maxAmt = 0;
		for (int i = 0; i < betAmtIntArray.length; i++) {
			if (!playerManager.getPlayer(i).isFolded()) {
				maxAmt = betAmtIntArray[i];
				break;
			}
		}
		for (int j = 0; j < betAmtIntArray.length; j++) {
			if (maxAmt < betAmtIntArray[j]) {
				maxAmt = betAmtIntArray[j];
			}
		}
		for (int k = 0; k < betAmtIntArray.length; k++) {
			if (!playerManager.getPlayer(k).isFolded()) {

				if (maxAmt != betAmtIntArray[k]) {
					flag = true;
					return false;
					// break;
				}
			}
		}
		if (!flag && maxAmt != 0) {
			return true;
		} else {
			return false;
		}
	}

	/** Reset values for the next round */

	public void nextRoundPreset() {
		switch (curRound) {

		case TEXASS_ROUND_PREFLOP:
			// curRound = ROUND_FLOP;
			FlopRoundCommCards();
			ResetForNextRound();
			break;
		case TEXASS_ROUND_FLOP:
			// curRound = ROUND_TURN;
			TurnRoundCommCards();
			ResetForNextRound();
			break;
		case TEXASS_ROUND_TURN:
			// curRound = ROUND_RIVER;
			RiverRoundCommCards();
			ResetForNextRound();
			break;
		case TEXASS_ROUND_RIVER:
			// curRound = -1;
			SetAllUserCardList();
			SetTableCardArray();
			CreatePokerHandForAllPlayers();
			DeterminePokerHands();
			FindAtivePlayerList();
			CompareAllPokerHand(TopWinnerRank);
			// ShowDownAllPlayerCards();
			ResetForNextRound();
			break;

		}
	}

	public void SetTableCardArray() {

		allFinalTableCards = new Card[5];

		for (int i = 0; i < allFinalTableCards.length; i++) {
			allFinalTableCards[i] = tableCards[i];
		}

	}

	// List of all users private two cards
	public void SetAllUserCardList() {
		UserCardsList = new ArrayList<PlayerCards>();
		for (int i = 0; i < nPlayer; i++) {
			UserCardsList.add(userData.get(i));
		}
	}

	// create poker hand object for all n Players
	public void CreatePokerHandForAllPlayers() {
		AllPokerHandsList = new ArrayList<PlayerHand>();
		for (int i = 0; i < UserCardsList.size(); i++) {
			PlayerHand pH = new PlayerHand(UserCardsList.get(i));
			AllPokerHandsList.add(pH);
		}
	}

	// Determining the ranks(categoury) of all Pokerhands

	public void DeterminePokerHands() {
		AllPlayersRankArray = new ArrayList<HAND_RANK>();
		for (int i = 0; i < nPlayer; i++) {
			AllPlayersRankArray.add(AllPokerHandsList.get(i).determineHandRank(
					allFinalTableCards));
			System.out.println("This is ith poker hand..."
					+ AllPlayersRankArray.get(i).toString());
		}
	}

	// create list of all active players at last round(River round)

	public void FindAtivePlayerList() {
		ActivePlayerList = new ArrayList<Integer>();

		for (int i = 0; i < nPlayer; i++) {
			if (!playerManager.getPlayer(i).isFolded()) {
				ActivePlayerList.add(i);
			}
		}
	}

	// Compare all poker hand, Find all top winners , Find winner among all top
	// winners by comparing cads, Declare and distribute money to each winner
	public void CompareAllPokerHand(int TopRank) {

		FindTopWinnerList(SortWinner(ActivePlayerList), TopRank);
		CompareWinners();
		DeclareMoneyToFinalWinners();
		ShowWinner();
	}

	// Sorts the winners form top to bottem according to their poker hands
	public ArrayList<Integer> SortWinner(ArrayList<Integer> allRank) {
		Integer[] allRanks = new Integer[allRank.size()];
		for (int k = 0; k < allRank.size(); k++) {
			allRanks[k] = allRank.get(k);
		}
		for (int i = 1; i <= allRanks.length; i++) {
			for (int j = 0; j < allRanks.length - i; j++) {
				if (AllPlayersRankArray.get(allRanks[j + 1]).ordinal() < AllPlayersRankArray
						.get(allRanks[j]).ordinal()) {
					int cTemp = allRanks[j];
					allRanks[j] = allRanks[j + 1];
					allRanks[j + 1] = cTemp;
				}
			}
		}
		allRank.clear();

		for (Integer i : allRanks) {
			allRank.add(i);
		}
		return allRank;
	}

	// Find all top winners
	public void FindTopWinnerList(ArrayList<Integer> allRanks, int TopRank) {

		WinnerList = new ArrayList<Integer>();
		WinnerList.clear();
		for (int i = TopRank; i < allRanks.size(); i++) {
			if (AllPlayersRankArray.get(allRanks.get(i)).ordinal() == AllPlayersRankArray
					.get(allRanks.get(TopRank)).ordinal()) {
				WinnerList.add(allRanks.get(i));
				System.out.println("these are winner " + i + " = "
						+ allRanks.get(i));
			}
		}
	}

	public void CompareWinners() {
		ArrayList<ArrayList<Card>> ListOfCardArray = new ArrayList<ArrayList<Card>>();
		ListOfCardArray.clear();
		for (int i = 0; i < WinnerList.size(); i++) {
			ListOfCardArray.add(AllPokerHandsList.get(WinnerList.get(i))
					.getPokerHandArray());
		}

		for (int j = 0, k = 0; j < 5; j++, k = 0) {

			while (k < ListOfCardArray.size() - 1) {
				if (ListOfCardArray.get(k).get(j).getValue() > ListOfCardArray
						.get(k + 1).get(j).getValue()) {
					ListOfCardArray.remove(k + 1);
					WinnerList.remove(k + 1);
					k = 0;
					continue;
				} else if (ListOfCardArray.get(k).get(j).getValue() < ListOfCardArray
						.get(k + 1).get(j).getValue()) {
					ListOfCardArray.remove(k);
					WinnerList.remove(k);
					k = 0;
					continue;
				} else {

				}
				k++;
			}
		}
		// ShowWinner();
	}

	public ArrayList<Integer> getWinnerList() {
		return WinnerList;
	}

	public ArrayList<String> getWinnerCards() {
		return AllPokerHandsList.get(WinnerList.get(0))
				.getWinnerHandCardNameList();
	}

	public void ShowWinner() {
		for (int i = 0; i < WinnerList.size(); i++) {
			System.out.println("these are the winner " + (i + 1) + " = "
					+ WinnerList.get(i));
		}

		for (int i = 0; i < nPlayer; i++) {
			System.out.println("total bal of " + (i + 1) + " = "
					+ playerManager.getPlayer(i).getTotalBalance());
		}
	}

	public String getWinner() {

		return playerManager.getPlayer(WinnerList.get(0)).getPlayerName();
	}

	// Declare and distribute money to each winner
	public void DeclareMoneyToFinalWinners() {
		for (int i = 0; i < WinnerList.size(); i++) {
			if (playerManager.getPlayer(WinnerList.get(i)).isAllIn()
					&& TotalTableAmt > 0) {

				curPlayerBalance = playerManager.getPlayer(WinnerList.get(i))
						.getTotalBalance();
				curPlayerBalance = curPlayerBalance
						+ allInAmountArray[WinnerList.get(i)];
				TotalTableAmt = TotalTableAmt
						- allInAmountArray[WinnerList.get(i)];
				// playerManager.updatePlayerBalance(
				// playerManager.getPlayer(WinnerList.get(i))
				// .getPlayeName(), WinnerList.get(i),
				// curPlayerBalance);
				playerManager.getPlayer(WinnerList.get(i)).setTotalBalance(
						curPlayerBalance);

			} else {
				curPlayerBalance = playerManager.getPlayer(WinnerList.get(i))
						.getTotalBalance();
				curPlayerBalance = curPlayerBalance + TotalTableAmt;
				TotalTableAmt = 0;
				// playerManager.updatePlayerBalance(
				// playerManager.getPlayer(WinnerList.get(i))
				// .getPlayeName(), WinnerList.get(i),
				// curPlayerBalance);
				playerManager.getPlayer(WinnerList.get(i)).setTotalBalance(
						curPlayerBalance);
			}

		}
		if (TotalTableAmt > 0) {
			CompareAllPokerHand(++TopWinnerRank); // Repeat the comparision with
													// second top player if
													// amount remains on table
		}
	}

	// Reset All variables for next Round

	public void ResetForNextRound() {

		if (curRound == -1) {
			isActiveBet = false;
			isActiveCall = false;
			isActiveCheck = false;
			isActiveFold = false;
			isActiveRaise = false;
			isActiveAllIn = false;
		} else {
			curBetAmount = 0;
			lastTurnAmt = 0;
			curPlayerId = 0;
			curTurnNo = 1;

			if (playerManager.getPlayer(curPlayerId).isFolded()) {
				curPlayerId++;
			}

			for (int i = 0; i < betAmtIntArray.length; i++) {
				betAmtIntArray[i] = 0;
			}

			for (int i = 0; i < betText.length; i++) {
				betText[i] = 0;
			}
			isActiveBet = true;
			isActiveCall = false;
			isActiveCheck = true;

			roundComplete = false;
		}

	}

	// Do all caculations to change the player
	public void ChangePlayerTurn() {

		for (int i = 0; i < betText.length; i++) {
			if (betText[i] != 0) {
				betAmtIntArray[i] = betText[i];
//				playerManager.getPlayer(i).setPlayerBetAmount(betText[i]);
			}
		}
		callAmt = 0;
		raiseAmt = "";
		curPlayerId++;
		curTurnNo++;
		if (curPlayerId > nPlayer - 1) {
			curPlayerId = 0;
		}

		// to check if curplayer is Active or not
		while (playerManager.getPlayer(curPlayerId).isFolded()) {
			boolean checkAnyActive = false;
			curPlayerId++;
			if (curPlayerId > nPlayer - 1) {
				curPlayerId = 0;
			}
			for (int i = 0; i < nPlayer; i++) // To get out from the infinite
			// loop if all players are
			// inactive
			{
				if (!playerManager.getPlayer(i).isFolded()) {
					checkAnyActive = true;
					break;
				}
			}
			if (!checkAnyActive) {
				break;
			}

		}
		NextTurnPreSet();

	}

	public void DoCallAmtCalculations() {
		if (betText[curPlayerId] != 0) {
			curBetAmount = betText[curPlayerId];
		} else {
			curBetAmount = 0;
		}
		// curPlayerBalance = Integer.valueOf(playerBalanceText[curPlayerId]);
		curPlayerBalance = playerManager.getPlayer(curPlayerId)
				.getTotalBalance();

		if (playerManager.getPlayer(curPlayerId).isSmallBlind()) {
			betText[curPlayerId] = sbBet;
			lastTurnAmt = sbBet;
			callAmt = sbBet;
			curCallAmt = sbBet;
			playerManager.getPlayer(curPlayerId).setSmallBlind(false);
			playerManager.getPlayer(curPlayerId).setBigBlind(false);

		} else if (playerManager.getPlayer(curPlayerId).isBigBlind()) {
			betText[curPlayerId] = bbBet;
			lastTurnAmt = bbBet;
			callAmt = bbBet;
			curCallAmt = bbBet;
			playerManager.getPlayer(curPlayerId).setBigBlind(false);
			playerManager.getPlayer(curPlayerId).setSmallBlind(false);
		} else {
			// IF Player has option to Call
			if (curBetAmount < lastTurnAmt) {
				curCallAmt = lastTurnAmt - curBetAmount;
				if (curCallAmt <= curPlayerBalance) {

					callAmt = curCallAmt;

				} else {
					callAmt = curPlayerBalance;
					isActiveCall = false;
					isActiveAllIn = true;
				}
			} else {

				// IF Player has option to bet(EX: starting of new round)
				// TODO: It's come from unity ( Bet text amount )
				// curCallAmt = int.Parse(callAmt.text.ToString());
				// TODO: For testing
				// curCallAmt = Integer.valueOf(callAmt);

				if (curCallAmt <= curPlayerBalance) {
					callAmt = curCallAmt;
				} else {
					callAmt = curPlayerBalance;
					isActiveCall = false;
					isActiveBet = false;
					isActiveAllIn = true;
				}

			}
		}

	}

	public void DoRaiseAmtCalculations() {
		if (betText[curPlayerId] != 0) {
			curBetAmount = betText[curPlayerId];

		} else {
			curBetAmount = 0;

		}
		curPlayerBalance = playerManager.getPlayer(curPlayerId)
				.getTotalBalance();
		// int curRaiseAmt = Integer.valueOf(raiseAmt);

		if (curBetAmount < lastTurnAmt) {
			int tempBetAmt = curBetAmount + curRaiseAmt;
			if (tempBetAmt >= lastTurnAmt && curRaiseAmt <= curPlayerBalance) {
				lastTurnAmt = tempBetAmt;
				curPlayerBalance = curPlayerBalance - curRaiseAmt;
				betText[curPlayerId] = tempBetAmt;
				if (curPlayerBalance == 0) {
					playerManager.getPlayer(curPlayerId).setPlayerFolded(false);
					playerManager.getPlayer(curPlayerId).setPlayerAllIn(true);
				}
				playerManager.getPlayer(curPlayerId).setTotalBalance(
						curPlayerBalance);
				ChangePlayerTurn();
			} else {
				System.out.println("amount is not sufficient");
			}
		}

	}

	// Do pre calculations for the next turn
	public void NextTurnPreSet() {

		int localCallAmt = 0;
		int localBetAmt = 0;
		int localPlayerBalance = 0;
		if (betText[curPlayerId] != 0) {
			localBetAmt = Integer.valueOf(betText[curPlayerId]);
		} else {
			localBetAmt = 0;
		}

		localPlayerBalance = playerManager.getPlayer(curPlayerId)
				.getTotalBalance();
		if (playerManager.getPlayer(curPlayerId).isSmallBlind()) {
			callAmt = sbBet;

		} else if (playerManager.getPlayer(curPlayerId).isBigBlind()) {
			callAmt = bbBet;

		} else {
			// IF Player has option to Call
			if (localBetAmt < lastTurnAmt) {
				localCallAmt = lastTurnAmt - localBetAmt;
				if (localCallAmt <= localPlayerBalance) {
					callAmt = localCallAmt;

				} else {
					callAmt = localPlayerBalance;

					isActiveCall = false;
					isActiveAllIn = true;
				}
			} else {

				// IF Player has option to bet(EX: starting of new round)
				if (callAmt != 0) {
					localCallAmt = callAmt;

					if (localCallAmt <= localPlayerBalance) {

						callAmt = localCallAmt;

					} else {
						callAmt = 0;
						isActiveCall = false;

						isActiveAllIn = true;
					}
				}
			}
		}

	}

	public void OnCallButtonPress() {

		if (isActiveCall) {
			DoCallAmtCalculations();
			if (isActiveCall) {
				curPlayerBalance = curPlayerBalance - curCallAmt;
				curBetAmount = curCallAmt + curBetAmount;
				lastTurnAmt = curBetAmount;
				betText[curPlayerId] = curBetAmount;
				if (curPlayerBalance == 0) {
					playerManager.getPlayer(curPlayerId).setPlayerFolded(false);
					playerManager.getPlayer(curPlayerId).setPlayerAllIn(true);
				}
				playerManager.getPlayer(curPlayerId).setTotalBalance(
						curPlayerBalance);
				ChangePlayerTurn();
			}
		}
	}

	public void OnBetBtnPressed() {

		// if (callAmt != "") {
		// curPlayerBalance = Integer.valueOf(playerBalanceText [curPlayerId]);
		curPlayerBalance = playerManager.getPlayer(curPlayerId)
				.getTotalBalance();
		// curCallAmt = Integer.valueOf(callAmt);
		if (curCallAmt > curPlayerBalance) {
			return;
		}

		if (isActiveBet) {
			DoCallAmtCalculations();
			if (isActiveBet) {
				curPlayerBalance = curPlayerBalance - curCallAmt;
				curBetAmount = curCallAmt + curBetAmount;
				lastTurnAmt = curBetAmount;

				betText[curPlayerId] = curBetAmount;

				if (curPlayerBalance == 0) {
					playerManager.getPlayer(curPlayerId).setPlayerFolded(false);
					playerManager.getPlayer(curPlayerId).setPlayerAllIn(true);
				}

				playerManager.getPlayer(curPlayerId).setTotalBalance(
						curPlayerBalance);

				ChangePlayerTurn();
				// BetBtn.gameObject.SetActive (false);
				// CallBtn.gameObject.SetActive (true);
				isActiveCall = true;
				isActiveBet = false;
				isActiveCheck = false;
				// Debug.Log ("this is curPlayer...." + curPlayerId);
			}
		}
		// }
	}

	public void OnRaiseButtonPress() {

		if (curRaiseAmt != 0) {
			DoRaiseAmtCalculations();
		} else {
			System.out.println("Amoount is not sufficient");
		}
	}

	public void OnFoldButtonPress() {
		playerManager.getPlayer(curPlayerId).setPlayerFolded(false);
		playerManager.getPlayer(curPlayerId).setPlayerFolded(true);
		ChangePlayerTurn();
	}

	public void OnCheckButtonPress() {
		if (lastTurnAmt == 0 && isActiveCheck) {
			turnExecuted = true;
			ChangePlayerTurn();
		}
	}

	public void OnAllInButtonPress() {

		if (isActiveAllIn) {
			if (betText[curPlayerId] != 0) {
				curBetAmount = Integer.valueOf(betText[curPlayerId]);

			} else {
				curBetAmount = 0;

			}
			curPlayerBalance = playerManager.getPlayer(curPlayerId)
					.getTotalBalance();
			curCallAmt = curPlayerBalance;
			curPlayerBalance = 0;
			curBetAmount = curCallAmt + curBetAmount;
			betText[curPlayerId] = curBetAmount;
			playerManager.getPlayer(curPlayerId).setTotalBalance(
					curPlayerBalance);
			playerManager.getPlayer(curPlayerId).setPlayerFolded(false);
			playerManager.getPlayer(curPlayerId).setPlayerAllIn(true);

			ChangePlayerTurn();
			isActiveCall = true;
			isActiveAllIn = false;

		}

	}

	// Calculate Pot amount for all (ALL IN ) members
	public void CountAmountForAllInMember() {

		for (int i = 0; i < nPlayer; i++) {
			boolean b = playerManager.getPlayer(i).isAllIn();
			if (b && allInAmountArray[i] == 0) {
				int allInBetAmt = betAmtIntArray[i];
				for (int j = 0; j < nPlayer; j++) {
					if (allInBetAmt < betAmtIntArray[j]) {
						allInAmountArray[i] = allInAmountArray[i] + allInBetAmt;

					} else {
						allInAmountArray[i] = allInAmountArray[i]
								+ betAmtIntArray[j];
					}
				}

				allInAmountArray[i] = allInAmountArray[i] + lastRoundTableAmt;
				System.out.println("this is Allin " + i + " = "
						+ allInAmountArray[i]);
			}

		}
		lastRoundTableAmt = TotalTableAmt;

	}

	public void OnRoundCompeleted() {

		for (int i = 0; i < nPlayer; i++) {

			if (betText[i] != 0) {
				int temp = betText[i];
				System.out
						.println("total amount is of " + curRound + "" + temp);
				TotalTableAmt = TotalTableAmt + temp;
			}
		}
		TotalTableText = TotalTableAmt + "";
		System.out.println("total amount is of " + curRound + ""
				+ TotalTableText);

		roundComplete = true;
	}

	// Open cards for PreFlop round

	public String getTotalTableAmount() {
		return this.TotalTableText;
	}

	public void FlopRoundCommCards() // Display of Community Cards
	{
	}

	// Open cards for Turn Round Cards
	public void TurnRoundCommCards() // Display of Community Cards
	{
	}

	public void RiverRoundCommCards() // Display of Community Cards
	{
	}
}
