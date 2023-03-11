package net.dirtcraft.mods.dirt_essentials.filter;

import net.dirtcraft.mods.dirt_essentials.config.SpamFixConfig;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;

public class SystemFilter extends PrintStream {
	public SystemFilter(@NotNull OutputStream out) {
		super(out, true);
	}

	public static void applyFilter() {
		System.setOut(new SystemFilter(System.out));
	}

	@Override
	public void println(String s) {
		if (!shouldFilter(s)) {
			super.println(s);
		}
	}

	private boolean shouldFilter(String s) {
		for (String filter : SpamFixConfig.MESSAGES_TO_FILTER.get()) {
			if (s.contains(filter)) {
				return true;
			}
		}
		return false;
	}
}
