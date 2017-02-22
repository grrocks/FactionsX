package com.massivecraft.factions.cmd;

import org.bukkit.ChatColor;

import com.massivecraft.factions.engine.EngineMain;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.type.primitive.TypeBoolean;

public class CmdFactionsStealth extends FactionsCommand {

    public CmdFactionsStealth() {
        this.addAliases("stealth");

        this.addParameter(TypeBoolean.getYes(), "yes/no");
    }

    @Override
    public void perform() throws MassiveException {
        Boolean value = this.readArg();

        if (value) {
            if (EngineMain.stealthPlayer.contains(me.getUniqueId())) {
                me.sendMessage(ChatColor.RED
                        + "You are already in stealth mode!");
            } else {
                me.sendMessage(ChatColor.AQUA + "You are now in stealth mode!");
                EngineMain.stealthPlayer.add(me.getUniqueId());
            }
        } else {
            if (!EngineMain.stealthPlayer.contains(me.getUniqueId())) {
                me.sendMessage(ChatColor.RED
                        + "You are already out of stealth mode!");
            } else {
                me.sendMessage(ChatColor.AQUA
                        + "You are now out of stealth mode!");
                EngineMain.stealthPlayer.remove(me.getUniqueId());
            }
        }
    }

}