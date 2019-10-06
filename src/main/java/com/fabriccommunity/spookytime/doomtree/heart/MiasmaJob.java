package com.fabriccommunity.spookytime.doomtree.heart;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.DoomTree;
import com.fabriccommunity.spookytime.doomtree.DoomTreePacket;

import io.netty.util.internal.ThreadLocalRandom;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MiasmaJob implements Job {
	static Job run(DoomTreeHeartBlockEntity heart) {
		final Random r = ThreadLocalRandom.current();
		if (placeMiasma(r, heart.getPos(), heart.mPos, heart.getWorld())) {
			heart.resetTickCounter(r);
		};
		return null;
	}

	static BlockState MIASMA_STATE = DoomTree.MIASMA_BLOCK.getDefaultState();

	static boolean placeMiasma(Random r, BlockPos pos, BlockPos.Mutable mPos, World world) {

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		final int dx = (int) (r.nextGaussian() * 24);
		final int dz = (int) (r.nextGaussian() * 24);
		final int dy = (int) (r.nextGaussian() * 24);

		if (dx * dx + dy * dy + dz * dz > 24 * 24) return false;

		boolean result = placeMiasma(r, mPos.set(x + dx, y + dy, z + dz), world, false);
		result |= placeMiasma(r, mPos.set(x + dx + 1, y + dy, z + dz), world, result);
		result |= placeMiasma(r, mPos.set(x + dx - 1, y + dy, z + dz), world, result);
		result |= placeMiasma(r, mPos.set(x + dx, y + dy + 1, z + dz), world, result);
		result |= placeMiasma(r, mPos.set(x + dx, y + dy - 1, z + dz), world, result);
		result |= placeMiasma(r, mPos.set(x + dx, y + dy, z + dz + 1), world, result);
		result |= placeMiasma(r, mPos.set(x + dx, y + dy, z + dz - 1), world, result);

		return result;
	}

	static boolean placeMiasma(Random r, BlockPos pos, World world, boolean smokeDone) {
		if (World.isValid(pos) && world.isBlockLoaded(pos) && world.isAir(pos)) {
			world.setBlockState(pos, MIASMA_STATE);
			if (!smokeDone) {
				final Packet<?> packet = DoomTreePacket.misama(pos);
				PlayerStream.around(world, pos, 32).forEach(p -> 
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(p, packet));
			}
			return true;
		} else {
			return false;
		}
	}
}
