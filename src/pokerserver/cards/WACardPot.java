package pokerserver.cards;

import java.util.ArrayList;
import java.util.List;

import pokerserver.players.PlayerBean;

public class WACardPot {
	
	int potAmt =0;
	List<PlayerBean> listPlayers = new ArrayList<PlayerBean>();
	PlayerBean winnerPlayer ;
	
	public WACardPot(int potAmt){
		this.potAmt =  potAmt;
	}
	
	public int getPotAmt (){
		return potAmt;
	}
	
	public void addPlayer(PlayerBean playerBean){
		this.listPlayers.add(playerBean);
	}
	
	public List<PlayerBean> getPlayers(){
		return listPlayers;
	}
	
	public void setWinnerPlayer(PlayerBean playerBean){
		this.winnerPlayer = playerBean;
	}
	public PlayerBean getWinnerPlayer(){
		return winnerPlayer;
	}
}

