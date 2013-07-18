package ch.spacebase.openclassic.client;

import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

import com.mojang.minecraft.render.ShapeRenderer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public final class ClientProgressBar implements ProgressBar {

	private String text = "";
	private String subtitle = "";
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
	public String getSubtitle() {
		return this.subtitle;
	}
	
	@Override
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
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
		if(!this.visible) {
			this.progress = -1;
			this.title = "";
			this.subtitle = "";
			this.text = "";
		}
	}
	
	@Override
	public void render() {
		this.render(true);
	}
	
	public void render(boolean fresh) {
		if(!Thread.currentThread().getName().equalsIgnoreCase("Client-Main")) {
			return;
		}
		
		if(this.isVisible()) {
			int x = ClientRenderHelper.getHelper().getGuiWidth();
			int sy = ClientRenderHelper.getHelper().getGuiHeight();
			if(fresh) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glOrtho(0, x, sy, 0, 100, 300);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				GL11.glTranslatef(0, 0, -200);
			}
			
			ClientRenderHelper.getHelper().drawDefaultBG();
			int width = RenderHelper.getHelper().getGuiWidth();
			int height = RenderHelper.getHelper().getGuiHeight();
			this.renderBar(false);
			ClientRenderHelper.getHelper().drawBlackBG(0, height - 28, width, height - (height - 28));
			GL11.glEnable(GL11.GL_BLEND);
			RenderHelper.getHelper().drawSubTex(GuiTextures.LOGO.getSubTexture(0), 10, 10, 0, 0.5625f, 1);
			GL11.glDisable(GL11.GL_BLEND);
			RenderHelper.getHelper().renderScaledText(this.title, width - 10 - ClientRenderHelper.getHelper().getStringWidth(this.title), 10);
			RenderHelper.getHelper().renderScaledText(this.subtitle, width / 2, height / 2 - 32);
			RenderHelper.getHelper().renderText(this.text, width / 2, height - 19);
			if(fresh) {
				Display.update();
			}
		}
	}
	
	@Override
	public void renderBar() {
		this.renderBar(true);
	}
	
	public void renderBar(boolean fresh) {
		int width = RenderHelper.getHelper().getGuiWidth();
		int height = RenderHelper.getHelper().getGuiHeight();
		int y = height - 30;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		ShapeRenderer.instance.begin();
		ShapeRenderer.instance.color(8421504);
		ShapeRenderer.instance.vertex(0, y, 0);
		ShapeRenderer.instance.vertex(0, y + 2, 0);
		ShapeRenderer.instance.vertex(width, y + 2, 0);
		ShapeRenderer.instance.vertex(width, y, 0);
		if(this.getProgress() >= 0) {
			ShapeRenderer.instance.color(8454016);
			ShapeRenderer.instance.vertex(0, y, 0);
			ShapeRenderer.instance.vertex(0, y + 2, 0);
			ShapeRenderer.instance.vertex(this.progress * 4.27f, y + 2, 0);
			ShapeRenderer.instance.vertex(this.progress * 4.27f, y, 0);
		}

		ShapeRenderer.instance.end();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		if(fresh) {
			Display.update();
		}
	}
	
}
