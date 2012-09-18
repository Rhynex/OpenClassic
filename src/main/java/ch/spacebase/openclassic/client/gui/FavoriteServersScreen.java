package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;


import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.LoginInfo;
import ch.spacebase.openclassic.client.util.Storage;

public class FavoriteServersScreen extends GuiScreen {

	private GuiScreen parent;
	private String title = "Select a server.";

	private boolean delete = false;

	public FavoriteServersScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.getWidget(0, ButtonList.class).setContents(new ArrayList<String>(Storage.getFavorites().keySet()));

		this.attachWidget(new Button(1, this.getWidth() / 2 - 156, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.remove")));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
	}

	public final void onButtonClick(Button button) {
		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new AddFavoriteScreen(this));
		}
		
		if(button.getId() == 2) {
			if (this.delete) {
				this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
				this.delete = false;
			} else {
				this.title = Color.RED + OpenClassic.getGame().getTranslator().translate("gui.favorites.delete");
				this.delete = true;
			}
		}
		
		if(button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		if (this.delete) {
			OpenClassic.getClient().setCurrentScreen(new ConfirmDeleteServerScreen(this, button.getText()));
			this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
			this.delete = false;
		} else {
			this.joinServer(Storage.getFavorites().get(button.getText()));
		}
	}
	
	private void joinServer(String url) {
		OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
		OpenClassic.getClient().getProgressBar().setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
		OpenClassic.getClient().getProgressBar().setProgress(-1);
		OpenClassic.getClient().getProgressBar().setVisible(true);
		String play = HTTPUtil.fetchUrl(url, "", "http://www.minecraft.net/classic/list");
		String mppass = HTTPUtil.getParameterOffPage(play, "mppass");
		
		if(mppass.length() > 0) {
			String user = HTTPUtil.getParameterOffPage(play, "username");
			LoginInfo.setName(user);
			LoginInfo.setKey(mppass);
			
			OpenClassic.getClient().getProgressBar().setText("Logging in...");
			Multiplayer mode = new Multiplayer(HTTPUtil.getParameterOffPage(play, "server"), Integer.parseInt(HTTPUtil.getParameterOffPage(play, "port")));
			((ClassicClient) OpenClassic.getClient()).setMode(mode);
			if(mode.getSession().isConnected()) {
				OpenClassic.getClient().setCurrentScreen(null);
			}
		} else {
			OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.failed"), OpenClassic.getGame().getTranslator().translate("connecting.check")));
			OpenClassic.getClient().getProgressBar().setVisible(false);
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15);

		super.render();
	}
}
