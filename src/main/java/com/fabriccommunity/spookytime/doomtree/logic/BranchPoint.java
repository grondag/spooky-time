package com.fabriccommunity.spookytime.doomtree.logic;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

class BranchPoint {
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