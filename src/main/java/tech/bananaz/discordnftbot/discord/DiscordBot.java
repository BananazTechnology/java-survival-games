package tech.bananaz.discordnftbot.discord;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.*;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.bananaz.discordnftbot.game.GameManager;
import tech.bananaz.discordnftbot.models.DiscordProperties;
import tech.bananaz.discordnftbot.models.Entry;
import tech.bananaz.discordnftbot.models.EventMessage;
import tech.bananaz.discordnftbot.utils.MockbinUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DiscordBot {
	
	@Autowired
	GameManager gameManager;
	
	@Autowired
	Commands commands;
	
	// Final
	private final String NEWLINE = "\n";
	private static final Logger LOGGER  = LoggerFactory.getLogger(DiscordBot.class);
	
	/** Required */
	private DiscordApi disc;
	private DiscordProperties config;
	private ServerTextChannel adminOut;
	private ServerTextChannel generalOut;
	private Role winnerRole;
	private MockbinUtils mUtils = new MockbinUtils();
	boolean hasWrappedUp = false;
	
	public void build(DiscordProperties config) {
		try {
			// Start Discord connection
	        this.disc = new DiscordApiBuilder()
	        					.setToken(config.getToken())
	        					.addIntents(Intent.MESSAGE_CONTENT)
	        					.login()
	        					.join();
	       
	        startupLogger();
	        // Obtain critical information about the Discord surhvur!
			Collection<Server> server = this.disc.getServers();
			if(!(server.size() > 0)) throw new RuntimeException("Bot not in any servers!");
			// Loops through all Servers the bot is in
			for(Server newServer : server) {
				// Loops all channels in each server
				for(ServerChannel channel : newServer.getChannels()) {
					// Ensure the channel in the loop is accessible to the 
					if(channel.getType().equals(ChannelType.SERVER_TEXT_CHANNEL) && channel.canYouSee()) {
						// Grab admin output channel
						boolean thisChannelMatchesAdminConfigByName = channel.getName().equalsIgnoreCase(config.getAdminChannel());
						boolean thisChannelMatchesAdminConfigById = (config.getAdminChannel().matches("[0-9]+")) ? channel.getId() == Long.parseLong(config.getAdminChannel()) : false;
						if(thisChannelMatchesAdminConfigByName || thisChannelMatchesAdminConfigById) {
							this.adminOut = channel.asServerTextChannel().get();
							
							if(!this.adminOut.canYouWrite()) throw new RuntimeException(String.format("Your bot does not have access to post \"%s\"!", config.getAdminChannel()));
						}
						// If additional output
						try {
							boolean thisChannelMatchesGeneralConfigByName = channel.getName().equalsIgnoreCase(config.getGeneralChannel());
							boolean thisChannelMatchesGeneralConfigById = (config.getGeneralChannel().matches("[0-9]+")) ? channel.getId() == Long.parseLong(config.getGeneralChannel()) : false;
							if(config.isGeneralOutput()) {
								// Grab general output channel
								if(thisChannelMatchesGeneralConfigByName || thisChannelMatchesGeneralConfigById) {
									this.generalOut = channel.asServerTextChannel().get();
									
									if(!this.generalOut.canYouWrite()) throw new RuntimeException(String.format("Your bot does not have access to post \"%s\"!", config.getGeneralChannel()));
								}
							}
						} catch (Exception e) {}
						
					}
				}
				if(newServer.canYouManageRoles()) {
					for(Role perm : newServer.getRoles()) {
						if(perm.getIdAsString().equalsIgnoreCase(config.getWinnerRole())) {
							this.winnerRole = perm;
						}
					}
				} else {
					throw new RuntimeException("Bot does not have permission to manage roles");
				}
			}
			if(this.adminOut == null)   throw new RuntimeException(String.format("No channels on the server matched the name \"%s\", Maybe you cannot see it?", config.getAdminChannel()));
			if(this.generalOut == null) LOGGER.info(String.format("No general output channel detected for \"%s\", I hope that's intended!", config.getGeneralChannel()));
			

	        // Games init
	        this.config = config;
	        this.disc.addMessageCreateListener(this.commands.build(config));
		} catch (Exception e) {
			LOGGER.error("Discord Error: Failed starting bot! Exception: " + e.getMessage());
        	throw new RuntimeException("Discord Error: Failed starting bot! Exception: " + e.getMessage());
		}
	}
	
	public void currentGame(EventMessage e) {
		MessageBuilder msg = new MessageBuilder();
		msg.append(this.gameManager.getSg().toString());
		msg.send(e.getChannel());
	}
	
	public void sendEntry(EventMessage e) {
		MessageBuilder msg = new MessageBuilder();
		String out = e.getOutboundMessage();
		msg.append(out);
		msg.send(e.getChannel());
		
		// Run a check if game is over
		if(!hasWrappedUp && this.gameManager.isFin()) {
			gameCleanup(e);
			hasWrappedUp=true;
		}
	}
	
	public void sendAdminNotification(Entry winner) {
		MessageBuilder msg = new MessageBuilder();
		String data = String.format("User %s(%s) has claimed an entry!", winner.getDirectMention(), winner.getDiscordUser());
		msg.append(data);
		msg.send(this.adminOut);
	}
	
	public void sendAdminMessage(String text) {
		MessageBuilder msg = new MessageBuilder();
		String data = String.format("%s", text);
		msg.append(data);
		msg.send(this.adminOut);
	}
	
	public void sendGeneralNotification(EventMessage e) {
		if(this.config.isGeneralOutput()) {
			MessageBuilder msg = new MessageBuilder();
			String formattedMsg = String.format("User %s has claimed entry into %s, give them a congrats!", e.getUser().getMentionTag(), this.config.getCustomName());
			msg.append(formattedMsg);
			msg.send(this.generalOut);
		}
	}
	
	public void sendGameEndMessage(EventMessage e) {
		MessageBuilder msg = new MessageBuilder();
		String formattedMsg = String.format(":fire:**Thank you to all who played, this game has concluded!**:fire:");
		msg.append(formattedMsg);
		msg.send(e.getChannel());
	}
	
	public void sendHelp(EventMessage e) {
		MessageBuilder msg = new MessageBuilder();
		String formattedMsg = 
						this.config.getCommandPrefix() + this.config.getGameCommand() + " - plays the game" + NEWLINE +
						this.config.getCommandPrefix() + "cooldown - displays user cooldown" + NEWLINE +
						this.config.getCommandPrefix() + "help - calls this menu" + NEWLINE +
						this.config.getCommandPrefix() + "currentGame - shows info about the game" + NEWLINE + 
						this.config.getCommandPrefix() + "resumeGame - configures the \"paused\" value to false" + NEWLINE +
						this.config.getCommandPrefix() + "pauseGame - configures \"paused\" to be true" + NEWLINE +
						this.config.getCommandPrefix() + "gameAdd <userTag> - use the @ tag to manually add user" + NEWLINE +
						this.config.getCommandPrefix() + "listWinners - can output winners in batch to the Discord channel" + NEWLINE +
						this.config.getCommandPrefix() + "exportWinners - exports a CSV, displays the URL" + NEWLINE +
						this.config.getCommandPrefix() + "exportEntries - exports a CSV, displays the URL" + NEWLINE +
						this.config.getCommandPrefix() + "setDropScale - can modify the drop rate, will need to resume too" + NEWLINE;
		msg.append(formattedMsg);
		msg.send(e.getChannel());
	}
	
	public void loggedWallet(EventMessage e) {
		MessageBuilder msg = new MessageBuilder();
		String formattedMsg = String.format("%s we logged [%s]", e.getUser().getMentionTag(), e.getOutboundMessage());
		msg.append(formattedMsg);
		msg.send(e.getChannel());
	}
	
	public void sendCooldown(EventMessage e) {
		Optional<Instant> last = this.gameManager.getCooldown(e.getUser());
		MessageBuilder msg = new MessageBuilder();
		String message;
		long cooldown = 0;
		if(!last.isEmpty()) {
			message  = "%s you're off cooldown <t:%s:R> (<t:%s>)";
			cooldown = last.get().getEpochSecond();
			if(Instant.now().isAfter(last.get())) message = "%s you were off cooldown <t:%s:R> (<t:%s>)";
		} else {
			message  = "%s you have not played!";
		}
		msg.append(String.format(message, e.getUser().getMentionTag(), cooldown, cooldown));
		msg.send(e.getChannel());
	}
	
	public void assignWinnerRole(User winner) {
		winner.addRole(this.winnerRole, "Winner of " + this.config.getCustomName());
	}
	
	public void exportWinners(EventMessage e) throws Exception {
		// Build header
		String data = String.format("id,created,discord_name,discord_id,info%s", NEWLINE);
		// Get all winners
		List<Entry> allWinners = this.gameManager.getWinners();
		
		// Add Winners to string
		for(int t = 0; t < allWinners.size(); t++) {
			Entry ticket = allWinners.get(t);
			data+=String.format("%s,%s,%s,%s,%s%s", 
									(t+1),
									ticket.getCreated(),
									ticket.getDiscordUser(),
									ticket.getDiscordId(),
									ticket.getWallet(),
									NEWLINE);
		}
		// Dispatch message
		String outputUrl = this.mUtils.createBinCSV(filenameBuilder("winners"), data);
		new MessageBuilder().append(outputUrl).send(e.getChannel());
	}
	
	public void exportEntries(EventMessage e) throws Exception {
		// Build header
		String data = String.format("id,created,discord_name,discord_id%s", NEWLINE);
		// Get all winners
		List<Entry> allWinners = this.gameManager.getEntries();
		
		// Add Winners to string
		for(int t = 0; t < allWinners.size(); t++) {
			Entry ticket = allWinners.get(t);
			data+=String.format("%s,%s,%s,%s%s", 
									(t+1),
									ticket.getCreated(),
									ticket.getDiscordUser(),
									ticket.getDiscordId(),
									NEWLINE);
		}
		// Dispatch message
		String outputUrl = this.mUtils.createBinCSV(filenameBuilder("entries"), data);
		new MessageBuilder().append(outputUrl).send(e.getChannel());
	}
	
	public void listWinners(EventMessage e) {
		// Into
		String data = String.format("Winners in %s (claimed: %s/%s): %s", 
										this.config.getCustomName(), 
										this.gameManager.getSg().getWinners(), 
										this.gameManager.getSg().getMaxWinners(),
										NEWLINE);
		new MessageBuilder().append(data).send(e.getChannel());
		
		// Logic for Discord
		List<Entry> allWinners = this.gameManager.getWinners();
		// Discord only supports so many char per msg
		// When running a game with more than ~50 people
		// This will cause the msg to be lost
		int maxPerMsg = 15;
		// Calculate how many batches of 25 will need
		int piece = (int) (Math.floor(allWinners.size() / maxPerMsg) + 1);
		// Hold overall position
		int overallPos = 0;
		// Run batches
		for(int w = 0; (w<piece); w++) {
			// Holds this msg
			String partialMsg = "";
			// Add Winners
			for(int t = 0; t < maxPerMsg; t++) {
				if(overallPos < allWinners.size()) {
					Entry ticket = allWinners.get(overallPos);
					partialMsg+=String.format("%s: %s(%s) %s", 
												(overallPos+1), 
												ticket.getDirectMention(), 
												ticket.getDiscordUser(),
												NEWLINE);
					overallPos++;
					// Break here to save CPU cycles
				} else break;
			}
			// Dispatch message
			new MessageBuilder().append(partialMsg).send(e.getChannel());
		}
	}
	
	private void gameCleanup(EventMessage ent) {
		if(this.gameManager.isFin()) {
			new Thread(() -> {
				try {
					Thread.sleep(5000);
					// Store EventMessage in this thread
					EventMessage local = ent;
					ServerTextChannel output = local.getChannel();
					// Delay channel
					output.updateSlowmodeDelayInSeconds(20000);
					// Inform users game is over
					sendGameEndMessage(local);
					// Notify an administrator
					String data = String.format("%s the game \"%s\" has ended! %s", 
													this.gameManager.getAdminContact(), 
													config.getCustomName(), 
													NEWLINE);
					// Dispatch message
					new MessageBuilder().append(data).send(this.adminOut);
					// Reuse this EventMessage and switch output for admins to see
					local.setChannel(this.adminOut);
					//listWinners(ent);
				} catch (Exception e) { }
			}).start();
		}
	}
	
	private void startupLogger() throws JsonProcessingException {
        LOGGER.info("--------");
		LOGGER.debug("Discord bot config: "+ new ObjectMapper().writeValueAsString(disc.getCachedApplicationInfo()));
        LOGGER.debug("Discord connection started!");
        LOGGER.info("Need this bot in your server? " + disc.createBotInvite(Permissions.fromBitmask(0x0000000008)));
        LOGGER.info("--------");
	}
	
	public DiscordApi getDiscord() {
		return this.disc;
	}
	
	public Role getWinnerRole() {
		return this.winnerRole;
	}
	
	private String filenameBuilder(String source) {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("m-d-Y");
		String date = ft.format(dNow);
		return String.format("%s(%s)-%s", config.getCustomName(), source, date);
	}

}