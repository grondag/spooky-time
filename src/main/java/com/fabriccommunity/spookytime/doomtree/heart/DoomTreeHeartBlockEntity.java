package com.fabriccommunity.spookytime.doomtree.heart;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.DoomTree;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

public class DoomTreeHeartBlockEntity extends BlockEntity implements Tickable {
	int tickCounter = 100;
	long[] logs = null;
	long[] branches = null;
	long power = 1000;
	
	Job job = null;
	final BlockPos.Mutable mPos = new BlockPos.Mutable();
	
	final LongArrayFIFOQueue logQueue = new LongArrayFIFOQueue();

	public DoomTreeHeartBlockEntity(BlockEntityType<?> entityType) {
		super(entityType);
	}

	public DoomTreeHeartBlockEntity() {
		this(DoomTree.HAUNTED_TREE);
	}

	@Override
	public void tick() {
		if (world == null || logs == null || world.isClient) {
			return;
		}

		++power;
		--tickCounter;
		
		if (job == null) {
			idle();
		} else {
			job = job.apply(this);
		}
	}

	void idle() {
		if (power >= 100 && !logQueue.isEmpty()) {
			job = Jobs.place(this);
		} else if (--tickCounter <= 0) {
			job = MiasmaJob.run(this);
		}
	}
	
	void resetTickCounter(Random r) {
		tickCounter = r.nextInt(20);
	}

	void setTemplate(long[] blocks) {
		this.logs = blocks;
		this.branches = null;
		job = new BuilderJob(this);
		this.markDirty();
	}

	static final String LOG_KEY = "logPositions";
	static final String BRANCH_KEY = "branchPositions";
	static final String POWER_KEY = "power";
	
	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);

		power = tag.getLong(POWER_KEY);
		logs = tag.containsKey(LOG_KEY) ? tag.getLongArray(LOG_KEY) : null;
		branches = tag.containsKey(BRANCH_KEY) ? tag.getLongArray(BRANCH_KEY) : null;

		if (logs != null) {
			job = branches == null ? new BuilderJob(this) : new LogCheckJob(this);
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag = super.toTag(tag);
		tag.putLong(POWER_KEY, power);
		
		if (logs != null) {
			tag.putLongArray(LOG_KEY, logs);

			if (branches != null) {
				tag.putLongArray(BRANCH_KEY, branches);
			}
		}

		return tag;
	}
}

