package com.fabriccommunity.spookytime.doomtree.heart;

import static com.fabriccommunity.spookytime.doomtree.heart.TreeBuilder.POINTS;
import static com.fabriccommunity.spookytime.doomtree.heart.TreeBuilder.POINT_COUNT;
import static com.fabriccommunity.spookytime.doomtree.heart.TreeBuilder.placeBranch;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.DoomLogBlock;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.math.BlockPos;

class BuilderJob implements Job {
	final int x;
	final int y;
	final int z;
	final long origin;
	final BlockPos.Mutable pos = new BlockPos.Mutable();
	final PositionCollector blocks = new PositionCollector();
	final ObjectArrayList<BranchPoint> points = new ObjectArrayList<>();
	BranchPoint lastPoint = null;
	final Random rand = new Random();
	final int centerHeight;

	int h = DoomLogBlock.TERMINAL_HEIGHT + 1;

	BuilderJob (DoomTreeHeartBlockEntity heart) {
		final BlockPos pos = heart.getPos();
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();

		origin = pos.asLong();
		points.addElements(0, POINTS, 0, POINT_COUNT);
		rand.setSeed(origin);
		centerHeight = TreeBuilder.centerHeight(pos);

		final PositionCollector blocks = this.blocks;
		for (long p : heart.logs) {
			blocks.add(p);
		}
	}

	@Override
	public Job apply(DoomTreeHeartBlockEntity heart) {
		BranchPoint point = points.get(rand.nextInt(points.size()));

		while (point == lastPoint ) {
			point = points.get(rand.nextInt(points.size()));
		}

		lastPoint = point;
		points.remove(point);

		if (points.isEmpty()) {
			points.addElements(0, POINTS, 0, POINT_COUNT);
		}

		pos.set(x + point.dx, y + h, z + point.dz);

		if (blocks.contains(pos.asLong())) {
			pos.set(x + point.xStart, y + h, z + point.zStart);
			placeBranch(heart.getWorld(), blocks, rand, x, z, pos.asLong(), pos, point, 36 - rand.nextInt(8));
		}

		if(++h > centerHeight) {
			for (long p : heart.logs) {
				blocks.remove(p);
			}

			heart.branches = blocks.toLongArray();
			heart.markDirty();
			return new PlacerJob(heart);
		} else {
			return this;
		}
	}
}