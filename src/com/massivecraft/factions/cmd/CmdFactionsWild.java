package com.massivecraft.factions.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.mixin.Mixin;
import com.massivecraft.massivecore.mixin.TeleporterException;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.teleport.Destination;
import com.massivecraft.massivecore.teleport.DestinationSimple;

public class CmdFactionsWild extends FactionsCommand implements Listener {

	private int maxX = MConf.get().wildMaxX;
	private int maxY = MConf.get().wildMaxY;
	List<String> enabledWorlds = MConf.get().wildEnabledWorlds.get("worlds");
	private int warmup = MConf.get().wildWarmupSeconds;
	private int cooldown = MConf.get().wildCooldownSeconds;
	public Map<UUID, Long> commandCooldown = new HashMap<UUID, Long>();

	public CmdFactionsWild() {
		aliases.add("wild");
	}

	@Override
	public void perform() throws MassiveException {
		if (!enabledWorlds.contains(me.getWorld().getName())) {
			msg(ChatColor.RED
					+ "This world has wilderness teleporting disabled!");
			return;
		}
		if (commandCooldown.containsKey(me.getUniqueId())
				&& commandCooldown.get(me.getUniqueId()) > System
						.currentTimeMillis()) {
			int timeLeft = (int) ((commandCooldown.get(me.getUniqueId()) - System
					.currentTimeMillis()) / 1000);

			msg(ChatColor.RED + "You cannot use this command yet wait "
					+ formatTime(timeLeft));
			return;
		}

		msg(ChatColor.YELLOW + "Looking for wilderness!");

		boolean successful = false;
		int trys = 0;

		Location loc = null;

		while (!successful) {

			int randomX = (int) (Math.random() * maxX);
			int randomY = (int) (Math.random() * maxY);

			loc = new Location(me.getWorld(), randomX, 0, randomY);

			PS ps = PS.valueOf(loc);
			trys++;

			if (BoardColl.get().getFactionAt(ps).isNone())
				successful = true;
			if (trys > 9) {
				msg(ChatColor.RED + "Could not find wilderness. Try again!");
				return;
			}
		}

		Block b = loc.getWorld().getHighestBlockAt(loc);

		Destination destination = new DestinationSimple(PS.valueOf(b));
		try {
			Mixin.teleport(me, destination, warmup);
			commandCooldown.put(me.getUniqueId(), System.currentTimeMillis()
					+ cooldown * 1000);
		} catch (TeleporterException e) {
			commandCooldown.remove(me.getUniqueId());
			msg("<b>%s", e.getMessage());
		}
	}

	public String formatTime(int secondsCount) {
		// Calculate the seconds to display:
		int seconds = (int) (secondsCount % 60);
		secondsCount -= seconds;
		// Calculate the minutes:
		long minutesCount = secondsCount / 60;
		long minutes = minutesCount % 60;
		minutesCount -= minutes;
		// Calculate the hours:
		long hoursCount = minutesCount / 60;
		// Build the String
		if (hoursCount == 0)
			return minutes + " minutes " + seconds + " seconds";
		if (minutes == 0)
			return seconds + " seconds";
		return hoursCount + " hours " + minutes + " minutes " + seconds
				+ " seconds";
	}
}
