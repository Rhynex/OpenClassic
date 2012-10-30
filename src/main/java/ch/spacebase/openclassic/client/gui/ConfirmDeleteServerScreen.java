package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.Storage;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ConfirmDeleteServerScreen extends GuiScreen {

	private GuiScreen parent;
	private String name;

	public ConfirmDeleteServerScreen(GuiScreen parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 204, this.getHeight() / 6 + 264, 200, 40, this, "Yes"));
		this.attachWidget(new Button(1, this.getWidth() / 2 + 4, this.getHeight() / 6 + 264, 200, 40, this, "No"));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			Storage.getFavorites().remove(this.name);
			Storage.saveFavorites();
		}
		
		OpenClassic.getClient().setCurrentScreen(this.parent);
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		
		RenderHelper.getHelper().renderText(String.format(OpenClassic.getGame().getTranslator().translate("gui.delete.server"), this.name), this.getWidth() / 2, (this.getHeight() / 2) - 64);
		super.render();
	}
}
