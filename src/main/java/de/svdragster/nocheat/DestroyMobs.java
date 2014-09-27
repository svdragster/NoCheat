package de.svdragster.nocheat;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.monster.EntityMob;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.TaskOwner;

public class DestroyMobs extends ServerTask {
	private Player player;
	private EntityMob mob;

	public DestroyMobs(TaskOwner owner, long delay, Player player, EntityMob mob) {
		super(owner, delay);
		this.player = player;
		this.mob = mob;
	}

	public void run() {
		this.mob.destroy();
	}
}
