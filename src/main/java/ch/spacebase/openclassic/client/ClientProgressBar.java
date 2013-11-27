package ch.spacebase.openclassic.client;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.client.render.GuiTextures;
import ch.spacebase.openclassic.client.render.RenderHelper;
import ch.spacebase.openclassic.client.render.Renderer;

public class ClientProgressBar implements ProgressBar {

	private String text = "";
	private String subtitle = "";
	private String title = "";
	private int progress = 0;
	private boolean visible = false;
	private boolean scaled = true;

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
			int width = Display.getWidth();
			int height = Display.getHeight();
			if(fresh) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				RenderHelper.getHelper().ortho();
			}

			RenderHelper.getHelper().drawDefaultBG();
			this.renderBar(false);
			RenderHelper.getHelper().drawBlackBG(0, height - 56, width, height - (height - 56));
			GL11.glEnable(GL11.GL_BLEND);
			RenderHelper.getHelper().drawSubTex(GuiTextures.LOGO.getSubTexture(0), 20, 20, 0, 0.5625f, 1);
			GL11.glDisable(GL11.GL_BLEND);
			RenderHelper.getHelper().renderScaledText(this.title, width - 20 - RenderHelper.getHelper().getStringWidth(this.title), 20);
			if(this.isSubtitleScaled()) {
				RenderHelper.getHelper().renderScaledText(this.subtitle, width / 2, height / 2 - 64);
			} else {
				RenderHelper.getHelper().renderText(this.subtitle, width / 2, height / 2 - 64);
			}
			RenderHelper.getHelper().renderText(this.text, width / 2, height - 38);
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
		int width = Display.getWidth();
		int height = Display.getHeight();
		int y = height - 60;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Renderer.get().begin();
		Renderer.get().color(8421504);
		Renderer.get().vertex(0, y, 0);
		Renderer.get().vertex(0, y + 4, 0);
		Renderer.get().vertex(width, y + 4, 0);
		Renderer.get().vertex(width, y, 0);
		if(this.getProgress() >= 0) {
			Renderer.get().color(8454016);
			Renderer.get().vertex(0, y, 0);
			Renderer.get().vertex(0, y + 4, 0);
			Renderer.get().vertex(this.progress * 8.54f, y + 4, 0);
			Renderer.get().vertex(this.progress * 8.54f, y, 0);
		}

		Renderer.get().end();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		if(fresh) {
			Display.update();
		}
	}

	@Override
	public boolean isSubtitleScaled() {
		return this.scaled;
	}

	@Override
	public void setSubtitleScaled(boolean scaled) {
		this.scaled = scaled;
	}

}
