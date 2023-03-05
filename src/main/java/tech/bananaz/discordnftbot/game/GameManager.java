package tech.bananaz.discordnftbot.game;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.javacord.api.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.bananaz.discordnftbot.discord.DiscordBot;
import tech.bananaz.discordnftbot.models.GameProperties;
import tech.bananaz.discordnftbot.models.Entry;
import tech.bananaz.discordnftbot.models.EventMessage;
import tech.bananaz.discordnftbot.repositories.EntryRepository;
import tech.bananaz.discordnftbot.services.OutboundScheduler;

import lombok.Data;

@Data
@Component
public class GameManager {
	
	@Autowired
	EntryRepository entryRepo;
	
	@Autowired
	DiscordBot bot;
	
	@Autowired
	OutboundScheduler outboundScheduler;
	
	private SurvivalGame sg;
	GameProperties config;
	private List<String> winningMessages;
	private List<String> loserMessages;
	private String adminContact;
	private int cooldown;
	
	public GameManager() {}
	
	public void build(GameProperties config) {
		this.sg 			 = new SurvivalGame(
									countEntries(),
									countWinners(),
									config.getMaxWinners(), 
									config.getDropScale(), 
									config.getCustomName());
		this.cooldown		 = config.getUserCooldown();
		this.adminContact    = config.getAdminContact();
		this.winningMessages = config.getWinnerMessages();
		this.loserMessages   = config.getLoserMessages();
		// Start the outbound msg queue
		this.outboundScheduler.start(config.getDispatchInterval());
		this.config 		 = config;
	}
	
	public void play(EventMessage e) {
		// Get send Discord info
		long discordId  = e.getUser().getId();
		String username = e.getUser().getDiscriminatedName();
		
		String outbound = null;
		if(isFin()) outbound = "The game is over %s!";
		if(isPaused()) outbound = "The game is inactive %s!";
		if(!isFin() && !isPaused()) {
			// Grab previous entry by this user
			Optional<Entry> last    = this.entryRepo.findFirstByDiscordIdOrderByCreatedDesc(discordId);
			boolean isOnCooldown    = false;
			Instant lastRequestTime = null;
			boolean hasWon 		 	= false;
			// If Entry exist, check if on cooldown
			if(last.isPresent()) {
				Entry usrPrevious = last.get();
				Instant cooldownTime = usrPrevious.getCreated().plus(this.cooldown, ChronoUnit.SECONDS);
				hasWon = usrPrevious.isWinner();
				if(Instant.now().isBefore(cooldownTime)) {
					isOnCooldown = true;
					lastRequestTime = cooldownTime;
				}
			}
			
			// Default cooldown msg
			if(!this.config.isDisableWinlock()) outbound = "%s you've already won this game! Save some glory for the rest of us!";
			long diff = (lastRequestTime != null) ? lastRequestTime.getEpochSecond() : 0;
			String cooldownString = "%s you're off cooldown <t:%s:R> (<t:%s>)";
			if(lastRequestTime != null) outbound = String.format(cooldownString, "%s", diff, diff);
			// If not on cooldown roll game!
			if(!isOnCooldown && (!hasWon || this.config.isDisableWinlock())) {
				// Roll for result
				boolean result = this.sg.roll();
				long id 	   = this.sg.getEntries();
				// Build entry and set winning status
				Entry reciept  = new Entry(id, discordId, username);
				reciept.setWinner(result);
				
				// Build outbound message
				outbound = (result) ? getRandomWinningMsg() : getRandomLoserMsg();
				
				// If winner Notify Discod admin
				if(result) {
					this.bot.assignWinnerRole(e.getUser());
					this.bot.sendAdminNotification(reciept);
					this.bot.sendGeneralNotification(e);
				}
				
				// Log in DB
				saveEntry(reciept);
			}
		}

		String fOutbound = String.format(outbound, e.getUser().getMentionTag(), this.adminContact);
		e.setOutboundMessage(fOutbound);
		this.outboundScheduler.add(e);
	}
	
	public boolean isFin() {
		boolean fin = false;
		if(this.sg.getWinners() == this.sg.getMaxWinners()) fin = true;
		return fin;
	}
	
	public void autoWin(EventMessage e) {
		// Roll for natural ID increment REQUIRED
		this.sg.manualWinner();
		long id 	   = this.sg.getEntries();
		long discordId = e.getUser().getId();
		Optional<Entry> last = this.entryRepo.findFirstByDiscordIdOrderByCreatedDesc(discordId);
		boolean hasWon = false;
		if(last.isPresent()) {
			Entry usrPrevious = last.get();
			hasWon = usrPrevious.isWinner();
		}
		if(!hasWon) {
			// Build entry and set winning status
			Entry reciept  = new Entry(id, discordId, e.getUser().getDiscriminatedName());
			reciept.setWinner(true);
			// Log in DB
			saveEntry(reciept);
			// Log to discord
			this.bot.assignWinnerRole(e.getUser());
			this.bot.sendAdminNotification(reciept);
		} else {
			this.bot.sendAdminMessage(String.format("User %s(%s) was already recorded!", e.getUser().getMentionTag(), e.getUser().getDiscriminatedName()));
		}
	}
	
	public boolean isPaused() {
		return this.sg.isPaused();
	}
	
	public void pauseGame() {
		this.sg.setPaused(true);
	}
	
	public void resumeGame() {
		this.sg.setPaused(false);
	}
	
	public long countWinners() {
		return this.entryRepo.countByWinner(true);
	}
	
	public long countEntries() {
		return this.entryRepo.count();
	}
	
	public List<Entry> getWinners() {
		return this.entryRepo.findByWinner(true);
	}
	
	public List<Entry> getEntries() {
		return this.entryRepo.findAll();
	}
	
	public String getRandomWinningMsg() {
		int pos = new SecureRandom().nextInt(this.winningMessages.size());
		return this.winningMessages.get(pos);
	}
	
	public String getRandomLoserMsg() {
		int pos = new SecureRandom().nextInt(this.loserMessages.size());
		return this.loserMessages.get(pos);
	}
	
	public Optional<Entry> getUserMostRecent(User user) {
		// Get send Discord info
		long discordId  = user.getId();
		Optional<Entry> last = this.entryRepo.findFirstByDiscordIdOrderByCreatedDesc(discordId);
		return last;
	}
	
	public void addWallet(EventMessage e) {
		new Thread(() -> {
			long userId = e.getUser().getId();
			List<Entry> entries = this.entryRepo.findByDiscordIdAndWinner(userId, true);
			for(int i = 0; i < entries.size(); i++) {
				Entry m = entries.get(i);
				m.setWallet(e.getOutboundMessage());
				saveEntry(m);
			}
		}).start();
	}
	
	private void saveEntry(Entry e) {
		new Thread(() -> {
			this.entryRepo.save(e);
		}).start();
	}
	
	public Optional<Instant> getCooldown(User u) {
		// Get send Discord info
		long discordId  = u.getId();
		Optional<Entry> last = this.entryRepo.findFirstByDiscordIdOrderByCreatedDesc(discordId);
		Optional<Instant> lastRequestTime = Optional.empty();
		// If Entry exist, check if on cooldown
		if(last.isPresent()) {
			Entry usrPrevious = last.get();
			Instant cooldownTime = usrPrevious.getCreated().plus(this.cooldown, ChronoUnit.SECONDS);
			lastRequestTime = Optional.of(cooldownTime);
		}
		return lastRequestTime;
	}

}
