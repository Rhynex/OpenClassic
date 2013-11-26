package ch.spacebase.openclassic.client.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.Server;
import ch.spacebase.openclassic.client.util.ServerDataStore;
import ch.spacebase.openclassic.client.util.cookie.Cookie;
import ch.spacebase.openclassic.client.util.cookie.CookieList;
import ch.spacebase.openclassic.game.util.InternalConstants;

public class LoginScreen extends GuiScreen {

	@Override
	public void onOpen(Player viewer) {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.login.login")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				String user = getWidget(3, TextBox.class).getText();
				String pass = getWidget(4, TextBox.class).getText();

				if(getWidget(2, StateButton.class).getState().equals(OpenClassic.getGame().getTranslator().translate("gui.yes"))) {
					BufferedWriter writer = null;
					try {
						writer = new BufferedWriter(new FileWriter(getLoginFile(true)));
						writer.write(user);
						writer.newLine();
						writer.write(pass);
					} catch(IOException e) {
						OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("gui.login.fail-create"));
						e.printStackTrace();
					} finally {
						IOUtils.closeQuietly(writer);
					}
				} else if(getLoginFile(false).exists()) {
					getLoginFile(false).delete();
				}

				OpenClassic.getClient().getProgressBar().setVisible(true);
				OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.loading"));
				OpenClassic.getClient().getProgressBar().setSubtitle(OpenClassic.getGame().getTranslator().translate("gui.login.logging-in"));
				OpenClassic.getClient().getProgressBar().setProgress(-1);
				OpenClassic.getClient().getProgressBar().render();
				if(!auth(user, pass)) {
					getWidget(6, Label.class).setText(Color.RED + OpenClassic.getGame().getTranslator().translate("gui.login.failed"));
					OpenClassic.getClient().getProgressBar().setVisible(false);
					return;
				}

				OpenClassic.getClient().getProgressBar().setVisible(false);
				OpenClassic.getClient().setCurrentScreen(new MainMenuScreen());
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newStateButton(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.login.remember")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				((StateButton) button).setState(((StateButton) button).getState() == OpenClassic.getGame().getTranslator().translate("gui.yes") ? OpenClassic.getGame().getTranslator().translate("gui.no") : OpenClassic.getGame().getTranslator().translate("gui.yes"));
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newTextBox(3, this.getWidth() / 2 - 100, this.getHeight() / 2 - 10, this, 64));
		this.attachWidget(WidgetFactory.getFactory().newPasswordTextBox(4, this.getWidth() / 2 - 100, this.getHeight() / 2 + 16, this, 64));
		this.attachWidget(WidgetFactory.getFactory().newButton(5, this.getWidth() / 2 + 2, this.getHeight() / 4 + 120, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.login.play-offline")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(new MainMenuScreen());
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(6, this.getWidth() / 2, 40, this, OpenClassic.getGame().getTranslator().translate("gui.login.enter"), true));
		this.attachWidget(WidgetFactory.getFactory().newLabel(7, this.getWidth() / 2 - 152, this.getHeight() / 2 - 6, this, OpenClassic.getGame().getTranslator().translate("gui.login.user"), true));
		this.attachWidget(WidgetFactory.getFactory().newLabel(8, this.getWidth() / 2 - 152, this.getHeight() / 2 + 20, this, OpenClassic.getGame().getTranslator().translate("gui.login.pass"), true));
		
		this.getWidget(2, StateButton.class).setState(this.getLoginFile(false).exists() ? OpenClassic.getGame().getTranslator().translate("gui.yes") : OpenClassic.getGame().getTranslator().translate("gui.no"));
		this.getWidget(3, TextBox.class).setFocus(true);

		if(this.getLoginFile(false).exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(this.getLoginFile(true)));
				String line = "";
				while((line = reader.readLine()) != null) {
					if(this.getWidget(3, TextBox.class).getText().equals("")) {
						this.getWidget(3, TextBox.class).setText(line);
					} else if(this.getWidget(4, TextBox.class).getText().equals("")) {
						this.getWidget(4, TextBox.class).setText(line);
						break;
					}
				}
			} catch(IOException e) {
				OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("gui.login.fail-check"));
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(reader);
			}
		} else {
			this.getWidget(1, Button.class).setActive(false);
		}
	}

	@Override
	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(1, Button.class).setActive(this.getWidget(3, TextBox.class).getText().length() > 0 && this.getWidget(4, TextBox.class).getText().length() > 0);
	}

	private File getLoginFile(boolean create) {
		File file = new File(OpenClassic.getClient().getDirectory(), ".login");
		if(!file.exists() && create) {
			try {
				file.createNewFile();
			} catch(IOException e) {
				OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("gui.login.fail-create"));
				e.printStackTrace();
			}
		}

		return file;
	}

	public static boolean auth(String username, String password) {
		CookieList cookies = new CookieList();
		CookieHandler.setDefault(cookies);
		String result = "";

		HTTPUtil.fetchUrl(InternalConstants.MINECRAFT_URL_HTTPS + "login", "", InternalConstants.MINECRAFT_URL_HTTPS);

		try {
			result = HTTPUtil.fetchUrl(InternalConstants.MINECRAFT_URL_HTTPS + "login", "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"), InternalConstants.MINECRAFT_URL_HTTPS);
		} catch(UnsupportedEncodingException e) {
			OpenClassic.getLogger().severe("UTF-8 not supported!");
			return false;
		}

		Cookie cookie = cookies.getCookie(InternalConstants.MINECRAFT_URL_HTTPS, "PLAY_SESSION");
		if(cookie != null) result = HTTPUtil.fetchUrl(InternalConstants.MINECRAFT_URL_HTTPS, "", InternalConstants.MINECRAFT_URL_HTTPS + "login");

		if(result.contains("Logged in as")) {
			((ClientPlayer) OpenClassic.getClient().getPlayer()).setName(result.substring(result.indexOf("Logged in as ") + 13, result.indexOf(" | ")));
			parseServers(HTTPUtil.rawFetchUrl(InternalConstants.MINECRAFT_URL_HTTPS + "classic/list", "", InternalConstants.MINECRAFT_URL_HTTPS));
			return true;
		}

		OpenClassic.getLogger().severe(result);
		return false;
	}

	private static void parseServers(String data) {
		int index = data.indexOf("<a href=\"");
		while(index != -1) {
			index = data.indexOf("classic/play/", index);
			if(index == -1) {
				break;
			}

			String id = data.substring(index + 13, data.indexOf("\"", index));
			index = data.indexOf(">", index) + 1;
			String name = data.substring(index, data.indexOf("</a>", index)).replaceAll("&amp;", "&").replaceAll("&hellip;", "...");
			index = data.indexOf("<td>", index) + 4;
			String users = data.substring(index, data.indexOf("</td>", index));
			index = data.indexOf("<td>", index) + 4;
			String max = data.substring(index, data.indexOf("</td>", index));

			Server s = new Server(name, Integer.valueOf(users).intValue(), Integer.valueOf(max).intValue(), id);
			ServerDataStore.addServer(s);
		}
	}

	public static String toHex(byte[] data) {
		StringBuffer buffer = new StringBuffer(data.length * 2);

		for(int index = 0; index < data.length; index++) {
			int hex = data[index] & 0xFF;
			if(hex < 16) {
				buffer.append("0");
			}

			buffer.append(Long.toString(hex, 16));
		}

		return buffer.toString();
	}

}
