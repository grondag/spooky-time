package com.fabriccommunity.spookytime.doomtree.logic;

import static com.fabriccommunity.spookytime.doomtree.logic.TreeDesigner.canReplace;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

class LogValidator implements Job {
	final LongArrayList blocks;
	int index = 0;
	final int limit;
	
	public LogValidator(DoomTreeHeartBlockEntity heart) {
		blocks = new LongArrayList(heart.logs.length + heart.branches.length);
		blocks.addElements(0, heart.logs, 0, heart.logs.length);
		blocks.addElements(heart.logs.length, heart.branches, 0, heart.branches.length);
		limit = blocks.size();
	}

	@Override
	public Job apply(DoomTreeHeartBlockEntity heart) {
		final int end = Math.min(index + 32, limit);
		final BlockPos.Mutable mPos = heart.mPos;
		for (int i = index; i < end; i++) {
			final long p = blocks.getLong(i);
			heart.mPos.set(p);
			final BlockState currentState = heart.getWorld().getBlockState(mPos);
			 
			if (TreeDesigner.logState(mPos, heart) != currentState && canReplace(heart.getWorld(), mPos)) {
				heart.builder.enqueue(p);
			}
		}
		
		index = end;
		
		return index < limit ? this : null;
	}
}