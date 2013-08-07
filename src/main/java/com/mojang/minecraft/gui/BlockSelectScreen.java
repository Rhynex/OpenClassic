package com.mojang.minecraft.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public final class BlockSelectScreen extends GuiScreen {

	private float yawAngle = 0;
	private float pitchAngle = 0;
	private float localSine = 0;
	private float localSineModifier = 0;
	private boolean fancy;
	
	private Map<Integer, List<ScreenBlock>> blocks = new LinkedHashMap<Integer, List<ScreenBlock>>();
	private int page = 0;

	@Override
	public void onOpen() {
		this.setGrabsInput(false);
		int count = 0;
		for(BlockType block : Blocks.getBlocks()) {
			if(block != null && block.isSelectable()) {
				int page = (int) Math.floor(((float) count) / 36);
				if(!this.blocks.containsKey(page)) {
					this.blocks.put(page, new ArrayList<ScreenBlock>());
				}
				
				int ind = count - (page * 36);
				int blockX = this.getWidth() / 2 + ind % 9 * 24 + -108 - 3;
				int blockY = this.getHeight() / 2 + ind / 9 * 24 + -60 + 3;
				this.blocks.get(page).add(new ScreenBlock(block, blockX, blockY));
				count++;
			}
		}
		
		this.attachWidget(new Button(0, this.getWidth() / 2 - 115, 155, 25, 20, this, "<<"));
		this.attachWidget(new Button(1, this.getWidth() / 2 + 91, 155, 25, 20, this, ">>"));
		
		if(this.blocks.size() == 0) {
			GeneralUtils.getMinecraft().setCurrentScreen(null);
		}
	}

	private ScreenBlock getBlockOnScreen(int x, int y) {
		for(ScreenBlock block : this.blocks.get(this.page)) {
			if(x >= block.x && x <= block.x + 24 && y >= block.y - 12 && y <= block.y + 12) {
				return block;
			}
		}

		return null;
	}

	@Override
	public void render() {
		int mouseX = RenderHelper.getHelper().getRenderMouseX();
		int mouseY = RenderHelper.getHelper().getRenderMouseY();

		ScreenBlock block = this.getBlockOnScreen(mouseX, mouseY);
		RenderHelper.getHelper().color(this.getWidth() / 2 - 120, 30, this.getWidth() / 2 + 120, 180, -1878719232, -1070583712);
		if(block != null) {
			RenderHelper.getHelper().color(block.x - 3, block.y - 8, block.x + 23, block.y + 18, -1862270977, -1056964609);
		}

		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.blocks.select"), this.getWidth() / 2, 40);

		switch(GeneralUtils.getMinecraft().settings.getIntSetting("options.blockChooser").getValue()) {
			case 0: {
				this.yawAngle = -45.0F;
				this.pitchAngle = -30.0F;
				this.fancy = false;
				break;
			}
			case 1: {
				this.yawAngle = -30.0F;
				this.pitchAngle = -20.0F;
				this.fancy = false;
				break;
			}
			case 2: {
				this.pitchAngle = -25.0F;
				this.fancy = true;
				break;
			}
		}

		if(this.fancy) {
			if(this.yawAngle >= 360.0F) {
				this.yawAngle = 0.0F;
			}
			
			if(this.localSineModifier >= 360.0F) {
				this.localSineModifier = 0.0F;
			}
			
			this.yawAngle += 0.6F;
			this.localSineModifier += 6F;

			this.localSine = MathHelper.sin(this.localSineModifier * MathHelper.DEG_TO_RAD);
		}
		
		for(ScreenBlock b : this.blocks.get(this.page)) {
			GL11.glPushMatrix();
			GL11.glTranslatef(b.x, b.y, 0);
			GL11.glScalef(10.0F, 10.0F, 10.0F);
			GL11.glTranslatef(1.0F, 0.5F, 8.0F);
			GL11.glRotatef(this.pitchAngle, 1.0F, 0.0F, 0.0F);
			if(this.fancy && block != null && b.block == block.block) {
				GL11.glTranslatef(0.0F, 0.15F * this.localSine, 0.0F);
			}
			
			GL11.glRotatef(this.yawAngle, 0.0F, 1.0F, 0.0F);
			if(block != null && b.block == block.block) {
				GL11.glScalef(1.55F, 1.55F, 1.55F);
			}

			GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
			GL11.glScalef(-1.0F, -1.0F, -1.0F);
			b.block.getModel().renderAll(-2, 0, 0, 1);
			GL11.glPopMatrix();
		}
		
		super.render();
	}
	
	@Override
	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			if(this.page > 0) {
				this.page--;
			}
		} else if(button.getId() == 1) {
			if(this.page < this.blocks.size() - 1) {
				this.page++;
			}
		}
	}

	@Override
	public void onMouseClick(int x, int y, int button) {
		if(button == 0) {
			ScreenBlock block = this.getBlockOnScreen(x, y);
			if(block != null) {
				GeneralUtils.getMinecraft().player.inventory.replaceSlot(block.block);
				GeneralUtils.getMinecraft().setCurrentScreen(null);
				return;
			}
		}
		
		super.onMouseClick(x, y, button);
	}
}
