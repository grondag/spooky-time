package com.fabriccommunity.spookytime.hauntree;

import java.util.Comparator;
import java.util.Random;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.IWorld;

public class TreeBuilder {
	static final int MAX_GEN_HEIGHT = 48;

	public static boolean canReplace(IWorld world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();

		return blockState.isAir()
				|| blockState.matches(BlockTags.LEAVES)
				|| blockState.getMaterial() == Material.REPLACEABLE_PLANT
				|| Block.isNaturalStone(block)
				|| block.matches(Hauntree.BLOCK_TROLL_WHITELIST)
				|| block.matches(BlockTags.LOGS)
				|| block.matches(BlockTags.SAPLINGS)
				|| block == Hauntree.MIASMA_BLOCK;
	}

	public static class BranchPoint {
		public final int xStart;
		public final int zStart;
		public final int dx;
		public final int dz;
		public final Direction[] faces;

		BranchPoint(int xOffset, int zOffset) {
			this.xStart = xOffset;
			this.zStart = zOffset;
			dx = xOffset == 0 ? 0 : xOffset / Math.abs(xOffset);
			dz = zOffset == 0 ? 0 : zOffset / Math.abs(zOffset);

			faces = makeFaces();
		}

		Direction[] makeFaces() {
			final Direction[] result = new Direction[16];

			final int xTest = Math.abs(xStart);
			final Direction primary = xTest == 2 ? Direction.fromVector(dx, 0, 0) : Direction.fromVector(0, 0, dz);


			result[0] = primary;
			result[1] = primary;
			result[2] = primary;
			result[3] = primary;
			result[4] = primary.getAxis() == Axis.X ? Direction.NORTH : Direction.EAST;
			result[5] = result[4].getOpposite();
			result[6] = primary;
			result[7] = Direction.UP;
			result[8] = result[4];
			result[9] = result[5];
			result[10] = primary;
			result[11] = Direction.UP;
			result[12] = result[8];
			result[13] = result[9];
			result[14] = Direction.DOWN;
			result[15] = Direction.UP;


			return result;
		}
	}

	public static BranchPoint[] POINTS = {
			new BranchPoint(-2, 0),
			new BranchPoint(2, 0),
			new BranchPoint(0, -2),
			new BranchPoint(0, 2)
	};

	public static final int POINT_COUNT = POINTS.length;

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

	public static void placeBranch(IWorld world, PositionCollector blocks, Random rand, final int x, final int z, long startPos, BlockPos.Mutable pos, BranchPoint point, int allowance) {
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

	public static boolean isUncrowdedExcept(PositionCollector blocks, long targetPos, long ex0, long ex1) {
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

	public static boolean inAndNotExcluded(PositionCollector blocks, long targetPos, long ex0, long ex1) {
		return targetPos != ex0 && targetPos != ex1 && blocks.contains(targetPos);
	}

	public static int centerHeight(BlockPos pos) {
		return MAX_GEN_HEIGHT - ((int) HashCommon.mix(pos.asLong()) & 0x7);
	}
	
	/** Maintains insertion order */
	public static class PositionCollector {
		final LongOpenHashSet set = new LongOpenHashSet();
		final LongArrayList list = new LongArrayList();
		
		public void add(long val) {
			if(set.add(val)) {
				list.add(val);
			}
		}

		public void remove(long val) {
			set.remove(val);
			list.removeIf((long l) -> l == val);
		}

		public long[] toLongArray() {
			return list.toLongArray();
		}

		public boolean contains(long val) {
			return set.contains(val);
		}

		public void sort(Comparator<? super Long> c) {
			list.sort(c);
		}
	}
}
