/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pokerserver.cards.Card;
import pokerserver.handrank.GeneralHandManager;
import pokerserver.handrank.WhoopAssHandManager;
import pokerserver.players.PlayerBean;
import pokerserver.players.PlayersManager;
import pokerserver.utils.GameConstants;

import com.shephertz.app42.server.AppWarpServer;

/**
 * @author Chirag
 */

public class Main implements GameConstants {
	static WhoopAssHandManager handManager;
	static PlayersManager playersManager ;
	static GeneralHandManager generalHandManager;
	public static void main(String[] args) throws Exception {
		generalHandManager = new GeneralHandManager(WA_PLAYER_CARD_LIMIT_FOR_HAND);
		playersManager = new PlayersManager();
		/*ArrayList<Card> listDefaultCards = new ArrayList<Card>();
		listDefaultCards.add(new Card(SUIT_SPADE, RANK_KING));
		listDefaultCards.add(new Card(SUIT_HEART, RANK_THREE));
		listDefaultCards.add(new Card(SUIT_HEART, RANK_SEVEN));
		listDefaultCards.add(new Card(SUIT_SPADE, RANK_ACE));
		listDefaultCards.add(new Card(SUIT_DIAMOND, RANK_JACK));
		listDefaultCards.add(new Card(SUIT_HEART, RANK_EIGHT));

		PlayerBean playerBean1 = new PlayerBean(1, "Player 1");
		playerBean1.setCards( new Card(SUIT_DIAMOND, RANK_THREE),  new Card(SUIT_CLUB, RANK_THREE),new Card(SUIT_DIAMOND, RANK_ACE));
		playerBean1.setWACardStatus(ACTION_WA_NO);
		playersManager.addNewPlayerInRoom(playerBean1);
		generalHandManager.generatePlayerBestRank(listDefaultCards, playerBean1);
		
		PlayerBean playerBean2 = new PlayerBean(2, "Player 2");
		playerBean2.setCards( new Card(SUIT_CLUB, RANK_FOUR),new Card(SUIT_CLUB, RANK_NINE),  new Card(SUIT_HEART, RANK_KING));
		playerBean2.setWACardStatus(ACTION_WA_DOWN);
		playersManager.addNewPlayerInRoom(playerBean2);
		generalHandManager.generatePlayerBestRank(listDefaultCards, playerBean2);
		
//		
		PlayerBean playerBean3 = new PlayerBean(2, "Player 3");
		playerBean3.setCards( new Card(SUIT_CLUB, RANK_SIX),new Card(SUIT_HEART, RANK_TEN),  new Card(SUIT_CLUB, RANK_KING));
		playerBean3.setWACardStatus(ACTION_WA_UP);
		playersManager.addNewPlayerInRoom(playerBean3);
		generalHandManager.generatePlayerBestRank(listDefaultCards, playerBean3);
		generateWinnerPlayers();
		*/
		String appconfigPath = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "AppConfig.json";
		System.out.print("AppConfig : " + appconfigPath);
		boolean started = AppWarpServer.start(new PokerServerExtension(),
				appconfigPath);
		if (!started) {
			System.out.println();
			System.out
					.print("Main : AppWarpServer did not start. See logs for details. ");
			throw new Exception(
					"AppWarpServer did not start. See logs for details.");
		} else {
			System.out.println();
			System.out.print("Main : Server Started ");
		}
	}

	public static List<PlayerBean>  generateWinnerPlayers(){
		List<PlayerBean> listWinnerPlayer = new ArrayList<PlayerBean>();
		// Sort winner players
		Collections.sort(playersManager.getAllAvailablePlayers(), new Comparator<PlayerBean>() {
			@Override
			public int compare(PlayerBean player1, PlayerBean player2) {
				return player1.getHandRank().compareTo(player2.getHandRank());
			}
		});
		for (int i =0;i<playersManager.getAllAvailablePlayers().size();i++) {
			List<PlayerBean> sameRankPlayer = new ArrayList<PlayerBean>();
			PlayerBean currentPlayer =playersManager.getAllAvailablePlayers().get(i); 
			if (!listWinnerPlayer.contains(currentPlayer)) {
				sameRankPlayer.add(currentPlayer);
				for (int j = i + 1; j < playersManager.getAllAvailablePlayers()
						.size(); j++) {
					PlayerBean nextPlayer = playersManager
							.getAllAvailablePlayers().get(j);
					if (currentPlayer.getHandRank() == nextPlayer.getHandRank()) {
						sameRankPlayer.add(nextPlayer);
					}
				}
				Collections.sort(sameRankPlayer, new Comparator<PlayerBean>() {
					@Override
					public int compare(PlayerBean player1, PlayerBean player2) {
						return getBestHandPlayer(player1,player2);
					}
				});
				listWinnerPlayer.addAll(sameRankPlayer);
			}
		}

		for(PlayerBean playerBean : listWinnerPlayer){
			System.out.println("Current Pla : "+playerBean.getPlayerName()+" >> "+playerBean.getBestHandRankTotal()+" >> "+playerBean.getHandRank());
		}
		return listWinnerPlayer;
	}

	public static int getBestHandPlayer(PlayerBean playerBean1,PlayerBean playerBean2){
		
		if(playerBean1.getHandRank().equals(playerBean2.getHandRank())){
			
			// If Pair is Full House
			if (playerBean1.getHandRank().equals(HAND_RANK.FULL_HOUSE)) {
				List<List<Card>> listOfListSameRankCardsForPlayer1 = generalHandManager.listAllSameRankCards(playerBean1.getMainHandCards());
				List<List<Card>> listOfListSameRankCardsForPlayer2 = generalHandManager.listAllSameRankCards(playerBean2.getMainHandCards());
				List<Card> listPlayer1Cards = new ArrayList<Card>();
				List<Card> listPlayer2Cards = new ArrayList<Card>();
				
				if(listOfListSameRankCardsForPlayer1.size()==2 && listOfListSameRankCardsForPlayer2.size()==2){
					if(listOfListSameRankCardsForPlayer1.get(0).size()==3){
						listPlayer1Cards.addAll(listOfListSameRankCardsForPlayer1.get(0));
						listPlayer1Cards.addAll(listOfListSameRankCardsForPlayer1.get(1));
					}else{
						listPlayer1Cards.addAll(listOfListSameRankCardsForPlayer1.get(1));
						listPlayer1Cards.addAll(listOfListSameRankCardsForPlayer1.get(0));
					}
					
					if(listOfListSameRankCardsForPlayer2.get(0).size()==3){
						listPlayer2Cards.addAll(listOfListSameRankCardsForPlayer2.get(0));
						listPlayer2Cards.addAll(listOfListSameRankCardsForPlayer2.get(1));
					}else{
						listPlayer2Cards.addAll(listOfListSameRankCardsForPlayer2.get(1));
						listPlayer2Cards.addAll(listOfListSameRankCardsForPlayer2.get(0));
					}
					for(int i =0 ;i<listPlayer1Cards.size();i++){
						if (listPlayer1Cards.get(i).getValue() == listPlayer2Cards.get(i).getValue()) {

						} else if (listPlayer1Cards.get(i).getValue() > listPlayer2Cards.get(i).getValue()) {
							return -1;
						} else if (listPlayer1Cards.get(i).getValue() < listPlayer2Cards.get(i).getValue()) {
							return 1;
						}
						
					}
				}
			} else {
				for (int i = 0; i < playerBean1.getMainHandCards().size(); i++) {
					if (playerBean1.getMainHandCards().get(i).getValue() == playerBean2
							.getMainHandCards().get(i).getValue()) {

					} else if (playerBean1.getMainHandCards().get(i).getValue() > playerBean2
							.getMainHandCards().get(i).getValue()) {
						return -1;
					} else if (playerBean1.getMainHandCards().get(i).getValue() < playerBean2
							.getMainHandCards().get(i).getValue()) {
						return 1;
					}
				}
			}
			for (int i = 0; i < playerBean1.getBestHandCards().size(); i++) {
				if (playerBean1.getBestHandCards().get(i).getValue() == playerBean2
						.getBestHandCards().get(i).getValue()) {

				} else if (playerBean1.getBestHandCards().get(i).getValue() > playerBean2
						.getBestHandCards().get(i).getValue()) {
					return -1;
				} else if (playerBean1.getBestHandCards().get(i).getValue() < playerBean2
						.getBestHandCards().get(i).getValue()) {
					return 1;
				}
			}
		}
		return 0;
	}
}
