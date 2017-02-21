package com.massivecraft.factions.cmd;

import org.bukkit.ChatColor;

import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementIsPlayer;
import com.massivecraft.massivecore.money.MoneyMixinVault;

public class CmdFactionsGetbanner extends FactionsCommand {
	public CmdFactionsGetbanner() {
		// Aliases
		this.addAliases("getbanner", "banner");

		// Parameters
		this.addParameter(TypeFaction.get(), "faction", "you");
		this.addRequirements(RequirementIsPlayer.get());
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@SuppressWarnings("deprecation")
	@Override
	public void perform() throws MassiveException {
		Faction faction = readArg(msenderFaction);

		if (faction.isNone()) {
			me.sendMessage(ChatColor.RED
					+ "Please join a faction to use this command!");
			return;
		}

		if (!MPerm.getPermGetbanner().has(msender, faction, true))
			return;
		if (!faction.hasBanner()) {
			me.sendMessage(faction.getColorTo(msender) + faction.getName()
					+ ChatColor.YELLOW + " does not have a banner set");
			return;
		}

		if (new MoneyMixinVault().getEconomy().has(me.getName(),
				MConf.get().costForBanner)) {
			if (me.getInventory().firstEmpty() == -1) {
				me.sendMessage(ChatColor.RED
						+ "Please leave 1 inventory space for the banner!");
				return;
			}
			new MoneyMixinVault().getEconomy().withdrawPlayer(me.getName(),
					MConf.get().costForBanner);
			me.getInventory().addItem(faction.getBanner());
			me.sendMessage(ChatColor.AQUA + "You have received a banner!");
		} else
			me.sendMessage(ChatColor.RED + "You need $"
					+ MConf.get().costForBanner + " to get a banner");
	}
}
