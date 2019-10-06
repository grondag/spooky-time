package com.fabriccommunity.spookytime.doomtree;

import static com.fabriccommunity.spookytime.doomtree.TreeBuilder.POINTS;
import static com.fabriccommunity.spookytime.doomtree.TreeBuilder.POINT_COUNT;
import static com.fabriccommunity.spookytime.doomtree.TreeBuilder.canReplace;
import static com.fabriccommunity.spookytime.doomtree.TreeBuilder.placeBranch;

import java.util.Random;

import com.fabriccommunity.spookytime.doomtree.TreeBuilder.BranchPoint;
import com.fabriccommunity.spookytime.doomtree.TreeBuilder.PositionCollector;

import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DoomTreeHeartBlockEntity extends BlockEntity implements Tickable {

	@FunctionalInterface
	private static interface Job {
		Job apply(DoomTreeHeartBlockEntity heart);
	}

	protected Job job = null;

	public DoomTreeHeartBlockEntity(BlockEntityType<?> entityType) {
		super(entityType);
	}

	public DoomTreeHeartBlockEntity() {
		this(DoomTree.HAUNTED_TREE);
	}

	protected int tickCounter = 100;
	protected long[] logs = null;
	protected long[] branches = null;

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

	private void idle() {
		if (--tickCounter <= 0) {
			Random r = ThreadLocalRandom.current();
			if (placeMiasma(r)) {
				// TODO: bump up
				tickCounter = r.nextInt(20);
			}
		}
	}

	final BlockPos.Mutable mPos = new BlockPos.Mutable();
	private static BlockState MIASMA_STATE = DoomTree.MIASMA_BLOCK.getDefaultState();


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
				final Packet<?> packet = DoomTreePacket.misama(pos);
				PlayerStream.around(world, pos, 32).forEach(p -> 
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(p, packet));
			}
			return true;
		} else {
			return false;
		}
	}

	private static final String LOG_KEY = "logPositions";
	private static final String BRANCH_KEY = "branchPositions";

	public void setTemplate(long[] blocks) {
		this.logs = blocks;
		this.branches = null;
		job = new Builder(this);
		this.markDirty();
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);

		logs = tag.containsKey(LOG_KEY) ? tag.getLongArray(LOG_KEY) : null;
		branches = tag.containsKey(BRANCH_KEY) ? tag.getLongArray(BRANCH_KEY) : null;

		if (logs != null && branches == null) {
			job = new Builder(this);
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

	private static class Builder implements Job {
		final int x;
		final int y;
		final int z;
		final long origin;
		final BlockPos.Mutable pos = new BlockPos.Mutable();
		final PositionCollector blocks = new PositionCollector();
		final ObjectArrayList<BranchPoint> points = new ObjectArrayList<>();
		BranchPoint lastPoint = null;
		final Random rand = new Random();
		final int centerHeight;

		int h = DoomLogBlock.TERMINAL_HEIGHT + 1;

		Builder (DoomTreeHeartBlockEntity heart) {
			final BlockPos pos = heart.pos;
			x = pos.getX();
			y = pos.getY();
			z = pos.getZ();

			origin = pos.asLong();
			points.addElements(0, POINTS, 0, POINT_COUNT);
			rand.setSeed(origin);
			centerHeight = TreeBuilder.centerHeight(pos);

			final PositionCollector blocks = this.blocks;
			for (long p : heart.logs) {
				blocks.add(p);
			}
		}

		@Override
		public Job apply(DoomTreeHeartBlockEntity heart) {
			BranchPoint point = points.get(rand.nextInt(points.size()));

			while (point == lastPoint ) {
				point = points.get(rand.nextInt(points.size()));
			}

			lastPoint = point;
			points.remove(point);

			if (points.isEmpty()) {
				points.addElements(0, POINTS, 0, POINT_COUNT);
			}

			pos.set(x + point.dx, y + h, z + point.dz);

			if (blocks.contains(pos.asLong())) {
				pos.set(x + point.xStart, y + h, z + point.zStart);
				placeBranch(heart.world, blocks, rand, x, z, pos.asLong(), pos, point, 36 - rand.nextInt(8));
			}

			if(++h > centerHeight) {
				for (long p : heart.logs) {
					blocks.remove(p);
				}

				heart.branches = blocks.toLongArray();
				heart.markDirty();
				return new Placer(heart);
			} else {
				return this;
			}
		}
	}

	private static class Placer implements Job {
		final int x;
		final int y;
		final int z;
		final BlockPos.Mutable mPos = new BlockPos.Mutable();
		final BlockState logState = DoomTree.DOOM_LOG.getDefaultState();
		final BlockState channelState = DoomTree.DOOM_LOG_CHANNEL.getDefaultState();
		final BlockState terminalState = DoomTree.DOOM_LOG_TERMINAL.getDefaultState();
		final LongArrayList blocks;
		int index = 0;
		final int limit;
		
		public Placer(DoomTreeHeartBlockEntity heart) {
			final BlockPos pos = heart.pos;
			x = pos.getX();
			y = pos.getY();
			z = pos.getZ();
			blocks = new LongArrayList(heart.logs.length + heart.branches.length);
			blocks.addElements(0, heart.logs, 0, heart.logs.length);
			blocks.addElements(heart.logs.length, heart.branches, 0, heart.branches.length);
			limit = blocks.size();
		}

		@Override
		public Job apply(DoomTreeHeartBlockEntity heart) {
			if (index < limit) {
				final long p = blocks.getLong(index++);
				mPos.set(p);
				
				if (canReplace(heart.world, mPos)) {
					final int dy = mPos.getY() - y;
					BlockState state = null;

					if (mPos.getX() == x && Math.abs(mPos.getZ() - z) == 1 || mPos.getZ() == z && Math.abs(mPos.getX() - x) == 1) {
						if (dy >= 0 && dy < DoomLogBlock.TERMINAL_HEIGHT) {
							state = channelState.with(DoomLogBlock.HEIGHT, MathHelper.clamp(dy, 0, DoomLogBlock.MAX_HEIGHT));
						} else if (dy == DoomLogBlock.TERMINAL_HEIGHT) {
							state = terminalState;
						}
					} 
					
					if (state == null) {
						state = logState.with(DoomLogBlock.HEIGHT, MathHelper.clamp(dy, 0, DoomLogBlock.MAX_HEIGHT));
					}

					heart.world.setBlockState(mPos.set(p), state, 18);
				}
				return this;
			} else {
				return null;
			}
		}

	}
}

