package com.fabriccommunity.spookytime.doomtree.heart;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.DoomTree;
import com.fabriccommunity.spookytime.registry.SpookyBlocks;

import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Seeker implements Job {
	private final LongOpenHashSet done = new LongOpenHashSet();
	private final LongArrayFIFOQueue todo = new LongArrayFIFOQueue();

	private final LongArrayList solid = new LongArrayList(8);
	private final LongArrayList air = new LongArrayList(8);

	static final Direction[] FACES = Direction.values();

	@Override
	public Job apply(DoomTreeHeartBlockEntity heart) {
		if (todo.isEmpty()) {
			start(heart);
		} else {
			process(heart);
		}
		return null;
	}

	private void start(DoomTreeHeartBlockEntity heart) {
		final long p = heart.getPos().asLong();
		done.clear();
		done.add(p);
		todo.enqueue(BlockPos.offset(p, FACES[ThreadLocalRandom.current().nextInt(6)]));
	}

	private void process(DoomTreeHeartBlockEntity heart) {
		final World world = heart.getWorld();
		final BlockPos.Mutable mPos = heart.mPos;
		final long origin = heart.getPos().asLong();
		final Random rand = ThreadLocalRandom.current();

		long p = todo.dequeueLong();

		for (int i = 0; i < 16; i++) {
			if (!done.contains(p)) {
				done.add(p);
				BlockState currentState = world.getBlockState(mPos.set(p));
				BlockState trollState = trollState(currentState);

				if (trollState != null) {
					if (trollState != currentState) {
						heart.trollQueue.enqueue(p);
					}

					continueFrom(world, origin, p, rand, mPos);
				}
			}

			if (todo.isEmpty()) {
				return;
			} else {
				p = todo.dequeueLong();
			}
		}
	}

	private void continueFrom(World world, long originPos, long fromPos, Random rand, BlockPos.Mutable mPos) {
		final int fromDist = squareDistance(originPos, fromPos);

		if (fromDist > 32 * 32 && rand.nextInt(10) < 4) {
			return;
		}

		final LongArrayList solid = this.solid;
		final LongArrayList air = this.air;
		solid.clear();
		air.clear();

		for (Direction face : FACES) {
			final long pos = BlockPos.offset(fromPos, face);

			if (done.contains(pos) || squareDistance(originPos, pos) < fromDist || !World.isValid(mPos.set(pos)) || !world.isBlockLoaded(mPos)) {
				continue;
			}

			if (world.isAir(mPos)) {
				air.add(pos);
			} else {
				solid.add(pos);
			}
		}

		boolean canAirBranch = true;

		if (!solid.isEmpty()) {
			todo.enqueue(solid.removeLong(rand.nextInt(solid.size())));
			if (rand.nextBoolean()) {
				return;
			}

			if (!solid.isEmpty()) {
				todo.enqueue(solid.removeLong(rand.nextInt(solid.size())));
				return;
			}

			canAirBranch = false;
		}

		if (!air.isEmpty()) {
			todo.enqueue(air.removeLong(rand.nextInt(air.size())));

			if (canAirBranch && !air.isEmpty() && rand.nextBoolean()) {
				todo.enqueue(air.removeLong(rand.nextInt(air.size())));
			}
		}
	}

	private int squareDistance(long fromPos, long toPos) {
		final int dx = BlockPos.unpackLongX(fromPos) - BlockPos.unpackLongX(toPos);
		final int dy = BlockPos.unpackLongY(fromPos) - BlockPos.unpackLongY(toPos);
		final int dz = BlockPos.unpackLongZ(fromPos) - BlockPos.unpackLongZ(toPos);
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Null means can't troll - tendril blocked 
	 */
	static BlockState trollState(BlockState fromState) {
		if (fromState.getMaterial() == Material.METAL) {
			return null;
		}

		final Block block = fromState.getBlock();

		if (block.matches(DoomTree.DOOM_TREE_IGNORED)) {
			return fromState;
		}

		if (block == Blocks.WATER) {
			return SpookyBlocks.WITCH_WATER_BLOCK.getDefaultState();
		}

		final Material material = fromState.getMaterial();

		if (TreeBuilder.canReplace(fromState)) {
			if (block.matches(BlockTags.LOGS) && fromState.contains(PillarBlock.AXIS)) {
				return DoomTree.DOOMED_LOG.getDefaultState().with(PillarBlock.AXIS, fromState.get(PillarBlock.AXIS));
			} else if (block.matches(BlockTags.DIRT_LIKE)) {
				return DoomTree.DOOMED_EARTH.getDefaultState();
			} else if (material == Material.AIR || PLANT_MATERIALS.contains(material)) {
				return DoomTree.MIASMA_BLOCK.getDefaultState();
			} else if (material == Material.STONE) {
				return DoomTree.DOOMED_STONE.getDefaultState();
			}
		}

		return fromState;
	}

	static final ObjectOpenHashSet<Material> PLANT_MATERIALS = new ObjectOpenHashSet<>();

	static {
		PLANT_MATERIALS.add(Material.BAMBOO);
		PLANT_MATERIALS.add(Material.BAMBOO_SAPLING);
		PLANT_MATERIALS.add(Material.CACTUS);
		PLANT_MATERIALS.add(Material.LEAVES);
		PLANT_MATERIALS.add(Material.ORGANIC);
		PLANT_MATERIALS.add(Material.PLANT);
		PLANT_MATERIALS.add(Material.PUMPKIN);
		PLANT_MATERIALS.add(Material.WOOD);
	}
}
