package de.svdragster.nocheat;

import net.canarymod.Canary;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.plugin.Plugin;

public class NoCheat extends Plugin {
	
	@Override
	public void disable() {
		//Canary.getServer().removeSynchronousTask(new NoCheatListener().task);
	}

	@Override
	public boolean enable() {
		Canary.hooks().registerListener(new NoCheatListener(), this);
		new NoCheatListener().loadProperties();
		try {
			Canary.commands().registerCommands(new NoCheatCommands(), this, false);
		} catch (CommandDependencyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
