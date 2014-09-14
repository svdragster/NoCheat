package de.svdragster.nocheat;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.monster.EntityMob;
import net.canarymod.api.world.position.Location;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.TaskOwner;

public class TeleportMobs extends ServerTask {
	Player player;
	EntityMob mob;

	public TeleportMobs(TaskOwner owner, long delay, Player _player,
			EntityMob _mob) {
		super(owner, delay);
		this.player = _player;
		this.mob = _mob;
	}

	public void run() {
		Location loc = new Location(this.player.getX(),	this.player.getY() + 3, this.player.getZ());
		this.mob.teleportTo(loc);
	}
}
