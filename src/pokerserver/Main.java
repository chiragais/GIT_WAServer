/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerserver;

import pokerserver.utils.GameConstants;

import com.shephertz.app42.server.AppWarpServer;

/**
 * @author Chirag
 */

public class Main implements GameConstants {
	
	public static void main(String[] args) throws Exception {
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
}
