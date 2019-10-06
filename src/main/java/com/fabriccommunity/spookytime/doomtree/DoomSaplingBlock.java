package com.fabriccommunity.spookytime.doomtree;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.heart.DoomTreeFeature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.EntityContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DoomSaplingBlock extends PlantBlock implements Fertilizable {
	public static final IntProperty STAGE = Properties.STAGE;
	protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);


	public DoomSaplingBlock(Block.Settings settings) {
		super(settings);
		setDefaultState(stateFactory.getDefaultState().with(STAGE, 0));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, EntityContext entityContext) {
		return SHAPE;
	}

	@Override
	public void onScheduledTick(BlockState blockState, World world, BlockPos blockPos, Random random) {
		super.onScheduledTick(blockState, world, blockPos, random);
		if (random.nextInt(7) == 0) {
			generate(world, blockPos, blockState, random);
		}
	}

	public void generate(IWorld world, BlockPos pos, BlockState blockState, Random random) {
		if (blockState.get(STAGE) == 0) {
			world.setBlockState(pos, (BlockState)blockState.cycle(STAGE), 4);
		} else {
			generateInner(world, pos, blockState, random);
		}
	}

	@Override
	public boolean isFertilizable(BlockView blockView, BlockPos blockPos, BlockState blockState, boolean flag) {
		return true;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos blockPos, BlockState blockState) {
		return world.random.nextFloat() < 0.45f;
	}

	@Override
	public void grow(World world, Random random, BlockPos blockPos, BlockState blockState) {
		generate(world, blockPos, blockState, random);
	}

	@Override
	protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
		builder.add(STAGE);
	}

	private boolean generateInner(IWorld world, BlockPos pos, BlockState blockState, Random random) {
		Feature<DefaultFeatureConfig> feature = new DoomTreeFeature(DefaultFeatureConfig::deserialize, true);
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);

		if (feature.generate(world, world.getChunkManager().getChunkGenerator(), random, pos, FeatureConfig.DEFAULT)) {
			return true;
		} else {
			world.setBlockState(pos, blockState, 4);
			return false;
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState blockState, World world, BlockPos pos, Random rand) {
		final double x = pos.getX() + rand.nextDouble();
		final double y = pos.getY() + rand.nextDouble();
		final double z = pos.getZ() + rand.nextDouble();
		world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
	}
}
