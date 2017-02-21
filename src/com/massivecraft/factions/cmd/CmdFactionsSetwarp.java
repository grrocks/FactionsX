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
import com.massivecraft.massivecore.money.MoneyMixinVault;
import com.massivecraft.massivecore.ps.PS;

public class CmdFactionsSetwarp extends FactionsCommand {

	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //

	public CmdFactionsSetwarp() {
		// Aliases
		this.addAliases("setwarp");

		// Parameters
		this.addParameter(TypeString.get(), "name");
		this.addParameter(TypeFaction.get(), "faction", "you");
		this.addParameter(null, TypeString.get(), "password");

		// Requirements
		this.addRequirements(RequirementHasPerm.get(Perm.SETWARP.node));
		this.addRequirements(RequirementIsPlayer.get());
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@SuppressWarnings("deprecation")
	@Override
	public void perform() throws MassiveException {
		// Args
		Faction faction = readArgAt(1, msenderFaction);

		String password = readArgAt(2, null);

		PS newHome = PS.valueOf(me.getLocation());

		// MPerm
		if (!MPerm.getPermSetwarp().has(msender, faction, true))
			return;

		// Verify
		if (!msender.isOverriding() && !faction.isValidWarp(newHome)) {
			msender.msg("<b>Sorry, your faction warps can only be set inside your own claimed territory.");
			return;
		}

		if (faction.getAllWarps().size() >= MConf.get().amountOfWarps) {
			msender.message(ChatColor.RED
					+ "You cannot set anymore warps for this faction!");
			return;
		}

		if (!new MoneyMixinVault().getEconomy().has(me.getName(),
				MConf.get().costPerWarp)) {
			me.sendMessage(ChatColor.RED + "You need $"
					+ MConf.get().costPerWarp + " to set a warp");
			return;
		}

		new MoneyMixinVault().getEconomy().withdrawPlayer(me.getName(),
				MConf.get().costPerWarp);
		faction.setWarp(newHome, (String) readArgAt(0), password);

		// Inform
		faction.msg("%s<i> set a warp for your faction",
				msender.describeTo(msenderFaction, true));
		if (faction != msenderFaction) {
			msender.msg("<i>You have set the warp for "
					+ faction.getName(msender) + "<i>.");
		}
	}
}
