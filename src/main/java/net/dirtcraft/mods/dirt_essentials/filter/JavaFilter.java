package net.dirtcraft.mods.dirt_essentials.filter;

import net.dirtcraft.mods.dirt_essentials.config.SpamFixConfig;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JavaFilter implements Filter {
	@Override
	public boolean isLoggable(LogRecord record) {
		for (String message : SpamFixConfig.MESSAGES_TO_FILTER.get()) {
			if (record.getMessage().contains(message)) return false;
		}

		return true;
	}

	public static void applyFilter() {
		Logger.getLogger("").setFilter(new JavaFilter());
	}
}
