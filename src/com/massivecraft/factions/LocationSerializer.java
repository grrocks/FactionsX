package com.massivecraft.factions;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSerializer {

	public static String getSerializedLocation(Location loc) { // Converts
																// location ->
																// String
		return (int) loc.getX() + ";" + (int) loc.getY() + ";"
				+ (int) loc.getZ() + ";" + loc.getWorld().getUID();
		// feel free to use something to split them other than semicolons (Don't
		// use periods or numbers)
	}

	public static Location getDeserializedLocation(String s) {// Converts String
																// -> Location
		String[] parts = s.split(";"); // If you changed the semicolon you must
										// change it here too
		double x = Double.parseDouble(parts[0]);
		double y = Double.parseDouble(parts[1]);
		double z = Double.parseDouble(parts[2]);
		UUID u = UUID.fromString(parts[3]);
		World w = Bukkit.getServer().getWorld(u);
		return new Location(w, x, y, z); // can return null if the world no
											// longer exists
	}
}