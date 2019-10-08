package com.fabriccommunity.spookytime.doomtree.logic;

import net.minecraft.util.math.BlockPos;

final class RelativePos {
	RelativePos() { }
	
	static final int DIAMETER = 128;
	static final int MIN = -63;
	/** INCLUSIVE! */
	static final int MAX = MIN + DIAMETER - 1;
	
	static int relativePos(final long originPos, final long blockPos) {
		return relativePos(
				BlockPos.unpackLongX(originPos), BlockPos.unpackLongY(originPos), BlockPos.unpackLongZ(originPos),
				BlockPos.unpackLongX(blockPos), BlockPos.unpackLongY(blockPos), BlockPos.unpackLongZ(blockPos));
	}
	
	static int relativePos(final int originX, final int originY, final int originZ, final int x, final int y, final int z) {
		final int dx = x - originX - MIN;
		final int dy = y - originY - MIN;
		final int dz = z - originZ - MIN;
		
		return dx | (dy << 7) | (dz << 14);
	}
	
	static int relativePos(final int dx, final int dy, final int dz) {
		return (dx - MIN) | ((dy - MIN) << 7) | ((dz - MIN) << 14);
	}
	
	static int rx(int relPos) {
		return (relPos & 127) + MIN;
	}
	
	static int ry(int relPos) {
		return ((relPos >> 7) & 127) + MIN;
	}
	
	static int rz(int relPos) {
		return ((relPos >> 14) & 127) + MIN;
	}
	
	static long absolutePos(BlockPos originPos, int relativePos) {
		return absolutePos(originPos.getX(), originPos.getY(), originPos.getZ(), relativePos);
	}
	
	static long absolutePos(long originPos, int relativePos) {
		return BlockPos.add(originPos, rx(relativePos), ry(relativePos), rz(relativePos));
	}
	
	static long absolutePos(int originX, int originY, int originZ, int relativePos) {
		return BlockPos.asLong(originX + rx(relativePos), originY + ry(relativePos), originZ + rz(relativePos));
	}
	
	static int squaredDistance(int relPos) {
		final int x = rx(relPos);
		final int y = ry(relPos);
		final int z = rz(relPos);
		return x * x + y * y + z * z;
	}
}
