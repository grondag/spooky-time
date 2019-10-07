package com.fabriccommunity.spookytime.doomtree.heart;

import java.util.BitSet;

import com.fabriccommunity.spookytime.doomtree.DoomTree;
import com.fabriccommunity.spookytime.registry.SpookyBlocks;

import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
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
	private final IntHeapPriorityQueue todo = new IntHeapPriorityQueue((i0, i1) -> Integer.compare(squaredDistance(i0), squaredDistance(i1)));
	private final BitSet visited = new BitSet();
	
	static final Direction[] FACES = Direction.values();
	static final int DIAMETER = 128;
	static final int MIN = -63;
	/** INCLUSIVE! */
	static final int MAX = MIN + DIAMETER - 1;
	
	static int relativePos(final long heartPos, final int blockPos) {
		return relativePos(
				BlockPos.unpackLongX(heartPos), BlockPos.unpackLongY(heartPos), BlockPos.unpackLongZ(heartPos),
				BlockPos.unpackLongX(blockPos), BlockPos.unpackLongY(blockPos), BlockPos.unpackLongZ(blockPos));
	}
	
	static int relativePos(final int heartX, final int heartY, final int heartZ, final int x, final int y, final int z) {
		final int dx = x - heartX - MIN;
		final int dy = y - heartY - MIN;
		final int dz = z - heartZ - MIN;
		
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
	
	static int squaredDistance(int relPos) {
		final int x = rx(relPos);
		final int y = ry(relPos);
		final int z = rz(relPos);
		return x * x + y * y + z * z;
	}
	
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
		visited.clear();
		visited.set(relativePos(0, 0, 0));
		
		todo.enqueue(relativePos(-1, 0, 0));
		todo.enqueue(relativePos(1, 0, 0));
		todo.enqueue(relativePos(0, -1, 0));
		todo.enqueue(relativePos(0, 1, 0));
		todo.enqueue(relativePos(0, 0, -1));
		todo.enqueue(relativePos(0, 0, 1));
	}

	private void process(DoomTreeHeartBlockEntity heart) {
		final World world = heart.getWorld();
		final long origin = heart.getPos().asLong();
		final int x = BlockPos.unpackLongX(origin);
		final int y = BlockPos.unpackLongY(origin);
		final int z = BlockPos.unpackLongZ(origin);
		final BlockPos.Mutable mPos = heart.mPos;

		int p = todo.dequeueInt();

		for (int i = 0; i < 16; i++) {
			if (!visited.get(p)) {
				visited.set(p);
				mPos.set(x + rx(p), y + ry(p), z + rz(p));
				
				if (World.isValid(mPos) && world.isBlockLoaded(mPos)) {
					BlockState currentState = world.getBlockState(mPos);
					BlockState trollState = trollState(currentState);
					
					if (trollState != null) {
						if (trollState != currentState) {
							heart.trollQueue.enqueue(mPos.asLong());
						}
						
						continueFrom(p);
					}
				}
			}

			if (todo.isEmpty()) {
				return;
			} else {
				p = todo.dequeueInt();
			}
		}
	}

	private void continueFrom(int relPos) {
		final int rx = rx(relPos);
		final int ry = ry(relPos);
		final int rz = rz(relPos);
		
		if(rx > MIN) visitIfNeeded(relativePos(rx - 1, ry, rz));
		if(ry > MIN) visitIfNeeded(relativePos(rx, ry - 1, rz));
		if(rz > MIN) visitIfNeeded(relativePos(rx, ry, rz - 1));
		
		if(rx < MAX) visitIfNeeded(relativePos(rx + 1, ry, rz));
		if(ry < MAX) visitIfNeeded(relativePos(rx, ry + 1, rz));
		if(rz < MAX) visitIfNeeded(relativePos(rx, ry, rz + 1));
	}
	
	private void visitIfNeeded(int relPos) {
		if (!visited.get(relPos)) todo.enqueue(relPos);
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
