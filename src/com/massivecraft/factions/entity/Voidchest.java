package com.massivecraft.factions.entity;

import org.bukkit.Location;

import com.massivecraft.massivecore.ps.PS;

public class Voidchest {

	private Location loc;
	private boolean active;

	public Voidchest(Location loc) {
		this.loc = loc.clone();
		active = true;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Location getLocation() {
		return loc;
	}

	public PS getPS() {
		return PS.valueOf(getLocation());
	}
}
