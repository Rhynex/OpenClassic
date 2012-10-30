package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class AboutScreen extends GuiScreen {

	private GuiScreen parent;

	public AboutScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 200, this.getHeight() / 6 + 264, this, "Back to Menu"));
	}

	public void onButtonClick(Button button) {
		if (button.getId() == 0) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().drawRotatedBlock(this.getWidth() / 2 - 10, (this.getHeight() / 2) - 112, VanillaBlock.GRASS, 2);
		
		RenderHelper.getHelper().renderText("OpenClassic Version " + Constants.CLIENT_VERSION, this.getWidth() / 2, (this.getHeight() / 2) - 42);
		RenderHelper.getHelper().renderText("Created By Steveice10 (Steveice10@gmail.com)", this.getWidth() / 2, (this.getHeight() / 2) - 20);
		super.render();
	}
	
}
