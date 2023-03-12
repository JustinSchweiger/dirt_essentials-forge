package net.dirtcraft.mods.dirt_essentials.data.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;

@Entity
public class Note {
	@Id
	@Getter
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Getter
	@Column(length = 999999)
	private String note;

	@Getter
	@ManyToOne
	private DirtPlayer player;

	@Getter
	private LocalDateTime added;

	@Getter
	private String addedBy;

	public Note() {}

	public Note(@NonNull DirtPlayer player, @NonNull String note, @NonNull String addedBy) {
		this.player = player;
		this.note = note;
		this.added = LocalDateTime.now();
		this.addedBy = addedBy;
	}
}
