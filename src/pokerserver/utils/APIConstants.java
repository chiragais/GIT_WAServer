package pokerserver.utils;

public class APIConstants {

	public static String BASE_URL = "http://pokernew.aistechnolabs.us/";

	public static String ACTION_END_GAME = BASE_URL + "admin/user/endgame";
	public static String ACTION_LEAVE_USER = BASE_URL + "admin/user/leaveuser";
	public static String ACTION_WIN_USER = BASE_URL + "admin/user/winuser";

	public static String TAG_USER_ID = "userid";
	public static String TAG_BALANCE = "balance";
	public static String TAG_TIMESTAMP = "timestamp";
	public static String TAG_GAME_ID = "game_id";
	public static String TAG_STATUS = "status";

}
