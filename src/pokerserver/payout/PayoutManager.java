package pokerserver.payout;

public class PayoutManager {

	/**
	 *  1 to 9 PLAYERS: Total up to 9 Players in Tournament
	 */
	float[] PLAN1 = {50f,30f,20f};
	/**
	 * 10 to 32 PLAYERS: Total from 10 player to 32 Players in Tournament
	 */
	float[] PLAN2 = {45f,25f,18f,12f};
	/**
	 * 33 to 56 PLAYERS
	 */
	float[] PLAN3 = {39f,24f,16f,12f,9f};
	/**
	 * 57 to 96 PLAYERS
	 */
	float[] PLAN4 = {31.00f,20.00f,11.50f,9.50f,8.50f,5.75f,4.75f,3.75f,2.75f,2.50f};
	/**
	 * 97 to 192 PLAYERS
	 */
	float[] PLAN5 = {27.00f,17.00f,11.00f,8.00f,7.50f,6.00f,4.75f,3.25f,2.25f,1.75f,1.30f,1.00f};
	/**
	 * 193 to 296 PLAYERS
	 */
	float[] PLAN6 = {26.50f,16.00f,11.00f,8.50f,7.00f,6.25f,5.00f,3.50f,2.00f,1.50f,1.00f,0.75f,0.40f};
	/**
	 * 297 to 392 PLAYERS
	 */
	float[] PLAN7 = {25.00f,15.50f,10.00f,8.50f,7.50f,5.50f,4.00f,3.50f,1.75f,1.25f,0.95f,0.75f,0.50f,0.40f};
	/**
	 * 393 to 496 PLAYERS
	 */
	float[] PLAN8 = {24.50f,15.50f,9.50f,8.00f,6.25f,5.50f,4.50f,3.25f,1.75f,1.25f,0.95f,0.75f,0.50f,0.35f,0.30f};
	/**
	 * 497 to 592 PLAYERS
	 */
	float[] PLAN9 = {24.50f,14.50f,9.50f,7.50f,6.25f,5.25f,3.75f,2.75f,1.75f,1.25f,1.05f,0.85f,0.50f,0.35f,0.30f,0.20f};
	/**
	 * 593 to 792 PLAYERS
	 */
	float[] PLAN10 = {24.50f,14.50f,9.50f,6.75f,5.75f,4.50f,3.50f,2.25f,2.00f,1.75f,1.05f,0.75f,0.50f,0.35f,0.30f,0.25f,0.20f};
	/**
	 * 793 to 992 PLAYERS
	 */
	float[] PLAN11 = {24.50f,12.50f,8.50f,6.25f,5.25f,4.25f,3.25f,2.25f,1.75f,1.25f,0.90f,0.75f,0.45f,0.35f,0.30f,0.25f,0.20f,0.15f};
	/**
	 * 993 +1999 PLAYERS
	 */
	float[] PLAN12 = {24.50f,12.50f,9.00f,6.25f,5.25f,4.25f,3.25f,2.25f,1.50f,1.25f,0.85f,0.65f,0.45f,0.35f,0.30f,0.25f,0.20f,0.15f,0.125f};
	
	float[] PAYOUT_PLAN;
	
	int totalPlayer;
	public PayoutManager(int totalPlayer) {
		this.totalPlayer = totalPlayer;
		this.PAYOUT_PLAN = getPayOutPlayerIndex(totalPlayer);
	}
	
	/**
	 * It will return winning percentage of player on the base of winning rank index and total player who was register in tournament
	 * 
	 * @param rankIndex
	 * @param totalPlayer
	 * @return
	 */
	public float getWinnigPercentage(int rankIndex){
		if (PAYOUT_PLAN != null && PAYOUT_PLAN.length>=rankIndex) {
			float winningPercentage = PAYOUT_PLAN[getPayOutRankIndex(rankIndex)];
			return winningPercentage;
		}
		return 0f;
	}
	
	public float[] getPrizeMoneyPlan(int totalPlayer){
		
		switch (totalPlayer) {
		case 1:
			return PLAN1;
		case 2:
			return PLAN2;
		case 3:
			return PLAN3;
		case 4:
			return PLAN4;
		case 5:
			return PLAN5;
		case 6:
			return PLAN6;
		case 7:
			return PLAN7;
		case 8:
			return PLAN8;
		case 9:
			return PLAN9;
		case 10:
			return PLAN10;
		case 11:
			return PLAN11;
		case 12:
			return PLAN12;
		default:
			return null;
		}
	}
	/**
	 * Prize money plan is based on total number of register player in tournament
	 * @param totalPlayer
	 * @return
	 */
	public float[] getPayOutPlayerIndex(int totalPlr){
		
		if(isBetween(totalPlr, 1, 9)){
			System.out.println("Payout Plan : 1");
			return PLAN1;
		}
		if(isBetween(totalPlr, 10, 32)){
			System.out.println("Payout Plan : 2");
			return PLAN2;
		}
		if(isBetween(totalPlr, 33, 56)){
			System.out.println("Payout Plan : 3");
			return PLAN3;
		}
		if(isBetween(totalPlr, 57, 96)){
			System.out.println("Payout Plan : 4");
			return PLAN4;
		}
		if(isBetween(totalPlr, 97, 192)){
			System.out.println("Payout Plan : 5");
			return PLAN5;
		}
		if(isBetween(totalPlr, 193, 296)){
			System.out.println("Payout Plan : 6");
			return PLAN6;
		}
		if(isBetween(totalPlr, 297, 392)){
			System.out.println("Payout Plan : 7");
			return PLAN7;
		}
		if(isBetween(totalPlr, 393, 496)){
			System.out.println("Payout Plan : 8");
			return PLAN8;
		}
		if(isBetween(totalPlr, 497, 592)){
			System.out.println("Payout Plan : 9");
			return PLAN9;
		}
		if(isBetween(totalPlr, 593, 792)){
			System.out.println("Payout Plan : 10");
			return PLAN10;
		}
		if(isBetween(totalPlr, 793, 992)){
			System.out.println("Payout Plan : 11");
			return PLAN11;
		}
		if(isBetween(totalPlr, 993, 1999)){
			System.out.println("Payout Plan : 12");
			return PLAN12;
		}
		return null;
	}
	public int getPayOutRankIndex(int rank){
		switch (rank) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return rank;
		}
		if(isBetween(rank, 11, 15))
			return 11;
		if(isBetween(rank, 16, 20))
			return 12;
		if(isBetween(rank, 21, 30))
			return 13;
		if(isBetween(rank, 31, 40))
			return 14;
		if(isBetween(rank, 41, 50))
			return 15;
		if(isBetween(rank, 51, 60))
			return 16;
		if(isBetween(rank, 61, 70))
			return 17;
		if(isBetween(rank, 71, 100))
			return 18;
		if(isBetween(rank, 101, 120))
			return 19;
		return 0;
	}
	public  boolean isBetween(int x, int lower, int upper) {
		  return lower <= x && x <= upper;
		}
}
