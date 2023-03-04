package net.dirtcraft.mods.dirt_essentials.data.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "players")
public class DirtPlayer {
	@Id
	@Getter
	@Column(name = "uuid")
	private UUID uuid;
	@Getter
	@Column(name = "username")
	private String username;
	@Getter @Setter
	@Column(name = "rules_accepted")
	private boolean rulesAccepted;

	public DirtPlayer() {}

	public DirtPlayer(UUID uuid, String username) {
		this.uuid = uuid;
		this.username = username;
		this.rulesAccepted = false;
	}
}
