package ch.spacebase.openclassic.client.gui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.HTTPUtil;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.gui.ErrorScreen;

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
		this.getWidget(0, ButtonList.class).setContents(new ArrayList<String>(SessionData.favorites.keySet()));

		this.attachWidget(new Button(1, this.getWidth() / 2 - 156, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.remove")));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
	}

	public final void onButtonClick(Button button) {
		if (button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new AddFavoriteScreen(this));
		}
		
		if (button.getId() == 2) {
			if (this.delete) {
				this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
				this.delete = false;
			} else {
				this.title = Color.RED + OpenClassic.getGame().getTranslator().translate("gui.favorites.delete");
				this.delete = true;
			}
		}
		
		if (button.getId() == 3) {
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
			this.joinServer(SessionData.favorites.get(button.getText()));
		}
	}
	
	private void joinServer(String url) {
		Minecraft mc = GeneralUtils.getMinecraft();
		
		mc.progressBar.setVisible(true);
		mc.progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
		mc.progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
		mc.progressBar.setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
		mc.progressBar.setProgress(0);
		mc.progressBar.render();
		String play = HTTPUtil.fetchUrl(url, "", Constants.MINECRAFT_URL + "classic/list");
		String mppass = HTTPUtil.getParameterOffPage(play, "mppass");
		
		if (mppass.length() > 0) {
			String user = HTTPUtil.getParameterOffPage(play, "username");
			mc.data = new SessionData(user);
			mc.data.key = mppass;
			
			try {
				mc.data.haspaid = Boolean.valueOf(HTTPUtil.fetchUrl(Constants.MINECRAFT_URL + "haspaid.jsp", "user=" + URLEncoder.encode(user, "UTF-8")));
			} catch(UnsupportedEncodingException e) {
			}
			
			mc.server = HTTPUtil.getParameterOffPage(play, "server");
			try {
				mc.port = Integer.parseInt(HTTPUtil.getParameterOffPage(play, "port"));
			} catch(NumberFormatException e) {
				mc.setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.fail-connect"), OpenClassic.getGame().getTranslator().translate("connecting.invalid-page")));
				mc.server = null;
				mc.progressBar.setVisible(false);
				return;
			}
		} else {
			mc.setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.failed"), OpenClassic.getGame().getTranslator().translate("connecting.check")));
			mc.progressBar.setVisible(false);
			return;
		}
		
		mc.progressBar.setVisible(false);
		mc.initGame();
		OpenClassic.getClient().setCurrentScreen(null);
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15);

		super.render();
	}
}
