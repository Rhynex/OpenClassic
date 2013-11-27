package ch.spacebase.openclassic.client.gui.base;

import ch.spacebase.openclassic.api.block.model.SubTexture;
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
	public void renderBlockPreview(BlockPreview preview) {
		if(preview.getBlock() != null) {
			RenderHelper.getHelper().drawRotatedBlock(preview.getX(), preview.getY(), preview.getBlock(), preview.getScale());
		}
	}

	@Override
	public void renderButton(Button button, int mouseX, int mouseY) {
		RenderHelper.getHelper().glColor(1, 1, 1, 1);
		SubTexture texture = GuiTextures.BUTTON;
		boolean hover = mouseX >= button.getX() && mouseY >= button.getY() && mouseX < button.getX() + button.getWidth() && mouseY < button.getY() + button.getHeight();
		if (!button.isActive()) {
			texture = GuiTextures.BUTTON_INACTIVE;
		} else if (hover) {
			texture = GuiTextures.BUTTON_HOVER;
		}

		SubTexture part1 = new SubTexture(texture.getParent(), texture.getX1(), texture.getY1(), button.getWidth() / 2, button.getHeight() / 2);
		SubTexture part2 = new SubTexture(texture.getParent(), texture.getX1() + 400 - button.getWidth() / 2, texture.getY1(), button.getWidth() / 2, button.getHeight() / 2);
		SubTexture part3 = new SubTexture(texture.getParent(), texture.getX1(), texture.getY1() + 40 - button.getHeight() / 2, button.getWidth() / 2, button.getHeight() / 2);
		SubTexture part4 = new SubTexture(texture.getParent(), texture.getX1() + 400 - button.getWidth() / 2, texture.getY1() + 40 - button.getHeight() / 2, button.getWidth() / 2, button.getHeight() / 2);
		RenderHelper.getHelper().drawSubTex(part1, button.getX(), button.getY(), 1);
		RenderHelper.getHelper().drawSubTex(part2, button.getX() + button.getWidth() / 2, button.getY(), 1);
		RenderHelper.getHelper().drawSubTex(part3, button.getX(), button.getY() + button.getHeight() / 2, 1);
		RenderHelper.getHelper().drawSubTex(part4, button.getX() + button.getWidth() / 2, button.getY() + button.getHeight() / 2, 1);

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
	}

	@Override
	public void renderDefaultBackground(DefaultBackground background) {
		RenderHelper.getHelper().drawDefaultBG(background.getX(), background.getY(), background.getWidth(), background.getHeight());
	}

	@Override
	public void renderFadingBox(FadingBox box) {
		RenderHelper.getHelper().color(box.getX(), box.getY(), box.getX() + box.getWidth(), box.getY() + box.getHeight(), box.getColor(), box.getFadeColor());
	}

	@Override
	public void renderImage(Image image) {
		RenderHelper.getHelper().enableBlend();
		RenderHelper.getHelper().drawSubTex(image.getTexture(), image.getX(), image.getY(), 1);
		RenderHelper.getHelper().disableBlend();
	}

	@Override
	public void renderLabel(Label label) {
		if(label.isScaled()) {
			RenderHelper.getHelper().renderScaledText(label.getText(), label.getX(), label.getY(), false);
		} else {
			RenderHelper.getHelper().renderText(label.getText(), label.getX(), label.getY(), false);
		}
	}

	@Override
	public void renderTextBox(TextBox box) {
		if(!box.isChatBox()) {
			RenderHelper.getHelper().drawBox(box.getX() - 2, box.getY() - 2, box.getX() + box.getWidth() + 2, box.getY() + box.getHeight() + 2, -6250336);
		}
		
		RenderHelper.getHelper().drawBox(box.getX(), box.getY(), box.getX() + box.getWidth(), box.getY() + box.getHeight(), (!box.isChatBox() ? -16777216 : Integer.MIN_VALUE));
		String render = box.getText();
		RenderHelper.getHelper().renderText(render.substring(0, box.getCursorPosition()) + (box.getBlinkState() && box.isFocused() ? "|" : "") + render.substring(box.getCursorPosition(), render.length()), box.getX() + 8, (box.isChatBox() ? box.getY() + 4 : box.getY() + 12), 14737632, false);
	}

	@Override
	public void renderTranslucentBackground(TranslucentBackground background) {
		RenderHelper.getHelper().color(background.getX(), background.getY(), background.getWidth(), background.getHeight(), 1610941696, -1607454624);
	}

}
