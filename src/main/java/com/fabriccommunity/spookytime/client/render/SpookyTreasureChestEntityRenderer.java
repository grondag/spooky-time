package com.fabriccommunity.spookytime.client.render;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.platform.GlStateManager;

import com.fabriccommunity.spookytime.SpookyTime;
import com.fabriccommunity.spookytime.client.model.TreasureChestModel;
import com.fabriccommunity.spookytime.entity.SpookyTreasureChestEntity;

public class SpookyTreasureChestEntityRenderer extends EntityRenderer<SpookyTreasureChestEntity> {
	private static final Identifier TEXTURE = new Identifier(SpookyTime.MOD_ID, "textures/entity/treasure_chest/default_chest.png");
	private final TreasureChestModel chestModel = new TreasureChestModel();
	
	public SpookyTreasureChestEntityRenderer(EntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public void render(SpookyTreasureChestEntity chest, double x, double y, double z, float partialTicks, float float_2) {
		GlStateManager.pushMatrix();
		
		// initial size and position
		GlStateManager.translatef((float) x - .275f, (float) y + .57f, (float) z + .275f);
		GlStateManager.rotatef(180, 1, 0, 0);
		GlStateManager.scalef(.57f, .57f, .57f);
		
		// calculate interpolated render rotation from last rotation
		double interpolated = chest.previousRotation + (chest.rotation - chest.previousRotation) * partialTicks;
		
		GlStateManager.translatef(0.5F, 0.5F, 0.5F);
		GlStateManager.rotated(interpolated, 0, 1, 0);
		GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
		
		// jiggle after finishing spin
		if (chest.getEndProgress() != 0) {
			GlStateManager.translatef(0.5F, 0.5F, 0.5F);
			GlStateManager.rotatef((float) Math.sin(chest.getEndProgress()), 0, 0, 1);
			GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
		}
		
		
		// render chest
		bindTexture(TEXTURE);
		updateHingeProgress(chest, chestModel);
		chestModel.render();
		
		
		// finish
		GlStateManager.popMatrix();
	}
	
	private void updateHingeProgress(SpookyTreasureChestEntity chest, TreasureChestModel chestModel) {
		// 6.28 (closed) -> 5 (open)
		float percentDistance = (chest.getHingeProgress() / (float) SpookyTreasureChestEntity.MAX_HINGE_PROGRESS) * 1.35f;
		chestModel.getLid().pitch = 6.28f - percentDistance;
	}
	
	@Override
	protected Identifier getTexture(SpookyTreasureChestEntity spookyTreasureChestEntity) {
		return null;
	}
}
