package com.fabriccommunity.spookytime.doomtree.logic;

import static com.fabriccommunity.spookytime.doomtree.logic.RelativePos.relativePos;
import static com.fabriccommunity.spookytime.doomtree.logic.RelativePos.rx;
import static com.fabriccommunity.spookytime.doomtree.logic.RelativePos.ry;
import static com.fabriccommunity.spookytime.doomtree.logic.RelativePos.rz;

import java.util.Random;
import java.util.function.Consumer;

import com.fabriccommunity.spookytime.doomtree.DoomTree;

import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LeafMaker {
	int[] roots;

	Consumer<DoomTreeHeartBlockEntity> runner = new Preparator();

	void run(DoomTreeHeartBlockEntity heart) {
		runner.accept(heart);
	}

	void growLeaves(DoomTreeHeartBlockEntity heart) {
		final BlockPos.Mutable mPos = heart.mPos;
		final Random r = ThreadLocalRandom.current();
		final World world = heart.getWorld();

		final BlockPos origin = heart.getPos();
		final int rp = roots[r.nextInt(roots.length)];
		final long pos = RelativePos.absolutePos(origin, rp);

		mPos.set(pos);

		BlockState currentState = world.getBlockState(mPos);

		if (currentState.isAir()) {
			small(world, pos, mPos, r);
		} else if (currentState == LEAF_STATE) {
			mPos.add(0, 2, 0);
			if (world.isAir(mPos)) {
				medium(world, pos, mPos, r);
			} else {
				for (int i = 0; i < 8; i++) {
					addRandom(world, pos, mPos, r);
				}
			}
		}
	}

	private static void addRandom(World world, long pos, Mutable mPos, Random r) {
		int x = 0;
		int y = 0;
		int z = 0;
		
		do {
			x = 1 + r.nextInt(8);
			y = r.nextInt(8);
			z = 1 + r.nextInt(8);
		} while (x * x + y * y + z * z > 64);
		
		if(r.nextBoolean()) x = -x;
		if(r.nextBoolean()) z = -z;
		
		final long p = BlockPos.add(pos, x, y, z);
		
		if (world.isAir(mPos.set(p)) && hasNeighborLeaf(world, p, mPos)) {
			world.setBlockState(mPos.set(p), LEAF_STATE, 18);
		}
	}

	private static boolean hasNeighborLeaf(World world, long pos, Mutable mPos) {
		if(world.getBlockState(mPos.set(pos).setOffset(Direction.DOWN)) == LEAF_STATE) return true;
		if(world.getBlockState(mPos.set(pos).setOffset(Direction.EAST)) == LEAF_STATE) return true;
		if(world.getBlockState(mPos.set(pos).setOffset(Direction.WEST)) == LEAF_STATE) return true;
		if(world.getBlockState(mPos.set(pos).setOffset(Direction.NORTH)) == LEAF_STATE) return true;
		if(world.getBlockState(mPos.set(pos).setOffset(Direction.SOUTH)) == LEAF_STATE) return true;
		return false;
	}
	
	static final BlockState LEAF_STATE = DoomTree.DOOM_LEAF.getDefaultState();

	private void small(World world, long pos, Mutable mPos, Random r) {
		world.setBlockState(mPos.set(pos), LEAF_STATE, 18);
		smallNeighbors(world, pos, mPos);
	}

	private void smallNeighbors(World world, long pos, Mutable mPos) {
		setLeaf(world, mPos.set(BlockPos.add(pos, -1, 0, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 1, 0, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 1, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 0, -1)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 0, 1)));
	}

	private boolean setLeaf(World world, BlockPos pos) {
		if (world.isAir(pos)) {
			world.setBlockState(pos, LEAF_STATE, 18);
			return true;
		} else {
			return false;
		}
	}

	private void medium(World world, long pos, Mutable mPos, Random r) {
		smallNeighbors(world, pos, mPos);
		
		setLeaf(world, mPos.set(BlockPos.add(pos, -1, 0, -1)));
		setLeaf(world, mPos.set(BlockPos.add(pos, -1, 0, 1)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 1, 0, -1)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 1, 0, 1)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 0, 1)));
		
		setLeaf(world, mPos.set(BlockPos.add(pos, -1, 1, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 1, 1, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 2, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 1, -1)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 1, 1)));
		
		setLeaf(world, mPos.set(BlockPos.add(pos, -2, 0, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 2, 0, 0)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 0, -2)));
		setLeaf(world, mPos.set(BlockPos.add(pos, 0, 0, 2)));
	}


	private class Preparator implements Consumer<DoomTreeHeartBlockEntity> {
		final IntOpenHashSet branches = new IntOpenHashSet();
		final IntArrayList spaces = new IntArrayList();
		IntIterator it = null;

		@Override
		public void accept(DoomTreeHeartBlockEntity heart) {
			if (branches.isEmpty()) {
				final long origin = heart.getPos().asLong();

				for (long pos : heart.branches) {
					branches.add(relativePos(origin, pos));
				}

				it = branches.iterator();
			} else {
				int i = 0;

				while (it.hasNext() && i++ < 128) {
					final int p = it.nextInt();
					final int y = ry(p) + 1;

					if (y < 7) continue;

					final int up = relativePos(rx(p), y, rz(p));

					if(branches.contains(up)) continue;

					spaces.add(up);
				}

				if (!it.hasNext()) {
					roots = spaces.toIntArray();
					runner = LeafMaker.this::growLeaves;
				}
			}
		}
	}
}
