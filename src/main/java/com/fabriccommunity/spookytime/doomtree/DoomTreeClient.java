package com.fabriccommunity.spookytime.doomtree;

import com.fabriccommunity.spookytime.client.model.SimpleUnbakedModel;
import com.fabriccommunity.spookytime.client.model.SpookyModels;
import com.fabriccommunity.spookytime.doomtree.model.DoomLog;
import com.fabriccommunity.spookytime.doomtree.model.DoomLogChannel;
import com.fabriccommunity.spookytime.doomtree.model.DoomLogTerminal;
import com.fabriccommunity.spookytime.doomtree.model.DoomTreeHeart;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

public class DoomTreeClient {
	public static void init() {
		SimpleUnbakedModel model = new SimpleUnbakedModel(DoomLog::create, DoomLog.TEXTURES);
		SpookyModels.register("doom_log", model);
		SpookyModels.register("doom_log_p", model);
		
		model = new SimpleUnbakedModel(DoomLogChannel::create, DoomLogChannel.CHANNEL_TEXTURES);
		SpookyModels.register("doom_log_channel", model);
		SpookyModels.register("doom_log_channel_p", model);
		
		model = new SimpleUnbakedModel(DoomLogTerminal::create, DoomLogTerminal.TERMINAL_TEXTURES);
		SpookyModels.register("doom_log_terminal", model);
		SpookyModels.register("doom_log_terminal_p", model);
		
		SpookyModels.register("doom_tree_heart", new SimpleUnbakedModel(DoomTreeHeart::create, DoomTreeHeart.TERMINAL_TEXTURES));
		ClientSidePacketRegistry.INSTANCE.register(DoomTreePacket.IDENTIFIER, DoomTreePacketHandler::accept);
	}
}
