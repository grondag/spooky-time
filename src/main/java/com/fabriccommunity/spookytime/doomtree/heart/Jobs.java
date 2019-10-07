package com.fabriccommunity.spookytime.doomtree.heart;

import static com.fabriccommunity.spookytime.doomtree.heart.TreeBuilder.canReplace;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class Jobs {

	static Job place(DoomTreeHeartBlockEntity heart) {
		final World world = heart.getWorld();
		final BlockPos.Mutable mPos = heart.mPos;
		boolean didPlace= false;

		for (int i = 0; i < 8; i++) {
			if (heart.power >= 100 && !heart.logQueue.isEmpty()) {
				final long p = heart.logQueue.dequeueLong();
				mPos.set(p);
				BlockState currentState = world.getBlockState(mPos);
				BlockState targetState = TreeBuilder.logState(mPos, heart);

				if (targetState != currentState && canReplace(world, mPos)) {
					world.setBlockState(mPos, targetState, 18);
					heart.power -= 100;
					didPlace = true;
				}
			}
		}

		if (didPlace) {
			heart.resetTickCounter(ThreadLocalRandom.current());
		}

		return null;
	}
}
