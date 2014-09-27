package de.svdragster.nocheat;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.monster.EntityMob;
import net.canarymod.api.world.position.Location;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.TaskOwner;

public class TeleportMobs extends ServerTask {
	private Player player;
	private EntityMob mob;

	public TeleportMobs(TaskOwner owner, long delay, Player player, EntityMob mob) {
		super(owner, delay);
		this.player = player;
		this.mob = mob;
	}

	public void run() {
		Location loc = new Location(this.player.getX(),	this.player.getY() + 3, this.player.getZ());
		this.mob.teleportTo(loc);
	}
}
