package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.client.render.GuiTextures;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientButton extends Button {

	public ClientButton(int id, int x, int y, Screen parent, String text) {
		super(id, x, y, parent, text);
	}
	
	public ClientButton(int id, int x, int y, int width, int height, Screen parent, String text) {
		super(id, x, y, width, height, parent, text);
	}

	@Override
	public void render() {
		renderButton(this);
	}
	
	public static void renderButton(Button button) {
		RenderHelper.getHelper().glColor(1, 1, 1, 1);
		
		int mouseX = RenderHelper.getHelper().getScaledMouseX();
		int mouseY = RenderHelper.getHelper().getScaledMouseY();
		
		SubTexture texture = GuiTextures.BUTTON;
		boolean hover = mouseX >= button.getX() && mouseY >= button.getY() && mouseX < button.getX() + button.getWidth() && mouseY < button.getY() + button.getHeight();
		if (!button.isActive()) {
			texture = GuiTextures.BUTTON_INACTIVE;
		} else if (hover) {
			texture = GuiTextures.BUTTON_HOVER;
		}

		SubTexture part1 = new SubTexture(texture.getParent(), texture.getX1(), texture.getY1(), button.getWidth() / 2, button.getHeight() / 2);
		SubTexture part2 = new SubTexture(texture.getParent(), texture.getX1() + 200 - button.getWidth() / 2, texture.getY1(), button.getWidth() / 2, button.getHeight() / 2);
		SubTexture part3 = new SubTexture(texture.getParent(), texture.getX1(), texture.getY1() + 20 - button.getHeight() / 2, button.getWidth() / 2, button.getHeight() / 2);
		SubTexture part4 = new SubTexture(texture.getParent(), texture.getX1() + 200 - button.getWidth() / 2, texture.getY1() + 20 - button.getHeight() / 2, button.getWidth() / 2, button.getHeight() / 2);
		RenderHelper.getHelper().drawSubTex(part1, button.getX(), button.getY(), 1);
		RenderHelper.getHelper().drawSubTex(part2, button.getX() + button.getWidth() / 2, button.getY(), 1);
		RenderHelper.getHelper().drawSubTex(part3, button.getX(), button.getY() + button.getHeight() / 2, 1);
		RenderHelper.getHelper().drawSubTex(part4, button.getX() + button.getWidth() / 2, button.getY() + button.getHeight() / 2, 1);

		String message = button.getText();
		if(message.length() > 30) {
			message = message.substring(0, 30) + "...";
		}
		
		if (!button.isActive()) {
			RenderHelper.getHelper().renderText(message, button.getX() + button.getWidth() / 2, button.getY() + (button.getHeight() - 8) / 2, -6250336);
		} else if (hover) {
			RenderHelper.getHelper().renderText(message, button.getX() + button.getWidth() / 2, button.getY() + (button.getHeight() - 8) / 2, 16777120);
		} else {
			RenderHelper.getHelper().renderText(message, button.getX() + button.getWidth() / 2, button.getY() + (button.getHeight() - 8) / 2, 14737632);
		}
	}
	
}
