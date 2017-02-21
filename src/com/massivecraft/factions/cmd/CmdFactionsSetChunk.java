package com.massivecraft.factions.cmd;

import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.type.primitive.TypeInteger;
import com.massivecraft.massivecore.ps.PS;

public class CmdFactionsSetChunk extends CmdFactionsSetX {
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //

	public CmdFactionsSetChunk(boolean claim) {
		// Super
		super(claim);
		// Aliases
		this.addAliases("chunk");

		this.addParameter(TypeInteger.get(), "X");
		this.addParameter(TypeInteger.get(), "Z");

		if (claim) {
			this.addParameter(TypeFaction.get(), "faction", "you");
			this.setFactionArgIndex(2);
		}

	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public Set<PS> getChunks() throws MassiveException {
		int x = this.readArgAt(0);
		int z = this.readArgAt(1);

		int abX = Math.abs(x);
		int abZ = Math.abs(z);

		int acAbX = Math.abs(me.getLocation().getChunk().getX());
		int acAbZ = Math.abs(me.getLocation().getChunk().getZ());

		int difX = Math.abs(acAbX - abX);
		int difZ = Math.abs(acAbZ - abZ);

		if (difX > MConf.get().claimChunkMaxDis
				|| difZ > MConf.get().claimChunkMaxDis) {
			me.sendMessage(ChatColor.RED
					+ "You are too far from the intended location to claim there");
			return null;
		}
		
		Location loc = new Location(me.getWorld(), x * 16, 0, z * 16);
		final PS chunk = PS.valueOf(loc).getChunk(true);
		final Set<PS> chunks = Collections.singleton(chunk);
		return chunks;
	}

	
	
	
}
