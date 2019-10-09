package com.fabriccommunity.spookytime.doomtree;

import com.fabriccommunity.spookytime.SpookyTime;
import com.fabriccommunity.spookytime.doomtree.logic.DoomTreeFeature;
import com.fabriccommunity.spookytime.doomtree.logic.DoomTreeHeartBlockEntity;
import com.fabriccommunity.spookytime.doomtree.logic.DoomTreeTracker;
import com.fabriccommunity.spookytime.registry.SpookyBlocks;
import com.fabriccommunity.spookytime.registry.SpookyFeatures;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class DoomTree {

	public static Block DOOM_SAPLING = SpookyBlocks.register("doom_sapling",
			new DoomSaplingBlock(FabricBlockSettings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS).build()));

	public static Block MIASMA_BLOCK = Registry.register(Registry.BLOCK, SpookyTime.id("miasma"), new MiasmaBlock());

	public static Block DOOM_TREE_HEART = Registry.register(Registry.BLOCK, SpookyTime.id("doom_tree_heart"), 
			new DoomTreeHeartBlock(FabricBlockSettings.of(Material.WOOD).dropsLike(DOOM_SAPLING).breakByTool(FabricToolTags.AXES, 3).strength(200.0F, 1200.0F).build()));

	public static Block PLACED_DOOM_LOG = SpookyBlocks.register("doom_log_p", new DoomLogBlock(logSettings().build(), true, 1));
	public static Block PLACED_DOOM_LOG_CHANNEL = SpookyBlocks.register("doom_log_channel_p", new DoomLogBlock(logSettings().build(), true, 1));
	public static Block PLACED_DOOM_LOG_TERMINAL = SpookyBlocks.register("doom_log_terminal_p", new DoomLogBlock(logSettings().build(), true, 1));

	public static Block DOOM_LOG = Registry.register(Registry.BLOCK, SpookyTime.id("doom_log"),
			new DoomLogBlock.Height(logSettings().dropsLike(PLACED_DOOM_LOG).build(), 0.04f));
	public static Block DOOM_LOG_CHANNEL = Registry.register(Registry.BLOCK, SpookyTime.id("doom_log_channel"),
			new DoomLogBlock.Height(logSettings().dropsLike(PLACED_DOOM_LOG_CHANNEL).build(), 0.02f));
	public static Block DOOM_LOG_TERMINAL = Registry.register(Registry.BLOCK, SpookyTime.id("doom_log_terminal"), 
			new DoomLogBlock(logSettings().dropsLike(PLACED_DOOM_LOG_TERMINAL).build(), false, 0.02f));
	
	public static Block DOOM_LEAF = SpookyBlocks.register("doom_leaves", new DoomLeafBlock(doomedSettings(), false, 1));
	
	public static Block DOOMED_LOG = SpookyBlocks.register("doomed_log", new DoomedLogBlock(doomedSettings()));
	public static Block DOOMED_EARTH = SpookyBlocks.register("doomed_earth", new DoomedBlock(doomedSettings()));
	public static Block DOOMED_STONE = SpookyBlocks.register("doomed_stone", new DoomedBlock(doomedSettings()));
	
	public static final BlockEntityType<DoomTreeHeartBlockEntity> HAUNTED_TREE = 
			Registry.register(Registry.BLOCK_ENTITY, SpookyTime.id("doom_tree"), BlockEntityType.Builder.create(DoomTreeHeartBlockEntity::new, DOOM_TREE_HEART).build(null));

	public static Feature<DefaultFeatureConfig> DOOM_TREE_FEATURE = SpookyFeatures.register("doom_tree", new DoomTreeFeature(DefaultFeatureConfig::deserialize, false)); 

	public static Tag<Block> DOOM_TREE_WHITELIST = TagRegistry.block(SpookyTime.id("doom_tree_whitelist"));
	
	public static Tag<Block> DOOM_TREE_IGNORED = TagRegistry.block(SpookyTime.id("doom_tree_ignored"));

	public static void init() {
		ServerStopCallback.EVENT.register(s -> DoomTreeTracker.clear());
	}

	private static FabricBlockSettings logSettings() {
		return FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES, 2).strength(3.0F, 20.0F);
	}
	
	private static Block.Settings doomedSettings() {
		return FabricBlockSettings.of(Material.EARTH).breakByHand(true).breakInstantly().dropsNothing().sounds(BlockSoundGroup.SAND).build();
	}
}

