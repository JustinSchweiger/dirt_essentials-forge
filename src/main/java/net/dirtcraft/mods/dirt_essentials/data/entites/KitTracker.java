package net.dirtcraft.mods.dirt_essentials.data.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
public class KitTracker {
	@Id
	@ManyToOne
	@Getter
	private DirtPlayer player;

	@Id
	@ManyToOne
	@Getter
	private Kit kit;

	@Getter
	@Setter
	private LocalDateTime lastClaimed;

	public KitTracker() {}

	public KitTracker(DirtPlayer player, Kit kit, LocalDateTime lastClaimed) {
		this.player = player;
		this.kit = kit;
		this.lastClaimed = lastClaimed;
	}
}
