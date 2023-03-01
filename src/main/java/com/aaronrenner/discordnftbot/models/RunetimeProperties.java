package com.aaronrenner.discordnftbot.models;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nft-bot")
public class RunetimeProperties {
	
	private String customName;
	
	@Autowired
	DiscordProperties discordProperties;
	
	@Autowired
	GameProperties gameProperties;
	
	public void setCustomName(String value) {
		this.customName = value;
	}
	
	/**
	 * Sets the discord object!
	 * @param token
	 * @throws RuntimeException
	 */
	public void setDiscord(Map<Object, Object> discordInfo) throws RuntimeException {
		this.discordProperties.configureDiscordProperties(discordInfo, this.customName);
	}
	
	public void setGame(Map<Object, Object> gameInfo) throws RuntimeException {
		this.gameProperties.configureGameProperties(gameInfo, this.discordProperties, this.customName);
	}
}