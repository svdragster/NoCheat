package de.svdragster.nocheat;

import java.util.ArrayList;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;

public class NoCheatCommands implements CommandListener {
	
	@Command(aliases = { "watch" },
            description = "Watch a player.",
            permissions = { "nocheat.watch" },
            toolTip = "/watch <playername> [type]")
	public void WatchCommand(MessageReceiver caller, String[] parameters) {
		if (parameters.length >= 2) {
			Player admin = (Player) caller;
			Player toWatch = Canary.getServer().getPlayer(parameters[1]);
			if (toWatch != null) {
				if (NoCheatListener.display.containsKey(toWatch)) {
					ArrayList<Player> admins = new ArrayList<Player>();
					if (NoCheatListener.display.get(admin) != null) {
						admins.addAll(NoCheatListener.display.get(admin));
						if (admins.contains(admin)) {
							NoCheatListener.display.get(toWatch).remove(admin);
							admin.message("Stopped watching " + parameters[1]);
							if (NoCheatListener.displaytype.containsKey(admin)) {
								NoCheatListener.displaytype.remove(admin);
							}
						} else {
							NoCheatListener.display.get(toWatch).add(admin);
							admin.message("Now watching " + parameters[1]);
							if (parameters.length >= 3) {
								NoCheatListener.displaytype.put(admin, parameters[2]);
							}
						}
					}
				} else {
					ArrayList<Player> admins = new ArrayList<Player>();
					admins.add(admin);
					NoCheatListener.display.put(toWatch, admins);
					admin.message("Now watching " + parameters[1]);
					if (parameters.length >= 3) {
						NoCheatListener.displaytype.put(admin, parameters[2]);
					}
				}
			} else {
				caller.notice(parameters[1] + " is not online.");
			}
		} else {
			caller.notice("Usage: /watch <playername> [type]");
		}
	}
	
	@Command(aliases = { "ncunban" },
            description = "Unban a player that has tempbanned by nocheat",
            permissions = { "nocheat.unban" },
            toolTip = "/ncunban <playername>")
	public void NCUnbanCommand(MessageReceiver caller, String[] parameters) {
		if (parameters.length > 1) {
			Player toUnban = Canary.getServer().getPlayer(parameters[1]);
			NoCheatListener l = new NoCheatListener();
			l.TempBans.remove(toUnban);
			l.TempReasons.remove(toUnban);
			caller.message(Colors.LIGHT_GREEN + parameters[1] + " has been unbanned.");
		} else {
			caller.notice("Usage: /ncunban <playername>");
		}
	}
}
