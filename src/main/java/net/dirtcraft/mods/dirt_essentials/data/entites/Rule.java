package net.dirtcraft.mods.dirt_essentials.data.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Rule {
	@Id
	@GeneratedValue
	@Getter
	private UUID id;

	@OneToOne
	@Getter
	@Setter
	private DirtPlayer creator;

	@Getter
	@Setter
	private String message;

	@Getter
	@Setter
	private LocalDateTime creationDate;

	@Getter
	@Setter
	private LocalDateTime lastEditDate;

	public Rule() {}

	public Rule(DirtPlayer creator, String message) {
		this.creator = creator;
		this.message = message;
		this.creationDate = LocalDateTime.now();
		this.lastEditDate = LocalDateTime.now();
	}
}
