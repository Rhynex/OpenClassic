package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.client.util.ServerDataStore;

public class FavoriteServersScreen extends GuiScreen {

	private GuiScreen parent;
	private boolean delete = false;

	public FavoriteServersScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		ButtonList list = new ButtonList(1, this);
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				if(delete) {
					OpenClassic.getClient().setCurrentScreen(new ConfirmDeleteServerScreen(FavoriteServersScreen.this, button.getText()));
					getWidget(5, Label.class).setText(OpenClassic.getGame().getTranslator().translate("gui.favorites.select"));
					delete = false;
				} else {
					OpenClassic.getClient().joinServer(ServerDataStore.getFavorites().get(button.getText()));
				}
			}
		});
		
		this.attachWidget(list);
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 156, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")));
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.remove")));
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 + 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
		this.attachWidget(WidgetFactory.getFactory().newLabel(5, this.getWidth() / 2, 15, this, OpenClassic.getGame().getTranslator().translate("gui.favorites.select"), true));
	
		this.getWidget(1, ButtonList.class).setContents(new ArrayList<String>(ServerDataStore.getFavorites().keySet()));
	}

	public final void onButtonClick(Button button) {
		if(button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(new AddFavoriteScreen(this));
		}

		if(button.getId() == 3) {
			Label label = this.getWidget(5, Label.class);
			if(this.delete) {
				label.setText(OpenClassic.getGame().getTranslator().translate("gui.favorites.select"));
				this.delete = false;
			} else {
				label.setText(Color.RED + OpenClassic.getGame().getTranslator().translate("gui.favorites.delete"));
				this.delete = true;
			}
		}

		if(button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}
	
}
