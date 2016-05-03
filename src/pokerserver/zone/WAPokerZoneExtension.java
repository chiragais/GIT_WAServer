package pokerserver.zone;


import pokerserver.room.WAPokerRoomAdapter;

import com.shephertz.app42.server.idomain.BaseZoneAdaptor;
import com.shephertz.app42.server.idomain.HandlingResult;
import com.shephertz.app42.server.idomain.IRoom;
import com.shephertz.app42.server.idomain.ITurnBasedRoom;
import com.shephertz.app42.server.idomain.IUser;
import com.shephertz.app42.server.idomain.IZone;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Chirag
 */
public class WAPokerZoneExtension extends BaseZoneAdaptor {

	private IZone izone;

	public WAPokerZoneExtension(IZone izone) {
		System.out.println();
    	System.out.print("Zone : "+izone.getName());
		this.izone = izone;
	}

	/*
	 * This function invoked when server receive create room request. we set
	 * adapter to room by checking maxUsers in room.
	 */

	@Override
	public void handleCreateRoomRequest(IUser user, IRoom room,
			HandlingResult result) {
		System.out.println("... "+result.toString());
		System.out.print("CD >> Room Name : " + room.getName()
				+ " >> Max User : " + room.getMaxUsers() + " >> User Name : "
				+ user.getName());
		room.setAdaptor(new WAPokerRoomAdapter(izone, (ITurnBasedRoom) room));
	}

	/*
	 * This function invoked when the given user loses its connection due to an
	 * intermittent connection failure. (Using Connection Resiliency feature)
	 */

	@Override
	public void onUserPaused(IUser user) {
		if (user.getLocation() == null) {
			return;
		}
		WAPokerRoomAdapter extension = (WAPokerRoomAdapter) user.getLocation()
				.getAdaptor();
		extension.onUserPaused(user);
	}

	/*
	 * This function invoked when the given user recovers its connection from an
	 * intermittent connection failure. (Using Connection Resiliency feature)
	 */
	@Override
	public void handleResumeUserRequest(IUser user, String authData,
			HandlingResult result) {
		if (user.getLocation() == null) {
			return;
		}
		WAPokerRoomAdapter extension = (WAPokerRoomAdapter) user.getLocation()
				.getAdaptor();
		extension.onUserResume(user);
	}

}
