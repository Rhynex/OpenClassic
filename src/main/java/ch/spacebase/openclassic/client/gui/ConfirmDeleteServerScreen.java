package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.client.util.ServerDataStore;

public class ConfirmDeleteServerScreen extends GuiScreen {

	private GuiScreen parent;
	private String name;

	public ConfirmDeleteServerScreen(GuiScreen parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 102, this.getHeight() / 6 + 132, 100, 20, this, "Yes"));
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 + 2, this.getHeight() / 6 + 132, 100, 20, this, "No"));
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, (this.getHeight() / 2) - 32, this, String.format(OpenClassic.getGame().getTranslator().translate("gui.delete.server"), this.name), true));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 1) {
			ServerDataStore.removeFavorite(this.name);
			ServerDataStore.saveFavorites();
		}
		
		OpenClassic.getClient().setCurrentScreen(this.parent);
	}
	
}
