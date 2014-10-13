package de.svdragster.nocheat;

import net.canarymod.Canary;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.plugin.Plugin;


public class NoCheat extends Plugin {
	
	NoCheatListener listener = new NoCheatListener();
	
	public void disable() {
		Canary.commands().unregisterCommands(this);
	}

	public boolean enable() {
		Canary.hooks().registerListener(listener, this);
		new NoCheatListener().loadProperties();
		try {
			Canary.commands().registerCommands(new NoCheatCommands(), this,	false);
		} catch (CommandDependencyException e) {
			e.printStackTrace();
		}
		/*if (Canary.manager().getPluginNames().contains("CustomMotd")) {
			Plugin plugin = Canary.manager().getPlugin("CustomMotd");
			if (plugin != null) {
				double version = Double.parseDouble(plugin.getVersion());
				if (version >= 0.3) {
					listener.CustomMotdEnabled = true;
				}
			}
		}*/
		return true;
	}
}
