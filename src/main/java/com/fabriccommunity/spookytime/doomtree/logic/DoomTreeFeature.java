package com.fabriccommunity.spookytime.doomtree.logic;

import java.util.Random;
import java.util.function.Function;

import com.fabriccommunity.spookytime.doomtree.DoomTree;
import com.mojang.datafixers.Dynamic;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class DoomTreeFeature extends Feature<DefaultFeatureConfig> {
	public DoomTreeFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> func, boolean flag) {
		super(func, flag);
	}

	@Override
	public boolean generate(IWorld world, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGen, Random rand, BlockPos posIn, DefaultFeatureConfig config) {
		final PositionCollector blocks = new PositionCollector();
		final BlockPos.Mutable mPos = new BlockPos.Mutable();

		if (!generateInner(world, rand, posIn, blocks, mPos)) {
			return false;
		}

		world.setBlockState(posIn, DoomTree.DOOM_TREE_HEART.getDefaultState(), 3);
		final BlockEntity be = world.getBlockEntity(posIn);

		if (be != null && be instanceof DoomTreeHeartBlockEntity) {
			DoomTreeHeartBlockEntity heart = (DoomTreeHeartBlockEntity) be;
			blocks.sort((l0, l1) -> Integer.compare(BlockPos.unpackLongY(l0), BlockPos.unpackLongY(l1)));
			heart.setTemplate(blocks.toLongArray());
		}

		return true;
	}
	
	protected boolean generateInner(IWorld world, Random rand, BlockPos posIn, PositionCollector blocks, BlockPos.Mutable mPos) {
		final int x = posIn.getX();
		final int y = posIn.getY();
		final int z = posIn.getZ();

		final int centerHeight = TreeDesigner.centerHeight(posIn);

		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z), centerHeight)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z - 1), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 1, y - 1, z), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x, y - 1, z + 1), centerHeight - rand.nextInt(5) - 1)) return false;

		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z - 1), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x - 1, y - 1, z + 1), centerHeight - rand.nextInt(5) - 1)) return false;
		if (!placeTrunkSection(world, blocks, mPos.set(x + 1, y - 1, z - 1), centerHeight - rand.nextInt(5) - 1)) return false;
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

		// heart position (origin) doesn't get log
		blocks.remove(posIn.asLong());

		return true;
	}

	protected static boolean placeTrunkSection(IWorld world, PositionCollector blocks, BlockPos.Mutable pos, int height) {
		final int yMin = pos.getY();

		for (int y = 0; y < height; y++) {
			pos.setY(yMin + y); 
			if (!placeTrunk(world, blocks, pos)) return false;
		}

		return true;
	}

	protected static boolean placeTrunk(IWorld world, PositionCollector blocks, BlockPos.Mutable pos) {
		if (TreeDesigner.canReplace(world, pos)) {
			blocks.add(pos.asLong());
			return true;
		} else {
			return false;
		}
	}
	

}