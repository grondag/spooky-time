package com.fabriccommunity.spookytime.hauntree;

import com.fabriccommunity.spookytime.client.model.SimpleUnbakedModel;
import com.fabriccommunity.spookytime.client.model.SpookyModels;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

public class HauntreeClient {
	public static void init() {
		SpookyModels.register("haunted_log", new SimpleUnbakedModel(HauntedLogModel::create, HauntedLogModel.TEXTURES));
		SpookyModels.register("haunted_log_channel", new SimpleUnbakedModel(HauntedLogChannel::create, HauntedLogChannel.CHANNEL_TEXTURES));
		SpookyModels.register("haunted_log_terminal", new SimpleUnbakedModel(HauntedLogTerminal::create, HauntedLogTerminal.TERMINAL_TEXTURES));
		ClientSidePacketRegistry.INSTANCE.register(HauntedTreePacket.IDENTIFIER, HauntedTreePacketHandler::accept);
	}
}
