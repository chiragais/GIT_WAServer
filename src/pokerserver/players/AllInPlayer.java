package pokerserver.players;

public class AllInPlayer extends PlayerBean {
	
	int allInPotAmount=0;
	
	public AllInPlayer(String playerName,int allInAmount){
		
		super.setPlayerName(playerName);
		this.allInPotAmount=allInAmount;
				
	}
	public void setTotalAllInBetAmount(int allInAmount) {
		this.allInPotAmount = allInAmount;
	}
	
	public int getTotalAllInPotAmount() {
		return this.allInPotAmount;
	}
	

}
