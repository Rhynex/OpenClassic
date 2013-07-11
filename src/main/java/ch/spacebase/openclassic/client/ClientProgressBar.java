package ch.spacebase.openclassic.client;

import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.render.Renderer;
import ch.spacebase.openclassic.client.util.Projection;

public class ClientProgressBar implements ProgressBar {

	private String title = "";
	private String subtitle = "";
	private String text = "";
	private int progress = -1;
	private boolean visible = false;

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String getSubTitle() {
		return this.subtitle;
	}

	@Override
	public void setSubTitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@Override
	public int getProgress() {
		return this.progress;
	}

	@Override
	public void setProgress(int progress) {
		int old = this.progress;
		this.progress = progress;
		if(this.isVisible() && this.progress != old) {
			this.render();
		}
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
		this.render();
	}

	public void render() {
		if(this.isVisible()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glLoadIdentity();
			Projection.ortho();
			ClientRenderHelper.getHelper().drawDefaultBG();
			int width = Display.getWidth();
			int height = Display.getHeight();
			int y = height - 60;
			glDisable(GL_TEXTURE_2D);
			Renderer.get().begin();
			Renderer.get().color(8421504);
			Renderer.get().vertex(0, y, 0);
			Renderer.get().vertex(0, (y + 4), 0);
			Renderer.get().vertex(width, (y + 4), 0);
			Renderer.get().vertex(width, y, 0);
			if(this.getProgress() >= 0) {
				Renderer.get().color(8454016);
				Renderer.get().vertex(0, y, 0);
				Renderer.get().vertex(0, (y + 4), 0);
				Renderer.get().vertex(this.progress * 8.54f, (y + 4), 0);
				Renderer.get().vertex(this.progress * 8.54f, y, 0);
			}
			
			Renderer.get().end();
			glEnable(GL_TEXTURE_2D);
			ClientRenderHelper.getHelper().drawGreenBackground(0, height - 56, width, height - (height - 56));
			RenderHelper.getHelper().drawSubTex(GuiTextures.LOGO.getSubTexture(0, GuiTextures.LOGO.getWidth(), GuiTextures.LOGO.getHeight()), 0, 20, 0, 0.5625f, 1);
			RenderHelper.getHelper().renderScaledText(this.title, width - 20 - ClientRenderHelper.getHelper().getStringWidth(this.subtitle), 30);
			RenderHelper.getHelper().renderScaledText(this.subtitle, width / 2, height / 2 - 32);
			RenderHelper.getHelper().renderText(this.text, width / 2, height - 38);
			Display.update();
		}
	}
	
}
