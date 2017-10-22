package com.massivecraft.factions.engine;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

import mkremins.fanciful.FancyMessage;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.factions.BlockRel;
import com.massivecraft.factions.Const;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.PlayerRoleComparator;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.entity.Board;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.factions.entity.Voidchest;
import com.massivecraft.factions.event.EventFactionsChunkChangeType;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsFactionShowAsync;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.massivecraft.factions.event.EventFactionsMembershipChange.MembershipChangeReason;
import com.massivecraft.factions.event.EventFactionsPowerChange;
import com.massivecraft.factions.event.EventFactionsPowerChange.PowerChangeReason;
import com.massivecraft.factions.event.EventFactionsPvpDisallowed;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.spigot.SpigotFeatures;
import com.massivecraft.factions.util.VisualizeUtil;
import com.massivecraft.massivecore.EngineAbstract;
import com.massivecraft.massivecore.PriorityLines;
import com.massivecraft.massivecore.collections.MassiveSet;
import com.massivecraft.massivecore.event.EventMassiveCorePlayerLeave;
import com.massivecraft.massivecore.mixin.Mixin;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.MUtil;
import com.massivecraft.massivecore.util.PlayerUtil;
import com.massivecraft.massivecore.util.TimeDiffUtil;
import com.massivecraft.massivecore.util.TimeUnit;
import com.massivecraft.massivecore.util.Txt;

public class EngineMain extends EngineAbstract {
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //

	private static EngineMain i = new EngineMain();

	private ArrayList<UUID> playersFlying = new ArrayList<UUID>();
	private ArrayList<UUID> playersFlyingAOE = new ArrayList<UUID>();
	private ArrayList<UUID> noFallDamagePlayers = new ArrayList<UUID>();
	public static ArrayList<UUID> stealthPlayer = new ArrayList<UUID>();
	public static Map<String, Location> assistFactions = new HashMap<String, Location>();
	public static ArrayList<Location> removeBanners = new ArrayList<Location>();

	public static EngineMain get() {
		return i;
	}

	public EngineMain() {
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public Plugin getPlugin() {
		return Factions.get();
	}

	// -------------------------------------------- //
	// CONSTANTS
	// -------------------------------------------- //

	public static final Set<SpawnReason> NATURAL_SPAWN_REASONS = new MassiveSet<SpawnReason>(
			SpawnReason.NATURAL, SpawnReason.JOCKEY, SpawnReason.CHUNK_GEN,
			SpawnReason.OCELOT_BABY, SpawnReason.NETHER_PORTAL,
			SpawnReason.MOUNT);

	// -------------------------------------------- //
	// FACTION SHOW
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFactionShow(EventFactionsFactionShowAsync event) {
		final int tableCols = 4;
		final CommandSender sender = event.getSender();
		final MPlayer mplayer = event.getMPlayer();
		final Faction faction = event.getFaction();
		final boolean normal = faction.isNormal();
		final Map<String, PriorityLines> idPriorityLiness = event
				.getIdPriorityLiness();
		final boolean peaceful = faction.getFlag(MFlag.getFlagPeaceful());

		// ID
		if (mplayer.isOverriding()) {
			show(idPriorityLiness, Const.SHOW_ID_FACTION_ID,
					Const.SHOW_PRIORITY_FACTION_ID, "ID", faction.getId());
		}

		// DESCRIPTION
		show(idPriorityLiness, Const.SHOW_ID_FACTION_DESCRIPTION,
				Const.SHOW_PRIORITY_FACTION_DESCRIPTION, "Description",
				faction.getDescription());

		// SECTION: NORMAL
		if (normal) {
			// AGE
			long ageMillis = faction.getCreatedAtMillis()
					- System.currentTimeMillis();
			LinkedHashMap<TimeUnit, Long> ageUnitcounts = TimeDiffUtil.limit(
					TimeDiffUtil.unitcounts(ageMillis,
							TimeUnit.getAllButMillis()), 3);
			String ageDesc = TimeDiffUtil
					.formatedVerboose(ageUnitcounts, "<i>");
			show(idPriorityLiness, Const.SHOW_ID_FACTION_AGE,
					Const.SHOW_PRIORITY_FACTION_AGE, "Age", ageDesc);

			// FLAGS
			// We display all editable and non default ones. The rest we skip.
			List<String> flagDescs = new LinkedList<String>();
			for (Entry<MFlag, Boolean> entry : faction.getFlags().entrySet()) {
				final MFlag mflag = entry.getKey();
				if (mflag == null)
					continue;

				final Boolean value = entry.getValue();
				if (value == null)
					continue;

				if (!mflag.isInteresting(value))
					continue;

				String flagDesc = Txt.parse(value ? "<g>" : "<b>")
						+ mflag.getName();
				flagDescs.add(flagDesc);
			}
			String flagsDesc = Txt.parse("<silver><italic>default");
			if (!flagDescs.isEmpty()) {
				flagsDesc = Txt.implode(flagDescs, Txt.parse(" <i>| "));
			}
			show(idPriorityLiness, Const.SHOW_ID_FACTION_FLAGS,
					Const.SHOW_PRIORITY_FACTION_FLAGS, "Flags", flagsDesc);

			// POWER
			double powerBoost = faction.getPowerBoost();
			String boost = (powerBoost == 0.0) ? ""
					: (powerBoost > 0.0 ? " (bonus: " : " (penalty: ")
							+ powerBoost + ")";
			String powerDesc = Txt.parse("%d/%d/%d%s", faction.getLandCount(),
					faction.getPowerRounded(), faction.getPowerMaxRounded(),
					boost);
			show(idPriorityLiness, Const.SHOW_ID_FACTION_POWER,
					Const.SHOW_PRIORITY_FACTION_POWER,
					"Land / Power / Maxpower", powerDesc);

			// SECTION: ECON
			if (Econ.isEnabled()) {
				// LANDVALUES
				List<String> landvalueLines = new LinkedList<String>();
				long landCount = faction.getLandCount();
				for (EventFactionsChunkChangeType type : EventFactionsChunkChangeType
						.values()) {
					Double money = MConf.get().econChunkCost.get(type);
					if (money == null)
						continue;
					if (money == 0)
						continue;
					money *= landCount;

					String word = "Cost";
					if (money <= 0) {
						word = "Reward";
						money *= -1;
					}

					String key = Txt.parse("Total Land %s %s", type.toString()
							.toLowerCase(), word);
					String value = Txt.parse("<h>%s", Money.format(money));
					String line = show(key, value);
					landvalueLines.add(line);
				}
				idPriorityLiness.put(Const.SHOW_ID_FACTION_LANDVALUES,
						new PriorityLines(
								Const.SHOW_PRIORITY_FACTION_LANDVALUES,
								landvalueLines));

				// BANK
				if (MConf.get().bankEnabled) {
					double bank = Money.get(faction);
					String bankDesc = Txt.parse("<h>%s",
							Money.format(bank, true));
					show(idPriorityLiness, Const.SHOW_ID_FACTION_BANK,
							Const.SHOW_PRIORITY_FACTION_BANK, "Bank", bankDesc);
				}
			}
		}

		// RELATIONS
		List<String> relationLines = new ArrayList<String>();
		String none = Txt.parse("<silver><italic>none");
		String everyone = MConf.get().colorTruce.toString()
				+ Txt.parse("<italic>*EVERYONE*");
		Set<Rel> rels = EnumSet.of(Rel.TRUCE, Rel.ALLY, Rel.ENEMY);
		Map<Rel, List<String>> relNames = faction.getRelationNames(mplayer,
				rels, true);
		for (Entry<Rel, List<String>> entry : relNames.entrySet()) {
			Rel rel = entry.getKey();
			List<String> names = entry.getValue();
			String header = Txt
					.parse("<a>Relation %s%s<a> (%d):", rel.getColor()
							.toString(), Txt.getNicedEnum(rel), names.size());
			relationLines.add(header);
			if (rel == Rel.TRUCE && peaceful) {
				relationLines.add(everyone);
			} else {
				if (names.isEmpty()) {
					relationLines.add(none);
				} else {
					relationLines.addAll(table(names, tableCols));
				}
			}
		}
		idPriorityLiness.put(Const.SHOW_ID_FACTION_RELATIONS,
				new PriorityLines(Const.SHOW_PRIORITY_FACTION_RELATIONS,
						relationLines));

		// FOLLOWERS
		List<String> followerLines = new ArrayList<String>();

		List<String> followerNamesOnline = new ArrayList<String>();
		List<String> followerNamesOffline = new ArrayList<String>();

		List<MPlayer> followers = faction.getMPlayers();
		Collections.sort(followers, PlayerRoleComparator.get());
		for (MPlayer follower : followers) {
			if (follower.isOnline(sender)) {
				followerNamesOnline.add(follower.getNameAndTitle(mplayer));
			} else if (normal) {
				// For the non-faction we skip the offline members since they
				// are far to many (infinite almost)
				followerNamesOffline.add(follower.getNameAndTitle(mplayer));
			}
		}

		String headerOnline = Txt.parse("<a>Followers Online (%s):",
				followerNamesOnline.size());
		followerLines.add(headerOnline);
		if (followerNamesOnline.isEmpty()) {
			followerLines.add(none);
		} else {
			followerLines.addAll(table(followerNamesOnline, tableCols));
		}

		if (normal) {
			String headerOffline = Txt.parse("<a>Followers Offline (%s):",
					followerNamesOffline.size());
			followerLines.add(headerOffline);
			if (followerNamesOffline.isEmpty()) {
				followerLines.add(none);
			} else {
				followerLines.addAll(table(followerNamesOffline, tableCols));
			}
		}
		idPriorityLiness.put(Const.SHOW_ID_FACTION_FOLLOWERS,
				new PriorityLines(Const.SHOW_PRIORITY_FACTION_FOLLOWERS,
						followerLines));
	}

	public static String show(String key, String value) {
		return Txt.parse("<a>%s: <i>%s", key, value);
	}

	public static PriorityLines show(int priority, String key, String value) {
		return new PriorityLines(priority, show(key, value));
	}

	public static void show(Map<String, PriorityLines> idPriorityLiness,
			String id, int priority, String key, String value) {
		idPriorityLiness.put(id, show(priority, key, value));
	}

	public static List<String> table(List<String> strings, int cols) {
		List<String> ret = new ArrayList<String>();

		StringBuilder row = new StringBuilder();
		int count = 0;

		Iterator<String> iter = strings.iterator();
		while (iter.hasNext()) {
			String string = iter.next();
			row.append(string);
			count++;

			if (iter.hasNext() && count != cols) {
				row.append(Txt.parse(" <i>| "));
			} else {
				ret.add(row.toString());
				row = new StringBuilder();
				count = 0;
			}
		}

		return ret;
	}

	// -------------------------------------------- //
	// UPDATE LAST ACTIVITY
	// -------------------------------------------- //

	public static void updateLastActivity(CommandSender sender) {
		if (sender == null)
			throw new RuntimeException("sender");
		if (MUtil.isntSender(sender))
			return;

		MPlayer mplayer = MPlayer.get(sender);
		mplayer.setLastActivityMillis();
	}

	public static void updateLastActivityInFactionLand(CommandSender sender) {
		if (sender == null)
			throw new RuntimeException("sender");
		if (MUtil.isntSender(sender))
			return;

		MPlayer mplayer = MPlayer.get(sender);
		mplayer.setLastActivityMillisInLand();
	}

	public static void updateLastActivitySoon(final CommandSender sender) {
		if (sender == null)
			throw new RuntimeException("sender");
		Bukkit.getScheduler().scheduleSyncDelayedTask(Factions.get(),
				new Runnable() {
					@Override
					public void run() {
						updateLastActivity(sender);
					}
				});
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.LOWEST)
	public void updateLastActivity(PlayerJoinEvent event) {
		// During the join event itself we want to be able to reach the old
		// data.
		// That is also the way the underlying fallback Mixin system does it and
		// we do it that way for the sake of symmetry.
		// For that reason we wait till the next tick with updating the value.
		updateLastActivitySoon(event.getPlayer());
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.LOWEST)
	public void updateLastActivity(EventMassiveCorePlayerLeave event) {
		// Here we do however update immediately.
		// The player data should be fully updated before leaving the server.
		updateLastActivity(event.getPlayer());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		final MPlayer mplayer = MPlayer.get(e.getPlayer());
		final Faction faction = mplayer.getFaction();
		updateLastActivityInFactionLand(e.getPlayer());
		String message = MConf.get().playerQuitMessage.replace("%PLAYER%",
				mplayer.getNameAndTitle(mplayer));

		for (Player p : faction.getOnlinePlayers()) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
		if (playersFlying.contains(e.getPlayer().getUniqueId())) {
			playersFlying.remove(e.getPlayer().getUniqueId());
		}
		if (playersFlyingAOE.contains(e.getPlayer().getUniqueId())) {
			playersFlyingAOE.remove(e.getPlayer().getUniqueId());
		}
		if (noFallDamagePlayers.contains(e.getPlayer().getUniqueId())) {
			noFallDamagePlayers.remove(e.getPlayer().getUniqueId());
		}
	}

	// VoidChest
	@EventHandler(priority = EventPriority.MONITOR)
	public void hopperMoveItem(final InventoryMoveItemEvent e) {
		if (e.getInitiator() == null || e.getSource() == null
				|| e.getItem() == null)
			return;
		if (e.getInitiator().getType() != InventoryType.HOPPER){
			if(e.getInitiator().getType() == InventoryType.CHEST
					&& e.getInitiator().getTitle().equals(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST"))
				e.setCancelled(true);
			return;
		}

		if (!e.getDestination().getTitle()
				.equals(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST"))
			return;

		if (!MConf.get().voidChestItemsSellPricePerItem.containsKey(e.getItem()
				.getType())) {
			new BukkitRunnable() {

				@Override
				public void run() {
					e.getDestination().clear();
				}
			}.runTaskLater(Factions.get(), 1);
			return;
		}

		Location loc = getLocation(e.getDestination());

		double amount = MConf.get().voidChestItemsSellPricePerItem.get(e
				.getItem().getType()) * e.getItem().getAmount();

		Faction fac = Board.get(PS.valueOf(loc)).getFactionAt(PS.valueOf(loc));

		new BukkitRunnable() {

			@Override
			public void run() {
				e.getDestination().clear();
			}
		}.runTaskLater(Factions.get(), 1);

		Econ.modifyMoney(fac, amount, "");
	}

	private Location getLocation(Inventory inventory) {
		InventoryHolder holder = inventory.getHolder();
		if (holder != null) {
			if (holder instanceof Chest)
				return ((Chest) holder).getLocation();
			else if (holder instanceof BlockState) {
				return ((BlockState) holder).getLocation();
			}
		}
		return null;
	}

	// -------------------------------------------- //
	// MOTD
	// -------------------------------------------- //

	public static void motd(PlayerJoinEvent event, EventPriority currentPriority) {
		// Gather info ...
		final Player player = event.getPlayer();
		if (MUtil.isntPlayer(player))
			return;
		final MPlayer mplayer = MPlayer.get(player);
		final Faction faction = mplayer.getFaction();

		if (currentPriority != MConf.get().motdPriority)
			return;

		Faction facAt = Board.get(PS.valueOf(player)).getFactionAt(
				PS.valueOf(player));

		if (!MPerm.getPermLogin().has(mplayer, facAt, false)) {
			if (System.currentTimeMillis() > mplayer.getLastActivityMillisInLand()) {
				Bukkit.getServer().dispatchCommand(
						Bukkit.getServer().getConsoleSender(),
						"spawn " + mplayer.getPlayer().getName());
				Bukkit.getServer().dispatchCommand(
						Bukkit.getServer().getConsoleSender(),
						"spawn " + mplayer.getPlayer().getName());
				mplayer.message(ChatColor.RED + "Checking for logout exploits..");
				Bukkit.getServer().dispatchCommand(
						Bukkit.getServer().getConsoleSender(),
						"spawn " + mplayer.getPlayer().getName());
				mplayer.message(ChatColor.RED + "You were sent to spawn!");
			}
		}

		String message = MConf.get().playerJoinMessage.replace("%PLAYER%",
				mplayer.getNameAndTitle(mplayer));

		for (Player p : faction.getOnlinePlayers()) {
			if (p == player)
				continue;
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}

		// ... if there is a motd ...
		if (!faction.hasMotd())
			return;

		// ... and this is the priority we are supposed to act on ...

		// ... and this is an actual join ...
		if (!Mixin.isActualJoin(event))
			return;

		// ... then prepare the messages ...
		final List<String> messages = faction.getMotdMessages();

		// ... and send to the player.
		if (MConf.get().motdDelayTicks < 0) {
			Mixin.messageOne(player, messages);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Factions.get(),
					new Runnable() {
						@Override
						public void run() {
							Mixin.messageOne(player, messages);
						}
					}, MConf.get().motdDelayTicks);
		}
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.LOWEST)
	public void motdLowest(PlayerJoinEvent event) {
		motd(event, EventPriority.LOWEST);
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.LOW)
	public void motdLow(PlayerJoinEvent event) {
		motd(event, EventPriority.LOW);
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.NORMAL)
	public void motdNormal(PlayerJoinEvent event) {
		motd(event, EventPriority.NORMAL);
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.HIGH)
	public void motdHigh(PlayerJoinEvent event) {
		motd(event, EventPriority.HIGH);
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.HIGHEST)
	public void motdHighest(PlayerJoinEvent event) {
		motd(event, EventPriority.HIGHEST);
	}

	// Can't be cancelled
	@EventHandler(priority = EventPriority.MONITOR)
	public void motdMonitor(PlayerJoinEvent event) {
		motd(event, EventPriority.MONITOR);
	}

	@EventHandler
	public void onPistonPush(BlockPistonExtendEvent e) {
		for (Block block : e.getBlocks()) {
			if (block.getType() == Material.SAND
					|| block.getType() == Material.GRAVEL
					|| block.getType() == Material.ANVIL) {
				Location loc = block.getLocation();
				for (int i = 0; i < MConf.get().maxSand; i++) {
					Location locPlus = loc.add(0, 1, 0);
					if (locPlus.getBlock().getType() == Material.SAND
							|| locPlus.getBlock().getType() == Material.GRAVEL) {
						if (i == MConf.get().maxSand - 1) {
							e.setCancelled(true);
							return;
						}
					} else
						break;
				}
			}
		}
	}

	@EventHandler
	public void onPistonRet(BlockPistonRetractEvent e) {
		for (Block block : e.getBlocks()) {
			if (block.getType() == Material.SAND
					|| block.getType() == Material.GRAVEL
					|| block.getType() == Material.ANVIL) {
				Location loc = block.getLocation();
				for (int i = 0; i < MConf.get().maxSand; i++) {
					Location locPlus = loc.add(0, 1, 0);
					if (locPlus.getBlock().getType() == Material.SAND
							|| locPlus.getBlock().getType() == Material.GRAVEL) {
						if (i == MConf.get().maxSand - 1) {
							e.setCancelled(true);
							return;
						}
					} else
						break;
				}
			}
		}
		Location loc = e.getBlock().getLocation();
		Location reLoc = e.getBlock().getLocation();
		switch (e.getDirection()) {
		case EAST:
			loc.add(1, 0, 0);
			reLoc.subtract(2, 0, 0);
			break;
		case NORTH:
			loc.subtract(0, 0, 1);
			reLoc.add(0, 0, 2);
			break;
		case SOUTH:
			loc.add(0, 0, 1);
			reLoc.subtract(0, 0, 2);
			break;
		case WEST:
			loc.subtract(1, 0, 0);
			reLoc.add(2, 0, 0);
			break;
		default:
			break;
		}
		loc.add(0, 1, 0);
		Block block = loc.getBlock();
		if (block.getType() == Material.SAND
				|| block.getType() == Material.GRAVEL
				|| block.getType() == Material.ANVIL) {
			for (int i = 0; i < MConf.get().maxNormCompSand; i++) {
				Location locPlus = loc.add(0, 1, 0);
				Block blockPlus = locPlus.getBlock();
				if (blockPlus.getType() == Material.SAND
						|| blockPlus.getType() == Material.GRAVEL
						|| blockPlus.getType() == Material.ANVIL) {
					if (i == MConf.get().maxNormCompSand - 1) {
						e.setCancelled(true);
						return;
					}
				} else
					break;
			}
		}
		reLoc.add(0, 1, 0);
		if (reLoc.getBlock().getType() == Material.SAND
				|| reLoc.getBlock().getType() == Material.GRAVEL
				|| reLoc.getBlock().getType() == Material.ANVIL) {
			for (int i = 0; i < MConf.get().maxNormCompSand; i++) {
				Location locPlus = reLoc.add(0, 1, 0);
				Block blockPlus = locPlus.getBlock();
				if (blockPlus.getType() == Material.SAND
						|| blockPlus.getType() == Material.GRAVEL
						|| blockPlus.getType() == Material.ANVIL) {
					if (i == MConf.get().maxNormCompSand - 1) {
						e.setCancelled(true);
						return;
					}
				} else
					break;
			}
		}
	}

	// -------------------------------------------- //
	// CHUNK CHANGE: DETECT
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onChunksChange(EventFactionsChunksChange event) {
		// For security reasons we block the chunk change on any error since an
		// error might block security checks from happening.
		try {
			onChunksChangeInner(event);
		} catch (Throwable throwable) {
			event.setCancelled(true);
			throwable.printStackTrace();
		}
	}

	public void onChunksChangeInner(EventFactionsChunksChange event) {
		// Args
		final MPlayer mplayer = event.getMPlayer();
		final Faction newFaction = event.getNewFaction();
		final Map<Faction, Set<PS>> currentFactionChunks = event
				.getOldFactionChunks();
		final Set<Faction> currentFactions = currentFactionChunks.keySet();
		final Set<PS> chunks = event.getChunks();

		// Override Mode? Sure!
		if (mplayer.isOverriding())
			return;

		// CALC: Is there at least one normal faction among the current ones?
		boolean currentFactionsContainsAtLeastOneNormal = false;
		for (Faction currentFaction : currentFactions) {
			if (currentFaction.isNormal()) {
				currentFactionsContainsAtLeastOneNormal = true;
				break;
			}
		}

		// If the new faction is normal (not wilderness/none), meaning if we are
		// claiming for a faction ...
		if (newFaction.isNormal()) {
			// ... ensure claiming is enabled for the worlds of all chunks ...
			for (PS chunk : chunks) {
				String worldId = chunk.getWorld();
				if (!MConf.get().worldsClaimingEnabled.contains(worldId)) {
					String worldName = Mixin.getWorldDisplayName(worldId);
					mplayer.msg("<b>Land claiming is disabled in <h>%s<b>.",
							worldName);
					event.setCancelled(true);
					return;
				}
			}

			// ... ensure we have permission to alter the territory of the new
			// faction ...
			if (!MPerm.getPermTerritory().has(mplayer, newFaction, true)) {
				// NOTE: No need to send a message. We send message from the
				// permission check itself.
				event.setCancelled(true);
				return;
			}

			// ... ensure the new faction has enough players to claim ...
			if (newFaction.getMPlayers().size() < MConf.get().claimsRequireMinFactionMembers) {
				mplayer.msg(
						"<b>Factions must have at least <h>%s<b> members to claim land.",
						MConf.get().claimsRequireMinFactionMembers);
				event.setCancelled(true);
				return;
			}

			// ... ensure the claim would not bypass the global max limit ...
			int ownedLand = newFaction.getLandCount();
			if (MConf.get().claimedLandsMax != 0
					&& ownedLand + chunks.size() > MConf.get().claimedLandsMax
					&& !newFaction.getFlag(MFlag.getFlagInfpower())) {
				mplayer.msg("<b>Limit reached. You can't claim more land.");
				event.setCancelled(true);
				return;
			}

			// ... ensure the claim would not bypass the faction power ...
			if (ownedLand + chunks.size() > newFaction.getPowerRounded()) {
				mplayer.msg("<b>You don't have enough power to claim that land.");
				event.setCancelled(true);
				return;
			}

			// ... ensure the claim would not violate distance to neighbors ...
			// HOW: Calculate the factions nearby, excluding the chunks
			// themselves, the faction itself and the wilderness faction.
			// HOW: The chunks themselves will be handled in the
			// "if (oldFaction.isNormal())" section below.
			Set<PS> nearbyChunks = BoardColl.getNearbyChunks(chunks,
					MConf.get().claimMinimumChunksDistanceToOthers);
			nearbyChunks.removeAll(chunks);
			Set<Faction> nearbyFactions = BoardColl
					.getDistinctFactions(nearbyChunks);
			nearbyFactions.remove(FactionColl.get().getNone());
			nearbyFactions.remove(newFaction);
			// HOW: Next we check if the new faction has permission to claim
			// nearby the nearby factions.
			MPerm claimnear = MPerm.getPermClaimnear();
			for (Faction nearbyFaction : nearbyFactions) {
				if (claimnear.has(newFaction, nearbyFaction))
					continue;
				mplayer.message(claimnear.createDeniedMessage(mplayer,
						nearbyFaction));
				event.setCancelled(true);
				return;
			}

			// ... ensure claims are properly connected ...
			if (
			// If claims must be connected ...
			MConf.get().claimsMustBeConnected
			// ... and this faction already has claimed something on this map
			// (meaning it's not their first claim) ...
					&& newFaction.getLandCountInWorld(chunks.iterator().next()
							.getWorld()) > 0
					// ... and none of the chunks are connected to an already
					// claimed chunk for the faction ...
					&& !BoardColl.get().isAnyConnectedPs(chunks, newFaction)
					// ... and either claims must always be connected or there
					// is at least one normal faction among the old factions ...
					&& (!MConf.get().claimsCanBeUnconnectedIfOwnedByOtherFaction || currentFactionsContainsAtLeastOneNormal)) {
				if (MConf.get().claimsCanBeUnconnectedIfOwnedByOtherFaction) {
					mplayer.msg("<b>You can only claim additional land which is connected to your first claim or controlled by another faction!");
				} else {
					mplayer.msg("<b>You can only claim additional land which is connected to your first claim!");
				}
				event.setCancelled(true);
				return;
			}
		}

		// For each of the old factions ...
		for (Entry<Faction, Set<PS>> entry : currentFactionChunks.entrySet()) {
			Faction oldFaction = entry.getKey();
			Set<PS> oldChunks = entry.getValue();

			// ... that is an actual faction ...
			if (oldFaction.isNone())
				continue;

			// ... for which the mplayer lacks permission ...
			if (MPerm.getPermTerritory().has(mplayer, oldFaction, false))
				continue;

			// ... consider all reasons to forbid "overclaiming/warclaiming" ...

			// ... claiming from others may be forbidden ...
			if (!MConf.get().claimingFromOthersAllowed) {
				mplayer.msg("<b>You may not claim land from others.");
				event.setCancelled(true);
				return;
			}

			// ... the relation may forbid ...
			if (oldFaction.getRelationTo(newFaction).isAtLeast(Rel.TRUCE)) {
				event.setCancelled(true);
				return;
			}

			// ... the old faction might not be inflated enough ...
			if (oldFaction.getPowerRounded() > oldFaction.getLandCount()
					- oldChunks.size()) {
				mplayer.msg(
						"%s<i> owns this land and is strong enough to keep it.",
						oldFaction.getName(mplayer));
				event.setCancelled(true);
				return;
			}

			// ... and you might be trying to claim without starting at the
			// border ...
			if (!BoardColl.get().isAnyBorderPs(chunks)) {
				mplayer.msg("<b>You must start claiming land at the border of the territory.");
				event.setCancelled(true);
				return;
			}

			// ... otherwise you may claim from this old faction even though you
			// lack explicit permission from them.
		}
	}

	// -------------------------------------------- //
	// FLY
	// -------------------------------------------- //

	// FLY
	public void onPlayerMove(PlayerMoveEvent e, Faction fac, MPlayer mp) {
		Location loc = e.getTo();
		Player p = e.getPlayer();
		Faction faction = mp.getFaction();

		Rel rel = fac.getRelationTo(faction);

		int howCloseToAOE = MConf.get().flyAOEDistance;
		int enemyX = MConf.get().flyCancelDistanceX;
		int enemyY = MConf.get().flyCancelDistanceY;
		int enemyZ = MConf.get().flyCancelDistanceZ;

		if (!canGoInTerritory(rel, mp)
				&& playersFlying.contains(p.getUniqueId())
				&& p.getGameMode() != GameMode.CREATIVE) {
			takeAwayFly(p);
			return;
		}
		if (fac.isNone() && playersFlying.contains(p.getUniqueId())
				&& p.getGameMode() != GameMode.CREATIVE) {
			if(!p.hasPermission("factions.fly.bypassland"))
			takeAwayFly(p);
			return;
		}
		if (p.getLocation().getY() > MConf.get().flyMaxHeight
				&& playersFlying.contains(p.getUniqueId())
				&& p.getGameMode() != GameMode.CREATIVE) {
			takeAwayFly(p);
			return;
		}
		if (faction.isNone()) {
			if (playersFlying.contains(p.getUniqueId())
					&& p.getGameMode() != GameMode.CREATIVE) {
				takeAwayFly(p);
			}
			return;
		}

		Collection<Entity> closeEntities = loc.getWorld().getNearbyEntities(
				loc, enemyX, enemyY, enemyZ);
		Collection<Entity> closeAOE = loc.getWorld().getNearbyEntities(loc,
				howCloseToAOE, 256, howCloseToAOE);

		boolean closeEnemy = false;
		for (Entity entity : closeEntities) {
			if (!(entity instanceof Player))
				continue;
			Player player = (Player) entity;

			if(player.getName().toLowerCase().startsWith("pvplogger"))
				continue;

			Faction opFaction = MPlayer.get(player).getFaction();
			Rel relation = opFaction.getRelationTo(faction);
			if (relation.getValue() >= Rel.TRUCE.getValue()
					|| player.hasPermission("factions.fly.bypass")) {
				continue;
			} else
				closeEnemy = true;
			if (playersFlying.contains(player.getUniqueId())
					& !p.hasPermission("factions.fly.bypass")
					& !stealthPlayer.contains(p.getUniqueId())
					&& player.getGameMode() != GameMode.CREATIVE) {
				takeAwayFly(player);
				return;
			}
			if (playersFlying.contains(p.getUniqueId())
					& !player.hasPermission("factions.fly.bypass")
					& !stealthPlayer.contains(player.getUniqueId())
					&& p.getGameMode() != GameMode.CREATIVE) {
				takeAwayFly(p);
				return;
			}
		}
		if (!closeEnemy && p.hasPermission("factions.fly")
				&& (canGoInTerritory(rel, mp))
				& !playersFlying.contains(p.getUniqueId())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&',
					MConf.get().flyEnableMessage));
			p.setAllowFlight(true);
			playersFlying.add(p.getUniqueId());
			noFallDamagePlayers.add(p.getUniqueId());
			return;
		}
		boolean foundAOE = false;
		for (Entity entity : closeAOE) {
			if (!(entity instanceof Player))
				continue;
			Player player = (Player) entity;

			if(player.getName().toLowerCase().startsWith("pvplogger"))
				continue;

			Faction opFaction = MPlayer.get(player).getFaction();
			Rel relation = opFaction.getRelationTo(faction);
			if (relation.getValue() >= MConf.get().flyAOERelationMin.getValue()
					&& player.hasPermission("factions.fly.aoe")) {
				if (!closeEnemy && (canGoInTerritory(rel, mp))
						& !playersFlying.contains(p.getUniqueId())) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&',
							MConf.get().flyEnableMessage));
					p.setAllowFlight(true);
					foundAOE = true;
					playersFlyingAOE.add(p.getUniqueId());
					playersFlying.add(p.getUniqueId());
					noFallDamagePlayers.add(p.getUniqueId());
				}
				foundAOE = true;
				break;
			}
		}
		if (!foundAOE && playersFlyingAOE.contains(p.getUniqueId())
				&& p.getGameMode() != GameMode.CREATIVE) {
			takeAwayFly(p);
		}
		if(playersFlying.contains(p.getUniqueId()) || playersFlyingAOE.contains(p.getUniqueId())){
			if(!p.getAllowFlight())
				p.setAllowFlight(true);
		}
	}

	public void takeAwayFly(final Player p) {
		if (playersFlying.contains(p.getUniqueId())) {
			playersFlying.remove(p.getUniqueId());
		}
		if (playersFlyingAOE.contains(p.getUniqueId())) {
			playersFlyingAOE.remove(p.getUniqueId());
		}
		if (p.isFlying())
			p.sendMessage(ChatColor.translateAlternateColorCodes('&',
					MConf.get().flyCancelMessage));
		p.setAllowFlight(false);
		new BukkitRunnable() {

			@Override
			public void run() {
				if (noFallDamagePlayers.contains(p.getUniqueId()))
					noFallDamagePlayers.remove(p.getUniqueId());
			}
		}.runTaskLater(getPlugin(), 20 * 10);
	}

	public void fallDamageHandler(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (e.getCause() == DamageCause.FALL
					&& noFallDamagePlayers.contains(p.getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}

	public boolean canGoInTerritory(Rel rel, MPlayer mPlayer){
		if(mPlayer.getPlayer().hasPermission("factions.fly.bypassland"))
			return true;

		return rel.getValue() >= MConf.get().flyMinLocationRealtion.getValue();
	}

	// -------------------------------------------- //
	// CHUNK CHANGE: DETECT
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void chunkChangeDetect(PlayerMoveEvent event) {
		// If the player is moving from one chunk to another ...

		Player player = event.getPlayer();
		if (MUtil.isntPlayer(player))
			return;

		// ... gather info on the player and the move ...
		MPlayer mplayer = MPlayer.get(player);

		PS chunkFrom = PS.valueOf(event.getFrom()).getChunk(true);
		PS chunkTo = PS.valueOf(event.getTo()).getChunk(true);

		Faction factionFrom = BoardColl.get().getFactionAt(chunkFrom);
		Faction factionTo = BoardColl.get().getFactionAt(chunkTo);

		onPlayerMove(event, factionTo, mplayer);

		if (MUtil.isSameChunk(event))
			return;
		// ... and send info onwards.
		this.chunkChangeTerritoryInfo(mplayer, player, chunkFrom, chunkTo,
				factionFrom, factionTo);
		this.chunkChangeAutoClaim(mplayer, chunkTo);
	}

	// -------------------------------------------- //
	// CHUNK CHANGE: TERRITORY INFO
	// -------------------------------------------- //

	public void chunkChangeTerritoryInfo(MPlayer mplayer, Player player,
			PS chunkFrom, PS chunkTo, Faction factionFrom, Faction factionTo) {
		// send host faction info updates
		if (mplayer.isMapAutoUpdating()) {
			List<FancyMessage> message = BoardColl.get().getMap(mplayer,
					chunkTo, player.getLocation().getYaw(), Const.MAP_WIDTH,
					Const.MAP_HEIGHT);
			for (FancyMessage msg : message) {
				msg.send(player);
			}
		} else if (factionFrom != factionTo) {
			if (mplayer.isTerritoryInfoTitles()) {
				String maintitle = parseTerritoryInfo(
						MConf.get().territoryInfoTitlesMain, mplayer, factionTo);
				String subtitle = parseTerritoryInfo(
						MConf.get().territoryInfoTitlesSub, mplayer, factionTo);
				Mixin.sendTitleMessage(player,
						MConf.get().territoryInfoTitlesTicksIn,
						MConf.get().territoryInfoTitlesTicksStay,
						MConf.get().territoryInfoTitleTicksOut, maintitle,
						subtitle);
			} else {
				String message = parseTerritoryInfo(
						MConf.get().territoryInfoChat, mplayer, factionTo);
				player.sendMessage(message);
			}
		}

		// Show access level message if it changed.
		TerritoryAccess accessFrom = BoardColl.get().getTerritoryAccessAt(
				chunkFrom);
		Boolean hasTerritoryAccessFrom = accessFrom.hasTerritoryAccess(mplayer);

		TerritoryAccess accessTo = BoardColl.get()
				.getTerritoryAccessAt(chunkTo);
		Boolean hasTerritoryAccessTo = accessTo.hasTerritoryAccess(mplayer);

		if (!MUtil.equals(hasTerritoryAccessFrom, hasTerritoryAccessTo)) {
			if (hasTerritoryAccessTo == null) {
				mplayer.msg("<i>You have standard access to this area.");
			} else if (hasTerritoryAccessTo) {
				mplayer.msg("<g>You have elevated access to this area.");
			} else {
				mplayer.msg("<b>You have decreased access to this area.");
			}
		}
	}

	public String parseTerritoryInfo(String string, MPlayer mplayer,
			Faction faction) {
		if (string == null)
			throw new NullPointerException("string");
		if (faction == null)
			throw new NullPointerException("faction");

		string = Txt.parse(string);

		string = string.replace("{name}", faction.getName());
		string = string.replace("{relcolor}", faction.getColorTo(mplayer)
				.toString());
		string = string.replace("{desc}", faction.getDescription());

		return string;
	}

	// -------------------------------------------- //
	// CHUNK CHANGE: AUTO CLAIM
	// -------------------------------------------- //

	public void chunkChangeAutoClaim(MPlayer mplayer, PS chunkTo) {
		// If the player is auto claiming ...
		Faction autoClaimFaction = mplayer.getAutoClaimFaction();
		if (autoClaimFaction == null)
			return;

		// ... try claim.
		mplayer.tryClaim(autoClaimFaction, Collections.singletonList(chunkTo));
	}

	// -------------------------------------------- //
	// POWER LOSS ON DEATH
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.NORMAL)
	public void powerLossOnDeath(PlayerDeathEvent event) {
		// If a player dies ...
		Player player = event.getEntity();
		if (MUtil.isntPlayer(player))
			return;

		// ... and this is the first death event this tick ...
		// (yeah other plugins can case death event to fire twice the same tick)
		if (PlayerUtil.isDuplicateDeathEvent(event))
			return;

		MPlayer mplayer = MPlayer.get(player);

		// ... and powerloss can happen here ...
		Faction faction = BoardColl.get().getFactionAt(
				PS.valueOf(player.getLocation()));

		if (!faction.getFlag(MFlag.getFlagPowerloss())) {
			mplayer.msg("<i>You didn't lose any power since the territory you died in works that way.");
			return;
		}

		if (!MConf.get().worldsPowerLossEnabled.contains(player.getWorld())) {
			mplayer.msg("<i>You didn't lose any power due to the world you died in.");
			return;
		}

		// ... alter the power ...
		double newPower = mplayer.getPower() + mplayer.getPowerPerDeath();

		EventFactionsPowerChange powerChangeEvent = new EventFactionsPowerChange(
				null, mplayer, PowerChangeReason.DEATH, newPower);
		powerChangeEvent.run();
		if (powerChangeEvent.isCancelled())
			return;
		newPower = powerChangeEvent.getNewPower();

		mplayer.setPower(newPower);

		// ... and inform the player.
		// TODO: A progress bar here would be epic :)
		mplayer.msg("<i>Your power is now <h>%.2f / %.2f", newPower,
				mplayer.getPowerMax());
	}

	// -------------------------------------------- //
	// CAN COMBAT DAMAGE HAPPEN
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void canCombatDamageHappen(EntityDamageEvent event) {
		fallDamageHandler(event);
		// TODO: Can't we just listen to the class type the sub is of?
		if (!(event instanceof EntityDamageByEntityEvent))
			return;
		EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

		if (this.canCombatDamageHappen(sub, true))
			return;
		event.setCancelled(true);
	}

	// mainly for flaming arrows; don't want allies or people in safe zones to
	// be ignited even after damage event is cancelled
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void canCombatDamageHappen(EntityCombustByEntityEvent event) {
		EntityDamageByEntityEvent sub = new EntityDamageByEntityEvent(
				event.getCombuster(), event.getEntity(),
				EntityDamageEvent.DamageCause.FIRE, 0D);
		if (this.canCombatDamageHappen(sub, false))
			return;
		event.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void canCombatDamageHappen(PotionSplashEvent event) {
		// If a harmful potion is splashing ...
		if (!MUtil.isHarmfulPotion(event.getPotion()))
			return;

		ProjectileSource projectileSource = event.getPotion().getShooter();
		if (!(projectileSource instanceof Entity))
			return;

		Entity thrower = (Entity) projectileSource;

		// ... scan through affected entities to make sure they're all valid
		// targets.
		for (LivingEntity affectedEntity : event.getAffectedEntities()) {
			EntityDamageByEntityEvent sub = new EntityDamageByEntityEvent(
					thrower, affectedEntity,
					EntityDamageEvent.DamageCause.CUSTOM, 0D);
			if (this.canCombatDamageHappen(sub, true))
				continue;

			// affected entity list doesn't accept modification (iter.remove()
			// is a no-go), but this works
			event.setIntensity(affectedEntity, 0.0);
		}
	}

	// Utility method used in "canCombatDamageHappen" below.
	public static boolean falseUnlessDisallowedPvpEventCancelled(
			Player attacker, Player defender, EntityDamageByEntityEvent event) {
		EventFactionsPvpDisallowed dpe = new EventFactionsPvpDisallowed(
				attacker, defender, event);
		dpe.run();
		return dpe.isCancelled();
	}

	public boolean canCombatDamageHappen(EntityDamageByEntityEvent event,
			boolean notify) {
		boolean ret = true;

		// If the defender is a player ...
		Entity edefender = event.getEntity();
		if (MUtil.isntPlayer(edefender))
			return true;
		Player defender = (Player) edefender;
		MPlayer mdefender = MPlayer.get(edefender);

		// ... and the attacker is someone else ...
		Entity eattacker = MUtil.getLiableDamager(event);

		// (we check null here since there may not be an attacker)
		// (lack of attacker situations can be caused by other bukkit plugins)
		if (eattacker != null && eattacker.equals(edefender))
			return true;

		// ... gather defender PS and faction information ...
		PS defenderPs = PS.valueOf(defender.getLocation());
		Faction defenderPsFaction = BoardColl.get().getFactionAt(defenderPs);

		// ... fast evaluate if the attacker is overriding ...
		MPlayer mplayer = MPlayer.get(eattacker);
		if (mplayer != null && mplayer.isOverriding())
			return true;

		// ... PVP flag may cause a damage block ...
		if (defenderPsFaction.getFlag(MFlag.getFlagPvp()) == false) {
			if (eattacker == null) {
				// No attacker?
				// Let's behave as if it were a player
				return falseUnlessDisallowedPvpEventCancelled(null, defender,
						event);
			}
			if (MUtil.isPlayer(eattacker)) {
				ret = falseUnlessDisallowedPvpEventCancelled(
						(Player) eattacker, defender, event);
				if (!ret && notify) {
					MPlayer attacker = MPlayer.get(eattacker);
					attacker.msg("<i>PVP is disabled in %s.",
							defenderPsFaction.describeTo(attacker));
				}
				return ret;
			}
			return defenderPsFaction.getFlag(MFlag.getFlagMonsters());
		}

		// ... and if the attacker is a player ...
		if (MUtil.isntPlayer(eattacker))
			return true;
		Player attacker = (Player) eattacker;
		MPlayer uattacker = MPlayer.get(attacker);

		// ... does this player bypass all protection? ...
		if (MConf.get().playersWhoBypassAllProtection.contains(attacker
				.getName()))
			return true;

		// ... gather attacker PS and faction information ...
		PS attackerPs = PS.valueOf(attacker.getLocation());
		Faction attackerPsFaction = BoardColl.get().getFactionAt(attackerPs);

		// ... PVP flag may cause a damage block ...
		// (just checking the defender as above isn't enough. What about the
		// attacker? It could be in a no-pvp area)
		// NOTE: This check is probably not that important but we could keep it
		// anyways.
		if (attackerPsFaction.getFlag(MFlag.getFlagPvp()) == false) {
			ret = falseUnlessDisallowedPvpEventCancelled(attacker, defender,
					event);
			if (!ret && notify)
				uattacker.msg("<i>PVP is disabled in %s.",
						attackerPsFaction.describeTo(uattacker));
			return ret;
		}

		// ... are PVP rules completely ignored in this world? ...
		if (!MConf.get().worldsPvpRulesEnabled.contains(defenderPs.getWorld()))
			return true;

		Faction defendFaction = mdefender.getFaction();
		Faction attackFaction = uattacker.getFaction();

		if (attackFaction.isNone()
				&& MConf.get().disablePVPForFactionlessPlayers) {
			ret = falseUnlessDisallowedPvpEventCancelled(attacker, defender,
					event);
			if (!ret && notify)
				uattacker
						.msg("<i>You can't hurt other players until you join a faction.");
			return ret;
		} else if (defendFaction.isNone()) {
			if (defenderPsFaction == attackFaction
					&& MConf.get().enablePVPAgainstFactionlessInAttackersLand) {
				// Allow PVP vs. Factionless in attacker's faction territory
				return true;
			} else if (MConf.get().disablePVPForFactionlessPlayers) {
				ret = falseUnlessDisallowedPvpEventCancelled(attacker,
						defender, event);
				if (!ret && notify)
					uattacker
							.msg("<i>You can't hurt players who are not currently in a faction.");
				return ret;
			}
		}

		Rel relation = defendFaction.getRelationTo(attackFaction);

		// Check the relation
		if (mdefender.hasFaction()
				&& relation.isFriend()
				&& defenderPsFaction.getFlag(MFlag.getFlagFriendlyire()) == false) {
			ret = falseUnlessDisallowedPvpEventCancelled(attacker, defender,
					event);
			if (!ret && notify)
				uattacker.msg("<i>You can't hurt %s<i>.",
						relation.getDescPlayerMany());
			return ret;
		}

		// You can not hurt neutrals in their own territory.
		boolean ownTerritory = mdefender.isInOwnTerritory();

		return true;
	}

	// -------------------------------------------- //
	// TERRITORY SHIELD
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void territoryShield(EntityDamageByEntityEvent event) {
		// If the entity is a player ...
		Entity entity = event.getEntity();
		if (MUtil.isntPlayer(entity))
			return;
		Player player = (Player) entity;
		MPlayer mplayer = MPlayer.get(player);

		// ... and the attacker is a player ...
		Entity attacker = MUtil.getLiableDamager(event);
		if (!(attacker instanceof Player))
			return;

		// ... and that player has a faction ...
		if (!mplayer.hasFaction())
			return;

		// ... and that player is in their own territory ...
		if (!mplayer.isInOwnTerritory())
			return;

		// ... and a territoryShieldFactor is configured ...
		if (MConf.get().territoryShieldFactor <= 0)
			return;

		// ... then scale the damage ...
		double factor = 1D - MConf.get().territoryShieldFactor;
		MUtil.scaleDamage(event, factor);

		// ... and inform.
		String perc = MessageFormat.format("{0,number,#%}",
				(MConf.get().territoryShieldFactor));
		mplayer.msg("<i>Enemy damage reduced by <rose>%s<i>.", perc);
	}

	// -------------------------------------------- //
	// REMOVE PLAYER DATA WHEN BANNED
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		// If a player was kicked from the server ...
		Player player = event.getPlayer();

		updateLastActivityInFactionLand(player);

		// ... and if the if player was banned (not just kicked) ...
		// if (!event.getReason().equals("Banned by admin.")) return;
		if (!player.isBanned())
			return;

		// ... and we remove player data when banned ...
		if (!MConf.get().removePlayerWhenBanned)
			return;

		// ... get rid of their stored info.
		MPlayer mplayer = MPlayerColl.get().get(player, false);
		if (mplayer == null)
			return;

		if (mplayer.getRole() == Rel.LEADER) {
			mplayer.getFaction().promoteNewLeader();
		}

		mplayer.leave();
		mplayer.detach();
	}

	// -------------------------------------------- //
	// VISUALIZE UTIL
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMoveClearVisualizations(PlayerMoveEvent event) {
		if (MUtil.isSameBlock(event))
			return;

		VisualizeUtil.clear(event.getPlayer());
	}

	// -------------------------------------------- //
	// DENY COMMANDS
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void denyCommands(PlayerCommandPreprocessEvent event) {
		// If a player is trying to run a command ...
		Player player = event.getPlayer();
		if (MUtil.isntPlayer(player))
			return;
		MPlayer mplayer = MPlayer.get(player);

		// ... and the player is not overriding ...
		if (mplayer.isOverriding())
			return;

		// ... clean up the command ...
		String command = event.getMessage();
		command = Txt.removeLeadingCommandDust(command);
		command = command.toLowerCase();
		command = command.trim();

		// ... the command may be denied for members of permanent factions ...
		if (mplayer.hasFaction()
				&& mplayer.getFaction().getFlag(MFlag.getFlagPermanent())
				&& containsCommand(command,
						MConf.get().denyCommandsPermanentFactionMember)) {
			mplayer.msg(
					"<b>You can't use \"<h>/%s<b>\" as member of a permanent faction.",
					command);
			event.setCancelled(true);
			return;
		}

		// ... if there is a faction at the players location ...
		PS ps = PS.valueOf(player.getLocation()).getChunk(true);
		Faction factionAtPs = BoardColl.get().getFactionAt(ps);
		if (factionAtPs == null)
			return;
		if (factionAtPs.isNone())
			return;

		// ... the command may be denied in the territory of this relation type
		// ...
		Rel rel = factionAtPs.getRelationTo(mplayer);

		List<String> deniedCommands = MConf.get().denyCommandsTerritoryRelation
				.get(rel);
		if (deniedCommands == null)
			return;
		if (!containsCommand(command, deniedCommands))
			return;

		mplayer.msg("<b>You can't use \"<h>/%s<b>\" in %s territory.", command,
				Txt.getNicedEnum(rel));
		event.setCancelled(true);
	}

	private static boolean containsCommand(String needle,
			Collection<String> haystack) {
		if (needle == null)
			return false;
		needle = Txt.removeLeadingCommandDust(needle);
		needle = needle.toLowerCase();

		for (String straw : haystack) {
			if (straw == null)
				continue;
			straw = Txt.removeLeadingCommandDust(straw);
			straw = straw.toLowerCase();

			// If it starts with then it is possibly a subject.
			if (needle.startsWith(straw)) {
				// Get the remainder.
				String remainder = needle.substring(straw.length());

				// If they were equal, definitely true.
				if (remainder.isEmpty())
					return true;

				// If the next is a space, the space is used as separator for
				// sub commands or arguments.
				// Otherwise it might just have been another command
				// coincidentally starting with the first command.
				// The old behaviour was if (needle.startsWith(straw)) return
				// true;
				// If "s" was block, then all commands starting with "s" was,
				// now it isn't.
				if (remainder.startsWith(" "))
					return true;
			}

		}

		return false;
	}

	// -------------------------------------------- //
	// FLAG: MONSTERS & ANIMALS
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockMonstersAndAnimals(CreatureSpawnEvent event) {
		// If this is a natural spawn ..
		if (!NATURAL_SPAWN_REASONS.contains(event.getSpawnReason()))
			return;

		// ... get the spawn location ...
		Location location = event.getLocation();
		if (location == null)
			return;
		PS ps = PS.valueOf(location);

		// ... get the faction there ...
		Faction faction = BoardColl.get().getFactionAt(ps);
		if (faction == null)
			return;

		// ... get the entity type ...
		EntityType type = event.getEntityType();

		// ... and if this type can't spawn in the faction ...
		if (canSpawn(faction, type))
			return;

		// ... then cancel.
		event.setCancelled(true);
	}

	public static boolean canSpawn(Faction faction, EntityType type) {
		if (MConf.get().entityTypesMonsters.contains(type)) {
			// Monster
			return faction.getFlag(MFlag.getFlagMonsters());
		} else if (MConf.get().entityTypesAnimals.contains(type)) {
			// Animal
			return faction.getFlag(MFlag.getFlagAnimals());
		} else {
			// Other
			return true;
		}
	}

	// -------------------------------------------- //
	// FLAG: EXPLOSIONS
	// -------------------------------------------- //

	protected Set<DamageCause> DAMAGE_CAUSE_EXPLOSIONS = EnumSet.of(
			DamageCause.BLOCK_EXPLOSION, DamageCause.ENTITY_EXPLOSION);

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockExplosion(HangingBreakEvent event) {
		// If a hanging entity was broken by an explosion ...
		if (event.getCause() != RemoveCause.EXPLOSION)
			return;
		Entity entity = event.getEntity();

		// ... and the faction there has explosions disabled ...
		Faction faction = BoardColl.get().getFactionAt(
				PS.valueOf(entity.getLocation()));
		if (faction.isExplosionsAllowed())
			return;

		// ... then cancel.
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockExplosion(EntityDamageEvent event) {
		// If an explosion damages ...
		if (DAMAGE_CAUSE_EXPLOSIONS.contains(event.getCause()))
			return;

		// ... an entity that is modified on damage ...
		if (!MConf.get().entityTypesEditOnDamage
				.contains(event.getEntityType()))
			return;

		// ... and the faction has explosions disabled ...
		if (BoardColl.get().getFactionAt(PS.valueOf(event.getEntity()))
				.isExplosionsAllowed())
			return;

		// ... then cancel!
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockExplosion(EntityExplodeEvent event) {
		// Prepare some variables:
		// Current faction
		Faction faction = null;
		// Current allowed
		Boolean allowed = true;
		// Caching to speed things up.
		Map<Faction, Boolean> faction2allowed = new HashMap<Faction, Boolean>();

		// If an explosion occurs at a location ...
		Location location = event.getLocation();

		// Check the entity. Are explosions disabled there?
		faction = BoardColl.get().getFactionAt(PS.valueOf(location));
		allowed = faction.isExplosionsAllowed();
		if (allowed == false) {
			event.setCancelled(true);
			return;
		}
		faction2allowed.put(faction, allowed);

		for (int x = (int) (location.getX() - 3); x <= location.getX() + 3; x++) {
			for (int y = (int) (location.getY() - 3); y <= location.getY() + 3; y++) {
				for (int z = (int) (location.getZ() - 3); z <= location.getZ() + 3; z++) {
					Material type = location.getWorld().getBlockAt(x, y, z)
							.getType();
					if (type == Material.LAVA
							|| type == Material.STATIONARY_LAVA)
						location.getWorld().getBlockAt(x, y, z)
								.setType(Material.AIR);
				}
			}
		}
		// Individually check the flag state for each block
		Iterator<Block> iter = event.blockList().iterator();
		while (iter.hasNext()) {
			Block block = iter.next();
			faction = BoardColl.get().getFactionAt(PS.valueOf(block));
			allowed = faction2allowed.get(faction);
			if (allowed == null) {
				allowed = faction.isExplosionsAllowed();
				faction2allowed.put(faction, allowed);
			}

			if (allowed == false)
				iter.remove();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlow(EntityExplodeEvent e) {
		for (Block block : e.blockList()) {
			if (!(block.getState() instanceof Chest)) {
				continue;
			}
			if (!Factions.get().isVoidchest(block.getLocation())) {
				continue;
			}
			ItemStack is = new ItemStack(Material.CHEST);
			ItemMeta im = is.getItemMeta();

			im.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST");

			List<String> lore = new ArrayList<String>();

			lore.add(ChatColor.YELLOW + "Sells items automatically.");

			im.setLore(lore);

			is.setItemMeta(im);

			block.setType(Material.AIR);

			block.getWorld().dropItemNaturally(block.getLocation(), is);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockExplosion(EntityChangeBlockEvent event) {
		// If a wither is changing a block ...
		Entity entity = event.getEntity();
		if (!(entity instanceof Wither))
			return;

		// ... and the faction there has explosions disabled ...
		PS ps = PS.valueOf(event.getBlock());
		Faction faction = BoardColl.get().getFactionAt(ps);

		if (faction.isExplosionsAllowed())
			return;

		// ... stop the block alteration.
		event.setCancelled(true);
	}

	// -------------------------------------------- //
	// FLAG: ENDERGRIEF
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockEndergrief(EntityChangeBlockEvent event) {
		// If an enderman is changing a block ...
		Entity entity = event.getEntity();
		if (!(entity instanceof Enderman))
			return;

		// ... and the faction there has endergrief disabled ...
		PS ps = PS.valueOf(event.getBlock());
		Faction faction = BoardColl.get().getFactionAt(ps);
		if (faction.getFlag(MFlag.getFlagEndergrief()))
			return;

		// ... stop the block alteration.
		event.setCancelled(true);
	}

	// -------------------------------------------- //
	// FLAG: ZOMBIEGRIEF
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void denyZombieGrief(EntityBreakDoorEvent event) {
		// If a zombie is breaking a door ...
		Entity entity = event.getEntity();
		if (!(entity instanceof Zombie))
			return;

		// ... and the faction there has zombiegrief disabled ...
		PS ps = PS.valueOf(event.getBlock());
		Faction faction = BoardColl.get().getFactionAt(ps);
		if (faction.getFlag(MFlag.getFlagZombiegrief()))
			return;

		// ... stop the door breakage.
		event.setCancelled(true);
	}

	// -------------------------------------------- //
	// FLAG: FIRE SPREAD
	// -------------------------------------------- //

	public void blockFireSpread(Block block, Cancellable cancellable) {
		// If the faction at the block has firespread disabled ...
		PS ps = PS.valueOf(block);
		Faction faction = BoardColl.get().getFactionAt(ps);

		if (faction.getFlag(MFlag.getFlagFirespread()))
			return;

		// then cancel the event.
		cancellable.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockFireSpread(BlockIgniteEvent event) {
		// If fire is spreading ...
		if (event.getCause() != IgniteCause.SPREAD
				&& event.getCause() != IgniteCause.LAVA)
			return;

		// ... consider blocking it.
		blockFireSpread(event.getBlock(), event);
	}

	// TODO: Is use of this event deprecated?
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockFireSpread(BlockSpreadEvent event) {
		// If fire is spreading ...
		if (event.getNewState().getType() != Material.FIRE)
			return;

		// ... consider blocking it.
		blockFireSpread(event.getBlock(), event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockFireSpread(BlockBurnEvent event) {
		// If a block is burning ...

		// ... consider blocking it.
		blockFireSpread(event.getBlock(), event);
	}

	// -------------------------------------------- //
	// FLAG: BUILD
	// -------------------------------------------- //

	public static boolean canPlayerBuildAt(Object senderObject, PS ps,
			boolean verboose) {
		MPlayer mplayer = MPlayer.get(senderObject);
		if (mplayer == null)
			return false;

		String name = mplayer.getName();
		if (MConf.get().playersWhoBypassAllProtection.contains(name))
			return true;

		if (mplayer.isOverriding())
			return true;

		if (!MPerm.getPermBuild().has(mplayer, ps, false)
				&& MPerm.getPermPainbuild().has(mplayer, ps, false)) {
			if (verboose) {
				Faction hostFaction = BoardColl.get().getFactionAt(ps);
				mplayer.msg(
						"<b>It is painful to build in the territory of %s<b>.",
						hostFaction.describeTo(mplayer));
				Player player = mplayer.getPlayer();
				if (player != null) {
					player.damage(MConf.get().actionDeniedPainAmount);
				}
			}
			return true;
		}

		return MPerm.getPermBuild().has(mplayer, ps, verboose);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityBlow(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Item
				&& e.getDamager() instanceof CraftTNTPrimed) {
			ItemStack is = ((Item) e.getEntity()).getItemStack();
			Material mat = is.getType();
			if (MConf.get().itemEntitiesNotToBlowUp.contains(mat)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		MPlayer mp = MPlayer.get(e.getPlayer());

		if (mp.isOverriding())
			return;

		String msg = e.getMessage().toLowerCase();

		if (msg.startsWith("/createhome") || msg.startsWith("/sethome")
				|| msg.startsWith("/essentials:createhome")
				|| msg.startsWith("/essentials:sethome")
				|| msg.startsWith("/ecreatehome")
				|| msg.startsWith("/esethome")
				|| msg.startsWith("/essentials:ecreatehome")
				|| msg.startsWith("/essentials:esethome")) {
			Faction faction = Board
					.get(PS.valueOf(e.getPlayer().getLocation())).getFactionAt(
							PS.valueOf(e.getPlayer().getLocation()));
			if (!MPerm.getPermEssSethome().has(mp, faction, true)) {
				e.setCancelled(true);
				return;
			}

		}

		String[] args = e.getMessage().toLowerCase().split(" ");

		if (msg.startsWith("/home") || msg.startsWith("/homes")
				|| msg.startsWith("/essentials:home")
				|| msg.startsWith("/essentials:homes")
				|| msg.startsWith("/ehome") || msg.startsWith("/ehomes")
				|| msg.startsWith("/essentials:ehome")
				|| msg.startsWith("/essentials:ehomes")) {
			File f = new File("plugins/Essentials/userdata/"
					+ e.getPlayer().getUniqueId() + ".yml");
			if (!f.exists())
				return;
			YamlConfiguration playerData = YamlConfiguration
					.loadConfiguration(f);
			if (args.length == 1) {
				if (playerData.getConfigurationSection("homes") == null)
					return;
				if (playerData.getConfigurationSection("homes").getKeys(false)
						.size() == 1) {
					for (String home : playerData.getConfigurationSection(
							"homes").getKeys(false)) {
						double x = playerData.getDouble("homes." + home + ".x");
						double y = playerData.getDouble("homes." + home + ".y");
						double z = playerData.getDouble("homes." + home + ".z");
						World world = Bukkit.getWorld(playerData
								.getString("homes." + home + ".world"));
						Location loc = new Location(world, x, y, z);
						Faction fac = Board.get(PS.valueOf(loc)).getFactionAt(
								PS.valueOf(loc));
						if (MPerm.getPermEssHome().has(mp, fac, false))
							return;
						if (fac.isNone()
								|| ChatColor.stripColor(fac.getName())
										.equalsIgnoreCase("wilderness")
								|| ChatColor.stripColor(fac.getName())
										.equalsIgnoreCase("warzone")
								|| ChatColor.stripColor(fac.getName())
										.equalsIgnoreCase("safezone"))
							return;
						e.setCancelled(true);
						e.getPlayer()
								.sendMessage(
										ChatColor.RED
												+ "Another faction has claimed over your home, it's no longer usable.");

					}
				} else
					return;
			} else if (args.length == 2) {
				String homeName = args[1].toLowerCase();
				if (playerData.getConfigurationSection("homes") == null)
					return;
				for (String home : playerData.getConfigurationSection("homes")
						.getKeys(false)) {
					if (!homeName.equalsIgnoreCase(home))
						continue;
					double x = playerData.getDouble("homes." + home + ".x");
					double y = playerData.getDouble("homes." + home + ".y");
					double z = playerData.getDouble("homes." + home + ".z");
					World world = Bukkit.getWorld(playerData.getString("homes."
							+ home + ".world"));
					Location loc = new Location(world, x, y, z);
					Faction fac = Board.get(PS.valueOf(loc)).getFactionAt(
							PS.valueOf(loc));
					if (MPerm.getPermEssHome().has(mp, fac, false))
						return;
					if (fac.isNone()
							|| ChatColor.stripColor(fac.getName())
									.equalsIgnoreCase("wilderness")
							|| ChatColor.stripColor(fac.getName())
									.equalsIgnoreCase("warzone")
							|| ChatColor.stripColor(fac.getName())
									.equalsIgnoreCase("safezone"))
						return;
					e.setCancelled(true);
					e.getPlayer()
							.sendMessage(
									ChatColor.RED
											+ "Another faction has claimed over your home, it's no longer usable.");
				}
			}
		}

		if (!MConf.get().disableUntrustedTpAccept)
			return;
		if (msg.startsWith("/tpyes") || msg.startsWith("/tpaccept")
				|| msg.startsWith("/tpahere")) {
			Faction pFaction = MPlayer.get(e.getPlayer()).getFaction();
			Faction faction = Board
					.get(PS.valueOf(e.getPlayer().getLocation())).getFactionAt(
							PS.valueOf(e.getPlayer().getLocation()));
			if (faction.isNone()
					|| ChatColor.stripColor(faction.getName())
							.equalsIgnoreCase("wilderness")
					|| ChatColor.stripColor(faction.getName())
							.equalsIgnoreCase("warzone")
					|| ChatColor.stripColor(faction.getName())
							.equalsIgnoreCase("safezone"))
				return;
			Rel rel = pFaction.getRelationTo(faction);
			if (rel.isAtLeast(Rel.TRUCE))
				if (!MPerm.getPermTrusted().has(MPlayer.get(e.getPlayer()),
						faction, true)) {
					e.setCancelled(true);
				}
		}
	}

	@EventHandler
	public void onFactionKick(EventFactionsMembershipChange e) {
		if (!MConf.get().teleportPlayerOnKickOrLeaveIfInOwnLand)
			return;
		if (e.getReason() == MembershipChangeReason.KICK
				|| e.getReason() == MembershipChangeReason.LEAVE) {
			if (e.getMPlayer() == null || e.getMPlayer().getPlayer() == null)
				return;
			Faction pFaction = e.getMPlayer().getFaction();
			if (pFaction == null)
				return;

			Faction faction = Board
					.get(PS.valueOf(e.getMPlayer().getPlayer().getLocation()))
					.getFactionAt(
							PS.valueOf(e.getMPlayer().getPlayer().getLocation()));
			if (pFaction == faction) {
				Bukkit.getServer().dispatchCommand(
						Bukkit.getServer().getConsoleSender(),
						"spawn " + e.getMPlayer().getPlayer().getName());
			}
		}
	}

	public void trustedPlace(BlockPlaceEvent event) {
		if (event.getItemInHand() == null)
			return;
		if (!MConf.get().trustedCannotPlaceBlocks.contains(event
				.getItemInHand().getType()))
			return;

		Block potentialBlock = event.getBlock().getRelative(BlockFace.UP, 1);
		Faction pFaction = MPlayer.get(event.getPlayer()).getFaction();
		Faction faction = Board.get(PS.valueOf(potentialBlock)).getFactionAt(
				PS.valueOf(potentialBlock));
		Rel rel = pFaction.getRelationTo(faction);
		if (rel.isAtLeast(Rel.TRUCE)) {
			if (!MPerm.getPermTrusted().has(MPlayer.get(event.getPlayer()),
					faction, true)) {
				event.setBuild(false);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockBuild(BlockPlaceEvent event) {
		trustedPlace(event);

		if (event.isCancelled())
			return;

		if (!event.canBuild())
			return;

		boolean verboose = !isFake(event);

		Player p = event.getPlayer();

		if (p.getItemInHand().getType() == Material.BANNER && p.isSneaking()) {
			if (canPlayerBuildAt(event.getPlayer(),
					PS.valueOf(event.getBlock()), false)
					|| (event.getBlockReplacedState().getType() != Material.WATER
							&& event.getBlockReplacedState().getType() != Material.LAVA
							&& event.getBlockReplacedState().getType() != Material.STATIONARY_LAVA && event
							.getBlockReplacedState().getType() != Material.STATIONARY_WATER)) {
				ItemStack is = p.getItemInHand();
				if (is.hasItemMeta()) {
					ItemMeta im = is.getItemMeta();
					if (im.hasLore()) {
						List<String> lore = im.getLore();
						String[] split = ChatColor.stripColor(lore.get(0))
								.split("'");
						final Faction fac = FactionColl.get().getByName(
								split[0].trim());
						if (fac != null
								&& MPerm.getPermUsebanner().has(MPlayer.get(p),
										fac, true)) {
							String msg = MPlayer.get(p).canPlaceBannerMessage();
							if(!msg.isEmpty()){
								p.sendMessage(ChatColor.RED + msg);
								event.setCancelled(true);
								return;
							}
							MPlayer.get(p).resetLastPlacedBanner();
							fac.msg(ChatColor.translateAlternateColorCodes('&',
									MConf.get().messageForAssist));
							if (!assistFactions.containsKey(fac.getId()))
								assistFactions.put(fac.getId(), event
										.getBlock().getLocation());
							else
								assistFactions.replace(fac.getId(), event
										.getBlock().getLocation());
							removeBanners.add(event.getBlock().getLocation());
							removeBanner(event.getBlock().getLocation(), fac);
							return;
						}
					}
				}
			}
		}

		if (canPlayerBuildAt(event.getPlayer(), PS.valueOf(event.getBlock()),
				verboose)) {
			ItemStack is = event.getItemInHand();

			if (is == null)
				return;

			if (is.getType() == Material.HOPPER) {
				Block up = event.getBlock().getRelative(BlockFace.UP);
				if (up.getType() == Material.CHEST){
					if(up.getState() instanceof  Chest){
						Chest c = (Chest) up.getState();
						if (c.getInventory().getTitle()
								.equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST")){
							p.sendMessage(ChatColor.RED + "You cannot place a hopper below a void chest.");
							event.setCancelled(true);
							return;
						}
					}
				}
			}

			if (is.getType() != Material.CHEST)
				return;

			Block b = event.getBlock();

			if(isBesideVoidChest(b)){
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You cannot place chests by a void chest.");
				return;
			}

			if (!is.hasItemMeta())
				return;

			ItemMeta im = is.getItemMeta();

			if (!im.hasLore())
				return;

			if (!im.hasDisplayName())
				return;

			if (!im.getDisplayName().equals(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST"))
				return;

			List<String> lore = im.getLore();

			if (lore.get(0) == null)
				return;

			if (lore.get(0).equals(
					ChatColor.YELLOW + "Sells items automatically.")) {
				Factions.get().voidchests.add(new Voidchest(event.getBlock()
						.getLocation()));
			}
			return;
		}

		event.setBuild(false);
		event.setCancelled(true);
	}

	public boolean isBesideVoidChest(Block b) {

		BlockFace bf = null;

		for (BlockRel br : BlockRel.values()) {
			switch (br) {
			case EAST:
				bf = BlockFace.EAST;
				break;
			case NORTH:
				bf = BlockFace.NORTH;
				break;
			case SOUTH:
				bf = BlockFace.SOUTH;
				break;
			case WEST:
				bf = BlockFace.WEST;
				break;
			default:
				return false;
			}
			if (b.getRelative(bf).getType() != Material.CHEST)
				continue;

			if (!(b.getState() instanceof Chest))
				continue;

			Chest c = (Chest) b.getState();

			if (!c.getInventory().getTitle()
					.equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST"))
				continue;

			return true;
		}
		return false;
	}

	public void removeBanner(final Location loc, final Faction fac) {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (loc.getBlock().getType() == Material.WALL_BANNER
						|| loc.getBlock().getType() == Material.STANDING_BANNER) {
					loc.getBlock().setType(Material.AIR);
					loc.getBlock().getState().update();
				}
				removeBanners.remove(fac.getId());
				assistFactions.remove(fac.getId());
			}
		}.runTaskLater(Factions.get(), MConf.get().timeTillAssistTimeout * 20);
	}

	public void trustedBlockBreak(BlockBreakEvent event) {
		if (event.getBlock() == null)
			return;
		if (!MConf.get().trustedCannotBreakBlocks.contains(event.getBlock()
				.getType()))
			return;
		Block potentialBlock = event.getBlock().getRelative(BlockFace.UP, 1);
		Faction pFaction = MPlayer.get(event.getPlayer()).getFaction();
		Faction faction = Board.get(PS.valueOf(potentialBlock)).getFactionAt(
				PS.valueOf(potentialBlock));
		Rel rel = pFaction.getRelationTo(faction);
		if (rel.isAtLeast(Rel.TRUCE)) {
			if (!MPerm.getPermTrusted().has(MPlayer.get(event.getPlayer()),
					faction, true)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBuild(BlockBreakEvent event) {
		trustedBlockBreak(event);
		if (event.isCancelled())
			return;
		boolean verboose = !isFake(event);

		if (canPlayerBuildAt(event.getPlayer(), PS.valueOf(event.getBlock()),
				verboose)) {
			if (!(event.getBlock().getState() instanceof Chest))
				return;

			Chest c = (Chest) event.getBlock().getState();

			if (!c.getInventory().getTitle()
					.equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST"))
				return;

			ItemStack is = new ItemStack(Material.CHEST);
			ItemMeta im = is.getItemMeta();

			im.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "VOID CHEST");

			List<String> lore = new ArrayList<String>();

			lore.add(ChatColor.YELLOW + "Sells items automatically.");

			im.setLore(lore);

			is.setItemMeta(im);

			event.setCancelled(true);

			event.getBlock().setType(Material.AIR);

			if(event.getPlayer().getGameMode() != GameMode.CREATIVE)
				event.getBlock().getWorld()
					.dropItemNaturally(event.getBlock().getLocation(), is);

			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBuild(BlockDamageEvent event) {
		if (!event.getInstaBreak())
			return;

		boolean verboose = !isFake(event);

		if (canPlayerBuildAt(event.getPlayer(), PS.valueOf(event.getBlock()),
				verboose))
			return;

		event.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBuild(BlockPistonExtendEvent event) {
		// Is using Spigot or is checking deactivated by MConf?
		if (SpigotFeatures.isActive()
				|| !MConf.get().handlePistonProtectionThroughDenyBuild)
			return;

		Block block = event.getBlock();

		// Targets end-of-the-line empty (air) block which is being pushed into,
		// including if piston itself would extend into air
		Block targetBlock = block.getRelative(event.getDirection(),
				event.getLength() + 1);

		// Factions involved
		Faction pistonFaction = BoardColl.get().getFactionAt(PS.valueOf(block));
		Faction targetFaction = BoardColl.get().getFactionAt(
				PS.valueOf(targetBlock));

		// Members of a faction might not have build rights in their own
		// territory, but pistons should still work regardless
		if (targetFaction == pistonFaction)
			return;

		// if potentially pushing into air/water/lava in another territory, we
		// need to check it out
		if ((targetBlock.isEmpty() || targetBlock.isLiquid())
				&& !MPerm.getPermBuild().has(pistonFaction, targetFaction)) {
			event.setCancelled(true);
		}

		/*
		 * note that I originally was testing the territory of each affected
		 * block, but since I found that pistons can only push up to 12 blocks
		 * and the width of any territory is 16 blocks, it should be safe (and
		 * much more lightweight) to test only the final target block as done
		 * above
		 */

	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBuild(BlockPistonRetractEvent event) {
		// Is using Spigot or is checking deactivated by MConf?
		if (SpigotFeatures.isActive()
				|| !MConf.get().handlePistonProtectionThroughDenyBuild)
			return;

		// If not a sticky piston, retraction should be fine
		if (!event.isSticky())
			return;

		Block retractBlock = event.getRetractLocation().getBlock();
		PS retractPs = PS.valueOf(retractBlock);

		// if potentially retracted block is just air/water/lava, no worries
		if (retractBlock.isEmpty() || retractBlock.isLiquid())
			return;

		// Factions involved
		Faction pistonFaction = BoardColl.get().getFactionAt(
				PS.valueOf(event.getBlock()));
		Faction targetFaction = BoardColl.get().getFactionAt(retractPs);

		// Members of a faction might not have build rights in their own
		// territory, but pistons should still work regardless
		if (targetFaction == pistonFaction)
			return;

		if (MPerm.getPermBuild().has(pistonFaction, targetFaction))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBuild(HangingPlaceEvent event) {
		boolean verboose = !isFake(event);

		if (canPlayerBuildAt(event.getPlayer(),
				PS.valueOf(event.getEntity().getLocation()), verboose))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBuild(HangingBreakEvent event) {
		if (!(event instanceof HangingBreakByEntityEvent))
			return;
		HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent) event;

		Entity breaker = entityEvent.getRemover();
		if (MUtil.isntPlayer(breaker))
			return;

		boolean verboose = !isFake(event);

		if (!canPlayerBuildAt(breaker,
				PS.valueOf(event.getEntity().getLocation()), verboose)) {
			event.setCancelled(true);
		}
	}

	public void onTrustedCheck(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock() == null)
				return;
			Block block = e.getClickedBlock();
			if(block!= null)
			if(block.getState() instanceof Chest){
				if(Factions.get().isVoidchest(block.getLocation()) &! e.getPlayer().isSneaking()){
					e.getPlayer().sendMessage(ChatColor.RED + "You cannot open void chests.");
					e.setCancelled(true);}
			}
			if (e.getPlayer().getItemInHand().getType() != Material.MONSTER_EGG)
				return;
			Block potentialBlock = e.getClickedBlock().getRelative(
					BlockFace.UP, 1);
			Faction pFaction = MPlayer.get(e.getPlayer()).getFaction();
			Faction faction = Board.get(PS.valueOf(potentialBlock))
					.getFactionAt(PS.valueOf(potentialBlock));
			Rel rel = pFaction.getRelationTo(faction);
			if (rel.isAtLeast(Rel.TRUCE)) {
				if (!MPerm.getPermTrusted().has(MPlayer.get(e.getPlayer()),
						faction, true))
					e.setCancelled(true);
			}
		}
	}

	// Check for punching out fires where players should not be able to
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockBuild(PlayerInteractEvent event) {
		onTrustedCheck(event);

		if (event.isCancelled())
			return;

		// .. and the clicked block is not null ...
		if (event.getClickedBlock() == null)
			return;

		Block potentialBlock = event.getClickedBlock().getRelative(
				BlockFace.UP, 1);

		// .. and the potential block is not null ...
		if (potentialBlock == null)
			return;
		if (event.getItem() != null
				&& event.getItem().getType() == Material.MONSTER_EGG) {
			Block newBlock = potentialBlock.getRelative(BlockFace.UP, 1);
			switch (event.getBlockFace()) {
			case DOWN:
				potentialBlock = event.getClickedBlock().getRelative(
						BlockFace.DOWN);
				newBlock = event.getClickedBlock();
				break;
			case UP:
				break;
			default:
				potentialBlock = event.getClickedBlock().getRelative(
						event.getBlockFace());
				newBlock = potentialBlock.getRelative(BlockFace.UP);
				break;

			}
			if (potentialBlock.getType() == Material.AIR)
				return;
			if (newBlock == null)
				return;
			if (newBlock.getState() instanceof Chest
					|| potentialBlock.getState() instanceof Chest)
				return;
			if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (newBlock.isLiquid() || newBlock.getType() == Material.AIR)
				return;
			event.setCancelled(true);
			event.getPlayer().sendMessage(
					ChatColor.RED + "The creeper needs more room to be spawned.");
		}

		if (event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		// ... and we're only going to check for fire ... (checking everything
		// else would be bad performance wise)
		if (potentialBlock.getType() != Material.FIRE)
			return;

		// ... check if they can build ...
		if (canPlayerBuildAt(event.getPlayer(), PS.valueOf(potentialBlock),
				true))
			return;

		// ... nope, cancel it
		event.setCancelled(true);

		// .. and compensate for client side prediction
		event.getPlayer().sendBlockChange(potentialBlock.getLocation(),
				potentialBlock.getType(),
				potentialBlock.getState().getRawData());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockLiquidFlow(BlockFromToEvent event) {
		// Prepare fields
		Block fromBlock = event.getBlock();
		int fromCX = fromBlock.getX() >> 4;
		int fromCZ = fromBlock.getZ() >> 4;
		BlockFace face = event.getFace();
		int toCX = (fromBlock.getX() + face.getModX()) >> 4;
		int toCZ = (fromBlock.getZ() + face.getModZ()) >> 4;

		// If a liquid (or dragon egg) moves from one chunk to another ...
		if (toCX == fromCX && toCZ == fromCZ)
			return;

		Board board = BoardColl.get().getFixed(
				fromBlock.getWorld().getName().toLowerCase(), false);
		if (board == null)
			return;
		Map<PS, TerritoryAccess> map = board.getMapRaw();
		if (map.isEmpty())
			return;

		PS fromPs = PS.valueOf(fromCX, fromCZ);
		PS toPs = PS.valueOf(toCX, toCZ);
		TerritoryAccess fromTa = map.get(fromPs);
		TerritoryAccess toTa = map.get(toPs);
		String fromId = fromTa != null ? fromTa.getHostFactionId()
				: Factions.ID_NONE;
		String toId = toTa != null ? toTa.getHostFactionId() : Factions.ID_NONE;

		// ... and the chunks belong to different factions ...
		if (toId.equals(fromId))
			return;

		// ... and the faction "from" can not build at "to" ...
		Faction fromFac = FactionColl.get().getFixed(fromId);
		Faction toFac = FactionColl.get().getFixed(toId);
		if (MPerm.getPermBuild().has(fromFac, toFac))
			return;

		// ... cancel!
		event.setCancelled(true);
	}

	// -------------------------------------------- //
	// ASSORTED BUILD AND INTERACT
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
		// If a player ...
		Entity edamager = MUtil.getLiableDamager(event);
		if (MUtil.isntPlayer(edamager))
			return;
		Player player = (Player) edamager;

		// ... damages an entity which is edited on damage ...
		Entity edamagee = event.getEntity();
		if (edamagee == null)
			return;
		if (!MConf.get().entityTypesEditOnDamage.contains(edamagee.getType()))
			return;

		// ... and the player can't build there ...
		if (canPlayerBuildAt(player, PS.valueOf(edamagee.getLocation()), true))
			return;

		// ... then cancel the event.
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		// only need to check right-clicks and physical as of MC 1.4+; good
		// performance boost
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK
				&& event.getAction() != Action.PHYSICAL)
			return;

		Block block = event.getClickedBlock();
		Player player = event.getPlayer();

		if (block == null)
			return; // clicked in air, apparently

		if (!canPlayerUseBlock(player, block, true)) {
			event.setCancelled(true);
			return;
		}

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return; // only interested on right-clicks for below

		if (!playerCanUseItemHere(player, PS.valueOf(block),
				event.getMaterial(), true)) {
			event.setCancelled(true);
			return;
		}
	}

	public static boolean playerCanUseItemHere(Player player, PS ps,
			Material material, boolean verboose) {
		if (MUtil.isntPlayer(player))
			return true;

		if (!MConf.get().materialsEditTools.contains(material)
				&& !MConf.get().materialsEditToolsDupeBug.contains(material))
			return true;

		String name = player.getName();
		if (MConf.get().playersWhoBypassAllProtection.contains(name))
			return true;

		MPlayer mplayer = MPlayer.get(player);
		if (mplayer.isOverriding())
			return true;

		return MPerm.getPermBuild().has(mplayer, ps, verboose);
	}

	public static boolean canPlayerUseBlock(Player player, Block block,
			boolean verboose) {
		if (MUtil.isntPlayer(player))
			return true;

		String name = player.getName();
		if (MConf.get().playersWhoBypassAllProtection.contains(name))
			return true;

		MPlayer me = MPlayer.get(player);
		if (me.isOverriding())
			return true;

		PS ps = PS.valueOf(block);
		Material material = block.getType();

		if (MConf.get().materialsEditOnInteract.contains(material)
				&& !MPerm.getPermBuild().has(me, ps, verboose))
			return false;
		if (MConf.get().materialsContainer.contains(material)
				&& !MPerm.getPermContainer().has(me, ps, verboose))
			return false;
		if (MConf.get().materialsDoor.contains(material)
				&& !MPerm.getPermDoor().has(me, ps, verboose))
			return false;
		if (material == Material.STONE_BUTTON
				&& !MPerm.getPermButton().has(me, ps, verboose))
			return false;
		if (material == Material.LEVER
				&& !MPerm.getPermLever().has(me, ps, verboose))
			return false;
		return true;
	}

	// This event will not fire for Minecraft 1.8 armor stands.
	// Armor stands are handled in EngineSpigot instead.
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		// Gather Info
		final Player player = event.getPlayer();
		final Entity entity = event.getRightClicked();
		final boolean verboose = true;
		if (player.getItemInHand().getType() == Material.MONSTER_EGG
				&& (entity instanceof Monster || entity instanceof Animals)) {
			event.setCancelled(true);
			return;
		}

		// If we can't use ...
		if (EngineMain.canPlayerUseEntity(player, entity, verboose))
			return;

		// ... block use.
		event.setCancelled(true);
	}

	public static boolean canPlayerUseEntity(Player player, Entity entity,
			boolean verboose) {
		// If a player ...
		if (MUtil.isntPlayer(player))
			return true;

		// ... interacts with an entity ...
		if (entity == null)
			return true;
		EntityType type = entity.getType();
		PS ps = PS.valueOf(entity.getLocation());

		// ... and the player does not bypass all protections ...
		String name = player.getName();
		if (MConf.get().playersWhoBypassAllProtection.contains(name))
			return true;

		// ... and the player is not using admin mode ...
		MPlayer me = MPlayer.get(player);
		if (me.isOverriding())
			return true;

		// ... check container entity rights ...
		if (MConf.get().entityTypesContainer.contains(type)
				&& !MPerm.getPermContainer().has(me, ps, verboose))
			return false;

		// ... check build entity rights ...
		if (MConf.get().entityTypesEditOnInteract.contains(type)
				&& !MPerm.getPermBuild().has(me, ps, verboose))
			return false;

		// ... otherwise we may use the entity.
		return true;
	}

	// For some reason onPlayerInteract() sometimes misses bucket events
	// depending on distance (something like 2-3 blocks away isn't detected),
	// but these separate bucket events below always fire without fail
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Block block = event.getBlockClicked();
		Player player = event.getPlayer();

		if (playerCanUseItemHere(player, PS.valueOf(block), event.getBucket(),
				true))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Block block = event.getBlockClicked();
		Player player = event.getPlayer();

		if (playerCanUseItemHere(player, PS.valueOf(block), event.getBucket(),
				true))
			return;

		event.setCancelled(true);
	}

	// -------------------------------------------- //
	// TELEPORT TO HOME ON DEATH
	// -------------------------------------------- //

	public void teleportToHomeOnDeath(PlayerRespawnEvent event,
			EventPriority priority) {
		// If a player is respawning ...
		final Player player = event.getPlayer();
		if (MUtil.isntPlayer(player))
			return;
		final MPlayer mplayer = MPlayer.get(player);

		// ... homes are enabled, active and at this priority ...
		if (!MConf.get().homesEnabled)
			return;
		if (!MConf.get().homesTeleportToOnDeathActive)
			return;
		if (MConf.get().homesTeleportToOnDeathPriority != priority)
			return;

		// ... and the player has a faction ...
		final Faction faction = mplayer.getFaction();
		if (faction.isNone())
			return;

		// ... and the faction has a home ...
		PS home = faction.getHome();
		if (home == null)
			return;

		// ... and the home is translatable ...
		Location respawnLocation = null;
		try {
			respawnLocation = home.asBukkitLocation(true);
		} catch (Exception e) {
			// The home location map may have been deleted
			return;
		}

		// ... then use it for the respawn location.
		event.setRespawnLocation(respawnLocation);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void teleportToHomeOnDeathLowest(PlayerRespawnEvent event) {
		this.teleportToHomeOnDeath(event, EventPriority.LOWEST);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void teleportToHomeOnDeathLow(PlayerRespawnEvent event) {
		this.teleportToHomeOnDeath(event, EventPriority.LOW);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void teleportToHomeOnDeathNormal(PlayerRespawnEvent event) {
		this.teleportToHomeOnDeath(event, EventPriority.NORMAL);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void teleportToHomeOnDeathHigh(PlayerRespawnEvent event) {
		this.teleportToHomeOnDeath(event, EventPriority.HIGH);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void teleportToHomeOnDeathHighest(PlayerRespawnEvent event) {
		this.teleportToHomeOnDeath(event, EventPriority.HIGHEST);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void teleportToHomeOnDeathMonitor(PlayerRespawnEvent event) {
		this.teleportToHomeOnDeath(event, EventPriority.MONITOR);
	}

}
