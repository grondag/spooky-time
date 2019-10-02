package com.fabriccommunity.spookytime.block;

import com.fabriccommunity.spookytime.entity.HauntedTreeHeartBlockEntity;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class HauntedTreeHeartBlock extends BlockWithEntity {

	public HauntedTreeHeartBlock() {
		super(FabricBlockSettings.of(Material.WOOD).dropsNothing().breakByTool(FabricToolTags.AXES, 3).strength(50.0F, 1200.0F).build());
	}

	@Override
	public BlockEntity createBlockEntity(BlockView var1) {
		return new HauntedTreeHeartBlockEntity();
	}

	@Override
	public float calcBlockBreakingDelta(BlockState blockState, PlayerEntity player, BlockView blockView, BlockPos pos) {
		final ItemStack stack = player.inventory.getInvStack(player.inventory.selectedSlot);

		if (stack.isEmpty() || !FabricToolTags.AXES.contains(stack.getItem())) {
			return 0;
		}

		if (EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) < 3) {
			return 0;
		}

		return super.calcBlockBreakingDelta(blockState, player, blockView, pos);
	}
}
