package com.fabriccommunity.spookytime.doomtree;

import com.fabriccommunity.spookytime.doomtree.logic.DoomTreeTracker;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class MiasmaBlock extends Block {

	public MiasmaBlock() {
		super(FabricBlockSettings.copy(Blocks.AIR).build());
	}

	@Override
	public BlockRenderType getRenderType(BlockState blockState_1) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, EntityContext entityContext_1) {
		return VoxelShapes.empty();
	}

	@Override
	public void onBlockRemoved(BlockState myState, World world, BlockPos blockPos, BlockState newState, boolean someFlag) {
		super.onBlockRemoved(myState, world, blockPos, newState, someFlag);
		
		if (!world.isClient) {
			DoomTreeTracker.reportBreak(world, blockPos, false);
		}
	}
}
