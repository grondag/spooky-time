package com.fabriccommunity.spookytime.entity;

import java.util.Random;

import com.fabriccommunity.spookytime.network.HauntedTreePacket;
import com.fabriccommunity.spookytime.registry.SpookyBlockEntities;
import com.fabriccommunity.spookytime.registry.SpookyBlocks;

import io.netty.util.internal.ThreadLocalRandom;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HauntedTreeHeartBlockEntity extends BlockEntity implements Tickable {
	public HauntedTreeHeartBlockEntity(BlockEntityType<?> entityType) {
		super(entityType);
	}

	public HauntedTreeHeartBlockEntity() {
		this(SpookyBlockEntities.HAUNTED_TREE);
	}

	protected int tickCounter = 100;
	protected long[] logs = null;
	
	@Override
	public void tick() {
		if (this.world == null || this.world.isClient) {
			return;
		}
		
		if(--tickCounter > 0) {
			return;
		}
		
		Random r = ThreadLocalRandom.current();
		if (placeMiasma(r)) {
			// TODO: bump up
			tickCounter = r.nextInt(20);
		}			
	}
	
	final BlockPos.Mutable mPos = new BlockPos.Mutable();
	private static BlockState MIASMA_STATE = SpookyBlocks.MIASMA_BLOCK.getDefaultState();
	
	
	protected boolean placeMiasma(Random r) {
		final BlockPos pos = this.pos;
		
		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();
		
		final int dx = (int) (r.nextGaussian() * 24);
		final int dz = (int) (r.nextGaussian() * 24);
		final int dy = (int) (r.nextGaussian() * 24);
		
		if (dx * dx + dy * dy + dz * dz > 24 * 24) return false;
		
		boolean result = placeMiasma(mPos.set(x + dx, y + dy, z + dz), r, false);
		result |= placeMiasma(mPos.set(x + dx + 1, y + dy, z + dz), r, result);
		result |= placeMiasma(mPos.set(x + dx - 1, y + dy, z + dz), r, result);
		result |= placeMiasma(mPos.set(x + dx, y + dy + 1, z + dz), r, result);
		result |= placeMiasma(mPos.set(x + dx, y + dy - 1, z + dz), r, result);
		result |= placeMiasma(mPos.set(x + dx, y + dy, z + dz + 1), r, result);
		result |= placeMiasma(mPos.set(x + dx, y + dy, z + dz - 1), r, result);
		
		return result;
	}
	
	protected boolean placeMiasma(BlockPos pos, Random r, boolean smokeDone) {
		if (World.isValid(pos) && world.isBlockLoaded(mPos) && world.isAir(pos)) {
			world.setBlockState(pos, MIASMA_STATE);
			if (!smokeDone) {
				final Packet<?> packet = HauntedTreePacket.misama(pos);
				PlayerStream.around(world, pos, 32).forEach(p -> 
					ServerSidePacketRegistry.INSTANCE.sendToPlayer(p, packet));
			}
			return true;
		} else {
			return false;
		}
	}
	
	private static final String LOG_KEY = "logPositions";

	public void setTemplate(long[] blocks) {
		this.logs = blocks;
		this.markDirty();
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		
		logs = tag.containsKey(LOG_KEY) ? tag.getLongArray(LOG_KEY) : null;
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag = super.toTag(tag);

		if (logs != null) {
			tag.putLongArray(LOG_KEY, logs);
		}
		
		return tag;
	}
}
	
