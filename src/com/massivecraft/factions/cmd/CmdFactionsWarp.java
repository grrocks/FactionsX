package com.massivecraft.factions.cmd;

import org.bukkit.ChatColor;

import com.massivecraft.factions.Perm;
import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;
import com.massivecraft.massivecore.command.requirement.RequirementIsPlayer;
import com.massivecraft.massivecore.command.type.primitive.TypeString;
import com.massivecraft.massivecore.mixin.Mixin;
import com.massivecraft.massivecore.mixin.TeleporterException;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.teleport.Destination;
import com.massivecraft.massivecore.teleport.DestinationSimple;

public class CmdFactionsWarp extends FactionsCommand {

	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //

	public CmdFactionsWarp() {
		// Aliases
		this.addAliases("warp");

		// Parameters
		this.addParameter(TypeString.get(), "name");
		this.addParameter(TypeFaction.get(), "faction", "you");
		this.addParameter(null, TypeString.get(), "password");
		
		this.addRequirements(RequirementHasPerm.get(Perm.WARP.node));
		this.addRequirements(RequirementIsPlayer.get());
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public void perform() throws MassiveException {
		// Args
		Faction faction = readArgAt(1, msenderFaction);

		String password = readArgAt(2, null);
		
		if ( ! MPerm.getPermWarp().has(msender, faction, true)) return;
		
		PS loc = faction.getWarp((String) readArgAt(0));
		// MPerm
		if (!MPerm.getPermWarp().has(msender, faction, true))
			return;

		if (loc == null) {
			msender.message(ChatColor.RED + "This is not a valid warp");
			return;
		}

		if(faction.getWarpPassword((String) readArgAt(0)) == null){
			Destination destination = new DestinationSimple(loc);
			try {
				Mixin.teleport(me, destination, MConf.get().warmup);
			} catch (TeleporterException e) {
				msg("<b>%s", e.getMessage());
			}
			return;
		}
		
		if (faction.getWarpPassword((String) readArgAt(0)).equals(password)) {
			Destination destination = new DestinationSimple(loc);
			try {
				Mixin.teleport(me, destination, MConf.get().warmup);
			} catch (TeleporterException e) {
				msg("<b>%s", e.getMessage());
			}
		} else {
			msender.message(ChatColor.RED + "Incorrect Password!");
		}

	}
}
