package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Perm;
import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;
import com.massivecraft.massivecore.command.type.primitive.TypeBoolean;
import com.massivecraft.massivecore.command.type.primitive.TypeInteger;
import com.massivecraft.massivecore.ps.PS;

public class CmdFactionsAccessFaction extends CmdFactionsAccessAbstract {
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //

	public CmdFactionsAccessFaction() {
		// Aliases
		this.addAliases("faction");

		// Parameters
		this.addParameter(TypeFaction.get(), "faction");
		this.addParameter(TypeBoolean.getYes(), "yes/no", "toggle");
		this.addParameter(1, TypeInteger.get(), "radius", "1");

		// Requirements
		this.addRequirements(RequirementHasPerm.get(Perm.ACCESS_FACTION.node));
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public void innerPerform() throws MassiveException {
		// Args
		Faction faction = this.readArg();
		boolean newValue = this
				.readArg(!ta.isFactionIdGranted(faction.getId()));
		Integer radiusZero = this.getRadiusZero();
		if (radiusZero == null)
			return;
		// MPerm
		if (MPerm.getPermAccess().has(msender, hostFaction, true)) {

			// Apply
			ta = ta.withFactionId(faction.getId(), newValue);
			BoardColl.get().setTerritoryAccessAt(chunk, ta);
			this.sendAccessInfo();
		}
		PS oldChunk = this.chunk;
		final PS chunk = PS.valueOf(me.getLocation()).getChunk(true);
		for (int dx = -radiusZero; dx <= radiusZero; dx++) {
			for (int dz = -radiusZero; dz <= radiusZero; dz++) {
				int x = chunk.getChunkX() + dx;
				int z = chunk.getChunkZ() + dz;
				
				if(x == oldChunk.getChunkX() && z == oldChunk.getChunkZ())
					continue;
				
				this.chunk = chunk.withChunkX(x).withChunkZ(z);
				
				// MPerm
				if (!MPerm.getPermAccess().has(
						msender,
						BoardColl.get().getFactionAt(this.chunk), true))
					continue;

				// Apply
				ta = BoardColl.get().getTerritoryAccessAt(this.chunk);
				ta = ta.withFactionId(faction.getId(), newValue);
				BoardColl.get().setTerritoryAccessAt(
						this.chunk, ta);
				hostFaction = ta.getHostFaction();

				// Inform
				this.sendAccessInfo();
			}
		}
	}

	public Integer getRadius() throws MassiveException {
		Integer radius = this.readArgAt(2);
		if (radius == null)
			return 1;

		// Radius Claim Min
		if (radius < 1) {
			msg("<b>If you specify a radius, it must be at least 1.");
			return null;
		}

		// Radius Claim Max
		if (radius > MConf.get().setRadiusMax && !msender.isOverriding()) {
			msg("<b>The maximum radius allowed is <h>%s<b>.",
					MConf.get().setRadiusMax);
			return null;
		}
		return radius;
	}

	public Integer getRadiusZero() throws MassiveException {
		Integer ret = this.getRadius();
		if (ret == null)
			return ret;
		return ret - 1;
	}

}
