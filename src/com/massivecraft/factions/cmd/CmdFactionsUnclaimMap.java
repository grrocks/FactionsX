package com.massivecraft.factions.cmd;

import java.util.List;

import mkremins.fanciful.FancyMessage;

import org.bukkit.Location;

import com.massivecraft.factions.Const;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementIsPlayer;
import com.massivecraft.massivecore.ps.PS;

public class CmdFactionsUnclaimMap extends FactionsCommand {

	public CmdFactionsUnclaimMap() {

		this.addAliases("map");

		this.addRequirements(RequirementIsPlayer.get());
	}

	@Override
	public void perform() throws MassiveException {
		showMap(Const.MAP_WIDTH, Const.MAP_HEIGHT_FULL);
		return;
	}
	
	public void showMap(int width, int height) {
		Location location = me.getLocation();
		List<FancyMessage> message = BoardColl.get().getMapUnclaim(msenderFaction,
				PS.valueOf(location), location.getYaw(), width, height);
		for (FancyMessage msg : message) {
			msg.send(me);
		}
	}
}
