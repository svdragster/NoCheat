package de.svdragster.nocheat;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.monster.EntityMob;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.TaskOwner;

public class DestroyMobs extends ServerTask {
	Player player;
	EntityMob mob;

	public DestroyMobs(TaskOwner owner, long delay, Player _player,
			EntityMob _mob) {
		super(owner, delay);
		this.player = _player;
		this.mob = _mob;
	}

	public void run() {
		this.mob.destroy();
	}
}
