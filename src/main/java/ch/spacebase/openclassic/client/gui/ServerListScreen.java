package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.Server;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.gui.ErrorScreen;

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
		Server server = SessionData.servers.get(list.getCurrentPage() * 5 + button.getId());

		if(this.select) {
			this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
			this.select = false;
			SessionData.favorites.put(server.name, server.getUrl());
		} else {
			this.joinServer(server);
		}
	}

	private void joinServer(Server server) {
		if(server != null) {
			Minecraft mc = GeneralUtils.getMinecraft();

			OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
			OpenClassic.getClient().getProgressBar().setSubtitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
			OpenClassic.getClient().getProgressBar().setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
			OpenClassic.getClient().getProgressBar().setProgress(-1);
			OpenClassic.getClient().getProgressBar().render();

			String page = HTTPUtil.fetchUrl(server.getUrl(), "", Constants.MINECRAFT_URL_HTTPS + "classic/list/");
			if(mc.data == null) {
				mc.data = new SessionData(HTTPUtil.getParameterOffPage(page, "username"));
			} else {
				mc.data.username = HTTPUtil.getParameterOffPage(page, "username");
			}
			
			mc.data.key = HTTPUtil.getParameterOffPage(page, "mppass");
			mc.data.haspaid = Boolean.valueOf(HTTPUtil.getParameterOffPage(page, "haspaid"));
			mc.server = HTTPUtil.getParameterOffPage(page, "server");
			try {
				mc.port = Integer.parseInt(HTTPUtil.getParameterOffPage(page, "port"));
			} catch(NumberFormatException e) {
				mc.setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.fail-connect"), OpenClassic.getGame().getTranslator().translate("connecting.invalid-page")));
				mc.server = null;
				return;
			}

			mc.initGame();
			mc.setCurrentScreen(null);
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15);

		super.render();
	}
}
