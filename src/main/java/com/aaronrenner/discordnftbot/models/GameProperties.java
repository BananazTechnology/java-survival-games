package com.aaronrenner.discordnftbot.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.aaronrenner.discordnftbot.game.GameManager;
import com.aaronrenner.discordnftbot.services.OutboundScheduler;
import lombok.Data;

@Component
@Data
public class GameProperties {
	
	@Autowired
	OutboundScheduler outboundScheduler;
	
	@Autowired
	GameManager gameManager;
	
	// Runtime
	private DiscordProperties conf;
	
	// Easy access to K/V pairs
	private final String ADMIN_CONTACT     = "adminContact";
	private final String DISABLE_WINLOCK   = "disableWinlock";
	private final String DISPATCH_INTERVAL = "dispatchInterval"; 
	private final String SCALE             = "dropScale";
	private final String LOSER_MSGS        = "loserMessages";
	private final String WINNERS    	   = "maxWinners";
	private final String COOLDOWN  		   = "userCooldown";
	private final String WINNER_MSGS       = "winnerMessages";
	private final String ROLE    		   = "winnerRole";
	
	// Filled with data at Runtime
	private String  adminContact;
	private String  customName;
	private boolean disableWinlock;
	private int     dispatchInterval;
	private int     dropScale;
	private int     maxWinners;
	private int     userCooldown;
	private String  winnerRole;
	private List<String> winnerMessages = new ArrayList<>();
	private List<String> loserMessages = new ArrayList<>();
	private static final Logger LOGGER  = LoggerFactory.getLogger(GameProperties.class);

	@SuppressWarnings("unchecked")
	public void configureGameProperties(Map<Object, Object> gameInfo, DiscordProperties dConf, String customName) throws RuntimeException {
		// Variables needed for GameManager
		this.customName       = customName;
		this.conf             = dConf;
		/** Variables of the discord root node*/
		this.adminContact     = (String) gameInfo.get(ADMIN_CONTACT);
		this.dropScale        = (int)    gameInfo.get(SCALE);
		this.dispatchInterval = (int)    gameInfo.get(DISPATCH_INTERVAL);
		this.maxWinners       = (int)    gameInfo.get(WINNERS);
		this.userCooldown     = (int)    gameInfo.get(COOLDOWN);
		this.winnerRole       = (String) gameInfo.get(ROLE);
		
		// Configures if winners can keep playing after winning
		String getDisableWinlock = String.valueOf(gameInfo.get(DISABLE_WINLOCK));
		this.disableWinlock  = (getDisableWinlock != null) ? Boolean.valueOf(getDisableWinlock) : false;
		
		/** Map through winner messages */
		try {
			LinkedHashMap<Integer, String> winMsgsList = (LinkedHashMap<Integer, String>) gameInfo.get(WINNER_MSGS);
			for(Entry<Integer, String> newMsg : winMsgsList.entrySet()) {
				this.winnerMessages.add(newMsg.getValue());
			}
		} catch (Exception e) {
			throw new RuntimeException("Check winners messages formatting!");
		}
		
		/** Map through winner messages */
		try {
			LinkedHashMap<Integer, String> loserMsgsList = (LinkedHashMap<Integer, String>) gameInfo.get(LOSER_MSGS);
			for(Entry<Integer, String> newMsg : loserMsgsList.entrySet()) {
				this.loserMessages.add(newMsg.getValue());
			}
		} catch (Exception e) {
			throw new RuntimeException("Check losers messages formatting!");
		}
		
		LOGGER.debug(this.toString());
		this.gameManager.build(this);
	}

}
