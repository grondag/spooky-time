package com.fabriccommunity.spookytime.doomtree.heart;

import static com.fabriccommunity.spookytime.doomtree.heart.TreeBuilder.canReplace;

import com.fabriccommunity.spookytime.doomtree.DoomLogBlock;
import com.fabriccommunity.spookytime.doomtree.DoomTree;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

class PlacerJob implements Job {
	final int x;
	final int y;
	final int z;
	final BlockPos.Mutable mPos = new BlockPos.Mutable();
	final BlockState logState = DoomTree.DOOM_LOG.getDefaultState();
	final BlockState channelState = DoomTree.DOOM_LOG_CHANNEL.getDefaultState();
	final BlockState terminalState = DoomTree.DOOM_LOG_TERMINAL.getDefaultState();
	final LongArrayList blocks;
	int index = 0;
	final int limit;
	
	public PlacerJob(DoomTreeHeartBlockEntity heart) {
		final BlockPos pos = heart.getPos();
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		blocks = new LongArrayList(heart.logs.length + heart.branches.length);
		blocks.addElements(0, heart.logs, 0, heart.logs.length);
		blocks.addElements(heart.logs.length, heart.branches, 0, heart.branches.length);
		limit = blocks.size();
	}

	@Override
	public Job apply(DoomTreeHeartBlockEntity heart) {
		if (index < limit) {
			final long p = blocks.getLong(index++);
			mPos.set(p);
			
			if (canReplace(heart.getWorld(), mPos)) {
				final int dy = mPos.getY() - y;
				BlockState state = null;

				if (mPos.getX() == x && Math.abs(mPos.getZ() - z) == 1 || mPos.getZ() == z && Math.abs(mPos.getX() - x) == 1) {
					if (dy >= 0 && dy < DoomLogBlock.TERMINAL_HEIGHT) {
						state = channelState.with(DoomLogBlock.HEIGHT, MathHelper.clamp(dy, 0, DoomLogBlock.MAX_HEIGHT));
					} else if (dy == DoomLogBlock.TERMINAL_HEIGHT) {
						state = terminalState;
					}
				} 
				
				if (state == null) {
					state = logState.with(DoomLogBlock.HEIGHT, MathHelper.clamp(dy, 0, DoomLogBlock.MAX_HEIGHT));
				}

				heart.getWorld().setBlockState(mPos.set(p), state, 18);
			}
			return this;
		} else {
			return null;
		}
	}

}