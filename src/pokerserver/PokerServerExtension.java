/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerserver;

import pokerserver.utils.GameConstants;
import pokerserver.zone.TexassPokerZoneExtension;
import pokerserver.zone.WAPokerZoneExtension;

import com.shephertz.app42.server.idomain.BaseServerAdaptor;
import com.shephertz.app42.server.idomain.IZone;

/**
 * @author Chirag
 */
public class PokerServerExtension extends BaseServerAdaptor implements GameConstants{
    /**
     * App Name : WAPokerGameZone
AppKey : 4318ddad-038a-409d-8

App Name : PokerGameZone
AppKey : 3689654b-d64f-421e-8
     */
    @Override
    public void onZoneCreated(IZone zone)
    {             
    	System.out.println();
    	System.out.print("Poker Server Extension : "+zone.getName());
    	if(zone.getAppKey().equals(TEXASS_APP_KEY) || zone.getAppKey().equals(TEXASS_APP_KEY_LOCAL)){
    		zone.setAdaptor(new TexassPokerZoneExtension(zone));
    	}else{
    		zone.setAdaptor(new WAPokerZoneExtension(zone));
    	}
    }
}
