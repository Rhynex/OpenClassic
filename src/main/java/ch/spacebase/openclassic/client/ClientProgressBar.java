package ch.spacebase.openclassic.client;

import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.render.ArrayRenderer;
import ch.spacebase.openclassic.client.util.Projection;

public class ClientProgressBar implements ProgressBar {

	private String title = "";
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
		this.render();
	}

	public void render() {
		if(this.isVisible()) {
			Projection.ortho();
			ClientRenderHelper.getHelper().drawDefaultBG();
			int width = Display.getWidth() * 240 / Display.getHeight();
			int height = Display.getHeight() * 240 / Display.getHeight();
			
			if(this.getProgress() >= 0) {
				int x = width / 2 - 50;
				int y = height / 2 + 16;
				glDisable(GL_TEXTURE_2D);
				ArrayRenderer.begin();
				ArrayRenderer.color(8421504);
				ArrayRenderer.vert(x, y, 0);
				ArrayRenderer.vert(x, (y + 2), 0);
				ArrayRenderer.vert((x + 100), (y + 2), 0);
				ArrayRenderer.vert((x + 100), y, 0);
				ArrayRenderer.color(8454016);
				ArrayRenderer.vert(x, y, 0);
				ArrayRenderer.vert(x, (y + 2), 0);
				ArrayRenderer.vert((x + this.progress), (y + 2), 0);
				ArrayRenderer.vert((x + this.progress), y, 0);
				ArrayRenderer.end();
				glEnable(GL_TEXTURE_2D);
			}
			
			RenderHelper.getHelper().renderText(this.title, (width - RenderHelper.getHelper().getStringWidth(this.title)) / 2, height / 2 - 20, false);
			RenderHelper.getHelper().renderText(this.text, (width - RenderHelper.getHelper().getStringWidth(this.text)) / 2, height / 2 + 4, false);
			Display.update();
		}
	}
	
}
