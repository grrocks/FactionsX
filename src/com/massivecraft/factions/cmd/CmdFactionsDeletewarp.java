package com.massivecraft.factions.cmd;

import org.bukkit.ChatColor;

import com.massivecraft.factions.Perm;
import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;
import com.massivecraft.massivecore.command.type.primitive.TypeString;

public class CmdFactionsDeletewarp extends FactionsCommand {
	public CmdFactionsDeletewarp() {
		// Aliases
		this.addAliases("deletewarp", "delwarp", "unsetwarp");

		// Parameters
		this.addParameter(TypeString.get(), "name");
		this.addParameter(TypeFaction.get(), "faction", "you");
		this.addRequirements(RequirementHasPerm.get(Perm.DELETEWARP.node));
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public void perform() throws MassiveException {
		Faction faction = readArgAt(1, msenderFaction);

		if (!MPerm.getPermDeletewarp().has(msender, faction, true))
			return;

		boolean deleted = faction.deleteWarp((String) readArgAt(0));

		msender.message((deleted ? (ChatColor.AQUA + "Deleted the warp " + readArgAt(0))
				: (ChatColor.RED + "Invalid warp name")));
	}
	
}
