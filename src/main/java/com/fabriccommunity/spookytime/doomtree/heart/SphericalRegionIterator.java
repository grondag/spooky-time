package com.fabriccommunity.spookytime.doomtree.heart;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class SphericalRegionIterator {

	private int octant = 0;
	private int index = 0;
	private int x = 0;
	private int y = 0;
	private int z = 0;

	boolean atEnd() {
		return index == POSITION_COUNT - 1 && index == 7;
	}

	void reset() {
		octant = 0;
		index = 0;
		readPos();
		advanceIfInvalid();
	}

	private static final int X_BIT = 1;
	private static final int Y_BIT = 2;
	private static final int Z_BIT = 4;

	int x() {
		return (octant & X_BIT) == 0 ? x : -x;
	}

	int y() {
		return (octant & Y_BIT) == 0 ? y : -y;
	}

	int z() {
		return (octant & Z_BIT) == 0 ? z : -z;
	}

	private void advanceIfInvalid() {
		while (!isValid()) {
			next();
		}
	}

	private boolean isValid() {
		// positions at zero for a given axis are only included in positive octants for that axis
		if(x == 0 && (octant & X_BIT) == X_BIT) return false;
		if(y == 0 && (octant & Y_BIT) == Y_BIT) return false;
		if(z == 0 && (octant & Z_BIT) == Z_BIT) return false;
		return true;
	}

	private void advance() {
		if (++octant == 8) {
			octant = 0;

			if (++index == POSITION_COUNT) {
				index = 0;
			}
		}
		readPos();
	}

	void next() {
		advance();
		advanceIfInvalid();
	}

	private void readPos() {
		final int position = POSITIONS[index];
		x = x(position);
		y = y(position);
		z = z(position);
	}

	static final int MAX_RADIUS = 64;
	private static final int[] POSITIONS;
	static final int POSITION_COUNT;

	static {
		final int limit = MAX_RADIUS * MAX_RADIUS;

		IntArrayList positions = new IntArrayList();
		for (int x = 0; x <= MAX_RADIUS; x++) {
			for (int y = 0; y <= MAX_RADIUS; y++) {
				for (int z = 0; z <= MAX_RADIUS; z++) {
					// don't include origin
					if ((x | y | z) == 0) {
						continue;
					}

					if (x * x + y * y + z * z <= limit) {
						positions.add(posToInt(x, y, z));
					}
				}
			}
		}

		// randomize
		positions.sort((i0, i1) -> Integer.compare(HashCommon.mix(i0), HashCommon.mix(i1)));

		// nearest first
		positions.sort((i0, i1) -> Integer.compare(squareDistance(i0), squareDistance(i1)));

		POSITIONS = positions.toIntArray();
		POSITION_COUNT = POSITIONS.length;
	}

	private static int posToInt(int x, int y, int z) {
		return (x + MAX_RADIUS) | ((y + MAX_RADIUS) << 8) | ((z + MAX_RADIUS) << 16);
	}

	private static int squareDistance(int pos) {
		final int x = x(pos);
		final int y = y(pos);
		final int z = z(pos);
		return x * x + y * y + z * z;
	}

	private static int x(int pos) {
		return (pos & 0xFF) - MAX_RADIUS;
	}

	private static int y(int pos) {
		return ((pos >> 8) & 0xFF) - MAX_RADIUS;
	}

	private static int z(int pos) {
		return ((pos >> 16) & 0xFF) - MAX_RADIUS;
	}

	public void apply(DoomTreeHeartBlockEntity heart) {
		final World world = heart.getWorld();
		final BlockPos pos = heart.getPos();
		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		final BlockPos.Mutable mPos = heart.mPos;
		for (int i = 0; i < 32; i++) {
			mPos.set(x() + x, y() + y, z() + z);
			final BlockState currentState = world.getBlockState(mPos);
			final BlockState trollState = Seeker.trollState(currentState);
			
			if (trollState != null && trollState != currentState) {
				heart.trollQueue.enqueue(mPos.asLong());
			}
			next();
		}
	}
}
