package com.aaronrenner.discordnftbot.models;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;

import lombok.Data;

@Data
public class EventMessage {

	ServerTextChannel channel;
	User     		  user;
	String			  outboundMessage;
	
	public EventMessage() { }
	
	public EventMessage(ServerTextChannel channel, User user) {
		this.channel = channel;
		this.user    = user;
	}
}
