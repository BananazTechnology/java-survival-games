package tech.bananaz.discordnftbot.game;

import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = true)
public class SurvivalGame {
	
	private int     maxWinners;
	private int     scale;
	private long    winners = 0;
	private long    entries = 0;
	private boolean paused  = true;
	private String  name;
	private static final Logger LOGGER  = LoggerFactory.getLogger(SurvivalGame.class);
	
	public SurvivalGame(long entries, long winners, int maxWinners, int scale, String name) {
		this.entries    = entries;
		this.winners    = winners;
		this.maxWinners = maxWinners;
		this.scale      = scale;
		this.name       = name; 
	}
	
	/**
	 * Roll for chance to win
	 * Game must be active
	 * @return true = winner // false = looser
	 */
	public boolean roll() {
		boolean result = false;
		if(!this.paused) {
			if(this.winners <= this.maxWinners) {
				int rand = new SecureRandom().nextInt(this.scale);
				LOGGER.info("Game rolled a {}/{}", rand, (this.scale-1));
				if(rand == (this.scale-1)) {
					result = true;
					this.winners++;
				}
				this.entries++;
			}
		}
		return result;
	}
	
	public void manualWinner() {
		this.entries++;
		this.winners++;
	}
}
