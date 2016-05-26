/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import pokerserver.utils.GameConstants;
import pokerserver.utils.LogUtils;

import com.shephertz.app42.server.AppWarpServer;

/**
 * @author Chirag
 */

public class Main implements GameConstants {

	public static void main(String[] args) throws Exception {
		String appconfigPath = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "AppConfig.json";
		LogUtils.Log("AppConfig : " + appconfigPath);
		boolean started = AppWarpServer.start(new PokerServerExtension(),
				appconfigPath);
		if (!started) {

			LogUtils.Log("Main : AppWarpServer did not start. See logs for details. ");
			throw new Exception(
					"AppWarpServer did not start. See logs for details.");
		} else {
			LogUtils.Log("Main : Server Started ");
		}
		changeGameStatusOnServer(278);
	}
	//278
	public static void changeGameStatusOnServer(int gameID){
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://pokernew.aistechnolabs.us/admin/user/endgame");
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("game_id", String.valueOf(gameID)));
		params.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));
		
		try {
		    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		    // writing error to Log
		    e.printStackTrace();
		}
		/*
		 * Execute the HTTP Request
		 */
		try {
		    HttpResponse response = httpClient.execute(httpPost);
		    HttpEntity respEntity = response.getEntity();

		    if (respEntity != null) {
		        // EntityUtils to get the response content
		        String content =  EntityUtils.toString(respEntity);
		        System.out.println("Response : "+ content);
		    }
		} catch (ClientProtocolException e) {
		    // writing exception to log
		    e.printStackTrace();
		} catch (IOException e) {
		    // writing exception to log
		    e.printStackTrace();
		}
	} 
}
