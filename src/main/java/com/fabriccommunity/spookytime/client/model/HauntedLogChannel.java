package com.fabriccommunity.spookytime.client.model;

import java.util.List;
import java.util.function.Function;

import com.fabriccommunity.spookytime.SpookyTime;
import com.google.common.collect.ImmutableList;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class HauntedLogChannel extends HauntedLogModel {
	
	public static final List<Identifier> CHANNEL_TEXTURES = ImmutableList.of(
			SpookyTime.id("block/haunted_log_channel_0_0"),
			SpookyTime.id("block/haunted_log_channel_0_1"),
			SpookyTime.id("block/haunted_log_channel_0_2"),
			SpookyTime.id("block/haunted_log_channel_0_3"));
	
	protected final Sprite[] channelSprite = new Sprite[4];
    
	protected HauntedLogChannel(Mesh mesh, Sprite sprite, Function<Identifier, Sprite> spriteMap) {
		super(mesh, sprite, spriteMap);
		for (int i = 0; i < 4; i++) {
			channelSprite[i] = spriteMap.apply(CHANNEL_TEXTURES.get(i));
		}
	}

	@Override
	protected void emitQuads(QuadEmitter qe, long bits, int height) {
		emitFace(qe, Direction.UP, (int) bits, height);
		emitFace(qe, Direction.DOWN, (int) (bits >> 8), height);
		emitChannelFace(qe, Direction.EAST, (int) (bits >> 16), height);
		emitChannelFace(qe, Direction.WEST, (int) (bits >> 24), height);
		emitChannelFace(qe, Direction.NORTH, (int) (bits >> 32), height);
		emitChannelFace(qe, Direction.SOUTH, (int) (bits >> 48), height);
	}
	
	@Override
	protected int[] makeGlowColors() {
		return makeGlowColors(CHANNEL_LOW_COLOR, CHANNEL_HIGH_COLOR);
	}

	private void emitChannelFace(QuadEmitter qe, Direction face, int bits, int height) {
		qe.material(innerMaterial)
		.square(face, 0, 0, 1, 1, 0)
		.spriteBake(0, innerSprite, MutableQuadView.BAKE_LOCK_UV + MutableQuadView.BAKE_FLIP_V);
		glow(qe, height);
		SpookyModels.contractUVs(0, innerSprite, qe);
		qe.emit();
		
		final int logTexture = (bits >> 2) & 3;
		qe.material(outerMaterial)
		.square(face, 0, 0, 1, 1, 0)
		.spriteColor(0, -1, -1, -1, -1)
		.spriteBake(0, channelSprite[logTexture], MutableQuadView.BAKE_LOCK_UV);
		SpookyModels.contractUVs(0, channelSprite[logTexture], qe);
		qe.emit();
	}
	
	static HauntedLogChannel create(Function<Identifier, Sprite> spriteMap) {
        final Sprite sprite = spriteMap.apply(SpookyTime.id("block/haunted_log_core_0_0"));
        final Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        final RenderMaterial mat = renderer.materialFinder().emissive(0, true).disableAo(0, true).disableDiffuse(0, true).find();
        final MeshBuilder mb = renderer.meshBuilder();
        SpookyModels.box(mb.getEmitter(), mat, -1, sprite, 0, 0, 0, 1, 1, 1);
		return new HauntedLogChannel(mb.build(), sprite, spriteMap);
	}
}
