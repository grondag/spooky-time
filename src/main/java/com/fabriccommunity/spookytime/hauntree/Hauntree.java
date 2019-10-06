package com.fabriccommunity.spookytime.hauntree;

import com.fabriccommunity.spookytime.SpookyTime;
import com.fabriccommunity.spookytime.registry.SpookyBlocks;
import com.fabriccommunity.spookytime.registry.SpookyFeatures;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class Hauntree {

	public static Block HAUNTED_SAPLING = SpookyBlocks.register("haunted_sapling",
			new HauntedSaplingBlock(FabricBlockSettings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS).build()));
	public static Block HAUNTED_TREE_HEART = SpookyBlocks.register("haunted_tree_heart", new HauntedTreeHeartBlock());
	public static Block MIASMA_BLOCK = SpookyBlocks.register("miasma", new MiasmaBlock());
	public static Block HAUNTED_LOG = SpookyBlocks.register("haunted_log", new HauntedLogBlock.Height());
	public static Block HAUNTED_LOG_CHANNEL = SpookyBlocks.register("haunted_log_channel", new HauntedLogBlock.Height());
	public static Block HAUNTED_LOG_TERMINAL = SpookyBlocks.register("haunted_log_terminal", new HauntedLogBlock());
	
	public static final BlockEntityType<HauntedTreeHeartBlockEntity> HAUNTED_TREE = 
			Registry.register(Registry.BLOCK_ENTITY, SpookyTime.id("haunted_tree"), BlockEntityType.Builder.create(HauntedTreeHeartBlockEntity::new, HAUNTED_TREE_HEART).build(null));
	
	public static Feature<DefaultFeatureConfig> HAUNTED_OAK = SpookyFeatures.register("haunted_oak", new HauntedTreeFeature(DefaultFeatureConfig::deserialize, false)); 

	public static Tag<Block> BLOCK_TROLL_WHITELIST = TagRegistry.block(SpookyTime.id("block_troll_whitelist"));

	public static void init() {
		
	}
}
