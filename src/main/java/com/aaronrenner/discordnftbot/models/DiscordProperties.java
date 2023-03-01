package com.aaronrenner.discordnftbot.models;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.aaronrenner.discordnftbot.discord.DiscordBot;
import lombok.Data;

@Data
@Component
public class DiscordProperties {
	
	@Autowired
	DiscordBot discordBot;
	
	// Easy access to K/V pairs
	private final String ADMIN_CHANNEL   = "adminChannel";
	private final String COMMAND_PREFIX  = "commandPrefix";
	private final String GAME_CHANNEL    = "gameChannel";
	private final String GAME_COMMAND    = "gameCommand";
	private final String GENERAL_CHANNEL = "generalChannel";
	private final String GENERAL_OUTOUT  = "generalOutput";
	private final String TOKEN_PAIR      = "token";
	private final String WINNER_ROLE     = "winnerRole";
	
	// Filled with data at Runtime
	private String  customName;
	private String  token;
	private String  commandPrefix;
	private String  gameChannel;
	private String  gameCommand;
	private String  generalChannel;
	private boolean generalOutput;
	private String  winnerRole;
	private String  adminChannel;
	private static final Logger LOGGER  = LoggerFactory.getLogger(DiscordProperties.class);

	public void configureDiscordProperties(Map<Object, Object> discordInfo, String customName) throws RuntimeException {
		// Store variable or use in the Discord interface
		this.customName	    = customName;
		this.adminChannel   = (String) discordInfo.get(ADMIN_CHANNEL);
		this.commandPrefix  = (String) discordInfo.get(COMMAND_PREFIX);
		this.gameChannel    = (String) discordInfo.get(GAME_CHANNEL);
		this.gameCommand    = (String) discordInfo.get(GAME_COMMAND);
		this.token          = (String) discordInfo.get(TOKEN_PAIR);
		this.winnerRole     = (String) discordInfo.get(WINNER_ROLE);
		
		// Allows winner output to be piped into additional channels
		String getGeneralOutput = String.valueOf(discordInfo.get(GENERAL_OUTOUT));
		this.generalOutput  = (getGeneralOutput != null) ? Boolean.valueOf(getGeneralOutput) : false;
		this.generalChannel = (String) discordInfo.get(GENERAL_CHANNEL);
		
		LOGGER.debug(this.toString());
		this.discordBot.build(this);
	}

}
