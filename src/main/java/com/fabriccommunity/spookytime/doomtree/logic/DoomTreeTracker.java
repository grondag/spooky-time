package com.fabriccommunity.spookytime.doomtree.logic;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DoomTreeTracker {
	private DoomTreeTracker() {}
	
	private static class TreeData {
		private final int dimId;
		private final long packedPos;
		private final int x;
		private final int y;
		private final int z;
		private final BlockPos pos;
		
		TreeData(int dimId, BlockPos pos) {
			this.dimId = dimId;
			this.pos = pos;
			this.packedPos = pos.asLong();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
		}
		
		boolean isNear(BlockPos pos) {
			final int dx = pos.getX() - x;
			final int dy = pos.getY() - y;
			final int dz = pos.getZ() - z;
			
			return (dx * dx + dy * dy + dz * dz <=  LIMIT);
		}
	}
	
	static final int LIMIT = RelativePos.MAX * RelativePos.MAX;
	
	// TODO: make configurable
	static final int MAX_TREES = 3;
	
	
	static final ObjectArrayList<TreeData> TREES = new ObjectArrayList<>(MAX_TREES);
	
	public static void clear() {
		TREES.clear();
	}
	
	static void track(World world, BlockPos pos) {
		if (!world.isClient && get(world, pos) == null) {
			//TODO: remove
			System.out.println("track " + pos.toString());
			TREES.add(new TreeData(world.dimension.getType().getRawId(), pos));
		}
	}
	
	static void untrack(World world, BlockPos pos) {
		if (!world.isClient) {
			//TODO: remove
			System.out.println("untrack " + pos.toString());
			
			TreeData tree = get(world, pos);
			if (tree != null) {
				TREES.remove(tree);
			}
		}
	}
	
	private static TreeData get(World world, BlockPos pos) {
		final int dim = world.dimension.getType().getRawId();
		final long p = pos.asLong();
		
		for (TreeData t : TREES) {
			if (t.dimId == dim && t.packedPos == p) {
				return t;
			}
		}
		
		return null;
	}
	
	private static TreeData getNear(World world, BlockPos pos) {
		final int dim = world.dimension.getType().getRawId();
		
		for (TreeData t : TREES) {
			if (t.dimId == dim && t.isNear(pos)) {
				return t;
			}
		}
		
		return null;
	}
	
	public static void reportBreak(World world, BlockPos pos, boolean isLog) {
		if (world.isClient) return;
		
		TreeData t = getNear(world, pos);
		
		if (t != null) {
			BlockEntity be = world.getBlockEntity(t.pos);
			
			if (be != null && be instanceof DoomTreeHeartBlockEntity) {
				((DoomTreeHeartBlockEntity) be).reportBreak(pos, isLog);
			}
		}
	}
	
}
