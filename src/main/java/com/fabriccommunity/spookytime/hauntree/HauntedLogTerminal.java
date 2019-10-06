package com.fabriccommunity.spookytime.hauntree;

import java.util.List;
import java.util.function.Function;

import com.fabriccommunity.spookytime.SpookyTime;
import com.fabriccommunity.spookytime.client.model.SpookyModels;
import com.google.common.collect.ImmutableList;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class HauntedLogTerminal extends HauntedLogModel {
	
	public static final List<Identifier> TERMINAL_TEXTURES = ImmutableList.of(
			SpookyTime.id("block/haunted_log_terminal"));
	
	protected final Sprite termimnalSprite;
    
	protected HauntedLogTerminal(Mesh mesh, Sprite sprite, Function<Identifier, Sprite> spriteMap) {
		super(mesh, sprite, spriteMap);
		termimnalSprite = spriteMap.apply(TERMINAL_TEXTURES.get(0));
	}
	
	@Override
	protected int getHeight(BlockState state) {
		return HauntedLogBlock.TERMINAL_HEIGHT;
	}

	@Override
	protected void emitQuads(QuadEmitter qe, long bits, int height) {
		emitFace(qe, Direction.UP, (int) bits, height);
		emitFace(qe, Direction.DOWN, (int) (bits >> 8), height);
		emitTerminalFace(qe, Direction.EAST, (int) (bits >> 16), height);
		emitTerminalFace(qe, Direction.WEST, (int) (bits >> 24), height);
		emitTerminalFace(qe, Direction.NORTH, (int) (bits >> 32), height);
		emitTerminalFace(qe, Direction.SOUTH, (int) (bits >> 48), height);
	}

	@Override
	protected int[] makeGlowColors() {
		return null;
	}
	
	private final int glowColor = interpolateColor(HauntedLogBlock.TERMINAL_HEIGHT, CHANNEL_LOW_COLOR, CHANNEL_HIGH_COLOR);
	
	@Override
	protected int glowColor(int height) {
		return glowColor;
	}
	
	private void emitTerminalFace(QuadEmitter qe, Direction face, int bits, int height) {
		qe.material(innerMaterial)
		.square(face, 0, 0, 1, 1, 0)
		.spriteBake(0, innerSprite, MutableQuadView.BAKE_LOCK_UV + MutableQuadView.BAKE_FLIP_V);
		glow(qe, height);
		SpookyModels.contractUVs(0, innerSprite, qe);
		qe.emit();
		
		qe.material(outerMaterial)
		.square(face, 0, 0, 1, 1, 0)
		.spriteColor(0, -1, -1, -1, -1)
		.spriteBake(0, termimnalSprite, MutableQuadView.BAKE_LOCK_UV);
		SpookyModels.contractUVs(0, termimnalSprite, qe);
		qe.emit();
	}
	
	public static HauntedLogTerminal create(Function<Identifier, Sprite> spriteMap) {
        final Sprite sprite = spriteMap.apply(TERMINAL_TEXTURES.get(0));
        final Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        final RenderMaterial mat = renderer.materialFinder().emissive(0, true).disableAo(0, true).disableDiffuse(0, true).find();
        final MeshBuilder mb = renderer.meshBuilder();
        SpookyModels.box(mb.getEmitter(), mat, -1, sprite, 0, 0, 0, 1, 1, 1);
		return new HauntedLogTerminal(mb.build(), sprite, spriteMap);
	}
}
