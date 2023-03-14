package net.dirtcraft.mods.dirt_essentials.data;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class TeleportRequest {
	@Getter
	private final UUID from;

	@Getter
	private final UUID to;

	@Getter
	private final LocalDateTime timeSent;

	@Getter
	private final Type type;

	@Getter
	private final TeleportType teleportType;

	public TeleportRequest(UUID from, UUID to, LocalDateTime timeSent, Type type, TeleportType teleportType) {
		this.from = from;
		this.to = to;
		this.timeSent = timeSent;
		this.type = type;
		this.teleportType = teleportType;
	}

	public enum Type {
		OUTGOING,
		INCOMING
	}

	public enum TeleportType {
		TPA,
		TPAHERE
	}
}
