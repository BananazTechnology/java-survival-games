package tech.bananaz.discordnftbot.discord;

import java.util.Optional;
import java.util.regex.Matcher;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.bananaz.discordnftbot.game.GameManager;
import tech.bananaz.discordnftbot.game.SurvivalGame;
import tech.bananaz.discordnftbot.models.DiscordProperties;
import tech.bananaz.discordnftbot.models.EventMessage;
import static tech.bananaz.discordnftbot.utils.CommandUtils.*;
import static org.javacord.api.util.DiscordRegexPattern.*;

@Component
public class Commands implements MessageCreateListener {

	@Autowired
	GameManager gameManager;
	
	@Autowired
	DiscordBot bot;
	
	// Custom
	private static final Logger LOGGER  = LoggerFactory.getLogger(Commands.class);
	private DiscordProperties config;
	private static final String CHECK = "👍🏼";
	private static final String WRONG = "👎🏼";
	
	public Commands() { }
	
	public Commands build(DiscordProperties config) {
		this.config = config;
		return this;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		// Grab individual data from message event
		Message message = event.getMessage();
		String parsedMsg = message.getContent().trim();
		Optional<User> sender = message.getUserAuthor();
		Optional<ServerTextChannel> outputChannel = message.getServerTextChannel();
		Optional<Server> server = event.getServer();
		
		// Grab a few config values
		String prefix = this.config.getCommandPrefix();
		String command = this.config.getGameCommand();
		
		// Don't even bother if message does not chart with char
		if(message.getContent().startsWith(prefix) && !sender.get().isYourself()) {
			// Build internal message
			EventMessage e = new EventMessage(outputChannel.get(), sender.get());
			LOGGER.info("User {} triggered the bot EventMessage with [{}]!", sender.get().getDiscriminatedName(), parsedMsg);
			// Run command checks
			// user self-add wallet
			if(parsedMsg.contains(prefix+"wallet")) {
				Role winner = this.bot.getWinnerRole();
				if(userHasWinnerRole(sender, server, winner)) {
					String[] splitMsg = parsedMsg.split(" ");
					String addy = splitMsg[1];
					if(addy.matches("^[a-zA-Z0-9]{30,42}$")) {
						splitMsg[0] = "";
						e.setOutboundMessage(convertObjectArrayToString(splitMsg));
						this.gameManager.addWallet(e);
						this.bot.loggedWallet(e);
						LOGGER.info("User {} linked wallet with [{}]!", sender.get().getDiscriminatedName(), e.getOutboundMessage());
						event.addReactionsToMessage(CHECK);
					} else {
						event.addReactionsToMessage(WRONG);
					}
				}
			}
			
			// listWinners
			if(parsedMsg.equalsIgnoreCase(prefix+"listWinners")) {
				if(userHasAdminPerms(sender, server))
					this.bot.listWinners(e);
					LOGGER.info("User {} called for list of winners in Discord!", sender.get().getDiscriminatedName());
			}
			
			// help
			if(parsedMsg.equalsIgnoreCase(prefix+"help")) {
				this.bot.sendHelp(e);
				LOGGER.info("User {} called the help menu!", sender.get().getDiscriminatedName());
			}
			
			// exportWinners
			if(parsedMsg.equalsIgnoreCase(prefix+"exportWinners")) {
				if(userHasAdminPerms(sender, server)) {
					try {
						this.bot.exportWinners(e);
						LOGGER.info("User {} exported winners!", sender.get().getDiscriminatedName());
						event.addReactionsToMessage(CHECK);
					} catch (Exception ex) {
						event.addReactionsToMessage(WRONG);
						ex.printStackTrace();
					}
				}
			}
			
			// exportEntries
			if(parsedMsg.equalsIgnoreCase(prefix+"exportEntries")) {
				if(userHasAdminPerms(sender, server)) {
					try {
						this.bot.exportEntries(e);
						LOGGER.info("User {} exported entries!", sender.get().getDiscriminatedName());
						event.addReactionsToMessage(CHECK);
					} catch (Exception ex) {
						event.addReactionsToMessage(WRONG);
						ex.printStackTrace();
					}
				}
			}
			
			// setDropScale
			if(parsedMsg.contains(prefix+"setDropScale")) {
				if(userHasAdminPerms(sender, server)) {
					try {
						String[] splitMsg  = parsedMsg.split(" ");
						int newDropScale = Integer.valueOf(splitMsg[1]);
						this.gameManager.setSg(new SurvivalGame(this.gameManager.countEntries(), 
																	this.gameManager.countWinners(), 
																	this.gameManager.getConfig().getMaxWinners(), 
																	newDropScale, 
																	this.gameManager.getConfig().getCustomName()));
						LOGGER.info("User {} set the srop scale to {}!", sender.get().getDiscriminatedName(), newDropScale);
						event.addReactionsToMessage(CHECK);
					} catch (Exception e2) {
						event.addReactionsToMessage(WRONG);
					}
				}
			}
			
			// currentGame
			if(parsedMsg.equalsIgnoreCase(prefix+"currentGame")) {
				if(userHasAdminPerms(sender, server))
					this.bot.currentGame(e);
					LOGGER.info("User {} requested the current game!", sender.get().getDiscriminatedName());
			}	
			
			// resumeGame
			if(parsedMsg.equalsIgnoreCase(prefix+"resumeGame")) {
				if(userHasAdminPerms(sender, event.getServer())) {
					this.bot.gameManager.resumeGame();
					LOGGER.info("User {} resumed the game!", sender.get().getDiscriminatedName());
					event.addReactionsToMessage(CHECK);
				}
			}	
			
			// pauseGame
			if(parsedMsg.equalsIgnoreCase(prefix+"pauseGame")) {
				if(userHasAdminPerms(sender, server)) {
					this.bot.gameManager.pauseGame();
					LOGGER.info("User {} paused the game!", sender.get().getDiscriminatedName());
					event.addReactionsToMessage(CHECK);
				}
			}	
			
			// gameAdd
			if(parsedMsg.contains(prefix+"gameAdd")) {
				if(userHasAdminPerms(sender, server)) {
					try {
						String[] splitMsg  = parsedMsg.split(" ");
						String mentionTag = splitMsg[1];
						Matcher validationCheck = USER_MENTION.matcher(mentionTag);
						if(validationCheck.matches()) {
							String discordId = validationCheck.group(1);
							User newWinner = this.bot.getDiscord().getUserById(discordId).get();
							e.setUser(newWinner);
							this.bot.gameManager.autoWin(e);
							LOGGER.info("User {} added a manual entry!", sender.get().getDiscriminatedName());
							event.addReactionsToMessage(CHECK);
						}
					} catch (Exception e2) {
						event.addReactionsToMessage(WRONG);
					}
				}
			}	
			
			// See if event occurs in game channel
			String channelValueFromConfig = config.getGameChannel();
			boolean thisChannelMatchesConfigChannel = message.getServerTextChannel().get().getName().equalsIgnoreCase(channelValueFromConfig);
			boolean thisChannelMatchesConfigById = (channelValueFromConfig.matches("[0-9]+")) ? message.getServerTextChannel().get().getId() == Long.parseLong(channelValueFromConfig) : false;
			if(thisChannelMatchesConfigChannel || thisChannelMatchesConfigById) {
				// Double ensure prefix + command was what user entered
				if(message.getContent().equalsIgnoreCase( prefix + "cooldown" )) {
					// Play game
					this.bot.sendCooldown(e);
					LOGGER.info("User {} called for cooldown!", sender.get().getDiscriminatedName());
				}	
				
				// Double ensure prefix + command was what user entered
				if(message.getContent().equalsIgnoreCase( prefix + command )) {
					// Play game
					try {
						this.gameManager.play(e);
					} catch (Exception ex) {
						ex.printStackTrace();
						event.addReactionsToMessage(WRONG);
					}
					LOGGER.info("User {} played the game!", sender.get().getDiscriminatedName());
				}
	        }
		}
	}
}
