package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.client.util.Server;
import ch.spacebase.openclassic.client.util.ServerDataStore;

public class ServerListScreen extends GuiScreen {

	private GuiScreen parent;
	private boolean select = false;

	public ServerListScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		List<String> contents = new ArrayList<String>();
		for(Server server : ServerDataStore.getServers()) {
			contents.add(server.name);
		}
		
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		ButtonList list = new ButtonList(1, this, true);
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				String text = button.getText();
				int index = 0;
				for(String t : list.getContents()) {
					if(text.equals(t)) {
						break;
					}
					
					index++;
				}
				
				Server server = ServerDataStore.getServers().get(index);
				if(select) {
					getWidget(6, Label.class).setText(OpenClassic.getGame().getTranslator().translate("gui.favorites.select"));
					select = false;
					ServerDataStore.addFavorite(server.name, server.getUrl());
					ServerDataStore.saveFavorites();
				} else {
					OpenClassic.getClient().joinServer(server.getUrl());
				}
			}
		});
		
		this.attachWidget(list);
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 206, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.servers.favorites")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(new FavoriteServersScreen(ServerListScreen.this));
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 102, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				Label label = getWidget(6, Label.class);
				if(select) {
					label.setText(OpenClassic.getGame().getTranslator().translate("gui.favorites.select"));
					select = false;
				} else {
					label.setText(Color.GREEN + OpenClassic.getGame().getTranslator().translate("gui.servers.select-fav"));
					select = true;
				}
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 + 2, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.servers.enter-url")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(new ServerURLScreen(ServerListScreen.this));
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(5, this.getWidth() / 2 + 106, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(6, this.getWidth() / 2, 15, this, OpenClassic.getGame().getTranslator().translate("gui.favorites.select"), true));
		this.getWidget(1, ButtonList.class).setContents(contents);
	}

}
