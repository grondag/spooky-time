package com.fabriccommunity.spookytime.doomtree;

import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory.Builder;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class DoomLogBlock extends Block {
	public static final int MAX_HEIGHT = 20;
	public static final int TERMINAL_HEIGHT = 12;
	
	public static IntProperty HEIGHT = IntProperty.of("height", 0, MAX_HEIGHT);
	
	public final boolean isPlaced;
	
	protected final float progressFactor;
	
	public DoomLogBlock(Block.Settings settings, boolean isPlaced, float progressFactor) {
		super(settings);
		this.isPlaced = isPlaced;
		this.progressFactor = progressFactor;
	}

	public static class Height extends DoomLogBlock {
		public Height(Block.Settings settings, float progressFactor) {
			super(settings, false, progressFactor);
		}

		@Override
		protected void appendProperties(Builder<Block, BlockState> builder) {
			super.appendProperties(builder);
			builder.add(HEIGHT);
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
}
