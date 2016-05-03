package pokerserver.players;

import java.util.ArrayList;

public class PlayersManager {

	public ArrayList<PlayerBean> roomPlayersList; // all player container list
	int dealerPosition,sbPosition,bbPosition,totalActivePlayers = 0;
	
	public PlayersManager() {
		 roomPlayersList = new ArrayList<>();
	}

	public void setCurrentGameCntr(int currentGameCntr){
		
		this.dealerPosition = currentGameCntr;
	}
	public void addNewPlayerInRoom(PlayerBean player) {
		this.roomPlayersList.add(player);
	}

	public ArrayList<PlayerBean> getAllAvailablePlayers(){
		return roomPlayersList;
	}
	public ArrayList<PlayerBean> getAllAactivePlayersForWinning(){
		ArrayList<PlayerBean> roomPlayersList = new ArrayList<PlayerBean>();
		for (PlayerBean player : this.roomPlayersList) {
			if (!player.isFolded()) {
				roomPlayersList.add(player);
			}
		}
		return roomPlayersList;
	}
	public ArrayList<PlayerBean> getAllAactivePlayersForTurn(){
		ArrayList<PlayerBean> roomPlayersList = new ArrayList<PlayerBean>();
		for (PlayerBean playerBean : this.roomPlayersList) {
			if (!playerBean.isAllIn())
				if (!playerBean.isFolded()) {
					roomPlayersList.add(playerBean);
			}
		}
		return roomPlayersList;
	}
	public PlayerBean getPlayerByName(String name) {
		for (PlayerBean player : roomPlayersList) {
			if (player.getPlayerName().equals(name)) {
				return player;
			}
		}
		return null;
	}
	
	public void setAllAvailablePlayers(ArrayList<PlayerBean> playerList){
		roomPlayersList.clear();
		roomPlayersList.addAll(playerList);
		
	}
	public int getTotalRoomPlayers() {
		return roomPlayersList.size();
	}

	public PlayerBean getPlayer(int plrId) {
		return roomPlayersList.get(plrId);
	}
	
	public void removePlayerFromRoom(PlayerBean player) {
		this.roomPlayersList.remove(player);
	}
	public void removeAllPlayers(){
		this.roomPlayersList.clear();
	}
	
	public int getTotalActivePlayerCounter(){
		int cntr = 0;
		for(PlayerBean playerBean : roomPlayersList){
			if(!playerBean.isAllIn() && !playerBean.isFolded() ){
				cntr++;
			}
		}
		return cntr;
	}
	public PlayerBean getDealerPayer(){
		PlayerBean dealerPlayer = null;
		for(int i = 0 ; i<roomPlayersList.size();i++){
//			if(i==0){
			if(dealerPosition==i){
				roomPlayersList.get(i).setDealer(true);
				dealerPlayer = roomPlayersList.get(i);
			}else{
				roomPlayersList.get(i).setDealer(false);
			}
		}
		if(dealerPlayer==null){
			dealerPosition=0;
			dealerPlayer = roomPlayersList.get(dealerPosition);
			dealerPlayer.setDealer(true);
		}
		System.out.println("Dealer Position : "+dealerPosition);
		sbPosition =dealerPosition+1;
		return dealerPlayer;
	}
	public PlayerBean getSmallBlindPayer(){
		PlayerBean sbPlayer = null;
		for(int i = 0 ; i<roomPlayersList.size();i++){
//			if(i==1){
			if(sbPosition == i){
				roomPlayersList.get(i).setSmallBlind(true);
				sbPlayer = roomPlayersList.get(i);
			}else{
				roomPlayersList.get(i).setSmallBlind(false);
			}
		}
		if(sbPlayer==null){
			sbPosition = 0;
			sbPlayer = roomPlayersList.get(sbPosition);
			sbPlayer.setSmallBlind(true);
		}
		bbPosition = sbPosition+1;
		return sbPlayer;
	}
	public PlayerBean getBigBlindPayer(){
		PlayerBean bbPlayer = null;
		for(int i = 0 ; i<roomPlayersList.size();i++){
//			if(i==2){
			if(bbPosition==i){
				roomPlayersList.get(i).setBigBlind(true);
				bbPlayer = roomPlayersList.get(i);
			}else{
				roomPlayersList.get(i).setBigBlind(false);
			}
		}
		if(bbPlayer==null){
			bbPosition = 0;
			bbPlayer=roomPlayersList.get(bbPosition);
			bbPlayer.setBigBlind(true);
		}
		return bbPlayer;
	}
}
