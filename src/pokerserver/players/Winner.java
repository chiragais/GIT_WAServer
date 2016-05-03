package pokerserver.players;

public class Winner {
  private	PlayerBean player;
  private	int winningAmount=0;
	 
  public Winner(PlayerBean player,int winningAmount){
		   this.player=player;
	       this.winningAmount=winningAmount;
	   }

  
    public PlayerBean getPlayer(){
    	return player;
    }
    
    public void setWinningAmount(int winningAmount) {
		this.winningAmount = winningAmount;
	}
	
	public int getWinningAmount() {
		return this.winningAmount;
	}
  
   
  
}
