package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.ServerDataStore;

public class FavoriteServersScreen extends GuiScreen {

	private GuiScreen parent;
	private boolean delete = false;

	public FavoriteServersScreen(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void onOpen(Player viewer) {
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
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 156, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(new AddFavoriteScreen(FavoriteServersScreen.this));
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.remove")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				Label label = getWidget(5, Label.class);
				if(delete) {
					label.setText(OpenClassic.getGame().getTranslator().translate("gui.favorites.select"));
					delete = false;
				} else {
					label.setText(Color.RED + OpenClassic.getGame().getTranslator().translate("gui.favorites.delete"));
					delete = true;
				}
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 + 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(5, this.getWidth() / 2, 15, this, OpenClassic.getGame().getTranslator().translate("gui.favorites.select"), true));
	
		this.getWidget(1, ButtonList.class).setContents(new ArrayList<String>(ServerDataStore.getFavorites().keySet()));
	}
	
}
