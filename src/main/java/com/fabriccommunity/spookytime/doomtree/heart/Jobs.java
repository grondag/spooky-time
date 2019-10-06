package com.fabriccommunity.spookytime.doomtree.heart;

import static com.fabriccommunity.spookytime.doomtree.heart.TreeBuilder.canReplace;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public abstract class Jobs {
	
	static Job place(DoomTreeHeartBlockEntity heart) {
		if (heart.power >= 100 && !heart.logQueue.isEmpty()) {
			final long p = heart.logQueue.dequeueLong();
			
			BlockPos mPos = heart.mPos.set(p);
			BlockState currentState = heart.getWorld().getBlockState(mPos);
			BlockState targetState = TreeBuilder.logState(mPos, heart);
			if (targetState != currentState && canReplace(heart.getWorld(), mPos)) {
				heart.getWorld().setBlockState(mPos, targetState, 18);
				heart.power -= 100;
			}
		}
		return null;
	}
}
