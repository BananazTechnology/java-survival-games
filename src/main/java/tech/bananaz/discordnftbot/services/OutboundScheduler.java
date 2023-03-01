package tech.bananaz.discordnftbot.services;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.bananaz.discordnftbot.discord.DiscordBot;
import tech.bananaz.discordnftbot.models.EventMessage;

@Component
public class OutboundScheduler extends TimerTask {
	
	@Autowired
	DiscordBot bot;

	// Regular
	Queue<EventMessage> departureList    = new LinkedList<>();
	private boolean active 				 = false;
	private Timer timer 				 = new Timer(); // creating timer
	private final int DELAY				 = 1000;
    private TimerTask task; // creating timer task
	
	// Final
	private static final Logger LOGGER = LoggerFactory.getLogger(OutboundScheduler.class);

	public OutboundScheduler() { }
	
	@Override
	public void run() {
		if(this.active && this.departureList.size() > 0) {
			dispatch();
		}
	}

	public boolean start(long interval) {
		if(this.bot != null) {
			this.active = true;
			this.task   = this;
			LOGGER.info(String.format("Starting new OutboundScheduler with %sms loop", interval));
			// runs per DELAY milliseconds
			this.timer.schedule(task, this.DELAY , interval);
		}
		return this.active;
	}
	
	public boolean stop() {
		this.active = false;
		LOGGER.info("Stopping OutboundScheduler");
		return this.active;
	}
	
	private void dispatch() {
		int maxResponse = 7;
		EventMessage fin = new EventMessage();
		int size = this.departureList.size();
		String complete = "";
		for(int i = 0; (i < size); i++) {
			if(i < maxResponse) {
				EventMessage e = this.departureList.poll();
				complete += String.format("%s%s%s", e.getOutboundMessage(), "\n", "\n");
				fin.setChannel(e.getChannel());
			} else break;
		}
		fin.setOutboundMessage(complete);
		this.bot.sendEntry(fin);
	}
	
	public boolean add(EventMessage e) {
		return this.departureList.add(e);
	}
}
