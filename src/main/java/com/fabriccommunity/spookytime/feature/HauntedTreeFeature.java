package com.fabriccommunity.spookytime.feature;

import java.util.Random;
import java.util.function.Function;

import com.fabriccommunity.spookytime.block.HauntedLogBlock;
import com.fabriccommunity.spookytime.entity.HauntedTreeHeartBlockEntity;
import com.fabriccommunity.spookytime.registry.SpookyBlocks;
import com.fabriccommunity.spookytime.registry.SpookyTags;
import com.mojang.datafixers.Dynamic;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class HauntedTreeFeature extends Feature<DefaultFeatureConfig> {
	public HauntedTreeFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> func, boolean flag) {
		super(func, flag);
	}

	protected static boolean canReplace(IWorld world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		
		boolean result = blockState.isAir()
				|| blockState.matches(BlockTags.LEAVES)
				|| blockState.getMaterial() == Material.REPLACEABLE_PLANT
				|| Block.isNaturalStone(block)
				|| block.matches(SpookyTags.BLOCK_TROLL_WHITELIST)
				|| block.matches(BlockTags.LOGS)
				|| block.matches(BlockTags.SAPLINGS)
				|| block == SpookyBlocks.MIASMA_BLOCK;
		
		
		//TODO: remove
		if (!result) {
			System.out.println("boop");
		}
		return result;
	}

	protected static final int MAX_GEN_HEIGHT = 32;
	
	@Override
	public boolean generate(IWorld world, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGen, Random rand, BlockPos posIn, DefaultFeatureConfig config) {
		final LongOpenHashSet blocks = new LongOpenHashSet();
		final BlockPos.Mutable mPos = new BlockPos.Mutable();

		if (!generateInner(world, rand, posIn, blocks, mPos)) {
			return false;
		}

		final int x = posIn.getX();
		final int y = posIn.getY();
		final int z = posIn.getZ();

		final BlockState logState = SpookyBlocks.HAUNTED_LOG.getDefaultState();
		final BlockState channelState = SpookyBlocks.HAUNTED_LOG_CHANNEL.getDefaultState();
		final BlockState terminalState = SpookyBlocks.HAUNTED_LOG_TERMINAL.getDefaultState();

		blocks.forEach((long p) -> {
			mPos.set(p);
			final int dy = mPos.getY() - y;
			BlockState state = null;

			if (mPos.getX() == x && Math.abs(mPos.getZ() - z) == 1 || mPos.getZ() == z && Math.abs(mPos.getX() - x) == 1) {
				if (dy >= 0 && dy < HauntedLogBlock.TERMINAL_HEIGHT) {
					state = channelState.with(HauntedLogBlock.HEIGHT, MathHelper.clamp(dy, 0, HauntedLogBlock.MAX_HEIGHT));
				} else if (dy == HauntedLogBlock.TERMINAL_HEIGHT) {
					state = terminalState;
				}
			} 
			
			if (state == null) {
				state = logState.with(HauntedLogBlock.HEIGHT, MathHelper.clamp(dy, 0, HauntedLogBlock.MAX_HEIGHT));
			}

			world.setBlockState(mPos.set(p), state, 18);
		});

		world.setBlockState(posIn, SpookyBlocks.HAUNTED_TREE_HEART.getDefaultState(), 3);
		final BlockEntity be = world.getBlockEntity(posIn);

		if (be != null && be instanceof HauntedTreeHeartBlockEntity) {
			HauntedTreeHeartBlockEntity heart = (HauntedTreeHeartBlockEntity) be;
			heart.setTemplate(blocks.toLongArray());
		}

		return true;
	}
	
	protected boolean generateInner(IWorld world, Random rand, BlockPos posIn, LongOpenHashSet blocks, BlockPos.Mutable mPos) {
		final int x = posIn.getX();
		final int y = posIn.getY();
		final int z = posIn.getZ();

		final int centerHeight = MAX_GEN_HEIGHT - rand.nextInt(5);

		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z - 1), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z + 1), centerHeight - rand.nextInt(5) - 1)) return false;

		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z - 1), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z), centerHeight)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z + 1), centerHeight - rand.nextInt(5) - 1)) return false;

		if (!placeTrunkSection(world, blocks, mPos.set(x + 1, y - 1, z - 1), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 1, y - 1, z), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 1, y - 1, z + 1), centerHeight - rand.nextInt(5) - 1)) return false;

		if (!placeTrunkSection(world, blocks, mPos.set(x + 2, y - 1, z), 4)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 2, y - 1, z), 4)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z - 2), 4)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z + 2), 4)) return false;

		if (!placeTrunkSection(world, blocks, mPos.set(x + 2, y - 1, z - 1), 3)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 2, y - 1, z + 1), 3)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 2, y - 1, z - 1), 3)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 2, y - 1, z + 1), 3)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z - 2), 3)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 1, y - 1, z - 2), 3)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z + 2), 3)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 1, y - 1, z + 2), 3)) return false;

		if (!placeTrunkSection(world, blocks, mPos.set(x - 2, y - 1, z - 2), 2)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 2, y - 1, z + 2), 2)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 2, y - 1, z - 2), 2)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 2, y - 1, z + 2), 2)) return false;

		if (!placeTrunkSection(world, blocks, mPos.set(x + 3, y - 1, z), 2)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 3, y - 1, z), 2)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z - 3), 2)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z + 3), 2)) return false;

		placeBranches(world, rand, posIn, blocks, mPos, centerHeight);
		
		// heart position (origin) doesn't get log
		blocks.remove(posIn.asLong());

		return true;
	}

	protected static boolean placeTrunkSection(IWorld world, LongOpenHashSet blocks, BlockPos.Mutable pos, int height) {
		final int yMin = pos.getY();

		for (int y = 0; y < height; y++) {
			pos.setY(yMin + y); 
			if (!placeTrunk(world, blocks, pos)) return false;
		}

		return true;
	}

	protected static boolean placeTrunk(IWorld world, LongOpenHashSet blocks, BlockPos.Mutable pos) {
		if (canReplace(world, pos)) {
			blocks.add(pos.asLong());
			return true;
		} else {
			return false;
		}
	}
	
	private static class BranchPoint {
		final int xStart;
		final int zStart;
		final int dx;
		final int dz;
		final Direction[] faces;
		
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
	
	private static BranchPoint[] POINTS = {
			new BranchPoint(-2, 0),
			new BranchPoint(2, 0),
			new BranchPoint(0, -2),
			new BranchPoint(0, 2)
	};
	
	private static final int POINT_COUNT = POINTS.length;
	
	protected static void placeBranches(IWorld world, Random rand, BlockPos posIn, LongOpenHashSet blocks, BlockPos.Mutable mPos, int maxHeight) {
		final int x = posIn.getX();
		final int y = posIn.getY();
		final int z = posIn.getZ();
		
		final ObjectArrayList<BranchPoint> points = new ObjectArrayList<>();
		points.addElements(0, POINTS, 0, POINT_COUNT);
		BranchPoint lastPoint = null;
		
		for (int h = HauntedLogBlock.TERMINAL_HEIGHT + 1; h < maxHeight; h++) {
			BranchPoint point = points.get(rand.nextInt(points.size()));
			while (point == lastPoint ) {
				point = points.get(rand.nextInt(points.size()));
			}
			lastPoint = point;
			points.remove(point);
			
			if (points.isEmpty()) {
				points.addElements(0, POINTS, 0, POINT_COUNT);
			}
			
			mPos.set(x + point.dx, y + h, z + point.dz);
			
			if (blocks.contains(mPos.asLong())) {
				mPos.set(x + point.xStart, y + h, z + point.zStart);
				placeBranch(world, blocks, rand, x, z, mPos.asLong(), mPos, point, 36 - rand.nextInt(8));
			}
		}
	}
	
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
	
	protected static void placeBranch(IWorld world, LongOpenHashSet blocks, Random rand, final int x, final int z, long startPos, BlockPos.Mutable pos, BranchPoint point, int allowance) {
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
	

	
	protected static boolean isUncrowdedExcept(LongOpenHashSet blocks, long targetPos, long ex0, long ex1) {
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
	
	protected static boolean inAndNotExcluded(LongOpenHashSet blocks, long targetPos, long ex0, long ex1) {
		return targetPos != ex0 && targetPos != ex1 && blocks.contains(targetPos);
	}
}