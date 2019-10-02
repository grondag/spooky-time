package com.fabriccommunity.spookytime.block;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.state.StateFactory.Builder;
import net.minecraft.state.property.IntProperty;

public class HauntedLogBlock extends Block {
	public static final int MAX_HEIGHT = 20;
	public static final int TERMINAL_HEIGHT = 12;
	
	public static IntProperty HEIGHT = IntProperty.of("height", 0, MAX_HEIGHT);
	
	public HauntedLogBlock() {
		super(FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES, 3).strength(50.0F, 1200.0F).build());
	}

	public static class Height extends HauntedLogBlock {
		@Override
		protected void appendProperties(Builder<Block, BlockState> builder) {
			super.appendProperties(builder);
			builder.add(HEIGHT);
		}
	}
}
