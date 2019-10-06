package com.fabriccommunity.spookytime.client.model;

import java.util.HashMap;

import com.fabriccommunity.spookytime.SpookyTime;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class SpookyModels {
	public static void init() {
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(manager -> ((modelId, context) -> {
			if (modelId.getNamespace().equals(SpookyTime.MOD_ID)) {
				return models.get(modelId.getPath());
			} else {
				return null;
			}
		}));
	}

	private static final HashMap<String, UnbakedModel> models = new HashMap<>();

	public static void register(String id, UnbakedModel unbakedModel) {
		models.put(id, unbakedModel);
	}

	public static void box(
			QuadEmitter qe,
			RenderMaterial material, 
			int color, Sprite sprite,
			float minX, float minY, float minZ, 
			float maxX, float maxY, float maxZ) {

		qe.material(material)
			.square(Direction.UP, minX, minZ, maxX, maxZ, 1-maxY)
			.spriteColor(0, color, color, color, color)
			.spriteUnitSquare(0)
			.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED)
			.emit()
	
			.material(material)
			.square(Direction.DOWN, minX, minZ, maxX, maxZ, minY)
			.spriteColor(0, color, color, color, color)
			.spriteUnitSquare(0)
			.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED)
			.emit()
	
			.material(material)
			.square(Direction.EAST, minZ, minY, maxZ, maxY, 1-maxX)
			.spriteColor(0, color, color, color, color)
			.spriteUnitSquare(0)
			.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED)
			.emit()
	
			.material(material)
			.square(Direction.WEST, minZ, minY, maxZ, maxY, minX)
			.spriteColor(0, color, color, color, color)
			.spriteUnitSquare(0)
			.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED)
			.emit()
	
			.material(material)
			.square(Direction.SOUTH, minX, minY, maxX, maxY, 1-maxZ)
			.spriteColor(0, color, color, color, color)
			.spriteUnitSquare(0)
			.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED)
			.emit()
	
			.material(material)
			.square(Direction.NORTH, minX, minY, maxX, maxY, minZ)
			.spriteColor(0, color, color, color, color)
			.spriteUnitSquare(0)
			.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED)
			.emit();
	}
	
    /**
     * Prevents pinholes or similar artifacts along texture seams by nudging all
     * texture coordinates slightly towards the vertex centroid of the UV
     * coordinates.
     */
    public static void contractUVs(int spriteIndex, Sprite sprite, MutableQuadView poly) {
        final float uPixels = (float) sprite.getWidth() / (sprite.getMaxU() - sprite.getMinU());
        final float vPixels = (float) sprite.getHeight() / (sprite.getMaxV() - sprite.getMinV());
        final float nudge = 4.0f / Math.max(vPixels, uPixels);

        final float u0 = poly.spriteU(0, spriteIndex);
        final float u1 = poly.spriteU(1, spriteIndex);
        final float u2 = poly.spriteU(2, spriteIndex);
        final float u3 = poly.spriteU(3, spriteIndex);

        final float v0 = poly.spriteV(0, spriteIndex);
        final float v1 = poly.spriteV(1, spriteIndex);
        final float v2 = poly.spriteV(2, spriteIndex);
        final float v3 = poly.spriteV(3, spriteIndex);

        final float uCenter = (u0 + u1 + u2 + u3) * 0.25F;
        final float vCenter = (v0 + v1 + v2 + v3) * 0.25F;

        poly.sprite(0, spriteIndex, MathHelper.lerp(nudge, u0, uCenter), MathHelper.lerp(nudge, v0, vCenter));
        poly.sprite(1, spriteIndex, MathHelper.lerp(nudge, u1, uCenter), MathHelper.lerp(nudge, v1, vCenter));
        poly.sprite(2, spriteIndex, MathHelper.lerp(nudge, u2, uCenter), MathHelper.lerp(nudge, v2, vCenter));
        poly.sprite(3, spriteIndex, MathHelper.lerp(nudge, u3, uCenter), MathHelper.lerp(nudge, v3, vCenter));
    }
}
