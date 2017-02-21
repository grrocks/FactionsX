package com.massivecraft.factions.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BannerMeta;

import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementIsPlayer;

public class CmdFactionsSetBanner extends FactionsCommand {
	public CmdFactionsSetBanner() {
		// Aliases
		this.addAliases("setbanner");

		// Parameters
		this.addParameter(TypeFaction.get(), "faction", "you");
		this.addRequirements(RequirementIsPlayer.get());
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public void perform() throws MassiveException {
		Faction faction = readArg(msenderFaction);

		if (faction.isNone()) {
			me.sendMessage(ChatColor.RED
					+ "Please join a faction to use this command!");
			return;
		}

		if (!MPerm.getPermSetbanner().has(msender, faction, true))
			return;

		if (me.getItemInHand().getType() == Material.BANNER) {
			BannerMeta data = (BannerMeta) me.getItemInHand().getItemMeta();

			List<String> patternlist = new ArrayList<String>();
			String basecolor;
			if ((data == null) || (data.getBaseColor() == null)
					|| (data.getBaseColor().toString() == null)) {
				basecolor = "BLACK";
			} else {
				basecolor = data.getBaseColor().toString();
			}
			patternlist.add(basecolor);
			int x = data.numberOfPatterns();
			for (int i = 0; i < x; i++) {
				String pattern = data.getPattern(i).getColor().toString() + " "
						+ data.getPattern(i).getPattern().toString();
				patternlist.add(pattern);
			}
			// save it TownBanners.towns.put(townname, patternlist);
			faction.setBanner(patternlist);
			sender.sendMessage(ChatColor.AQUA
					+ "Your factions banner was successfully set.");
			return;
		}
		me.sendMessage(ChatColor.RED + "Please hold a banner in your hand!");
	}
}
