package com.massivecraft.factions.cmd;

import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.engine.EngineMain;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementIsPlayer;
import com.massivecraft.massivecore.mixin.Mixin;
import com.massivecraft.massivecore.mixin.TeleporterException;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.teleport.Destination;
import com.massivecraft.massivecore.teleport.DestinationSimple;

public class CmdFactionsAssist extends FactionsCommand {

	public CmdFactionsAssist() {
		this.addAliases("assist");

		// Parameters
		this.addParameter(TypeFaction.get(), "faction", "you");
		this.addRequirements(RequirementIsPlayer.get());
	}

	@Override
	public void perform() throws MassiveException {
		Faction faction = readArg(msenderFaction);

		if (!MPerm.getPermAssist().has(msender, faction, true))
			return;

		if (!EngineMain.assistFactions.containsKey(faction.getId())) {
			me.sendMessage(faction.getName() + " does not need assitance");
			return;
		}

		Destination destination = new DestinationSimple(
				PS.valueOf(EngineMain.assistFactions.get(faction.getId())));
		try {
			Mixin.teleport(me, destination, MConf.get().warmup);
		} catch (TeleporterException e) {
			msg("<b>%s", e.getMessage());
		}
	}
}
