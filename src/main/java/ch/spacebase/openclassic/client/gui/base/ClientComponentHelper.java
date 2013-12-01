package ch.spacebase.openclassic.client.gui.base;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.gui.base.BlockPreview;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.DefaultBackground;
import ch.spacebase.openclassic.api.gui.base.FadingBox;
import ch.spacebase.openclassic.api.gui.base.Image;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.api.gui.base.TextBox;
import ch.spacebase.openclassic.api.gui.base.TranslucentBackground;
import ch.spacebase.openclassic.api.gui.base.ComponentHelper;
import ch.spacebase.openclassic.client.render.GuiTextures;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientComponentHelper extends ComponentHelper {

	@Override
	public int getStringWidth(String string, boolean scaled) {
		int width = (int) RenderHelper.getHelper().getStringWidth(string);
		if(scaled) {
			width *= 2;
		}
		
		return width;
	}

	@Override
	public void renderBlockPreview(BlockPreview preview, int popTime) {
		if(preview.getBlock() != null) {
			RenderHelper.getHelper().drawRotatedBlock(preview.getX(), preview.getY(), preview.getBlock(), preview.getScale(), popTime);
		}
	}

	@Override
	public void renderButton(Button button, int mouseX, int mouseY) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		Texture texture = GuiTextures.BUTTON;
		boolean hover = mouseX >= 0 && mouseY >= 0 && mouseX < button.getWidth() && mouseY < button.getHeight();
		if (!button.isActive()) {
			texture = GuiTextures.BUTTON_INACTIVE;
		} else if (hover) {
			texture = GuiTextures.BUTTON_HOVER;
		}

		RenderHelper.getHelper().drawStretchedTexture(texture, button.getX(), button.getY(), button.getWidth(), button.getHeight());
		String message = button.getText();
		if(message.length() > 30) {
			message = message.substring(0, 30) + "...";
		}
		
		if (!button.isActive()) {
			RenderHelper.getHelper().renderText(message, button.getX() + button.getWidth() / 2, button.getY() + (button.getHeight() - 16) / 2, -6250336);
		} else if (hover) {
			RenderHelper.getHelper().renderText(message, button.getX() + button.getWidth() / 2, button.getY() + (button.getHeight() - 16) / 2, 16777120);
		} else {
			RenderHelper.getHelper().renderText(message, button.getX() + button.getWidth() / 2, button.getY() + (button.getHeight() - 16) / 2, 14737632);
		}
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void renderDefaultBackground(DefaultBackground background) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.getHelper().drawDefaultBG(background.getX(), background.getY(), background.getWidth(), background.getHeight());
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void renderFadingBox(FadingBox box) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.getHelper().color(box.getX(), box.getY(), box.getX() + box.getWidth(), box.getY() + box.getHeight(), box.getColor(), box.getFadeColor());
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void renderImage(Image image) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.getHelper().drawStretchedTexture(image.getTexture(), image.getX(), image.getY(), image.getWidth(), image.getHeight());
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void renderLabel(Label label) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		if(label.isScaled()) {
			RenderHelper.getHelper().renderScaledText(label.getText(), label.getX(), label.getY(), false);
		} else {
			RenderHelper.getHelper().renderText(label.getText(), label.getX(), label.getY(), false);
		}
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void renderTextBox(TextBox box) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		if(!box.isChatBox()) {
			RenderHelper.getHelper().drawBox(box.getX() - 2, box.getY() - 2, box.getX() + box.getWidth() + 2, box.getY() + box.getHeight() + 2, -6250336);
		}
		
		RenderHelper.getHelper().drawBox(box.getX(), box.getY(), box.getX() + box.getWidth(), box.getY() + box.getHeight(), (!box.isChatBox() ? -16777216 : Integer.MIN_VALUE));
		String render = box.getText();
		RenderHelper.getHelper().renderText(render.substring(0, box.getCursorPosition()) + (box.getBlinkState() && box.isFocused() ? "|" : "") + render.substring(box.getCursorPosition(), render.length()), box.getX() + 8, (box.isChatBox() ? box.getY() + 4 : box.getY() + 12), 14737632, false);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void renderTranslucentBackground(TranslucentBackground background) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.getHelper().color(background.getX(), background.getY(), background.getWidth(), background.getHeight(), 1610941696, -1607454624);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

}
