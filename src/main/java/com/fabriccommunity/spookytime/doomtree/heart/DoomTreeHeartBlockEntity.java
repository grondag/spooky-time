package com.fabriccommunity.spookytime.doomtree.heart;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.DoomTree;
import com.fabriccommunity.spookytime.doomtree.DoomTreePacket;

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

public class DoomTreeHeartBlockEntity extends BlockEntity implements Tickable {

	Job job = null;

	public DoomTreeHeartBlockEntity(BlockEntityType<?> entityType) {
		super(entityType);
	}

	public DoomTreeHeartBlockEntity() {
		this(DoomTree.HAUNTED_TREE);
	}

	int tickCounter = 100;
	long[] logs = null;
	long[] branches = null;

	@Override
	public void tick() {
		if (this.world == null || this.logs == null || this.world.isClient) {
			return;
		}

		if (job == null) {
			idle();
		} else {
			--tickCounter;
			job = job.apply(this);
		}
	}

	void idle() {
		if (--tickCounter <= 0) {
			Random r = ThreadLocalRandom.current();
			if (placeMiasma(r)) {
				// TODO: bump up
				tickCounter = r.nextInt(20);
			}
		}
	}

	final BlockPos.Mutable mPos = new BlockPos.Mutable();
	static BlockState MIASMA_STATE = DoomTree.MIASMA_BLOCK.getDefaultState();


	boolean placeMiasma(Random r) {
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

	boolean placeMiasma(BlockPos pos, Random r, boolean smokeDone) {
		if (World.isValid(pos) && world.isBlockLoaded(mPos) && world.isAir(pos)) {
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

	static final String LOG_KEY = "logPositions";
	static final String BRANCH_KEY = "branchPositions";

	void setTemplate(long[] blocks) {
		this.logs = blocks;
		this.branches = null;
		job = new BuilderJob(this);
		this.markDirty();
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);

		logs = tag.containsKey(LOG_KEY) ? tag.getLongArray(LOG_KEY) : null;
		branches = tag.containsKey(BRANCH_KEY) ? tag.getLongArray(BRANCH_KEY) : null;

		if (logs != null && branches == null) {
			job = new BuilderJob(this);
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag = super.toTag(tag);

		if (logs != null) {
			tag.putLongArray(LOG_KEY, logs);

			if (branches != null) {
				tag.putLongArray(BRANCH_KEY, branches);
			}
		}

		return tag;
	}
}

