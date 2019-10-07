package com.fabriccommunity.spookytime.doomtree.heart;

import com.fabriccommunity.spookytime.doomtree.DoomTree;
import com.fabriccommunity.spookytime.doomtree.DoomTreePacket;
import com.fabriccommunity.spookytime.registry.SpookyBlocks;

import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TrollJob implements Job {
	static Job run(DoomTreeHeartBlockEntity heart) {
		final BlockPos.Mutable mPos = heart.mPos;
		final World world = heart.getWorld();
		final LongArrayFIFOQueue trollQueue = heart.trollQueue;

		BlockState trollState = null;
		BlockState currentState = null;
		
		long pos = 0;
		boolean didUpdate = false;
		
		for (int i = 0; i < 8; i++) {
			if(trollQueue.isEmpty()) {
				return null;
			}

			pos = heart.trollQueue.dequeueLong();
			mPos.set(pos);

			if (!World.isValid(mPos) || !world.isBlockLoaded(mPos)) {
				continue;
			}

			currentState = world.getBlockState(mPos);
			trollState = Seeker.trollState(currentState);

			if (trollState != null) {
				heart.resetTickCounter(ThreadLocalRandom.current());
				final Block block = trollState.getBlock();
				if (block == DoomTree.MIASMA_BLOCK) {
					placeMiasma(mPos, world);
					
					if (!currentState.isAir()) {
						heart.power += 20;
					}
				} else {
					if (block != SpookyBlocks.WITCH_WATER_BLOCK) {
						heart.power += 20;
					}
					world.setBlockState(mPos, trollState, 19);
					didUpdate = true;
				}
			}
		}
		
		if (didUpdate) {
			heart.markDirty();
		}

		
		return null;
	}

	static BlockState MIASMA_STATE = DoomTree.MIASMA_BLOCK.getDefaultState();

	static void placeMiasma(BlockPos pos, World world) {
		world.setBlockState(pos, MIASMA_STATE);
		final Packet<?> packet = DoomTreePacket.misama(pos);
		PlayerStream.around(world, pos, 32).forEach(p -> 
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(p, packet));
	}
}
