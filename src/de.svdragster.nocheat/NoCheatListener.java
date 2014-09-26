package de.svdragster.nocheat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.canarymod.Canary;
import net.canarymod.LineTracer;
import net.canarymod.api.DamageType;
import net.canarymod.api.GameMode;
import net.canarymod.api.entity.Arrow;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.entity.EntityType;
import net.canarymod.api.entity.living.EntityLiving;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.monster.EntityMob;
import net.canarymod.api.factory.EntityFactory;
import net.canarymod.api.potion.PotionEffect;
import net.canarymod.api.potion.PotionEffectType;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.chat.Colors;
import net.canarymod.chat.TextFormat;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.entity.DamageHook;
import net.canarymod.hook.entity.EntityMoveHook;
import net.canarymod.hook.entity.EntitySpawnHook;
import net.canarymod.hook.entity.PotionEffectAppliedHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockPlaceHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.hook.player.HealthChangeHook;
import net.canarymod.hook.player.InventoryHook;
import net.canarymod.hook.player.PlayerArmSwingHook;
import net.canarymod.hook.player.PlayerMoveHook;
import net.canarymod.plugin.PluginListener;
import net.visualillusionsent.utils.PropertiesFile;

public class NoCheatListener implements PluginListener {
	public static final String PERMISSION_ADMIN = "nocheat.admin.";
	public static final String PERMISSION_CHECKFORUPDATES = "checkforupdates";
	//private static final String USER_AGENT = "canary_minecraft";
	public static final String VERSION = new NoCheat().getVersion();
	public static final String DIR = "config/nocheat/";
	public static final String PLAYERS = "config/nocheat/players/";
	public static HashMap<Player, ArrayList<Player>> display = new HashMap<Player, ArrayList<Player>>();
	public static HashMap<Player, String> displaytype = new HashMap<Player, String>();
	HashMap<Player, Long> TempBans = new HashMap<Player, Long>();
	HashMap<Player, String> TempReasons = new HashMap<Player, String>();
	HashMap<Player, Long> map = new HashMap<Player, Long>();
	HashMap<Player, Location> lastLocation = new HashMap<Player, Location>();
	public static List<World> FastBreak = new ArrayList<World>();
	public static List<World> Nuker = new ArrayList<World>();
	public static List<World> FastBuild = new ArrayList<World>();
	public static List<World> NoSlowdown = new ArrayList<World>();
	public static List<World> NoFall = new ArrayList<World>();
	public static List<World> Jump = new ArrayList<World>();
	public static List<World> Flying = new ArrayList<World>();
	public static List<World> Speed = new ArrayList<World>();
	public static List<World> Forcefield = new ArrayList<World>();
	public static List<World> AntiForcefield = new ArrayList<World>();
	public static int AntiForcefieldType = 1;
	public static int AntiForcefieldTime = 15;
	public static String combatAction = "tempban";
	public static int combatBan = 20;
	public static String buildAction = "kick";
	public static int buildBan = 5;
	public static String movementAction = "block";
	public static int movementBan = 5;
	HashMap<Player, Long> bypass = new HashMap<Player, Long>();
	//List<Integer> allowedBlocks;
	//List<Integer> organics; // like flowers, tall grass etc
	
	public NoCheatListener() {
		//allowedBlocks.addAll(Arrays.asList(new Integer[]{65, 8, 9})); // Block Id's
		//organics.addAll(Arrays.asList(new Integer[]{37, 38, 40, 175, 6, 31, 32, 106})); // Block Id's
	}
	
	public int random(int numOne, int numTwo) {
		int RandomNumber = (int) (Math.random() * numOne + numTwo);
		return RandomNumber;
	}

	
	HashMap<Player, Long> slowdown = new HashMap<Player, Long>();
	HashMap<Player, Integer> amountslowdown = new HashMap<Player, Integer>();
	HashMap<Player, Integer> walk = new HashMap<Player, Integer>();
	HashMap<Player, Long> lastattack = new HashMap<Player, Long>();

	public void bypass(Player player, int seconds) {
		bypass.put(player, (long) (seconds*1000));
	}
	
	public void tempban(Player player, String reason, int minutes) {
		if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
			player.kick(Colors.LIGHT_RED + reason + "\n\n" + Colors.RED + "You have been banned for " + Colors.YELLOW + minutes + Colors.RED + " minutes.");
			TempReasons.put(player, reason);
			TempBans.put(player, (long) (System.currentTimeMillis() + 1000*60*minutes));
		} else {
			player.message(Colors.RED + "You would have been tempbanned: " + reason);
		}
	}
	
	public void performAction(Player player, String type) {
		if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
			if (type.equalsIgnoreCase("combat")) {
				if (combatAction.equalsIgnoreCase("tempban")) {
					tempban(player, "Combat Hacks detected.", combatBan);
				} else if (combatAction.equalsIgnoreCase("kick")) {
					player.kick(Colors.RED + "Combat Hacks detected.");
				} else if (combatAction.equalsIgnoreCase("block")) {
					player.addPotionEffect(PotionEffectType.WEAKNESS, 40, 200);
				} else {
					alert(player.getName() + Colors.YELLOW + ": Combat Hacks detected.");
				}
			} else if (type.equalsIgnoreCase("build")) {
				if (buildAction.equalsIgnoreCase("tempban")) {
					tempban(player, "Build Hacks detected.", buildBan);
				} else if (buildAction.equalsIgnoreCase("kick")) {
					player.kick(Colors.RED + "Build Hacks detected.");
				} else if (buildAction.equalsIgnoreCase("block")) {
					player.addPotionEffect(PotionEffectType.DIGSLOWDOWN, 40, 200);
				} else {
					alert(player.getName() + Colors.YELLOW + ": Build Hacks detected.");
				}
			} else if (type.equalsIgnoreCase("movement")) {
				if (movementAction.equalsIgnoreCase("tempban")) {
					tempban(player, "Movement Hacks detected.", movementBan);
				} else if (movementAction.equalsIgnoreCase("kick")) {
					player.kick(Colors.RED + "Movement Hacks detected.");
				} else if (movementAction.equalsIgnoreCase("block")) {
					if (lastLocation.containsKey(player)) {
						player.teleportTo(lastLocation.get(player));
					}
				} else {
					alert(player.getName() + Colors.YELLOW + ": Movement Hacks detected.");
				}
			}
		} else {
			player.message(Colors.RED + "NoCheat would perform action: " + Colors.LIGHT_RED + type);
		}
	}
	
	public void castToAdmins(Player toWatch, String str) {
		for (Player player : display.keySet()) {
			if (player.equals(toWatch)) {
				ArrayList<Player> admins = new ArrayList<Player>();
				admins.addAll(display.get(player));
				for (int i = 0; i < admins.size(); i++) {
					if (displaytype.containsKey(admins.get(i))) {
						String[] type = ((String) displaytype
								.get(admins.get(i))).toLowerCase().split(",");
						for (int s = 0; s < type.length; s++) {
							String strType = type[s];
							if ((strType != null)
									&& (str.toLowerCase().contains(strType))) {
								((Player) admins.get(i))
										.message("§6[NoCheat]§f"
												+ player.getName() + ": "
												+ "§c" + str);
							}
						}
					} else {
						((Player) admins.get(i)).message("§6[NoCheat]§f"
								+ player.getName() + ": " + "§c" + str);
					}
				}
			}
		}
	}

	public void alert(String str) {
		ArrayList<Player> players = new ArrayList<Player>();
		players.addAll(Canary.getServer().getPlayerList());
		for (int i = 0; i < players.size(); i++) {
			Player player = (Player) players.get(i);
			if (player.hasPermission("nocheat.alert")) {
				player.message(Colors.ORANGE + "------" + Colors.YELLOW + " NoCheat Alert " + Colors.ORANGE + "------");
				player.message(Colors.RED + "   "+ str);
				player.message(Colors.ORANGE + "------" + Colors.YELLOW + " ----------- " + Colors.ORANGE + "------");
			}
		}
	}

	public static boolean isNumeric(String str) {
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public List<World> getWorldFromString(String str) {
		String[] strings = str.split(";");
		List<World> worlds = new ArrayList<World>();
		for (int i = 0; i < strings.length; i++) {
			World world = Canary.getServer().getWorld(
					TextFormat.removeFormatting(strings[i]));
			worlds.add(world);
		}
		return worlds;
	}

	public void loadProperties() {
		File file = new File("config/nocheat/nocheat.properties");
		File dir = new File("config/nocheat/");
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		PropertiesFile prop = new PropertiesFile(
				"config/nocheat/nocheat.properties");
		prop.addHeaderLines(new String[] { "Edit the NoCheat settings here." });
		String defaultworldname = Canary.getServer().getDefaultWorldName()
				+ ";";
		if (!prop.containsKey("FastBreak")) {
			prop.setString("FastBreak", defaultworldname);
			prop.addComment("FastBreak",
					new String[] { "Disabled by default because it's buggy" });
		}
		if (!prop.containsKey("FastBuild")) {
			prop.setString("FastBuild", defaultworldname);
		}
		if (!prop.containsKey("Nuker")) {
			prop.setString("Nuker", defaultworldname);
		}
		if (!prop.containsKey("NoSlowdown")) {
			prop.setString("NoSlowdown", defaultworldname);
		}
		if (!prop.containsKey("Jump")) {
			prop.setString("Jump", defaultworldname);
		}
		if (!prop.containsKey("Flying")) {
			prop.setString("Flying", defaultworldname);
		}
		if (!prop.containsKey("Speed")) {
			prop.setString("Speed", defaultworldname);
		}
		if (!prop.containsKey("NoFall")) {
			prop.setString("NoFall", defaultworldname);
		}
		if (!prop.containsKey("Forcefield")) {
			prop.setString("Forcefield", defaultworldname);
		}
		if (!prop.containsKey("AntiForcefield")) {
			prop.setString("AntiForcefield", defaultworldname);
			prop.addComment(
					"AntiForcefield",
					new String[] { "Spawns a zombie/npc every x seconds next to a random player, only when no mobs or players are nearby" });
		}
		if (!prop.containsKey("AntiForcefieldType")) {
			prop.setInt("AntiForcefieldType", 1);
			prop.addComment(
					"AntiForcefieldType",
					new String[] { "Set if a zombie or npc should spawn: 1 is zombie, 2 is npc, 0 is both" });
		}
		if (!prop.containsKey("AntiForcefieldTime")) {
			prop.setInt("AntiForcefieldTime", 10);
			prop.addComment(
					"AntiForcefieldTime",
					new String[] { "Set when the AntiForcefield zombie/npc should spawn in minutes." });
		}
		if (!prop.containsKey("combat-action")) {
			prop.setString("combat-action", combatAction);
		}
		prop.addComment("combat-action", "What action to perform when detecting combat hacks: tempban, kick, block or info");
		if (!prop.containsKey("build-action")) {
			prop.setString("build-action", buildAction);
		}
		prop.addComment("build-action", "What action to perform when detecting build hacks: tempban, kick, block or info");
		if (!prop.containsKey("movement-action")) {
			prop.setString("movement-action", movementAction);
		}
		prop.addComment("movement-action", "What action to perform when detecting movement hacks: tempban, kick, block or info");
		if (!prop.containsKey("combat-bantime")) {
			prop.setInt("combat-bantime", combatBan);
			prop.addComment("combat-bantime", "How long a player should be banned in minutes.");
		}
		if (!prop.containsKey("build-bantime")) {
			prop.setInt("build-bantime", buildBan);
			prop.addComment("build-bantime", "How long a player should be banned in minutes.");
		}
		if (!prop.containsKey("movement-bantime")) {
			prop.setInt("movement-bantime", movementBan);
			prop.addComment("movement-bantime", "How long a player should be banned in minutes.");
		}
		prop.save();
		if (!prop.getString("FastBreak").isEmpty()) {
			String str = prop.getString("FastBreak");
			List<World> worlds = getWorldFromString(str);
			FastBreak.addAll(worlds);
		}
		if (!prop.getString("FastBuild").isEmpty()) {
			FastBuild.addAll(getWorldFromString(prop.getString("FastBuild")));
		}
		if (!prop.getString("Nuker").isEmpty()) {
			Nuker.addAll(getWorldFromString(prop.getString("Nuker")));
		}
		if (!prop.getString("NoSlowdown").isEmpty()) {
			NoSlowdown.addAll(getWorldFromString(prop.getString("NoSlowdown")));
		}
		if (!prop.getString("Jump").isEmpty()) {
			Jump.addAll(getWorldFromString(prop.getString("Jump")));
		}
		if (!prop.getString("Flying").isEmpty()) {
			Flying.addAll(getWorldFromString(prop.getString("Flying")));
		}
		if (!prop.getString("Speed").isEmpty()) {
			Speed.addAll(getWorldFromString(prop.getString("Speed")));
		}
		if (!prop.getString("NoFall").isEmpty()) {
			NoFall.addAll(getWorldFromString(prop.getString("NoFall")));
		}
		if (!prop.getString("Forcefield").isEmpty()) {
			Forcefield.addAll(getWorldFromString(prop.getString("Forcefield")));
		}
		if (!prop.getString("AntiForcefield").isEmpty()) {
			AntiForcefield.addAll(getWorldFromString(prop
					.getString("AntiForcefield")));
		}
		AntiForcefieldTime = prop.getInt("AntiForcefieldTime");
		AntiForcefieldType = prop.getInt("AntiForcefieldType");
		if (!prop.getString("combat-action").isEmpty()) {
			combatAction = prop.getString("combat-action");
		}
		if (!prop.getString("build-action").isEmpty()) {
			buildAction = prop.getString("build-action");
		}
		if (!prop.getString("movement-action").isEmpty()) {
			movementAction = prop.getString("movement-action");
		}
		if (!prop.getString("combat-bantime").isEmpty()) {
			combatBan = prop.getInt("combat-bantime");
		}
		if (!prop.getString("build-bantime").isEmpty()) {
			buildBan = prop.getInt("build-bantime");
		}
		if (!prop.getString("movement-bantime").isEmpty()) {
			movementBan = prop.getInt("movement-bantime");
		}
	}

	public void AntiForceField(Player p, int type) {
		if (AntiForcefield.isEmpty()) {
			return;
		}
		Player player = null;
		if (p == null) {
			List<Player> players = Canary.getServer().getPlayerList();
			if (players.size() > 0) {
				player = (Player) players.get(random(players.size(), 0));
			}
		} else {
			player = p;
		}
		if (!AntiForcefield.contains(player.getWorld())) {
			return;
		}
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();
		switch (player.getCardinalDirection().getIntValue()) {
		case 0:
			if (z >= 0.0D) {
				z += 1.0D;
			} else {
				z += 1.0D;
			}
			break;
		case 1:
			if (z >= 0.0D) {
				z += 1.0D;
			} else {
				z += 1.0D;
			}
			if (x >= 0.0D) {
				x -= 1.0D;
			} else {
				x += 1.0D;
			}
			break;
		case 2:
			if (x >= 0.0D) {
				x -= 1.0D;
			} else {
				x += 1.0D;
			}
			break;
		case 3:
			if (z >= 0.0D) {
				z -= 1.0D;
			} else {
				z += 1.0D;
			}
			if (x >= 0.0D) {
				x -= 1.0D;
			} else {
				x += 1.0D;
			}
			break;
		case 4:
			if (z >= 0.0D) {
				z -= 1.0D;
			} else {
				z -= 1.0D;
			}
			break;
		case 5:
			if (z >= 0.0D) {
				z -= 1.0D;
			} else {
				z += 1.0D;
			}
			if (x >= 0.0D) {
				x += 1.0D;
			} else {
				x -= 1.0D;
			}
			break;
		case 6:
			if (x >= 0.0D) {
				x += 1.0D;
			} else {
				x -= 1.0D;
			}
			break;
		case 7:
			if (z >= 0.0D) {
				z -= 1.0D;
			} else {
				z += 1.0D;
			}
			if (x >= 0.0D) {
				x += 1.0D;
			} else {
				x -= 1.0D;
			}
			break;
		}
		Location loc = new Location(x, y, z);
		EntityFactory factory = Canary.factory().getEntityFactory();

		EntityType entityType = EntityType.NPC;
		if (type == 0) {
			int random = random(2, 1);
			if (random == 1) {
				entityType = EntityType.ZOMBIE;
			}
		} else if (type == 1) {
			entityType = EntityType.ZOMBIE;
		}
		final EntityLiving mob = factory.newEntityMob(entityType, loc);

		mob.setDisplayName(player.getName());
		mob.setHealth(100.0F);
		mob.addPotionEffect(PotionEffectType.WEAKNESS, 1000, 10);
		mob.addPotionEffect(PotionEffectType.MOVESLOWDOWN, 1000, 10);
		mob.addPotionEffect(PotionEffectType.INVISIBILITY, 1000, 0);
		mob.setShowDisplayName(false);
		mob.spawn();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(400L);
					mob.destroy();
				} catch (InterruptedException localInterruptedException) {
				}
			}
		}.start();
	}

	@HookHandler
	public void onLogin(ConnectionHook hook) {
		Player player = hook.getPlayer();
		if (this.TempBans.containsKey(player)) {
			long time = ((Long) this.TempBans.get(player)).longValue();
			if (System.currentTimeMillis() < time) {
				hook.setHidden(true);
				int back = (int) ((int) time - System.currentTimeMillis());
				back = back / 1000 / 60;
				String reason = "N/A";
				if (this.TempReasons.containsKey(player)) {
					reason = (String) this.TempReasons.get(player);
				}
				player.kickNoHook("§4You have been automatically tempbanned.§6\nReason: §e"
						+ reason
						+ "§4"
						+ "\n\nYou can join again in "
						+ back
						+ " minute(s).");
			} else {
				this.TempBans.remove(player);
				this.TempReasons.remove(player);
			}
		}
		if (hook.getPlayer().hasPermission("nocheat.admin.checkforupdates")) {
			try {
				String result = sendGet(hook.getPlayer().getName());
				if ((result != null) && (!result.isEmpty())) {
					hook.getPlayer().message(result);
					hook.getPlayer()
							.message("Or you can check the forum post.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String sendGet(String playername) throws Exception {
		String MYIDSTART = "svdragster>";
		String MYIDEND = "<svdragster";
		String url = "http://svdragster.dtdns.net/checkupdate.php?version=" + VERSION
				+ "&plugin=nocheat&player=" + playername;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");

		con.setRequestProperty("User-Agent", "canary_minecraft");

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));

		StringBuffer response = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		String result = response.toString();
		if ((result.contains(MYIDSTART)) && (result.contains(MYIDEND))) {
			int endPos = result.indexOf(MYIDEND);
			result = "§6[NoCheat] §2Update available at: §f"
					+ result.substring(MYIDSTART.length(), endPos);
		}
		return result;
	}

	@HookHandler
	public void onLogout(DisconnectionHook hook) {
		if (this.TempBans.containsKey(hook.getPlayer())) {
			hook.setHidden(true);
		}
	}

	HashMap<Player, Integer> ban = new HashMap<Player, Integer>();
	HashMap<Player, Long> bantime = new HashMap<Player, Long>();
	int maxbans = 20;

	public void addToBan(Player player, int num) {
		if (!player.hasPermission("nocheat.admin.exception")) {
			if (this.ban.containsKey(player)) {
				if (this.bantime.containsKey(player)) {
					long diff = System.currentTimeMillis()
							- ((Long) this.bantime.get(player)).longValue();
					if (diff > 300000L) {
						this.ban.remove(player);
						this.bantime.remove(player);
						return;
					}
				}
				int n = ((Integer) this.ban.get(player)).intValue();
				int newnumber = n + num;
				castToAdmins(player, "ban amount: " + newnumber);
				if (newnumber > this.maxbans) {
					tempban(player, "Hacked Client detected.", 60);
					String property = getProperty(player, "hacks");
					int tempnum = 1;
					if ((property != null) && (isNumeric(property))) {
						tempnum = Integer.parseInt(property) + 1;
					}
					setProperty(player, "hacks", tempnum + "");
					this.ban.remove(player);
				} else {
					this.ban.put(player, Integer.valueOf(newnumber));
				}
			} else {
				this.ban.put(player, Integer.valueOf(num));
			}
			this.bantime.put(player, Long.valueOf(System.currentTimeMillis()));
		} else {
			player.message("§4A ban would have been added (" + num + ")");
		}
	}
	
	public boolean isOrganic(Block block) {
		ArrayList<Integer> flowers = new ArrayList<Integer>();
		flowers.addAll(Arrays.asList(new Integer[]{37, 38, 40, 175, 6, 31, 32, 106}));
		if (flowers.contains(block.getTypeId())) {
			return true;
		}
		return false;
	}

	public boolean isAllowed(Block block) {
		int id = block.getTypeId();
		ArrayList<Integer> types = new ArrayList<Integer>();
		types.addAll(Arrays.asList(new Integer[]{65, 8, 9}));
		if (types.contains(id) || isOrganic(block) || block.isAir()) {
			return true;
		}
		return false;
	}
	
	public boolean isSurroundingAllowed(Player player) {
		Block block = player.getWorld().getBlockAt(
				(int) player.getX(), (int) player.getY() - 1,
				(int) player.getZ());
		Block block2 = player.getWorld().getBlockAt(
				(int) player.getX(), (int) player.getY() - 2,
				(int) player.getZ());
		Block block3 = player.getWorld().getBlockAt( // Blocks like Halfslabs, ladders etc the player can stand "inside"
				(int) player.getX(), (int) player.getY(),
				(int) player.getZ());
		Block front = player.getWorld().getBlockAt(
				(int) player.getX() + 1, (int) player.getY()-1,
				(int) player.getZ());
		Block frontRight = player.getWorld().getBlockAt(
				(int) player.getX() + 1, (int) player.getY()-1,
				(int) player.getZ() + 1);
		Block right = player.getWorld().getBlockAt(
				(int) player.getX(), (int) player.getY()-1,
				(int) player.getZ() + 1);
		Block backRight = player.getWorld().getBlockAt(
				(int) player.getX() - 1, (int) player.getY()-1,
				(int) player.getZ() + 1);
		Block back = player.getWorld().getBlockAt(
				(int) player.getX() - 1, (int) player.getY()-1,
				(int) player.getZ());
		Block backLeft = player.getWorld().getBlockAt(
				(int) player.getX() - 1, (int) player.getY()-1,
				(int) player.getZ() - 1);
		Block left = player.getWorld().getBlockAt(
				(int) player.getX(), (int) player.getY()-1,
				(int) player.getZ() - 1);
		Block frontLeft = player.getWorld().getBlockAt(
				(int) player.getX() + 1, (int) player.getY()-1,
				(int) player.getZ() - 1);
		if (isAllowed(block) && isAllowed(block2) && isAllowed(block3) && isAllowed(front) && isAllowed(frontRight) && isAllowed(right) && isAllowed(backRight) && isAllowed(back) && isAllowed(backLeft) && isAllowed(left) && isAllowed(frontLeft)) {
			return true;
		}
		return false;
	}
	
	public void resetLocation(Player player) {
		if (lastLocation.containsKey(player)) {
			player.teleportTo(lastLocation.get(player));
		}
	}
	
	HashMap<Player, Integer> fallen = new HashMap<Player, Integer>();
	HashMap<Player, Integer> flying = new HashMap<Player, Integer>();
	HashMap<Player, Integer> gliding = new HashMap<Player, Integer>();
	HashMap<Player, Integer> jesus = new HashMap<Player, Integer>();
	HashMap<Player, Double> lastResult = new HashMap<Player, Double>(); // Against Blink hack

	@HookHandler
	public void onPlayerMove(final PlayerMoveHook hook) {
		final Player player = hook.getPlayer();
		World world = player.getWorld();
		if (map.containsKey(player)) {
			int mobcount = CountNearbyMobs(player);
			double temp = hook.getTo().getY() - hook.getFrom().getY();
			boolean negative = false;
			if (temp < 0) {
				negative = true;
			}
			Vector3D v = new Vector3D(hook.getTo().getX(), 0.0D, hook.getTo()
					.getZ());
			Vector3D from = new Vector3D(hook.getFrom());
			from.setY(0);
			double distance = v.getDistance(from);
			double diffy = Math.abs(temp);
			long difftime = System.currentTimeMillis() - map.get(player);
			if (difftime == 0L) {
				difftime = 120L;
			}
			if (distance == 0.0D) {
				distance = 0.22D;
			}
			double result = difftime / distance;
			List<PotionEffect> effects = new ArrayList<PotionEffect>();
			effects.addAll(player.getAllActivePotionEffects());
			if ((!Flying.isEmpty()) && (Flying.contains(world))) {
				verticalCheck:
				if ((diffy >= 0.25 || diffy < 0.21) && !negative && !player.getMode().equals(GameMode.CREATIVE) && (!player.getCapabilities().mayFly())) {
					for (int i=0; i<effects.size(); i++) {
						PotionEffect effect = effects.get(i);
						int id = effect.getPotionID();
						if (id == 8 || id == 1) { // Jump boost, Speed boost
							break verticalCheck;
						}
					}
					if ((distance < 0.2 || distance > 0.325) && mobcount < 1) {
						if (world.getBlockAt((int) player.getX(),(int) player.getY()-1, (int) player.getZ()).isAir() //&& world.getBlockAt((int) player.getX()+1,(int) player.getY()-1, (int) player.getZ()).isAir() && world.getBlockAt((int) player.getX()+1,(int) player.getY()-1, (int) player.getZ()+1).isAir() && world.getBlockAt((int) player.getX(),(int) player.getY()-1, (int) player.getZ()+1).isAir() && world.getBlockAt((int) player.getX()-1,(int) player.getY()-1, (int) player.getZ()+1).isAir() && world.getBlockAt((int) player.getX()-1,(int) player.getY()-1, (int) player.getZ()).isAir() && world.getBlockAt((int) player.getX()-1,(int) player.getY()-1, (int) player.getZ()-1).isAir() && world.getBlockAt((int) player.getX(),(int) player.getY()-1, (int) player.getZ()-1).isAir() && world.getBlockAt((int) player.getX()-1,(int) player.getY()-1, (int) player.getZ()-1).isAir()
								&& world.getBlockAt((int) player.getX(),(int) player.getY()-2, (int) player.getZ()).isAir()) {
							if (flying.containsKey(player)) {
								flying.put(player, flying.get(player) + 1);
								if (flying.get(player) >= 5) {
									hook.setCanceled();
									flying.remove(player);
									resetLocation(player);
								}
							} else {
								flying.put(player, 1);
							}
						}
					}
				}
			}
			if ((!Flying.isEmpty()) && (Flying.contains(world))) {
				if ((diffy < 0.21D) && (diffy > 0.2D) && (!negative) && (!player.hasPermission("nocheat.admin.exception"))) {
					if (this.lastattack.containsKey(player)) {
						if ((System.currentTimeMillis() - ((Long) this.lastattack.get(player)).longValue() > 5000L) && (mobcount < 1)) {
							hook.setCanceled();
						}
					} else {
						hook.setCanceled();
					}
				}
				if ((diffy == 0.0D) && (!player.getMode().equals(GameMode.CREATIVE)) && (!player.getCapabilities().mayFly()) && (!negative)) {
					Block block = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY() - 1,
							(int) player.getZ());
					Block block2 = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY() - 2,
							(int) player.getZ());
					Block block3 = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY(),
							(int) player.getZ());
					Block front = player.getWorld().getBlockAt(
							(int) player.getX() + 1, (int) player.getY(),
							(int) player.getZ());
					Block back = player.getWorld().getBlockAt(
							(int) player.getX() - 1, (int) player.getY(),
							(int) player.getZ());
					Block left = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY(),
							(int) player.getZ() + 1);
					Block right = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY(),
							(int) player.getZ() - 1);
					if ((block.isAir()) && (block2.isAir()) && (block3.isAir())
							&& (front.isAir()) && (back.isAir())
							&& (left.isAir()) && (right.isAir())) {
						if (this.flying.containsKey(player)) {
							this.flying.put(player, Integer.valueOf(((Integer) flying.get(player)).intValue() + 1));
							if (((Integer) flying.get(player)).intValue() > 5) {
								if (!player.hasPermission("nocheat.admin.exception")) {
									if ((this.lastattack.containsKey(player)) && (System.currentTimeMillis() - ((Long) this.lastattack.get(player)).longValue() > 8000L)) {
										return;
									}
									performAction(player, "movement");
									addToBan(player, 4);
								} else {
									player.message("§4Flyhacks detected.");
									if (lastLocation.containsKey(player)) {
										player.teleportTo(lastLocation.get(player));
									}
								}
								flying.remove(player);
							}
						} else {
							this.flying.put(player, Integer.valueOf(1));
						}
					} else {
						flying.remove(player);
					}
				}
				if ((diffy == 0.0D)
						&& (!player.getMode().equals(GameMode.CREATIVE))
						&& (!player.getCapabilities().mayFly())) {
					Block blockbelow = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY() - 1,
							(int) player.getZ());
					Block blockbelow2 = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY() - 2,
							(int) player.getZ());
					Block block = player.getWorld().getBlockAt(
							(int) player.getX(), (int) player.getY(),
							(int) player.getZ());
					if ((block.isAir())
							&& ((blockbelow.getType().equals(BlockType.Water)) || (blockbelow2
									.getType().equals(BlockType.Water)))) {
						Block front = player.getWorld().getBlockAt(
								(int) player.getX() + 1,
								(int) player.getY() - 1, (int) player.getZ());
						Block back = player.getWorld().getBlockAt(
								(int) player.getX() - 1,
								(int) player.getY() - 1, (int) player.getZ());
						Block left = player.getWorld().getBlockAt(
								(int) player.getX(), (int) player.getY() - 1,
								(int) player.getZ() + 1);
						Block right = player.getWorld().getBlockAt(
								(int) player.getX(), (int) player.getY() - 1,
								(int) player.getZ() - 1);
						if ((front.getType().equals(BlockType.Water))
								&& (right.getType().equals(BlockType.Water))
								&& (left.getType().equals(BlockType.Water))
								&& (back.getType().equals(BlockType.Water))) {
							if (this.jesus.containsKey(player)) {
								this.jesus.put(player, Integer
										.valueOf(((Integer) this.jesus
												.get(player)).intValue() + 1));
								if (((Integer) this.jesus.get(player))
										.intValue() > 7) {
									if (!player
											.hasPermission("nocheat.admin.exception")) {
										if ((this.lastattack
												.containsKey(player))
												&& (System.currentTimeMillis()
														- ((Long) this.lastattack
																.get(player))
																.longValue() > 8000L)) {
											return;
										}
										performAction(player, "movement");
										addToBan(player, 5);
									} else {
										player.message("§4You would have been kicked for jesus hacks.");
									}
									this.jesus.remove(player);
								}
							} else {
								this.jesus.put(player, Integer.valueOf(1));
							}
						}
					} else {
						this.jesus.remove(player);
					}
				}
			}
			if ((!player.getMode().equals(GameMode.CREATIVE)) && (!player.getCapabilities().mayFly()) && (!NoFall.isEmpty()) && (NoFall.contains(world))) {
				if (negative && world.getBlockAt((int) player.getX(), (int) player.getY() - 1, (int) player.getZ()).getType().equals(BlockType.Air) && world.getBlockAt((int) player.getX(), (int) player.getY() - 2, (int) player.getZ()).getType().equals(BlockType.Air)) {
					if (fallen.containsKey(player)) {
						fallen.put(player, fallen.get(player) + 1);
					} else {
						fallen.put(player, Integer.valueOf(1));
					}
				}
				/**
				 * Glide (Gently moving towards the ground), or using flyhacks to move downwards
				 */
				
				if (negative && world.getBlockAt((int) player.getX(), (int) player.getY() - 1, (int) player.getZ()).getType().equals(BlockType.Air) && world.getBlockAt((int) player.getX(), (int) player.getY() - 2, (int) player.getZ()).getType().equals(BlockType.Air)) {
					if (fallen.containsKey(player)) {
						int fall = fallen.get(player);
						if (fall >= 7) {
							if (result > 2700) {
								resetLocation(player);
								castToAdmins(player, "§4[move] Glide");
							}
						}
					}
				}
				
				/**
				 * NoFall
				 */
				NoFallCheck:
				if ((fallen.containsKey(player)) && (((Integer) fallen.get(player)).intValue() >= 7) && !isSurroundingAllowed(player)) {
					for (int i=0; i<effects.size(); i++) {
						PotionEffect effect = effects.get(i);
						int id = effect.getPotionID();
						if (id == 8) {
							break NoFallCheck;
						}
					}
					if (lastDamageType.containsKey(player)) {
						new Thread() {
							public void run() {
								try {
									Thread.sleep(140);
									if (!(lastDamageType.get(player)).equals(DamageType.FALL)) {
										castToAdmins(player, "§4[move] NoFall");
										resetLocation(player);
										hook.setCanceled();
									} else {
										NoCheatListener.this.lastDamageType
												.put(player, DamageType.GENERIC);
									}
								} catch (InterruptedException localInterruptedException) {
								}
							}
						}.start();
					} else {
						this.lastDamageType.put(player, DamageType.GENERIC);
					}
					this.fallen.remove(player);
				}
				if (!player.getWorld().getBlockAt((int) player.getX(), (int) player.getY() - 1, (int) player.getZ()).getType().equals(BlockType.Air)) {
					this.fallen.remove(player);
				}
			}
			double maxjump = 0.54;
			// Increases jump height by about [level]/8+.46 per level. (Jump height is ([level]+4.2)^2/16, ignoring drag). Taken from http://minecraft.gamepedia.com/Status_effect
			for (int i=0; i<effects.size(); i++) {
				PotionEffect effect = effects.get(i);
				int id = effect.getPotionID();
				if (id == 8) { // Jump boost
					int amp = effect.getAmplifier(); // amp 0 is level 1 ingame
					double add = 1 + (0.22*amp);
					maxjump = maxjump * add;
				}
			}
			if ((!negative) && (diffy > maxjump) && (!player.getMode().equals(GameMode.CREATIVE))	&& (!Jump.isEmpty()) && (Jump.contains(world))) {
				if (lastattack.containsKey(player)) {
					if ((System.currentTimeMillis() - lastattack.get(player) > 3000L) && (mobcount < 1)) {
						hook.setCanceled();
						addToBan(player, 1);
						castToAdmins(player, "§4diffy > " + maxjump);
					}
				} else {
					hook.setCanceled();
				}
			}
			String str = "[move]";
			if (diffy < 0.5D) {
				if ((player.isBlocking()) && (!NoSlowdown.isEmpty())
						&& (NoSlowdown.contains(world)) && (!negative)
						&& (result < 2000.0D)) {
					if (this.amountslowdown.containsKey(player)) {
						this.amountslowdown.put(player, Integer
								.valueOf(((Integer) this.amountslowdown
										.get(player)).intValue() + 1));
						if (((Integer) this.amountslowdown.get(player))
								.intValue() > 4) {
							if (this.slowdown.containsKey(player)) {
								long diffs = System.currentTimeMillis()
										- ((Long) this.slowdown.get(player))
												.longValue();
								if (diffs < 2000L) {
									str = str.concat("§4 (noslowdown) "
											+ result + " < 2000");
									if (this.lastattack.containsKey(player)) {
										if ((System.currentTimeMillis()
												- ((Long) this.lastattack
														.get(player))
														.longValue() > 2000L)
												&& (mobcount < 1)) {
											hook.setCanceled();
										}
									} else {
										hook.setCanceled();
									}
									this.amountslowdown.put(player,
											Integer.valueOf(1));
									this.slowdown.remove(player);
								} else {
									this.slowdown.put(player,
											Long.valueOf(System
													.currentTimeMillis()));
								}
							} else {
								this.slowdown.put(player, Long.valueOf(System
										.currentTimeMillis()));
							}
						}
					} else {
						this.amountslowdown.put(player, Integer.valueOf(1));
					}
				}
			} else if ((diffy > 0.8D) && (!Flying.isEmpty())
					&& (Flying.contains(player)) && (!negative)
					&& (!player.isBlocking()) && (result < 300.0D)
					&& (result > 2.5D)) {
				str = str.concat("§4 (speed) " + diffy + " > 0.8, " + result
						+ " < 300");
				if (hook.getPlayer().getMode() != GameMode.CREATIVE) {
					if (this.lastattack.containsKey(player)) {
						if ((System.currentTimeMillis() - (lastattack.get(player))
										.longValue() > 4000L)
								&& (mobcount < 1)) {
							hook.setCanceled();
						}
					} else {
						hook.setCanceled();
					}
				}
			}
			if ((diffy == 0) && (player.isSneaking()) && (!NoSlowdown.isEmpty()) && (NoSlowdown.contains(world)) && (result < 600.0D)) {
				str = str.concat("§4 (sneak) " + result + " < 600");
				if (player.getMode() != GameMode.CREATIVE) {
					if (this.lastattack.containsKey(player)) {
						if ((System.currentTimeMillis() - ((Long) this.lastattack.get(player)).longValue() > 2000L) && (mobcount < 1)) {
							hook.setCanceled();
						}
					} else {
						hook.setCanceled();
					}
				}
			}
			double number = 224;
			if (player.isSprinting()) {
				number = 97;
			}
			for (int i=0; i<effects.size(); i++) {
				PotionEffect effect = effects.get(i);
				int id = effect.getPotionID();
				if (id == 1) { // Speed
					int amp = effect.getAmplifier(); // amp 0 is level 1 ingame
					amp = amp + 1;
					double remove = 1 - (0.2*amp); // Every additional level makes the player 20% faster. 0.2*amp removes 0.2 each level.
					number = number * remove; // If 'remove' is 0.8 it removes 20% of number;
					if (number < 0) {
						number = 0;
					}
				}
			}
			if (world.getBlockAt((int) player.getX(), (int) player.getY(), (int) player.getZ()).getType().equals(BlockType.Water)) {
				number = 600;
			}
			if ((!Speed.isEmpty()) && (Speed.contains(world))) {
				int intlevel = 0;
				String color = Colors.LIGHT_GREEN;
				if (walk.containsKey(player)) {
					intlevel = walk.get(player);
					if (intlevel > 40) {
						alert(player.getName() + ": " + Colors.LIGHT_RED + "Movement Hacks detected.");
					}
					if (intlevel < 5){
						color = Colors.GREEN;
					} else if (intlevel < 10) {
						color = Colors.YELLOW;
					} else if (intlevel < 15) {
						color = Colors.LIGHT_RED;
					} else {
						color = Colors.RED;
					}
					if (result >= 3 && (hook.getPlayer().getMode() != GameMode.CREATIVE) && (!hook.getPlayer().getCapabilities().mayFly())) { // Against lag
						if (lastResult.containsKey(player)) {
							double last = lastResult.get(player);
							double resultDifference = Math.abs(result - last);
							if (resultDifference <= 2) { // I noticed using the hack 'Blink' sometimes gives the last result number for a couple of times. For example: 300.1, 300.0, 7, 300.2, 300.1, 300.1, 9
								walk.put(player, intlevel + 5);
								str = str.concat(Colors.RED + " (Blink) ");
							}
						}
						lastResult.put(player, result);
					}
				} else {
					walk.put(player, 0);
				}
				if ((result >= 3) && (result <= number)) { // Checks result being lower than 3 to prevent false detection when it's lagging
					if ((!negative)	&& (!hook.getPlayer().isBlocking()) && (hook.getPlayer().getMode() != GameMode.CREATIVE) && (!hook.getPlayer().getCapabilities().mayFly())) {
						walk.put(player, intlevel + 4);
						str = str.concat("§4 (speed) " + result + " < " + number + ", Danger level: " + color + intlevel);
						if (intlevel > 55) {
							hook.setCanceled();
							performAction(player, "movement");
							alert(player.getName() + ": " + Colors.RED + "Movement Hacks detected.");
							walk.put(player, 15);
						} else if (intlevel > 20) {
							if (walk.get(player) > 20) { 
								if (lastattack.containsKey(player)) {
									if ((System.currentTimeMillis() - (lastattack.get(player)).longValue() > 5000L) && (mobcount < 1)) {
										hook.setCanceled();
									}
								} else {
									hook.setCanceled();
								}
							}
						}
					}
				} else if (walk.containsKey(player)) {
					int num = walk.get(player) - 1;
					if (num < 0) {
						num = 0;
					}
					walk.put(player, num);
				}
				if (!str.contains("(speed)")) {
					str = str.concat("§2 (speed) " + result + ", Danger level: " + color + intlevel);
				}
				castToAdmins(player, str);
			}
			if (!hook.isCanceled()) {
				if (!world.getBlockAt((int) player.getX(), (int) player.getY()-1, (int) player.getZ()).isAir() && diffy == 0) {
					lastLocation.put(player, player.getLocation());
				}
			}
		}
		this.map.put(player, Long.valueOf(System.currentTimeMillis()));
	}

	HashMap<Player, Long> arrow = new HashMap<Player, Long>();
	HashMap<Player, Integer> arrowcount = new HashMap<Player, Integer>();

	@HookHandler
	public void onEntitySpawn(EntitySpawnHook hook) {
		if (hook.getEntity().getEntityType().equals(EntityType.ARROW)) {
			String str = "[combat]";
			Arrow a = (Arrow) hook.getEntity();
			if ((a.getOwner() != null) && (a.getOwner().isPlayer())) {
				Player player = (Player) a.getOwner();
				if (this.arrow.containsKey(player)) {
					long diff = System.currentTimeMillis()
							- ((Long) this.arrow.get(player)).longValue();
					if ((diff < 230L) && (diff > 32L)) {
						if (this.arrowcount.containsKey(player)) {
							this.arrowcount.put(player, Integer.valueOf(((Integer) this.arrowcount.get(player)).intValue() + 5));
							if (((Integer) this.arrowcount.get(player))
									.intValue() > 20) {
								str = str.concat("§4 (arrow) " + diff
										+ " < 230");
								addToBan(player, 5);
							}
						} else {
							this.arrowcount.put(player, Integer.valueOf(5));
						}
					} else {
						str = str.concat("§2(arrow) " + diff + " > 230");
					}
				}
				if (this.arrowcount.containsKey(player)) {
					this.arrowcount.put(player, Integer
							.valueOf(((Integer) this.arrowcount.get(player))
									.intValue() - 1));
					if (((Integer) this.arrowcount.get(player)).intValue() < 0) {
						this.arrowcount.put(player, Integer.valueOf(0));
					}
				}
				this.arrow
						.put(player, Long.valueOf(System.currentTimeMillis()));
				castToAdmins(player, str);
			}
		}
	}

	HashMap<Player, Boolean> inventory = new HashMap<Player, Boolean>();

	@HookHandler
	public void onInventory(InventoryHook hook) {
		if (hook.isClosing()) {
			this.inventory.put(hook.getPlayer(), Boolean.valueOf(false));
		} else {
			this.inventory.put(hook.getPlayer(), Boolean.valueOf(true));
		}
	}

	public int CountNearbyMobs(Player player) {
		int count = 0;
		List<EntityMob> mobs = player.getWorld().getMobList();
		for (int i = 0; i < mobs.size(); i++) {
			Vector3D v = new Vector3D(((EntityMob) mobs.get(i)).getX(),
					((EntityMob) mobs.get(i)).getY(),
					((EntityMob) mobs.get(i)).getZ());
			if (v.getDistance(player.getPosition()) <= 5.0D) {
				count++;
			}
		}
		return count;
	}

	HashMap<Player, Integer> ff = new HashMap<Player, Integer>();
	HashMap<Player, Long> hits = new HashMap<Player, Long>();
	HashMap<Player, Integer> hitsanzahl = new HashMap<Player, Integer>();
	HashMap<Player, DamageType> lastDamageType = new HashMap<Player, DamageType>();

	@HookHandler
	public void onDamage(DamageHook hook) {
		if ((hook.getAttacker() != null) && (hook.getAttacker().isPlayer())) {
			Player player = (Player) hook.getAttacker();
			if ((this.inventory.containsKey(player)) && (((Boolean) this.inventory.get(player)).booleanValue())) {
				player.addPotionEffect(PotionEffectType.WEAKNESS, 20, 100);
				player.message("§4You are hitting while an inventory is opened!");
				castToAdmins(player, "[combat] Hitting while an inventory is opened");
				hook.setCanceled();
				if ((!Forcefield.isEmpty()) && (Forcefield.contains(player))) {
					if (this.ff.containsKey(player)) {
						this.ff.put(player, Integer.valueOf(((Integer) this.ff
								.get(player)).intValue() + 1));
						if (((Integer) this.ff.get(player)).intValue() > 3) {
							if (!player
									.hasPermission("nocheat.admin.exception")) {
								addToBan(player, 5);
								performAction(player, "combat");
								String property = getProperty(player,
										"combat-hacks");
								int num = 1;
								if ((property != null) && (isNumeric(property))) {
									num = Integer.parseInt(property) + 1;
								}
								setProperty(player, "combat-hacks", num + "");
							} else {
								player.message("§4You would have been tempbanned for combat hacks.");
							}
						}
					} else {
						this.ff.put(player, Integer.valueOf(0));
					}
				}
			}
			if (hook.getDefender().getEntityType().equals(EntityType.ZOMBIE)) {
				EntityMob mob = (EntityMob) hook.getDefender();
				if (mob.hasDisplayName()) {
					if (mob.getDisplayName().equals("n")) {
						alert(player.getName() + ": Combat hacks detected, banning player.");
						addToBan(player, 20);
					}
				}
			}
		}
		if (hook.getDefender().getEntityType().equals(EntityType.ZOMBIE)) {
			EntityMob mob = (EntityMob) hook.getDefender();
			if (mob.hasDisplayName()) {
				if (mob.getDisplayName().equals("n")) {
					hook.setCanceled();
				}
			}
		}
		if (hook.getDefender().isPlayer()) {
			Player player = (Player) hook.getDefender();
			this.lastDamageType.put(player, hook.getDamageSource()
					.getDamagetype());
			this.lastattack.put(player,
					Long.valueOf(System.currentTimeMillis()));
		}
		if (hook.getDefender().isMob()) {
			EntityMob mob = (EntityMob) hook.getDefender();
			if ((hook.getDamageSource() != null)
					&& (hook.getDamageSource().getDamageDealer() != null)
					&& (hook.getDamageSource().getDamageDealer().isPlayer())) {
				Player player = (Player) hook.getDamageSource()
						.getDamageDealer();
				float maxrange = 5.7F;
				Vector3D v = new Vector3D(player.getLocation());
				double distance = v.getDistance(mob.getPosition());
				if (distance > maxrange) {
					if (this.hitsanzahl.containsKey(player)) {
						this.hitsanzahl.put(player,
								Integer.valueOf(((Integer) this.hitsanzahl
										.get(player)).intValue() + 10));
						castToAdmins(player, "Range: §4" + distance);
					} else {
						this.hitsanzahl.put(player, Integer.valueOf(0));
					}
				}
				if (this.hits.containsKey(player)) {
					long diff = System.currentTimeMillis()
							- ((Long) this.hits.get(player)).longValue();
					if ((diff < 148L) && (diff > 20L)) {
						castToAdmins(player, "[combat] Damaging too fast: §4"
								+ diff);
						if (this.hitsanzahl.containsKey(player)) {
							this.hitsanzahl.put(player, Integer
									.valueOf(((Integer) this.hitsanzahl
											.get(player)).intValue() + 5));
							if (((Integer) this.hitsanzahl.get(player))
									.intValue() >= 25) {
								this.hitsanzahl
										.put(player, Integer.valueOf(20));
								if (!player
										.hasPermission("nocheat.admin.exception")) {
									addToBan(player, 5);
									performAction(player, "combat");
									String property = getProperty(player,
											"combat-hacks");
									int num = 1;
									if ((property != null)
											&& (isNumeric(property))) {
										num = Integer.parseInt(property) + 1;
									}
									setProperty(player, "combat-hacks", num + "");
								} else {
									player.message("§4You would have been tempbanned for combat hacks.");
								}
							}
						} else {
							this.hitsanzahl.put(player, Integer.valueOf(1));
						}
					} else if (this.hitsanzahl.containsKey(player)) {
						this.hitsanzahl.put(player,
								Integer.valueOf(((Integer) this.hitsanzahl
										.get(player)).intValue() - 1));
					}
				}
				this.hits.put(player, Long.valueOf(System.currentTimeMillis()));
			}
			if ((mob.hasDisplayName())
					&& (hook.getDamageSource().getDamageDealer() != null)
					&& (hook.getDamageSource().getDamageDealer().isPlayer())) {
				Player player = (Player) hook.getDamageSource()
						.getDamageDealer();
				if ((mob.getDisplayName().equalsIgnoreCase(player.getName()))
						&& (CountNearbyMobs(player) == 2)) {
					if (this.ff.containsKey(player)) {
						this.ff.put(player, Integer.valueOf(((Integer) this.ff
								.get(player)).intValue() + 1));
						if (((Integer) this.ff.get(player)).intValue() > 3) {
							if (!player
									.hasPermission("nocheat.admin.exception")) {
								performAction(player, "combat");
								String property = getProperty(player,
										"combat-hacks");
								int num = 1;
								if ((property != null) && (isNumeric(property))) {
									num = Integer.parseInt(property) + 1;
								}
								setProperty(player, "combat-hacks", num + "");
							} else {
								player.message("§4You would have been tempbanned for combat hacks.");
							}
						}
					} else {
						this.ff.put(player, Integer.valueOf(0));
					}
				}
			}
		}
	}

	HashMap<Player, Long> blocks = new HashMap<Player, Long>();
	HashMap<Player, Integer> blocksamount = new HashMap<Player, Integer>();
	static HashMap<Player, Integer> buffer = new HashMap<Player, Integer>();

	@HookHandler
	public void onBlockDestroy(BlockDestroyHook hook) {
		World world = hook.getPlayer().getWorld();
		Player player = hook.getPlayer();
		if ((!hook.getBlock().getType().equals(BlockType.TallGrass))
				&& (!FastBreak.isEmpty()) && (FastBreak.contains(world))
				&& (this.blocks.containsKey(hook.getPlayer()))) {
			long diff = System.currentTimeMillis()
					- ((Long) this.blocks.get(hook.getPlayer())).longValue();
			if (!hook.getPlayer().getMode().equals(GameMode.CREATIVE)) {
				int number = 80000;
				if (player.getItemHeld() != null) {
					int id = player.getItemHeld().getId();
					if (id == 278) {
						number = 300000;
					}
					if (id == 277) {
						number = 300000;
					}
					if (id == 274) {
						number = 160000;
					}
					if (id == 273) {
						number = 100000;
					}
					if (id == 256) {
						number = 120000;
					}
				}
				if (diff > number) {
					if (buffer.containsKey(player)) {
						int num = ((Integer) buffer.get(player)).intValue() + 4;
						buffer.put(player, Integer.valueOf(num));
						if (((Integer) buffer.get(player)).intValue() > 12) {
							hook.setCanceled();
							hook.getPlayer().message("§4FastBreak!!!");
							hook.getPlayer().addPotionEffect(
									PotionEffectType.DIGSLOWDOWN, 30, 50);
							if (this.blocksamount.containsKey(hook.getPlayer())) {
								this.blocksamount.put(hook.getPlayer(), Integer
										.valueOf(((Integer) this.blocksamount
												.get(hook.getPlayer()))
												.intValue() + 1));
							} else {
								this.blocksamount.put(hook.getPlayer(),
										Integer.valueOf(1));
							}
							buffer.put(player, Integer.valueOf(8));
						}
					} else {
						buffer.put(player, Integer.valueOf(4));
					}
				}
				if (buffer.containsKey(player)) {
					int num = ((Integer) buffer.get(player)).intValue() - 1;
					if (num < 0) {
						num = 0;
					}
					buffer.put(player, Integer.valueOf(num));
				}
			}
		}
		if ((!Nuker.isEmpty()) && (Nuker.contains(world))
				&& (this.blocks.containsKey(hook.getPlayer()))) {
			long diff = System.currentTimeMillis()
					- ((Long) this.blocks.get(player)).longValue();
			if (diff < 2L) {
				hook.getPlayer().message("§4You break blocks too fast.");
				castToAdmins(hook.getPlayer(),
						"[blocks] destroying blocks too fast. (" + diff + ")");
				hook.setCanceled();
				if (this.blocksamount.containsKey(hook.getPlayer())) {
					this.blocksamount.put(hook.getPlayer(), Integer
							.valueOf(((Integer) this.blocksamount.get(hook
									.getPlayer())).intValue() + 1));
				} else {
					this.blocksamount.put(hook.getPlayer(), Integer.valueOf(1));
				}
			}
		}
		this.blocks.put(player, Long.valueOf(System.currentTimeMillis()));
		if ((this.blocksamount.containsKey(hook.getPlayer()))
				&& (((Integer) this.blocksamount.get(hook.getPlayer()))
						.intValue() > 5)) {
			if (!hook.getPlayer().hasPermission("nocheat.admin.exception")) {
				performAction(player, "build");
				this.blocksamount.put(hook.getPlayer(), Integer.valueOf(1));
				String property = getProperty(hook.getPlayer(), "destroy-hacks");
				int num = 1;
				if ((property != null) && (isNumeric(property))) {
					num = Integer.parseInt(property) + 1;
				}
				setProperty(hook.getPlayer(), "destroy-hacks", num + "");
				addToBan(hook.getPlayer(), 4);
			} else {
				hook.getPlayer().message(
						"§4You would have been banned for destroy hacks.");
				this.blocksamount.put(hook.getPlayer(), Integer.valueOf(1));
			}
		}
	}

	HashMap<Player, Long> swing = new HashMap<Player, Long>();
	HashMap<Player, Long> beforediff = new HashMap<Player, Long>();
	HashMap<Player, Integer> anzahl = new HashMap<Player, Integer>();

	@HookHandler
	public void onPlayerArmSwing(PlayerArmSwingHook hook) {
		Player player = hook.getPlayer();
		if (!player.getMode().equals(GameMode.CREATIVE)) {
			LineTracer lt = new LineTracer(player, 7, 0.02D);
			Block target = lt.getTargetBlock();
			if (this.swing.containsKey(player)) {
				long diff = System.currentTimeMillis()
						- ((Long) this.swing.get(player)).longValue();
				if (this.beforediff.containsKey(player)) {
					long before = ((Long) this.beforediff.get(player))
							.longValue();
					long wholediff = Math.abs(diff - before);
					if (!this.anzahl.containsKey(player)) {
						this.anzahl.put(player, Integer.valueOf(0));
					}
					if (diff < 20L) {
						return;
					}
					String strDiff;
					Entity etarget = player.getTargetLookingAt();
					if (diff <= 51L) {
						int toadd = 8;
						if (target != null && etarget == null) {
							toadd = 0;
						} else if (target != null && etarget != null) {
							toadd = 9;
						}
						anzahl.put(player,	anzahl.get(player) + toadd);
						strDiff = "§4" + diff;
					} else {
						strDiff = "§a" + diff;
					}
					String strWholediff;
					//Entity etarget = player.getTargetLookingAt();
					if (wholediff <= 4L) {
						int toadd = 3;
						if (target != null) {
							toadd = 1;
						}
						this.anzahl.put(
								player,
								Integer.valueOf(((Integer) this.anzahl
										.get(player)).intValue() + toadd));
						strWholediff = "§4" + wholediff;
					} else {
						int toremove = 2;
						if (target != null && etarget == null) {
							toremove = 10;
						}
						this.anzahl.put(
								player,
								Integer.valueOf(((Integer) this.anzahl
										.get(player)).intValue() - toremove));
						strWholediff = "§a" + wholediff;
					}
					if (((Integer) this.anzahl.get(player)).intValue() < 0) {
						this.anzahl.put(player, Integer.valueOf(0));
					}
					if (this.anzahl.containsKey(player)) {
						if (((Integer) this.anzahl.get(player)).intValue() > 100) {
							this.anzahl.remove(player);
							performAction(player, "combat");

							String property = getProperty(player,
									"combat-hacks");
							int num = 1;
							if ((property != null) && (isNumeric(property))) {
								num = Integer.parseInt(property) + 1;
							}
							setProperty(player, "combat-hacks", num + "");
							alert("§6" + player.getName() + "§c" + ": Combat hacks detected");
						}
						if (!this.anzahl.containsKey(player)) {
							return;
						}
						int num = ((Integer) this.anzahl.get(player))
								.intValue();
						String n;
						if (num < 20) {
							n = "§2" + num;
						} else {
							if (num < 40) {
								n = "§e" + num;
							} else {
								if (num < 60) {
									n = "§6" + num;
								} else {
									if (num < 80) {
										n = "§c" + num;
									} else {
										n = "§4" + num;
									}
								}
							}
						}
						castToAdmins(player, "[combat] §a" + before + "/"
								+ strDiff + "§e" + " > " + strWholediff + ", "
								+ "§c" + "Danger level: " + "§6" + n);
					}
				}
				this.beforediff.put(player, Long.valueOf(diff));
			}
			this.swing.put(player, Long.valueOf(System.currentTimeMillis()));
		}
	}

	HashMap<Player, Long> buildtime = new HashMap<Player, Long>();
	HashMap<Player, Integer> buildhack = new HashMap<Player, Integer>();
	HashMap<Player, Integer> buildbuffer = new HashMap<Player, Integer>();

	@HookHandler
	public void onBlockPlace(BlockPlaceHook hook) {
		World world = hook.getPlayer().getWorld();
		if ((!FastBuild.isEmpty()) && (FastBuild.contains(world))) {
			Player player = hook.getPlayer();
			if (!player.hasPermission("nocheat.admin.exception")) {
				if (this.buildtime.containsKey(player)) {
					long diff = System.currentTimeMillis()
							- ((Long) this.buildtime.get(player)).longValue();
					if (this.buildbuffer.containsKey(player)) {
						if (diff < 26L) {
							this.buildbuffer.put(player, Integer
									.valueOf(((Integer) this.buildbuffer
											.get(player)).intValue() + 5));
							if (((Integer) this.buildbuffer.get(player))
									.intValue() >= 20) {
								this.buildbuffer.put(player,
										Integer.valueOf(14));
								hook.setCanceled();
								player.message("§4You are building too fast.§2("
										+ diff + ")");
							}
						} else {
							this.buildbuffer.put(player, Integer
									.valueOf(((Integer) this.buildbuffer
											.get(player)).intValue() - 1));
							if (((Integer) this.buildbuffer.get(player))
									.intValue() < 0) {
								this.buildbuffer
										.put(player, Integer.valueOf(0));
							}
						}
					} else {
						this.buildbuffer.put(player, Integer.valueOf(0));
					}
					if (diff <= 1L) {
						hook.setCanceled();
						if (this.buildhack.containsKey(player)) {
							this.buildhack.put(player, Integer
									.valueOf(((Integer) this.buildhack
											.get(player)).intValue() + 1));
							if (((Integer) this.buildhack.get(player))
									.intValue() > 15) {
								performAction(player, "build");
								this.buildhack.remove(player);
								String property = getProperty(hook.getPlayer(),
										"build-hacks");
								int num = 1;
								if ((property != null) && (isNumeric(property))) {
									num = Integer.parseInt(property) + 1;
								}
								setProperty(hook.getPlayer(), "build-hacks",
										num + "");
							}
						} else {
							this.buildhack.put(player, Integer.valueOf(1));
						}
					}
				}
				this.buildtime.put(player,
						Long.valueOf(System.currentTimeMillis()));
			}
		}
	}

	HashMap<Player, PotionEffect> lastPotion = new HashMap<Player, PotionEffect>();

	@HookHandler
	public void onPotionEffectApplied(PotionEffectAppliedHook hook) {
		if (hook.getEntity().isPlayer()) {
			Player player = (Player) hook.getEntity();
			this.lastPotion.put(player, hook.getPotionEffect());
		}
	}

	HashMap<Player, Long> health = new HashMap<Player, Long> ();
	HashMap<Player, Integer> healthamount = new HashMap<Player, Integer>();

	@HookHandler
	public void onHealthChange(HealthChangeHook hook) {
		Player player = hook.getPlayer();
		if (player.getHealth() > hook.getOldValue()) {
			if (this.health.containsKey(player)) {
				if ((this.lastPotion.containsKey(player))
						&& (((PotionEffect) this.lastPotion.get(player))
								.getName().endsWith("potion.heal"))) {
					this.lastPotion.remove(player);
					return;
				}
				if (!player.getAllActivePotionEffects().isEmpty()) {
					for (int i = 0; i < player.getAllActivePotionEffects()
							.size(); i++) {
						PotionEffect effect = (PotionEffect) player
								.getAllActivePotionEffects().get(i);
						if (effect.getName().equalsIgnoreCase(
								"potion.regeneration")) {
							return;
						}
					}
				}
				float diff = (float) (System.currentTimeMillis() - ((Long) this.health
						.get(player)).longValue());
				float healthdiff = player.getHealth() - hook.getOldValue();
				String str = "[health] ";
				if ((healthdiff <= 1.0F) && (healthdiff > 0.99D)) {
					this.health.put(player,
							Long.valueOf(System.currentTimeMillis()));
					int toadd = 0;
					if (diff < 3200.0F) {
						str = str.concat("§4" + diff);
						toadd = 4;
					} else if (diff < 3400.0F) {
						str = str.concat("§c" + diff);
						toadd = 3;
					} else if (diff < 3600.0F) {
						str = str.concat("§6" + diff);
						toadd = 2;
					} else if (diff < 3900.0F) {
						str = str.concat("§e" + diff);
						toadd = 1;
					} else {
						str = str.concat("§2" + diff);
						toadd = -2;
					}
					if (this.healthamount.containsKey(player)) {
						if (((Integer) this.healthamount.get(player))
								.intValue() > 10) {
							addToBan(player, 1);
							player.message("§4Your hp is regenerating too fast ("
									+ diff + ")");
							hook.setCanceled();
							this.health.remove(player);
							this.healthamount.remove(player);
							alert(player.getName() + ": Health regenerating too fast.");
						} else {
							this.healthamount.put(player, Integer
									.valueOf(((Integer) this.healthamount
											.get(player)).intValue() + toadd));
							if (((Integer) this.healthamount.get(player))
									.intValue() < 0) {
								this.healthamount.put(player,
										Integer.valueOf(0));
							}
							str = str.concat("("
									+ this.healthamount.get(player) + ")");
						}
					} else {
						this.healthamount.put(player, Integer.valueOf(toadd));
						this.health.remove(player);
					}
				}
				castToAdmins(player, str);
			} else {
				this.health.put(player,
						Long.valueOf(System.currentTimeMillis()));
			}
		}
	}

	@HookHandler
	public void onEntityMove(EntityMoveHook hook) {
		if (hook.getEntity().isMob()) {
			EntityMob mob = (EntityMob) hook.getEntity();
			if (mob.hasDisplayName()) {
				if (mob.getDisplayName().equals("n")) {
					mob.teleportTo(mob.getX(), mob.getY()+0.9, mob.getZ());
					hook.setCanceled();
				}
			}
		}
	}

	public void setProperty(Player player, String key, String value) {
		try {
			File file = new File("config/nocheat/config/nocheat/players/" + player.getName() + ".txt");
			File dir = new File("config/nocheat/");
			if (!dir.exists()) {
				dir.mkdir();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileInputStream in = new FileInputStream("config/nocheat/" + player.getName() + ".txt");
			Properties props = new Properties();
			props.load(in);
			in.close();
			props.remove(key);
			FileOutputStream out = new FileOutputStream("config/nocheat/" + player.getName() + ".txt");
			props.setProperty(key, value);
			props.store(out, null);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getProperty(Player player, String key) {
		try {
			FileInputStream in = new FileInputStream(
					"config/nocheat/config/nocheat/players/" + player.getName()
							+ ".txt");
			Properties props = new Properties();
			props.load(in);
			String value = props.getProperty(key);
			in.close();
			return value;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
