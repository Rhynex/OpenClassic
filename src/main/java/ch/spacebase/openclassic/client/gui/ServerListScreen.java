package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.Server;

import com.mojang.minecraft.SessionData;

public class ServerListScreen extends GuiScreen {

	private GuiScreen parent;
	private String title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");

	private boolean select = false;

	public ServerListScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this, true));
		this.getWidget(0, ButtonList.class).setContents(SessionData.serverInfo);

		this.attachWidget(new Button(1, this.getWidth() / 2 - 206, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.servers.favorites")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 102, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 2, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.servers.enter-url")));
		this.attachWidget(new Button(4, this.getWidth() / 2 + 106, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
	}

	public final void onButtonClick(Button button) {
		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new FavoriteServersScreen(this));
		}

		if(button.getId() == 2) {
			if(this.select) {
				this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
				this.select = false;
			} else {
				this.title = Color.GREEN + OpenClassic.getGame().getTranslator().translate("gui.servers.select-fav");
				this.select = true;
			}
		}

		if(button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(new ServerURLScreen(this));
		}

		if(button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

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
		
		Server server = SessionData.servers.get(index);
		if(this.select) {
			this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
			this.select = false;
			SessionData.favorites.put(server.name, server.getUrl());
		} else {
			OpenClassic.getClient().joinServer(server.getUrl());
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15);

		super.render();
	}
}
