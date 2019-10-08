package com.fabriccommunity.spookytime.doomtree.logic;

import static com.fabriccommunity.spookytime.doomtree.logic.TreeDesigner.canReplace;

import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Builder {
	final LongArrayFIFOQueue logQueue = new LongArrayFIFOQueue();
	
	void build(DoomTreeHeartBlockEntity heart) {
		final World world = heart.getWorld();
		final BlockPos.Mutable mPos = heart.mPos;
		boolean didPlace= false;

		for (int i = 0; i < 8; i++) {
			if (heart.power >= 100 && !logQueue.isEmpty()) {
				final long p = logQueue.dequeueLong();
				mPos.set(p);
				BlockState currentState = world.getBlockState(mPos);
				BlockState targetState = TreeDesigner.logState(mPos, heart);

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
	}

	boolean canBuild() {
		return !logQueue.isEmpty();
	}

	void enqueue(long pos) {
		logQueue.enqueue(pos);
	}
}
