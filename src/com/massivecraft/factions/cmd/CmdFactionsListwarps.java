package com.massivecraft.factions.cmd;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.massivecore.MassiveException;

public class CmdFactionsListwarps extends FactionsCommand {

	public CmdFactionsListwarps() {
		// Aliases
		this.addAliases("listwarps");

		// Parameters
		this.addParameter(TypeFaction.get(), "faction", "you");

	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public void perform() throws MassiveException {
		Faction fac = readArg(msenderFaction);
		
		if ( ! MPerm.getPermWarp().has(msender, fac, true)) return;
		
		ArrayList<String> warps = fac.getAllWarps();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(ChatColor.GOLD + "Your faction has the following warps set: ");
		for(String warp : warps){
			sb.append(ChatColor.AQUA + warp + ChatColor.GOLD + ", ");
		}
		msender.message(sb.toString());
	}
}
