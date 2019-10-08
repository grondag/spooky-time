package com.fabriccommunity.spookytime.doomtree;

import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class DoomLeafBlock extends Block {

	public final boolean isPlaced;

	protected final float progressFactor;

	public DoomLeafBlock(Block.Settings settings, boolean isPlaced, float progressFactor) {
		super(settings);
		this.isPlaced = isPlaced;
		this.progressFactor = progressFactor;
	}

	public static class Height extends DoomLeafBlock {
		public Height(Block.Settings settings, float progressFactor) {
			super(settings, false, progressFactor);
		}
	}

	@Override
	public float calcBlockBreakingDelta(BlockState blockState, PlayerEntity player, BlockView blockView, BlockPos pos) {
		if (isPlaced) {
			return super.calcBlockBreakingDelta(blockState, player, blockView, pos);
		}

		final ItemStack stack = player.inventory.getInvStack(player.inventory.selectedSlot);

		if (stack.isEmpty() || !FabricToolTags.AXES.contains(stack.getItem()) || !stack.hasEnchantments()) {
			return 0;
		}

		return super.calcBlockBreakingDelta(blockState, player, blockView, pos) * progressFactor;
	}

	@Override
	public boolean isOpaque(BlockState blockState_1) {
		return false;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean canSuffocate(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1) {
		return false;
	}

	@Override
	public boolean allowsSpawning(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, EntityType<?> entityType_1) {
		return false;
	}

	@Override
	public int getLightSubtracted(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1) {
		return 1;
	}
}
