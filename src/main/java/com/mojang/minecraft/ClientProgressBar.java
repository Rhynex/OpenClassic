package com.mojang.minecraft;

import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

import com.mojang.minecraft.render.ShapeRenderer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public final class ClientProgressBar implements ProgressBar {

	public String text = "";
	private String title = "";
	private int progress = 0;
	private boolean visible = false;

	@Override
	public String getTitle() {
		return this.title;
	}
	
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getText() {
		return this.text;
	}
	
	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int getProgress() {
		return this.progress;
	}
	
	@Override
	public void setProgress(int progress) {
		this.progress = progress;
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public void render() {
		/*
		  	int x = this.mc.width * 240 / this.mc.height;
			int y = this.mc.height * 240 / this.mc.height;
			
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, x, y, 0, 100, 300);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glTranslatef(0, 0, -200);
		 */
		if(!this.isVisible()) {
			return;
		}
		
		int x = ClientRenderHelper.getHelper().getDisplayWidth() * 240 / ClientRenderHelper.getHelper().getDisplayHeight();
		int y = ClientRenderHelper.getHelper().getDisplayHeight() * 240 / ClientRenderHelper.getHelper().getDisplayHeight();
		ClientRenderHelper.getHelper().drawDefaultBG();
		if (progress >= 0) {
			int barX = x / 2 - 50;
			int barY = y / 2 + 16;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			ShapeRenderer.instance.begin();
			ShapeRenderer.instance.color(8421504);
			ShapeRenderer.instance.vertex(barX, barY, 0.0F);
			ShapeRenderer.instance.vertex(barX, (barY + 2), 0.0F);
			ShapeRenderer.instance.vertex((barX + 100), (barY + 2), 0.0F);
			ShapeRenderer.instance.vertex((barX + 100), barY, 0.0F);
			ShapeRenderer.instance.color(8454016);
			ShapeRenderer.instance.vertex(barX, barY, 0.0F);
			ShapeRenderer.instance.vertex(barX, (barY + 2), 0.0F);
			ShapeRenderer.instance.vertex((barX + progress), (barY + 2), 0.0F);
			ShapeRenderer.instance.vertex((barX + progress), barY, 0.0F);
			ShapeRenderer.instance.end();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		ClientRenderHelper.getHelper().renderText(this.title, x, y / 2 - 4 - 16, 16777215);
		ClientRenderHelper.getHelper().renderText(this.text, x, y / 2 - 4 + 8, 16777215);
		Display.update();
	}
	
}
