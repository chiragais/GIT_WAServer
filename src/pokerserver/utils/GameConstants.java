/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerserver.utils;

/**
 * @author Chirag
 */
public interface GameConstants {

	public enum SUITS {
		heart, spade, diamond, club
	}

	public enum RANKS {
		two, three, four, five, six, seven, eight, nine, ten, jack, queen, king, ace
	}
	public enum HAND_RANK {
		ROYAL_FLUSH, STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE, FLUSH, STRAIGHT, THREE_OF_A_KIND, TWO_PAIR, PAIR, HIGH_CARD,FOLDED
	}

	public String SUIT_HEART = "heart";
	public String SUIT_SPADE = "spade";
	public String SUIT_DIAMOND = "diamond";
	public String SUIT_CLUB = "club";

	public static int maxPlayers = 6;
	public int TEXASS_PLAYER_CARD_LIMIT_FOR_HAND = 0;
	public int WA_PLAYER_CARD_LIMIT_FOR_HAND = 2;
	
	public int MIN_PLAYER_TO_START_GAME = 3;

	// All Texass Rounds constants
	public int TEXASS_ROUND_PREFLOP = 0;
	public int TEXASS_ROUND_FLOP = 1;
	public int TEXASS_ROUND_TURN = 2;
	public int TEXASS_ROUND_RIVER = 3;
	
	// All WhoopAss round 
	public int WA_ROUND_START= 1;
	public int WA_ROUND_FIRST_FLOP =2;
	public int WA_ROUND_SECOND_FLOP = 3;
	public int WA_ROUND_WHOOPASS =4;
	public int WA_ROUND_THIRD_FLOP = 5;

	public String RANK_TWO = "two";
	public String RANK_THREE = "three";
	public String RANK_FOUR = "four";
	public String RANK_FIVE = "five";
	public String RANK_SIX = "six";
	public String RANK_SEVEN = "seven";
	public String RANK_EIGHT = "eight";
	public String RANK_NINE = "nine";
	public String RANK_TEN = "ten";
	public String RANK_JACK = "jack";
	public String RANK_QUEEN = "queen";
	public String RANK_KING = "king";
	public String RANK_ACE = "ace";

	// For texass game
	public int INDEX_FLOP_1 = 0;
	public int INDEX_FLOP_2 = 1;
	public int INDEX_FLOP_3 = 2;
	public int INDEX_TURN = 3;
	public int INDEX_RIVER = 4;

	// For WA game
	public int INDEX_FIRST_FLOP_1 = 0;
	public int INDEX_FIRST_FLOP_2 = 1;
	public int INDEX_SECOND_FLOP_1 = 2;
	public int INDEX_SECOND_FLOP_2 = 3;
	public int INDEX_THIRD_FLOP_1 = 4;
	public int INDEX_THIRD_FLOP_2 = 5;
	
	
	String TEXASS_SERVER_NAME = "TexassAppWarpS2";
	String WA_SERVER_NAME = "WAAppWarpS2";

	public static final byte MAX_CARD = 52;
	// Message Constants

	public static final byte PLAYER_HAND = 1;

	public static final byte RESULT_GAME_OVER = 3;

	public static final byte RESULT_USER_LEFT = 4;

	// error code
	public static final int SUBMIT_CARD = 111;
	public static final int INVALID_MOVE = 121;

	// GAME_STATUS

	public int STOPPED = 71;
	public int RUNNING = 72;
	public int PAUSED = 73;
	public int RESUMED = 74;
	public int FINISHED = 75;
	public int CARD_DISTRIBUTE = 76;
	public int RESTART = 77;

	// Player actions
	/** Betting the same amount as BB */
	public int ACTION_CALL = 1;
	/** Throw your cards away */
	public int ACTION_FOLD = 2;
	/** Betting more amount as BB */
	public int ACTION_RAISE = 3;
	/** Putting some money on middle */
	public int ACTION_BET = 4;
	/** Putting all money on middle */
	public int ACTION_ALL_IN = 5;
	/** Betting zero and/or to calling the current bet of zero */
	public int ACTION_CHECK = 6;
	/** If player time is out for select action */
	public int ACTION_TIMEOUT = 7;
	
	public int ACTION_DEALER = 8;
	public int ACTION_FOLDED = 9;
	public int ACTION_PENDING= 10;
	public int ACTION_WA_UP = 11;
	public int ACTION_WA_DOWN = 12;
	public int ACTION_WA_NO = 13;
	public int ACTION_NO_TURN = 14;

	// Round status
	int ROUND_STATUS_ACTIVE = 1;
	int ROUND_STATUS_PENDING = 2;
	int ROUND_STATUS_FINISH = 3;

	// Common for server and client
	String RESPONSE_DATA_SEPRATOR = "#";
	String RESPONSE_FOR_DEFAULT_CARDS = 1 + RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_PLAYERS_INFO = 2 + RESPONSE_DATA_SEPRATOR;
	String REQUEST_FOR_ACTION = 3 + RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_ACTION_DONE = 4 + RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_ROUND_COMPLETE = 5 + RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_GAME_START = 6+RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_GAME_COMPLETE = 7 + RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_BLIEND_PLAYER = 8 + RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_DESTRIBUTE_CARD = 9 + RESPONSE_DATA_SEPRATOR;
	String RESPONSE_FOR_WINNIER_INFO = 10 + RESPONSE_DATA_SEPRATOR;
	String REQUEST_FOR_RESTART_GAME = 11 + RESPONSE_DATA_SEPRATOR;
	String REQUEST_FOR_WA_POT_WINNER = 12 + RESPONSE_DATA_SEPRATOR;

	String TAG_ACTION = "Action";
	String TAG_BET_AMOUNT = "Bet_Amount";
	String TAG_TABLE_AMOUNT = "Total_Table_Amount";
	String TAG_ROUND = "Round";
	String TAG_PLAYER_NAME = "Player_Name";
	String TAG_PLAYER = "Player";
	// Texass Game
	String TAG_CARD_FLOP_1 = "Flop1";
	String TAG_CARD_FLOP_2 = "Flop2";
	String TAG_CARD_FLOP_3 = "Flop3";
	String TAG_CARD_TURN = "Turn";
	String TAG_CARD_RIVER = "River";
	// WA Game
	String TAG_CARD_FIRST_FLOP_1 = "FirstFlop1";
	String TAG_CARD_FIRST_FLOP_2 = "FirstFlop2";
	String TAG_CARD_SECOND_FLOP_1 = "SecondFlop1";
	String TAG_CARD_SECOND_FLOP_2 = "SecondFlop2";
	String TAG_CARD_THIRD_FLOP_1 = "ThirdFlop1";
	String TAG_CARD_THIRD_FLOP_2 = "ThirdFlop2";
	
	String TAG_CARD_PLAYER_1 = "Card1";
	String TAG_CARD_PLAYER_2 = "Card2";
	String TAG_CARD_WA = "WACard";
	String TAG_PLAYER_BALANCE = "Player_Balance";
	String TAG_PLAYER_SMALL_BLIND = "Small_Blind";
	String TAG_PLAYER_BIG_BLIND = "Big_Blind";
	String TAG_PLAYER_ACTIVE = "Player_Active";
	String TAG_PLAYER_DEALER = "Player_Dealer";

	String TAG_SMALL_BLIEND_AMOUNT = "SBAmount";
	String TAG_WINNER = "winner";
	String TAG_WINNER_NAME = "Winner_Name";
	String TAG_WINNER_RANK = "Winner_Rank";
	String TAG_WINNER_BEST_CARDS = "Winner_Best_Cards";
	String TAG_WINNERS_WINNING_AMOUNT = "Winning_Amount";
	
	String TAG_WINNER_TOTAL_BALENCE = "winner_balance";

	int SBAmount = 10;
	
	String WA_APP_KEY_LOCAL = "4318ddad-038a-409d-8";
	String TEXASS_APP_KEY_LOCAL = "3689654b-d64f-421e-8";
	String WA_APP_KEY = "970f84e7-bb66-4499-b";
	String TEXASS_APP_KEY = "6be0f405-c805-45c2-9";
}
