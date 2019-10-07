package com.fabriccommunity.spookytime.doomtree.heart;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.DoomLogBlock;
import com.fabriccommunity.spookytime.doomtree.DoomTree;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

class TreeBuilder {
	static final int MAX_GEN_HEIGHT = 48;

	static boolean canReplace(IWorld world, BlockPos pos) {
		return canReplace(world.getBlockState(pos));
	}
		
	static boolean canReplace(BlockState blockState) {
		Block block = blockState.getBlock();

		return blockState.isAir()
				|| blockState.matches(BlockTags.LEAVES)
				|| blockState.getMaterial() == Material.REPLACEABLE_PLANT
				|| Block.isNaturalStone(block)
				|| block.matches(DoomTree.DOOM_TREE_WHITELIST)
				|| block.matches(BlockTags.LOGS)
				|| block.matches(BlockTags.SAPLINGS)
				|| block == DoomTree.MIASMA_BLOCK;
	}

	static BranchPoint[] POINTS = {
			new BranchPoint(-2, 0),
			new BranchPoint(2, 0),
			new BranchPoint(0, -2),
			new BranchPoint(0, 2)
	};

	static final int POINT_COUNT = POINTS.length;

	static int DISTANCES[] = new int[64];

	static {
		for (int i = 0; i < 64; i++) {
			DISTANCES[i] = (int) Math.round(Math.sqrt(i));
		}
	}

	static int dist(int dx, int dz) {
		final int d = dx * dx + dz * dz;
		return d < 64 ? DISTANCES[d] : 8;
	}

	static void placeBranch(IWorld world, PositionCollector blocks, Random rand, final int x, final int z, long startPos, BlockPos.Mutable pos, BranchPoint point, int allowance) {
		int branchLength = 1;

		if (canReplace(world, pos.set(startPos))) {
			blocks.add(startPos);
			--allowance;
		} else {
			return;
		}

		long priorFromPos = Long.MIN_VALUE;
		long currentPos = startPos;
		int retryCount = 0;

		while (allowance > 0 && retryCount < 10) {
			final int range = 2 * dist(BlockPos.unpackLongX(currentPos) - x, BlockPos.unpackLongZ(currentPos) - z);

			final Direction face = point.faces[rand.nextInt(range)];
			final long fromPos = currentPos;
			currentPos = BlockPos.offset(currentPos, face);

			if (isUncrowdedExcept(blocks, currentPos, fromPos, priorFromPos) && canReplace(world, pos.set(currentPos))) {
				if (++branchLength + rand.nextInt(2) > 4) {
					final long nextPos =  BlockPos.offset(currentPos, face);

					if (isUncrowdedExcept(blocks, nextPos, currentPos, fromPos) && canReplace(world, pos.set(nextPos))) {
						// have one valid position, try to branch
						Direction branchFace = face;

						while (branchFace == face) {
							branchFace = point.faces[rand.nextInt(range)];
						}

						final long branchPos = BlockPos.offset(fromPos, branchFace);

						if (isUncrowdedExcept(blocks, branchPos, fromPos, priorFromPos) && canReplace(world, pos.set(branchPos))) {
							final long nextBranchPos = BlockPos.offset(branchPos, branchFace);

							if (isUncrowdedExcept(blocks, nextBranchPos, branchPos, fromPos) && canReplace(world, pos.set(nextBranchPos))) {
								blocks.add(currentPos);
								blocks.add(branchPos);
								placeBranch(world, blocks, rand, x, z, nextPos, pos, point, allowance - 1);
								placeBranch(world, blocks, rand, x, z, nextBranchPos, pos, point, allowance - 1);
								return;
							}
						}
					}
				}

				// no branch, keep going
				blocks.add(currentPos);
				priorFromPos = fromPos;
				--allowance;
				retryCount = 0;
			} else {
				currentPos = fromPos;
				++retryCount;
			}
		}
	}

	static boolean isUncrowdedExcept(PositionCollector blocks, long targetPos, long ex0, long ex1) {
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, -1, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, -1, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, -1, 1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, 0, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, 0, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, 0, 1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, 1, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, 1, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, -1, 1, 1), ex0, ex1)) return false;

		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, -1, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, -1, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, -1, 1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, 0, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, 0, 1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, 1, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, 1, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 0, 1, 1), ex0, ex1)) return false;

		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, -1, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, -1, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, -1, 1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, 0, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, 0, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, 0, 1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, 1, -1), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, 1, 0), ex0, ex1)) return false;
		if (inAndNotExcluded(blocks, BlockPos.add(targetPos, 1, 1, 1), ex0, ex1)) return false;
		return true;
	}

	static boolean inAndNotExcluded(PositionCollector blocks, long targetPos, long ex0, long ex1) {
		return targetPos != ex0 && targetPos != ex1 && blocks.contains(targetPos);
	}

	static int centerHeight(BlockPos pos) {
		return MAX_GEN_HEIGHT - ((int) HashCommon.mix(pos.asLong()) & 0x7);
	}
	
	static final BlockState LOG_STATE = DoomTree.DOOM_LOG.getDefaultState();
	static final BlockState CHANNEL_STATE = DoomTree.DOOM_LOG_CHANNEL.getDefaultState();
	static final BlockState TERMINAL_STATE = DoomTree.DOOM_LOG_TERMINAL.getDefaultState();
	
	static BlockState logState(BlockPos pos,DoomTreeHeartBlockEntity heart) {
		final BlockPos heartPos = heart.getPos();
		final int dy = pos.getY() - heart.getPos().getY();
		final int x  = heartPos.getX();
		final int z = heartPos.getZ();
		
		if (pos.getX() == x && Math.abs(pos.getZ() - z) == 1 || pos.getZ() == z && Math.abs(pos.getX() - x) == 1) {
			if (dy >= 0 && dy < DoomLogBlock.TERMINAL_HEIGHT) {
				return CHANNEL_STATE.with(DoomLogBlock.HEIGHT, MathHelper.clamp(dy, 0, DoomLogBlock.MAX_HEIGHT));
			} else if (dy == DoomLogBlock.TERMINAL_HEIGHT) {
				return TERMINAL_STATE;
			}
		} 
		
		return LOG_STATE.with(DoomLogBlock.HEIGHT, MathHelper.clamp(dy, 0, DoomLogBlock.MAX_HEIGHT));
	}
}
