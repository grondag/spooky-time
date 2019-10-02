package com.fabriccommunity.spookytime.registry;

import net.fabricmc.fabric.api.tag.TagRegistry;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;

import com.fabriccommunity.spookytime.SpookyTime;

public class SpookyTags {
	public static Tag<Item> COSTUMES = register("costumes");
	
	public static Tag<Block> BLOCK_TROLL_WHITELIST = TagRegistry.block(SpookyTime.id("block_troll_whitelist"));
	
	private SpookyTags() {
		// NO-OP
	}
	
	public static void init() {
	
	}
	
	public static Tag<Item> register(String name) {
		return TagRegistry.item(SpookyTime.id(name));
	}
}
