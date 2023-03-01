package com.aaronrenner.discordnftbot.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.aaronrenner.discordnftbot.models.Entry;

public interface EntryRepository extends JpaRepository<Entry, Long> {
	
	List<Entry>     findByWinner(boolean status);
	Optional<Entry> findFirstByDiscordIdOrderByCreatedDesc(long discordId);
	long			countByWinner(boolean status);
	List<Entry>     findByDiscordIdAndWinner(long discordId, boolean status);
	
}
