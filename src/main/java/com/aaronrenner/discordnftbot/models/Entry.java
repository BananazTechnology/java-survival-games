package com.aaronrenner.discordnftbot.models;

import java.time.Instant;
import javax.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString(includeFieldNames = true)
public class Entry {

	@Id
	private long    id;
	private long    discordId;
	private String  discordUser;
	private String  directMention;
	private boolean winner;
	private Instant created;
	private String  wallet;
	
	protected Entry() {}
	
	public Entry(long id, long discordId, String name) {
		this.id            = id;
		this.discordId 	   = discordId;
		this.directMention = String.format("<@%s>", this.discordId);
		this.discordUser   = name;
		this.winner        = false;
		this.created	   = Instant.now();
	}
}
