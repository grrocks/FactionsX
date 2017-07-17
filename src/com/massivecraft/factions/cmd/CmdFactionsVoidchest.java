package com.massivecraft.factions.cmd;

import java.util.ArrayList;
import java.util.List;

import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.massivecraft.factions.entity.MConf;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementIsPlayer;
import com.massivecraft.massivecore.money.MoneyMixinVault;

public class CmdFactionsVoidchest extends FactionsCommand {

	public CmdFactionsVoidchest() {
		// Aliases
		this.addAliases("voidchest");

		this.addRequirements(RequirementIsPlayer.get());
		this.addRequirements(RequirementHasPerm.get("factions.voidchest"));
	}

	@Override
	public void perform() throws MassiveException {
		if (new MoneyMixinVault().getEconomy().has(me.getPlayer(),
				MConf.get().voidchestCost)) {
			if (me.getInventory().firstEmpty() == -1) {
				me.sendMessage(ChatColor.RED
						+ "Please leave 1 inventory space for a void chest!");
				return;
			}
			new MoneyMixinVault().getEconomy().withdrawPlayer(me.getPlayer(),
					MConf.get().voidchestCost);

			ItemStack is = new ItemStack(Material.CHEST);
			ItemMeta im = is.getItemMeta();

			im.setDisplayName(ChatColor.DARK_AQUA + "Void Chest");

			List<String> lore = new ArrayList<String>();

			lore.add(ChatColor.DARK_AQUA + "This is a void chest!");

			im.setLore(lore);

			is.setItemMeta(im);

			me.getInventory().addItem(is);

			me.sendMessage(ChatColor.AQUA + "You have received a void chest!");
		} else
			me.sendMessage(ChatColor.RED + "You need $"
					+ MConf.get().voidchestCost + " to get a void chest");
	}

}
