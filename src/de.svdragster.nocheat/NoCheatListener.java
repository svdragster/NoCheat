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
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.canarymod.Canary;
import net.canarymod.LineTracer;
import net.canarymod.api.DamageType;
import net.canarymod.api.GameMode;
import net.canarymod.api.entity.Arrow;
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
import net.canarymod.tasks.ServerTask;
import net.visualillusionsent.utils.PropertiesFile;

public class NoCheatListener implements PluginListener {

	public static final String PERMISSION_ADMIN =  "nocheat.admin.";
	public static final String PERMISSION_CHECKFORUPDATES =  "checkforupdates";
	private static final String USER_AGENT = "canary_minecraft";
	public static final String VERSION = new NoCheat().getVersion();
	
	public ServerTask task = new ServerTask(new NoCheat(), AntiForcefieldTime*20*60, true) { // 20 ticks are 1 second, the time set in AntiForcefieldTime is in minutes
		
		@Override
		public void run() {
			AntiForceField(null, AntiForcefieldType);
		}
	};
	
	public final static String DIR = "config/nocheat/";
	public final static String PLAYERS = DIR + "players/";
	
	//public static HashMap<Player, EntityLiving> mobs = new HashMap<Player, EntityLiving>();
	
	//HashMap<Player, Integer> level = new HashMap<Player, Integer>();
	
	public static HashMap<Player, ArrayList<Player>> display = new HashMap<Player, ArrayList<Player>>();
	public static HashMap<Player, String> displaytype = new HashMap<Player, String>();
	
	HashMap<Player, Long> TempBans = new HashMap<Player, Long>();
	HashMap<Player, String> TempReasons = new HashMap<Player, String>();
	
	HashMap<Player, Long> map = new HashMap<Player, Long>();
	
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
	
	public int random(int numOne, int numTwo) {
		int RandomNumber = (int) (Math.random()*numOne+numTwo);
		return RandomNumber;
	}
	
	HashMap<Player, Long> slowdown = new HashMap<Player, Long>();
	HashMap<Player, Integer> amountslowdown = new HashMap<Player, Integer>();
	
	HashMap<Player, Integer> walk = new HashMap<Player, Integer>();
	
	HashMap<Player, Long> lastattack = new HashMap<Player, Long>();
	
	public void castToAdmins(Player toWatch, String str) {
		for (Player player : display.keySet()) {
			if (player.equals(toWatch)) {
			    ArrayList<Player> admins = new ArrayList<Player>();
			    admins.addAll(display.get(player));
			    for (int i=0; i<admins.size(); i++) {
			    	if (displaytype.containsKey(admins.get(i))) {
			    		String[] type = displaytype.get(admins.get(i)).toLowerCase().split(",");
			    		
			    		adminLoop:
			    		for (int s=0; s<type.length; s++) {
			    			String strType = type[s];
			    			if (strType != null) {
				    			if (str.toLowerCase().contains(strType)) {
				    				admins.get(i).message(Colors.ORANGE + "[NoCheat]" + Colors.WHITE + player.getName() + ": " + Colors.LIGHT_RED + str);
				    				continue adminLoop;
				    			}
			    			}
			    		}
			    		
			    	} else {
			    		admins.get(i).message(Colors.ORANGE + "[NoCheat]" + Colors.WHITE + player.getName() + ": " + Colors.LIGHT_RED + str);
			    	}
			    }
			}
		}
	}
	
	public static boolean isNumeric(String str) {  
		try {  
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);  
		} catch(NumberFormatException nfe) {  
			return false;  
		}  
		return true;  
	}
	
	public List<World> getWorldFromString(String str) {
		String[] strings = str.split(";");
		List<World> worlds = new ArrayList<World>();
		for (int i=0; i<strings.length; i++) {
			World world = Canary.getServer().getWorld(TextFormat.removeFormatting(strings[i]));
			worlds.add(world);
		}
		return worlds;
	}
	
	public void loadProperties() {
		File file = new File(DIR + "nocheat.properties");
		File dir = new File(DIR);
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
		PropertiesFile prop = new PropertiesFile(DIR + "nocheat.properties");
		prop.addHeaderLines("Edit the NoCheat settings here.");
		String defaultworldname = Canary.getServer().getDefaultWorldName() + ";";
		if (!prop.containsKey("FastBreak")) {
			prop.setString("FastBreak", defaultworldname);
			prop.addComment("FastBreak", "Disabled by default because it's buggy");
		}	
		if (!prop.containsKey("FastBuild"))
			prop.setString("FastBuild", defaultworldname);
		if (!prop.containsKey("Nuker"))
			prop.setString("Nuker", defaultworldname);
		if (!prop.containsKey("NoSlowdown"))
			prop.setString("NoSlowdown", defaultworldname);
		if (!prop.containsKey("Jump"))
			prop.setString("Jump", defaultworldname);
		if (!prop.containsKey("Flying"))
			prop.setString("Flying", defaultworldname);
		if (!prop.containsKey("Speed"))
			prop.setString("Speed", defaultworldname);
		if (!prop.containsKey("NoFall"))
			prop.setString("NoFall", defaultworldname);
		if (!prop.containsKey("Forcefield"))
			prop.setString("Forcefield", defaultworldname);
		if (!prop.containsKey("AntiForcefield")) {
			prop.setString("AntiForcefield", defaultworldname);
			prop.addComment("AntiForcefield", "Spawns a zombie/npc every x seconds next to a random player, only when no mobs or players are nearby");
		}
		prop.save();
		if (!prop.containsKey("AntiForcefieldType")) {
			prop.setInt("AntiForcefieldType", 1);
			prop.addComment("AntiForcefieldType", "Set if a zombie or npc should spawn: 1 is zombie, 2 is npc, 0 is both");
		}
		if (!prop.containsKey("AntiForcefieldTime")) {
			prop.setInt("AntiForcefieldTime", 10);
			prop.addComment("AntiForcefieldTime", "Set when the AntiForcefield zombie/npc should spawn in minutes.");
		}
		if (!prop.getString("FastBreak").isEmpty()) {
			String str = prop.getString("FastBreak");
			List<World> worlds = getWorldFromString(str);
			FastBreak.addAll(worlds);
		}
		if (!prop.getString("FastBuild").isEmpty()) 
			FastBuild.addAll(getWorldFromString(prop.getString("FastBuild")));
		if (!prop.getString("Nuker").isEmpty()) 
			Nuker.addAll(getWorldFromString(prop.getString("Nuker")));
		if (!prop.getString("NoSlowdown").isEmpty()) 
			NoSlowdown.addAll(getWorldFromString(prop.getString("NoSlowdown")));
		if (!prop.getString("Jump").isEmpty())
			Jump.addAll(getWorldFromString(prop.getString("Jump")));
		if (!prop.getString("Flying").isEmpty())
			Flying.addAll(getWorldFromString(prop.getString("Flying")));
		if (!prop.getString("Speed").isEmpty())
			Speed.addAll(getWorldFromString(prop.getString("Speed")));
		if (!prop.getString("NoFall").isEmpty())
			NoFall.addAll(getWorldFromString(prop.getString("NoFall")));
		if (!prop.getString("Forcefield").isEmpty())
			Forcefield.addAll(getWorldFromString(prop.getString("Forcefield")));
		if (!prop.getString("AntiForcefield").isEmpty())
			AntiForcefield.addAll(getWorldFromString(prop.getString("AntiForcefield")));
		
		AntiForcefieldTime = prop.getInt("AntiForcefieldTime");
		AntiForcefieldType = prop.getInt("AntiForcefieldType");
		
		//Canary.getServer().addSynchronousTask(task);
	}
	
	public void AntiForceField(Player p, int type) { // if player is null a random player gets selected. Type 1 is a zombie, type 2 is a npc, type 0 are both(random) 
		Player player;
		if (AntiForcefield.isEmpty()) {
			return;
		}
		if (p == null) {
			List<Player> players = Canary.getServer().getPlayerList();
			if (players.size() > 0) {
				player = players.get(random(players.size(), 0));
			} else {
				return;
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
		switch(player.getCardinalDirection().getIntValue()) { 
        case 0: 
            if (z >= 0) {
            	z = z + 1;
            } else {
            	z = z + 1;
            }
            break; 
        case 1: 
        	 if (z >= 0) {
             	z = z + 1;
             } else {
             	z = z + 1;
             }
        	 if (x >= 0) {
        		 x = x - 1;
        	 } else {
        		 x = x + 1;
        	 }
            break; 
        case 2: 
            if (x >= 0) {
            	x = x - 1;
            } else {
            	x = x + 1;
            }
            break; 
        case 3:
        	 if (z >= 0) {
              	z = z - 1;
              } else {
              	z = z + 1;
              }
         	 if (x >= 0) {
         		 x = x - 1;
         	 } else {
         		 x = x + 1;
         	 }
            break; 
        case 4:
        	 if (z >= 0) {
             	z = z - 1;
             } else {
             	z = z - 1;
             }
        	 break;
        case 5:
        	 if (z >= 0) {
              	z = z - 1;
             } else {
              	z = z + 1;
             }
         	 if (x >= 0) {
         		 x = x + 1;
         	 } else {
         		 x = x - 1;
         	 }
         	 break;
        case 6:
        	if (x >= 0) {
            	x = x + 1;
            } else {
            	x = x - 1;
            }
        	break;
        case 7:
        	 if (z >= 0) {
              	z = z - 1;
              } else {
              	z = z + 1;
              }
         	 if (x >= 0) {
         		 x = x + 1;
         	 } else {
         		 x = x - 1;
         	 }
         	 break;
		} 
		Location loc = new Location(x, y, z);
		EntityFactory factory = Canary.factory().getEntityFactory();
		//Vector3D v = new Vector3D(x, 0, y);
		//Vector3D playerv = new Vector3D(p.getX(), p.getY(), p.getZ());
		//p.message(x + ", " + y + ", " + z + ", " + playerv.subtract(v));
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
		//p.message(p.getX()-v.getX() + ", " + p.getY() + ", " + p.getZ());
		mob.setDisplayName(player.getName());
		mob.setHealth(100.0f);
		mob.addPotionEffect(PotionEffectType.WEAKNESS, 1000, 10);
		mob.addPotionEffect(PotionEffectType.MOVESLOWDOWN, 1000, 10);
		mob.addPotionEffect(PotionEffectType.INVISIBILITY, 1000, 0);
		mob.setShowDisplayName(false);
		mob.spawn();
		new Thread() {
		     public void run() {
		          try{  
		        	  Thread.sleep(400);
		        	  mob.destroy();
		          }catch(InterruptedException e) {}
		     }
		}.start();
		
	}
	
	@HookHandler
	public void onLogin(ConnectionHook hook) {
		Player player = hook.getPlayer();
		if (TempBans.containsKey(player)) {
			long time = TempBans.get(player);
			if (System.currentTimeMillis() < time) {
				hook.setHidden(true);
				int back = (int) ((int) time-System.currentTimeMillis());
				back = back/1000/60;
				String reason = "N/A";
				if (TempReasons.containsKey(player)) {
					reason = TempReasons.get(player);
				}
				player.kickNoHook(Colors.RED + "You have been automatically tempbanned." + Colors.ORANGE + "\nReason: " + Colors.YELLOW + reason + Colors.RED + "\n\nYou can join again in " + back + " minute(s).");
			} else {
				TempBans.remove(player);
				TempReasons.remove(player);
			}
		}
		if (hook.getPlayer().hasPermission(PERMISSION_ADMIN + PERMISSION_CHECKFORUPDATES)) {
			try {
				String result = sendGet();
				if (result != null) {
					if (!result.isEmpty()) {
						hook.getPlayer().message(result);
						hook.getPlayer().message("Or you can check the forum post.");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String sendGet() throws Exception {
		String MYIDSTART = "svdragster>";
		String MYIDEND = "<svdragster";
		String url = "http://sv.slyip.net/checkupdate.php?version=" + VERSION + "&plugin=nocheat";
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		String result = response.toString();
		if (result.contains(MYIDSTART) && result.contains(MYIDEND)) {
			int endPos = result.indexOf(MYIDEND);
			result = Colors.ORANGE + "<NoCheat> " + Colors.GREEN + "Update available at: " + Colors.WHITE + result.substring(MYIDSTART.length(), endPos);
		}
		return result;
	}
	
	@HookHandler
	public void onLogout(DisconnectionHook hook) {
		if (TempBans.containsKey(hook.getPlayer())) {
			hook.setHidden(true);
		}
	}
	
	HashMap<Player, Integer> ban = new HashMap<Player, Integer>();
	HashMap<Player, Long> bantime = new HashMap<Player, Long>(); //If the last addToBan was more than 5 minutes ago it sets ban to 0
	int maxbans = 20; //The maximal amount a player can receive until he gets a temporary ban
	
	public void addToBan(Player player, int num) {
		if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
			if (ban.containsKey(player)) {
				if (bantime.containsKey(player)) {
					long diff = System.currentTimeMillis() - bantime.get(player);
					if (diff > 300000) { // 5 minutes
						ban.remove(player);
						bantime.remove(player);
						return;
					}
				}
				int n = ban.get(player);
				int newnumber = n + num;
				castToAdmins(player, "ban amount: " + newnumber);
				if (newnumber > maxbans) {
					player.kick(Colors.RED + "Hacked Client detected." + Colors.YELLOW + "\nYou have been banned for 1 hour." + Colors.YELLOW + "\nThis has been recorded and saved.");
					TempBans.put(player, System.currentTimeMillis()+60*60*1000);
					TempReasons.put(player, "Hacked Client");
					String property = getProperty(player, "hacks");
					int tempnum = 1;
					if (property != null) {
						if (isNumeric(property)) {
							tempnum = Integer.parseInt(property) + 1;
						}
					}
					setProperty(player, "hacks", tempnum + "");
					ban.remove(player);
				} else {
					ban.put(player, newnumber);
				}
			} else {
				ban.put(player, num);
			}
			bantime.put(player, System.currentTimeMillis());
		} else {
			player.message(Colors.RED + "A ban would have been added (" + num + ")");
		}
	}
	
	HashMap<Player, Integer> fallen = new HashMap<Player, Integer>();
	HashMap<Player, Integer> flying = new HashMap<Player, Integer>();
	HashMap<Player, Integer> jesus = new HashMap<Player, Integer>();
	
	@HookHandler
	public void onPlayerMove(PlayerMoveHook hook) {
		final Player player = hook.getPlayer();
		World world = player.getWorld();
		if (map.containsKey(player)) {
			int mobcount = CountNearbyMobs(player);
			double temp = hook.getTo().getY() - hook.getFrom().getY();
			boolean negative = false;
			if (temp < 0) {
				negative = true;
			}
			/**
			 * Against Spider Hack
			 */
			double diffy = Math.abs(temp);
			if (!Flying.isEmpty()) {
				if (Flying.contains(world)) {
					if (diffy < 0.21 && diffy > 0.2) {
						if (!negative) {
							if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
								if (lastattack.containsKey(player)) {
									if (System.currentTimeMillis() - lastattack.get(player) > 5000) {
										if (mobcount < 1) {
											hook.setCanceled();
										}
									}
								} else {
									hook.setCanceled();
								}
							}
						}
					}
					if (diffy >= 0 && diffy <= 0.1) {
						if (!player.getMode().equals(GameMode.CREATIVE) && !player.getCapabilities().mayFly()) {
							if (!negative) {
								Block block = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-1, (int) player.getZ());
								Block block2 = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-2, (int) player.getZ());
								Block block3 = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY(), (int) player.getZ()); // prevents player kicking when walking over halfslabs
								Block front = player.getWorld().getBlockAt((int) player.getX()+1, (int) player.getY(), (int) player.getZ());
								Block back = player.getWorld().getBlockAt((int) player.getX()-1, (int) player.getY(), (int) player.getZ());
								Block left = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY(), (int) player.getZ()+1);
								Block right = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY(), (int) player.getZ()-1);
								if (block.isAir() && block2.isAir() && block3.isAir() && front.isAir() && back.isAir() && left.isAir() && right.isAir()) {
									if (flying.containsKey(player)) {
										flying.put(player, flying.get(player) + 1);
										if (flying.get(player) > 5) {
											if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
												if (lastattack.containsKey(player)) {
													if (System.currentTimeMillis() - lastattack.get(player) > 8000) {
														return;
													}
												}
												player.kick(Colors.RED + "You have been kicked for flying.\n" + Colors.YELLOW + "This has been recorded and saved.");
												addToBan(player, 4);
											} else {
												player.message(Colors.RED + "You would have been kicked for flying.");
											}
											flying.remove(player);
										}
									} else {
										flying.put(player, 1);
									}
								} else {
									flying.remove(player);
								}
							}
						}
					}
					if (diffy == 0) {
						if (!player.getMode().equals(GameMode.CREATIVE) && !player.getCapabilities().mayFly()) {
							Block blockbelow = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-1, (int) player.getZ());
							Block blockbelow2 = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-2, (int) player.getZ());
							Block block = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY(), (int) player.getZ()); // prevents player kicking when walking over halfslabs
							if (block.isAir() && (blockbelow.getType().equals(BlockType.Water) || blockbelow2.getType().equals(BlockType.Water))) {
								Block front = player.getWorld().getBlockAt((int) player.getX()+1, (int) player.getY()-1, (int) player.getZ());
								Block back = player.getWorld().getBlockAt((int) player.getX()-1, (int) player.getY()-1, (int) player.getZ());
								Block left = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-1, (int) player.getZ()+1);
								Block right = player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-1, (int) player.getZ()-1);
								if (front.getType().equals(BlockType.Water) && right.getType().equals(BlockType.Water) && left.getType().equals(BlockType.Water) && back.getType().equals(BlockType.Water)) {
									if (jesus.containsKey(player)) {
										jesus.put(player, jesus.get(player) + 1);
										if (jesus.get(player) > 7) {
											if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
												if (lastattack.containsKey(player)) {
													if (System.currentTimeMillis() - lastattack.get(player) > 8000) {
														return;
													}
												}
												player.kick(Colors.RED + "Jesus hacks detected.\n" + Colors.YELLOW + "This has been recorded and saved.");
												addToBan(player, 5);
											} else {
												player.message(Colors.RED + "You would have been kicked for jesus hacks.");
											}
											jesus.remove(player);
										}
									} else {
										jesus.put(player, 1);
									}
								}
							} else {
								jesus.remove(player);
							}
						}
					}
				}
			}
			if (!player.getMode().equals(GameMode.CREATIVE) && !player.getCapabilities().mayFly()) {
				if (!NoFall.isEmpty()) {
					if (NoFall.contains(world)) {
						if (negative) {
							if (fallen.containsKey(player)) {
								fallen.put(player, fallen.get(player)+1);
							} else {
								fallen.put(player, 1);
							}
						}
						if (fallen.containsKey(player)) {
							if (fallen.get(player) > 6) {
								if (!player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-2, (int) player.getZ()).getType().equals(BlockType.Air)) {
									if (lastDamageType.containsKey(player)) {
										new Thread() {
										     public void run() {
										          try{  
										        	 Thread.sleep(100);
										        	 if (!lastDamageType.get(player).equals(DamageType.FALL)) {
										        		 //player.message(Colors.RED + "NoFall!!!");
										        		 //player.addPotionEffect(PotionEffectType.HARM, 1, 1);
										        		 //addToBan(player, 1);
										        		 castToAdmins(player, Colors.RED + "[move] NoFall");
										        	 } else {
										        		 lastDamageType.put(player, DamageType.GENERIC);
										        	 }
										          } catch(InterruptedException e) {}
										     }
										}.start();	
									} else {
										lastDamageType.put(player, DamageType.GENERIC);
									}
									fallen.remove(player);
								}
							}
						}
						if (!player.getWorld().getBlockAt((int) player.getX(), (int) player.getY()-1, (int) player.getZ()).getType().equals(BlockType.Air)) {
							fallen.remove(player);
						}
					}
				}
			}
			if (!negative) {
				if (diffy > 0.52) {
					if (!player.getMode().equals(GameMode.CREATIVE)) {
						if (!Jump.isEmpty()) {
							if (Jump.contains(world)) {
								if (lastattack.containsKey(player)) {
									if (System.currentTimeMillis() - lastattack.get(player) > 3000) {
										if (mobcount < 1) {
											hook.setCanceled();
											addToBan(player, 1);
											castToAdmins(player, Colors.RED + "diffy > 0.52");
										}
									}
								} else {
									hook.setCanceled();
								}
							}
						}
					}
				}
			}
			Vector3D v = new Vector3D(hook.getTo().getX(), 0, hook.getTo().getZ());
			Vector3D from = new Vector3D(hook.getFrom());
			from.setY(0);
			double distance = v.getDistance(from);
			long difftime = System.currentTimeMillis() - map.get(player);
			if (difftime == 0) {
				difftime = 120;
			}
			if (distance == 0) {
				distance = 0.22;
			}
			double result = difftime/distance;
			String str = "[move]";
			//player.message(difftime + " / " + distance + " = " + result + Colors.ORANGE + " " + diffy);
			if (diffy < 0.5) {
				if (player.isBlocking()) {
					if (!NoSlowdown.isEmpty()) {
						if (NoSlowdown.contains(world)) {
							if (!negative) {
								if (result < 2000) {
									if (amountslowdown.containsKey(player)) {
										amountslowdown.put(player, amountslowdown.get(player) + 1);
										if (amountslowdown.get(player) > 4) {
											if (slowdown.containsKey(player)) {
												long diffs = System.currentTimeMillis() - slowdown.get(player);
												if (diffs < 2000) {
													str = str.concat(Colors.RED + " (noslowdown) " + result + " < 2000");
													if (lastattack.containsKey(player)) {
														if (System.currentTimeMillis() - lastattack.get(player) > 2000) {
															if (mobcount < 1) {
																
																hook.setCanceled();
															}
														}
													} else {
														hook.setCanceled();
													}
													amountslowdown.put(player, 1);
													slowdown.remove(player);
												} else {
													slowdown.put(player, System.currentTimeMillis());
												}
											} else {
												slowdown.put(player, System.currentTimeMillis());
											}
										}
									} else {
										amountslowdown.put(player, 1);
									}
								}
							}
						}
					}
				}
			} else if (diffy > 0.8) {
				if (!Flying.isEmpty()) {
					if (Flying.contains(player)) {
						if (!negative) {
							if (!player.isBlocking()) {
								if (result < 300 && result > 2.5) {
									str = str.concat(Colors.RED + " (speed) " + diffy + " > 0.8, " + result + " < 300");
									if (hook.getPlayer().getMode() != GameMode.CREATIVE) {
										//player.message(Colors.RED + "Fly hacks!!!");
										if (lastattack.containsKey(player)) {
											if (System.currentTimeMillis() - lastattack.get(player) > 4000) {
												if (mobcount < 1) {
													hook.setCanceled();
												}
											}
										} else {
											hook.setCanceled();
										}
									}
								}
							}
						}
					}
				}
			}
			if (!negative) {
				if (player.isSneaking()) {
					if (!NoSlowdown.isEmpty()) {
						if (NoSlowdown.contains(world)) {
							if (result < 600) {
								str = str.concat(Colors.RED + " (sneak) " + result + " < 600");
								if (player.getMode() != GameMode.CREATIVE) {
									//player.message(Colors.RED + "You are sneaking too fast!!!");
									if (lastattack.containsKey(player)) {
										if (System.currentTimeMillis() - lastattack.get(player) > 2000) {
											if (mobcount < 1) {
												hook.setCanceled();
											}
										}
									} else {
										hook.setCanceled();
									}
								}
							}
						}
					}
				}
			}
			int number = 0;
			if (player.isSprinting()) {
				number = 48;
			} else {
				number = 200;
			}
			if (!Speed.isEmpty()) {
				if (Speed.contains(world)) {
					if ((result > 2.5) && (result < number)) {
						str = str.concat(Colors.RED + " (speed) " + result + " < " + number);
						if (!negative) {
							if (!hook.getPlayer().isBlocking()) {
								if (hook.getPlayer().getMode() != GameMode.CREATIVE && !hook.getPlayer().getCapabilities().mayFly()) {
									if (walk.containsKey(player)) {
										walk.put(player, walk.get(player) + 1);
										if (walk.get(player) > 2) {
											if (lastattack.containsKey(player)) {
												if (System.currentTimeMillis() - lastattack.get(player) > 5000) {
													if (mobcount < 1) {
														hook.setCanceled();
														player.message(Colors.RED + "Too fast!!!");
													}
												}
											} else {
												hook.setCanceled();
											}
										}
									} else {
										walk.put(player, 1);
									}
								}
							}
						}
					} else {
						if (walk.containsKey(player)) {
							int num = walk.get(player) - 1;
							if (num < 0) {
								num = 0;
							}
							walk.put(player, num);
						}
					}
				}
			}
			if (!str.contains("(speed)")) {
				str = str.concat(Colors.GREEN + "(speed) " + result);
			}
			castToAdmins(player, str);
		}
		map.put(player, System.currentTimeMillis());
		//EntityFactory factory = Canary.factory().getEntityFactory();
		/*if (random(20, 1) == 2) {
			//for (int i=0; i<Canary.getServer().getPlayerList().size(); i++) {
			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();
			switch(player.getCardinalDirection().getIntValue()) { 
	        case 0: 
	            if (z >= 0) {
	            	z = z + 1;
	            } else {
	            	z = z - 1;
	            }
	            break; 
	        case 1: 
	        	 if (z >= 0) {
	             	z = z + 1;
	             } else {
	             	z = z - 1;
	             }
	        	 if (x >= 0) {
	        		 x = x - 1;
	        	 } else {
	        		 x = x + 1;
	        	 }
	            break; 
	        case 2: 
	            if (x >= 0) {
	            	x = x - 1;
	            } else {
	            	x = x + 1;
	            }
	            break; 
	        case 3:
	        	 if (z >= 0) {
	              	z = z - 1;
	              } else {
	              	z = z + 1;
	              }
	         	 if (x >= 0) {
	         		 x = x - 1;
	         	 } else {
	         		 x = x + 1;
	         	 }
	            break; 
	        case 4:
	        	 if (z >= 0) {
	             	z = z - 1;
	             } else {
	             	z = z + 1;
	             }
	        	 break;
	        case 5:
	        	 if (z >= 0) {
	              	z = z - 1;
	             } else {
	              	z = z + 1;
	             }
	         	 if (x >= 0) {
	         		 x = x + 1;
	         	 } else {
	         		 x = x - 1;
	         	 }
	         	 break;
	        case 6:
	        	if (x >= 0) {
	            	x = x + 1;
	            } else {
	            	x = x - 1;
	            }
	        	break;
	        case 7:
	        	 if (z >= 0) {
	              	z = z - 1;
	              } else {
	              	z = z + 1;
	              }
	         	 if (x >= 0) {
	         		 x = x + 1;
	         	 } else {
	         		 x = x - 1;
	         	 }
	         	 break;
			} 
			Location loc = new Location(x, y, z);
			player.message(loc + "");
			//Vector3D v = new Vector3D(x, 0, y);
			//Vector3D playerv = new Vector3D(p.getX(), p.getY(), p.getZ());
			//p.message(x + ", " + y + ", " + z + ", " + playerv.subtract(v));
			final EntityLiving mob = factory.newEntityMob(EntityType.ZOMBIE, loc);
			//p.message(p.getX()-v.getX() + ", " + p.getY() + ", " + p.getZ());
			mob.setDisplayName(player.getName());
			mob.setHealth(0.5f);
			mob.addPotionEffect(PotionEffectType.WEAKNESS, 1000, 10);
			mob.addPotionEffect(PotionEffectType.MOVESLOWDOWN, 1000, 10);
			mob.addPotionEffect(PotionEffectType.INVISIBILITY, 1000, 0);
			mob.spawn();
			new Thread() {
			     public void run() {
			          try{  
			        	  Thread.sleep(250);
			        	  mob.destroy();
			          }catch(InterruptedException e) {}
			     }
			}.start();
			//}
		}*/
	}
	
	HashMap<Player, Long> arrow = new HashMap<Player, Long>();
	HashMap<Player, Integer> arrowcount = new HashMap<Player, Integer>();
	
	@HookHandler
	public void onEntitySpawn(EntitySpawnHook hook) {
		if (hook.getEntity().getEntityType().equals(EntityType.ARROW)) {
			String str = "[combat]";
			Arrow a = (Arrow) hook.getEntity();
			if (a.getOwner() != null) {
				if (a.getOwner().isPlayer()) {
					Player player = (Player) a.getOwner();
					//player.message(a.getMotionX() + ", " + a.getMotionY() + ", " + a.getMotionZ());
					if (arrow.containsKey(player)) {
						long diff = System.currentTimeMillis() - arrow.get(player);
						//player.message(Colors.BLUE + diff);
						
						if (diff < 230 && diff > 32) {
							if (arrowcount.containsKey(player)) {
								arrowcount.put(player, arrowcount.get(player)+5);
								if (arrowcount.get(player) > 20) {
									str = str.concat(Colors.RED + " (arrow) " + diff + " < 230");
									addToBan(player, 5);
								}
							} else {
								arrowcount.put(player, 5);
							}
						} else {
							str = str.concat(Colors.GREEN + "(arrow) " + diff + " > 230");
						}
					}
					if (arrowcount.containsKey(player)) {
						arrowcount.put(player, arrowcount.get(player)-1);
						if (arrowcount.get(player) < 0) {
							arrowcount.put(player, 0);
						}
					}
					arrow.put(player, System.currentTimeMillis());
					castToAdmins(player, str);
				}
			}
		}
	}
	
	HashMap<Player, Boolean> inventory = new HashMap<Player, Boolean>();
	
	@HookHandler
	public void onInventory(InventoryHook hook) {
		if (hook.isClosing()) {
			inventory.put(hook.getPlayer(), false);
		} else {
			inventory.put(hook.getPlayer(), true);
		}
	}
	
	public int CountNearbyMobs(Player player) {
		int count = 0;
		List<EntityMob> mobs = player.getWorld().getMobList();
		for (int i=0; i<mobs.size(); i++) {
			Vector3D v = new Vector3D(mobs.get(i).getX(),mobs.get(i).getY(), mobs.get(i).getZ());
			if (v.getDistance(player.getPosition()) <= 5) {
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
		if (hook.getAttacker() != null) {
			if (hook.getAttacker().isPlayer()) {
				Player player = (Player) hook.getAttacker();
				if (inventory.containsKey(player)) {
					if (inventory.get(player)) {
						player.addPotionEffect(PotionEffectType.WEAKNESS, 20, 100);
						player.message(Colors.RED + "You are hitting while an inventory is opened!");
						castToAdmins(player, "[combat] Hitting while an inventory is opened");
						hook.setCanceled();
						if (!Forcefield.isEmpty()) {
							if (Forcefield.contains(player)) {
								if (ff.containsKey(player)) {
									ff.put(player, ff.get(player) + 1);
									if (ff.get(player) > 3) {
										if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
											addToBan(player, 5);
											player.kick(Colors.RED + "Disable your combat hacks or check your ping." + Colors.YELLOW + "\nYou have been banned for 10 minutes." + Colors.YELLOW + "\nThis has been recorded and saved.");
											TempBans.put(player, System.currentTimeMillis()+10*60*1000);
											TempReasons.put(player, "Combat Hacks");
											String property = getProperty(player, "combat-hacks");
											int num = 1;
											if (property != null) {
												if (isNumeric(property)) {
													num = Integer.parseInt(property) + 1;
												}
											}
											setProperty(player, "combat-hacks", num + "");
										} else {
											player.message(Colors.RED + "You would have been tempbanned for combat hacks.");	
										}
									}
								} else {
									ff.put(player, 0);
								}
							}
						}
					}
				}
			}	
		}
		if (hook.getDefender().isPlayer()) {
			Player player = (Player) hook.getDefender();
			lastDamageType.put(player, hook.getDamageSource().getDamagetype());
			lastattack.put(player, System.currentTimeMillis());
		}
		if (hook.getDefender().isMob()) {
			EntityMob mob = (EntityMob) hook.getDefender();
			if (hook.getDamageSource() != null) {
				if (hook.getDamageSource().getDamageDealer() != null) {
					if (hook.getDamageSource().getDamageDealer().isPlayer()) {
						Player player = (Player) hook.getDamageSource().getDamageDealer();
						float maxrange = 5.7f;
						Vector3D v = new Vector3D(player.getLocation());
						double distance = v.getDistance(mob.getPosition());
						if (distance > maxrange) {
							if (hitsanzahl.containsKey(player)) {
								hitsanzahl.put(player, hitsanzahl.get(player) + 10);
								castToAdmins(player, "Range: " + Colors.RED + distance);
							} else {
								hitsanzahl.put(player, 0);
							}
						}
						
						if (hits.containsKey(player)) {
							long diff = System.currentTimeMillis() - hits.get(player);
							//player.message(diff + "");
							if (diff < 148 && diff > 20) {
								castToAdmins(player, "[combat] Damaging too fast: " + Colors.RED + diff);
								if (hitsanzahl.containsKey(player)) {
									hitsanzahl.put(player, hitsanzahl.get(player) + 5);
									if (hitsanzahl.get(player) >= 25) {
										hitsanzahl.put(player, 20);
										if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
											addToBan(player, 5);
											player.kick(Colors.RED + "Disable your combat hacks or check your ping." + Colors.YELLOW + "\nYou have been banned for 15 minutes." + Colors.YELLOW + "\nThis has been recorded and saved.");
											TempBans.put(player, System.currentTimeMillis()+15*60*1000);
											TempReasons.put(player, "Combat Hacks");
											String property = getProperty(player, "combat-hacks");
											int num = 1;
											if (property != null) {
												if (isNumeric(property)) {
													num = Integer.parseInt(property) + 1;
												}
											}
											setProperty(player, "combat-hacks", num + "");
										} else {
											player.message(Colors.RED + "You would have been tempbanned for combat hacks.");	
										}
									}
								} else {
									hitsanzahl.put(player, 1);
								}
							} else {
								if (hitsanzahl.containsKey(player)) {
									hitsanzahl.put(player, hitsanzahl.get(player) - 1);
								}
							}
						}
						hits.put(player, System.currentTimeMillis());
					}
				}
			}
			if (mob.hasDisplayName()) {
				if (hook.getDamageSource().getDamageDealer() != null) {
					if (hook.getDamageSource().getDamageDealer().isPlayer()) {
						Player player = (Player) hook.getDamageSource().getDamageDealer();
						if (mob.getDisplayName().equalsIgnoreCase(player.getName())) {
							if (CountNearbyMobs(player) == 2) {
								if (ff.containsKey(player)) {
									ff.put(player, ff.get(player) + 1);
									if (ff.get(player) > 3) {
										if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
											player.kick(Colors.RED + "Disable your combat hacks or check your ping." + Colors.YELLOW + "\nYou have been banned for 10 minutes." + Colors.YELLOW + "\nThis has been recorded and saved.");
											TempBans.put(player, System.currentTimeMillis()+10*60*1000);
											TempReasons.put(player, "Combat Hacks");
											String property = getProperty(player, "combat-hacks");
											int num = 1;
											if (property != null) {
												if (isNumeric(property)) {
													num = Integer.parseInt(property) + 1;
												}
											}
											setProperty(player, "combat-hacks", num + "");
										} else {
											player.message(Colors.RED + "You would have been tempbanned for combat hacks.");	
										}
									}
								} else {
									ff.put(player, 0);
								}
							}
						}
					}
				}
			}
		}
	}
	
	HashMap<Player, Long> blocks = new HashMap<Player, Long>();
	HashMap<Player, Integer> blocksamount = new HashMap<Player, Integer>();
	static HashMap<Player, Integer> buffer = new HashMap<Player, Integer>();
	
	/*@HookHandler
	public void onBlockLeftClick(BlockLeftClickHook hook) {
		blocks.put(hook.getPlayer(), System.nanoTime());
	}*/
	
	
	@HookHandler
	public void onBlockDestroy(BlockDestroyHook hook) {
		World world = hook.getPlayer().getWorld();
		Player player = hook.getPlayer();
		if (!hook.getBlock().getType().equals(BlockType.TallGrass)) {
			if (!FastBreak.isEmpty()) {
				if (FastBreak.contains(world)) {
					if (blocks.containsKey(hook.getPlayer())) {
						long diff = System.currentTimeMillis() - blocks.get(hook.getPlayer());
						if (!hook.getPlayer().getMode().equals(GameMode.CREATIVE)) {
							//hook.getPlayer().message(diff + "");
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
									int num = buffer.get(player)+4;
									buffer.put(player, num);
									if (buffer.get(player) > 12) {
										hook.setCanceled();
										hook.getPlayer().message(Colors.RED + "FastBreak!!!");
										hook.getPlayer().addPotionEffect(PotionEffectType.DIGSLOWDOWN, 30, 50);
										if (blocksamount.containsKey(hook.getPlayer())) {
											blocksamount.put(hook.getPlayer(), blocksamount.get(hook.getPlayer()) + 1);
										} else {
											blocksamount.put(hook.getPlayer(), 1);
										}
										buffer.put(player, 8);
									}
								} else {
									buffer.put(player, 4);
								}
							}
							if (buffer.containsKey(player)) {
								int num = buffer.get(player) - 1;
								if (num < 0) {
									num = 0;
								}
								buffer.put(player, num);
							}
						}
					}
				}
			}
		}
		if (!Nuker.isEmpty()) {
			if (Nuker.contains(world)) {
				if (blocks.containsKey(hook.getPlayer())) {
					long diff = System.currentTimeMillis() - blocks.get(player);
					if (diff < 2) {
						hook.getPlayer().message(Colors.RED + "You break blocks too fast.");
						castToAdmins(hook.getPlayer(), "[blocks] destroying blocks too fast. (" + diff + ")");
						hook.setCanceled();
						if (blocksamount.containsKey(hook.getPlayer())) {
							blocksamount.put(hook.getPlayer(), blocksamount.get(hook.getPlayer()) + 1);
						} else {
							blocksamount.put(hook.getPlayer(), 1);
						}
					}
				}
			}
		}
		blocks.put(player, System.currentTimeMillis());
		if (blocksamount.containsKey(hook.getPlayer())) {
			if (blocksamount.get(hook.getPlayer()) > 5) {
				if (!hook.getPlayer().hasPermission(PERMISSION_ADMIN + "exception")) {
					hook.getPlayer().kick(Colors.RED + "Disable your hacks or check your ping." + Colors.YELLOW + "\nYou have been banned for 5 minutes." + Colors.RED + "\nThis has been recorded and saved.");
					TempReasons.put(hook.getPlayer(), "Build Hacks");
					TempBans.put(hook.getPlayer(), System.currentTimeMillis() + 5*60*1000);
					blocksamount.put(hook.getPlayer(), 1);
					String property = getProperty(hook.getPlayer(), "destroy-hacks");
					int num = 1;
					if (property != null) {
						if (isNumeric(property)) {
							num = Integer.parseInt(property) + 1;
						}
					}
					setProperty(hook.getPlayer(), "destroy-hacks", num + "");
					addToBan(hook.getPlayer(), 4);
				} else {
					hook.getPlayer().message(Colors.RED + "You would have been banned for destroy hacks.");
					blocksamount.put(hook.getPlayer(), 1);
				}
			}
		}
	}
	
	/*HashMap<Player, Long> slot = new HashMap<Player, Long>();
	
	@HookHandler
	public void onSlotClick(SlotClickHook hook) {
		Player player = hook.getPlayer();
		if (hook.getItem() != null) {
			//hook.getPlayer().message("SlotClickHook: " + hook.getItem().getDisplayName());
			if (slot.containsKey(player)) {
				long diff = System.nanoTime() - slot.get(player);
				player.message(diff + ", " + hook.getButtonPress().toString());
				if (diff < 50000000) {
					if (!hook.getButtonPress().equals(ButtonPress.LEFT_PAINT_PROGRESS)) {
						if (!hook.getButtonPress().equals(ButtonPress.RIGHT_PAINT_PROGRESS)) {
							player.message(Colors.RED + "You clicked too fast!");
							if (!player.hasPermission(PERMISSION_ADMIN)) {
								hook.setCanceled();
							}
						}
					}
				}
			}
			slot.put(player, System.nanoTime());
		}
	}*/

	
	HashMap<Player, Long> swing = new HashMap<Player, Long>();
	HashMap<Player, Long> beforediff = new HashMap<Player, Long>();
	HashMap<Player, Integer> anzahl = new HashMap<Player, Integer>();
	
	@HookHandler
	public void onPlayerArmSwing(PlayerArmSwingHook hook) {
		Player player = hook.getPlayer();
		if (!player.getMode().equals(GameMode.CREATIVE)) {
			LineTracer lt = new LineTracer(player, 6, 1);
			Block target = lt.getTargetBlock();
			if (swing.containsKey(player)) {
				long diff = System.currentTimeMillis() - swing.get(player);
				if (beforediff.containsKey(player)) {
					long before = beforediff.get(player);
					long wholediff = Math.abs(diff - before);
					if (!anzahl.containsKey(player)) {
						anzahl.put(player, 0);
					}
					if (diff < 20) { // Against lag
						return;
					}
					String strDiff;
					if (diff <= 52) {
						if (target == null) {
							anzahl.put(player, anzahl.get(player) + 10);
						}
						strDiff = Colors.RED + diff;
					} else {
						strDiff = Colors.LIGHT_GREEN + diff;
					}
					String strWholediff;
					if (wholediff <= 4) {
						int toadd = 4;
						if (target != null) {
							toadd = 1;
						}
						anzahl.put(player, anzahl.get(player) + toadd);
						strWholediff = Colors.RED + wholediff;
					} else {
						int toremove = 2;
						if (target != null) {
							toremove = 15;
						}
						anzahl.put(player, anzahl.get(player) - toremove);
						strWholediff = Colors.LIGHT_GREEN + wholediff;
					}
					if (anzahl.get(player) < 0) {
						anzahl.put(player, 0);
					}
					if (anzahl.containsKey(player)) {
						if (anzahl.get(player) > 100) {
							anzahl.remove(player);
							if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
								addToBan(player, 5);
								player.kick(Colors.RED + "You are hitting too fast." + Colors.YELLOW + "\nYou have been banned for 15 minutes." + Colors.RED + "\nThis has been recorded and saved.");
								TempReasons.put(player, "Combat Hacks");
								TempBans.put(player, System.currentTimeMillis() + 15*60*1000);
							} else {
								player.message(Colors.RED + "You would have been tempbanned for clicking too fast.");
							}
							String property = getProperty(player, "combat-hacks");
							int num = 1;
							if (property != null) {
								if (isNumeric(property)) {
									num = Integer.parseInt(property) + 1;
								}
							}
							setProperty(player, "combat-hacks", num + "");
							//player.addPotionEffect(PotionEffectType.WEAKNESS, 40, 40);
						}
						int num = anzahl.get(player);
						String n;
						if (num < 20) {
							n = Colors.GREEN + num;
						} else if (num < 40) {
							n = Colors.YELLOW + num;
						} else if (num < 60) {
							n = Colors.ORANGE + num;
						} else if (num < 80) {
							n = Colors.LIGHT_RED + num;
						} else {
							n = Colors.RED + num;
						}
						castToAdmins(player, "[combat] " + Colors.LIGHT_GREEN + before + "/"+ strDiff + Colors.YELLOW + " > " + strWholediff + ", " + Colors.LIGHT_RED + "Danger level: " + Colors.ORANGE + n);
					}
				}
				beforediff.put(player, diff);
			}
			swing.put(player, System.currentTimeMillis());
		}
	}
	
	/*@HookHandler
	public void onDamage(DamageHook hook) {
		Player player = null;
		if (hook.getDefender().isPlayer()) {
			player = (Player) hook.getDefender();
		}
		if (player != null) {
			lastattack.put(player, System.currentTimeMillis());
			/*if (!player.getMode().equals(GameMode.CREATIVE)) {
				if (hits.containsKey(player)) {
					long diff = System.currentTimeMillis() - hits.get(player);	
					if (!hitsanzahl.containsKey(player)) {
						hitsanzahl.put(player, 0);
					}
					if (diff <= 2) {
						hitsanzahl.put(player, hitsanzahl.get(player) + 1);
					}
					//player.message("" + diff + ", " + Colors.ORANGE + hitsanzahl.get(player));
					if (hitsanzahl.containsKey(player)) {
						if (hitsanzahl.get(player) > 12) {
							hitsanzahl.remove(player);
							if (player.getGroup().getName().equalsIgnoreCase("players") || player.getGroup().getName().equalsIgnoreCase("visitors")) {
								player.kick(Colors.RED + "Disable your forcefield or check your ping." + Colors.YELLOW + "\nYou have been banned for 15 minutes." + Colors.RED + "\nThis has been recorded and saved.");
								TempReasons.put(player, "Combat Hacks");
								TempBans.put(player, System.currentTimeMillis() + 15*60*1000);
							} else {
								player.message(Colors.RED + "You would have been kicked for hitting too many mobs at once.");
							}
							String property = getProperty(player, "combat-hacks");
							int num = 1;
							if (property != null) {
								if (isNumeric(property)) {
									num = Integer.parseInt(property) + 1;
								}
							}
							setProperty(player, "combat-hacks", num + "");
							player.addPotionEffect(PotionEffectType.WEAKNESS, 40, 40);
						}
					}
				}
				hits.put(player, System.currentTimeMillis());
			}
		}
	}*/
	
	HashMap<Player, Long> buildtime = new HashMap<Player, Long>();
	HashMap<Player, Integer> buildhack = new HashMap<Player, Integer>();
	HashMap<Player, Integer> buildbuffer = new HashMap<Player, Integer>();
	
	@HookHandler
	public void onBlockPlace(BlockPlaceHook hook) {
		World world = hook.getPlayer().getWorld();
		if (!FastBuild.isEmpty()) {
			if (FastBuild.contains(world)) {
				Player player = hook.getPlayer();
				if (!player.hasPermission(PERMISSION_ADMIN + "exception")) {
					if (buildtime.containsKey(player)) {
						long diff = System.currentTimeMillis() - buildtime.get(player);
						if (buildbuffer.containsKey(player)) {
							if (diff < 26) {
								buildbuffer.put(player, buildbuffer.get(player) + 5);
								if (buildbuffer.get(player) >= 20) {
									buildbuffer.put(player, 14);
									hook.setCanceled();
									player.message(Colors.RED + "You are building too fast." + Colors.GREEN + "(" + diff + ")");
								}
							} else {
								buildbuffer.put(player, buildbuffer.get(player) - 1);
								if (buildbuffer.get(player) < 0) {
									buildbuffer.put(player, 0);
								}
							}
						} else {
							buildbuffer.put(player, 0);
						}
						if (diff <= 1) {
							hook.setCanceled();
							if (buildhack.containsKey(player)) {
								buildhack.put(player, buildhack.get(player) + 1);
								if (buildhack.get(player) > 15) {
									player.kick(Colors.RED + "Disable your buildhacks or check your ping.\n\nThis has been recorded and saved.");
									buildhack.remove(player);
									String property = getProperty(hook.getPlayer(), "build-hacks");
									int num = 1;
									if (property != null) {
										if (isNumeric(property)) {
											num = Integer.parseInt(property) + 1;
										}
									}
									setProperty(hook.getPlayer(), "build-hacks", num + "");
								}
							} else {
								buildhack.put(player, 1);
							}
						}
					}
					buildtime.put(player, System.currentTimeMillis());
				}
			}
		}
	}
	
	HashMap<Player, PotionEffect> lastPotion = new HashMap<Player, PotionEffect>();
	
	@HookHandler
	public void onPotionEffectApplied(PotionEffectAppliedHook hook) {
		if (hook.getEntity().isPlayer()) {
			Player player = (Player) hook.getEntity();
			lastPotion.put(player, hook.getPotionEffect());
		} 
	}
	
	HashMap<Player, Long> health = new HashMap<Player, Long>();
	
	@HookHandler
	public void onHealthChange(HealthChangeHook hook) {
		Player player = hook.getPlayer();
		if (player.getHealth() > hook.getOldValue()) {
			if (health.containsKey(player)) {
				if (lastPotion.containsKey(player)) {
					if (lastPotion.get(player).getName().endsWith("potion.heal")) {
						lastPotion.remove(player);
						return;
					}
				}
				if (!player.getAllActivePotionEffects().isEmpty()) {
					for (int i=0; i<player.getAllActivePotionEffects().size(); i++) {
						PotionEffect effect = player.getAllActivePotionEffects().get(i);
						if (effect.getName().equalsIgnoreCase("potion.regeneration")) {
							return;
						}
					}
				}
				float diff = System.currentTimeMillis() - health.get(player);
				float healthdiff = player.getHealth() - hook.getOldValue();
				String str = "[health] ";
				if (healthdiff <= 1 && healthdiff > 0.99) { // To prevent errors with the instant health potion
					health.put(player, System.currentTimeMillis());
					if (diff < 3500) {
						addToBan(player, 2);
						player.message(Colors.RED + "Your hp is regenerating too fast (" + diff + ")");
						str = str.concat(Colors.RED + diff);
						hook.setCanceled();
					} else if (diff < 3850) {
						str = str.concat(Colors.YELLOW + diff);
					} else {
						str = str.concat(Colors.GREEN + diff);
					}
				}
				castToAdmins(player, str);
			} else {
				health.put(player, System.currentTimeMillis());
			}
		}
	}
	
	public void setProperty(Player player, String key, String value) {
		try {		
			File file = new File(DIR + PLAYERS + player.getName() + ".txt");
			File dir = new File(DIR);
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
        FileInputStream in;
		try {
			in = new FileInputStream(DIR + PLAYERS + player.getName() + ".txt");
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
