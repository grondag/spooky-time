package com.fabriccommunity.spookytime.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import com.fabriccommunity.spookytime.network.HauntedTreePacket;
import com.fabriccommunity.spookytime.network.HauntedTreePacketHandler;
import com.fabriccommunity.spookytime.registry.SpookyNetworking;

@Environment(EnvType.CLIENT)
public class SpookyClientNetworking {
	private SpookyClientNetworking() {
		// NO-OP
	}
	
	public static void init() {
		ClientSidePacketRegistry.INSTANCE.register(SpookyNetworking.SHOW_FLOATING_ITEM_S2C, (context, buf) -> {
			int rawId = buf.readVarInt();
			ItemStack stack = Registry.ITEM.get(rawId).getStackForRender();
			MinecraftClient.getInstance().gameRenderer.showFloatingItem(stack);
		});
		
		ClientSidePacketRegistry.INSTANCE.register(HauntedTreePacket.IDENTIFIER, HauntedTreePacketHandler::accept);
	}
}
