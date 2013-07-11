package ch.spacebase.openclassic.client.gui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.HeartbeatManager;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.LoginInfo;
import ch.spacebase.openclassic.client.util.Server;
import ch.spacebase.openclassic.client.util.Storage;
import ch.spacebase.openclassic.server.ClassicServer;
import ch.spacebase.openclassic.server.ui.SettingsFrame;

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
		List<String> content = new ArrayList<String>();
		for(String cont : Storage.getServers().keySet()) {
			content.add(cont);
		}
		
		this.getWidget(0, ButtonList.class).setContents(content);

		this.attachWidget(new Button(1, this.getWidth() / 2 - 412, this.getHeight() / 6 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.servers.favorites")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 204, this.getHeight() / 6 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 4, this.getHeight() / 6 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.servers.enter-url")));
		this.attachWidget(new Button(4, this.getWidth() / 2 + 212, this.getHeight() / 6 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
		this.attachWidget(new Button(5, this.getWidth() / 2 - 412, this.getHeight() / 6 + 336, 200, 40, this, OpenClassic.getServer() != null && OpenClassic.getServer().isRunning() ? OpenClassic.getGame().getTranslator().translate("gui.servers.stop-server") : OpenClassic.getGame().getTranslator().translate("gui.servers.start-server")));
		this.attachWidget(new Button(6, this.getWidth() / 2 - 204, this.getHeight() / 6 + 336, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.servers.settings")));
		this.attachWidget(new Button(7, this.getWidth() / 2 + 4, this.getHeight() / 6 + 336, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.servers.console")));
		this.attachWidget(new Button(8, this.getWidth() / 2 + 212, this.getHeight() / 6 + 336, 200, 40, this, OpenClassic.getServer() != null && OpenClassic.getServer().isRunning() && HeartbeatManager.getURL().equals("") ? OpenClassic.getGame().getTranslator().translate("gui.servers.awaiting") : OpenClassic.getGame().getTranslator().translate("gui.servers.connect")));
		if(OpenClassic.getServer() == null || !OpenClassic.getServer().isRunning()) {
			this.getWidget(6, Button.class).setActive(false);
			this.getWidget(7, Button.class).setActive(false);
			this.getWidget(8, Button.class).setActive(false);
		}
		
		if(HeartbeatManager.getURL().equals("")) {
			this.getWidget(8, Button.class).setActive(false);
		}
	}

	public final void onButtonClick(Button button) {
		if (button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new FavoriteServersScreen(this));
		}

		if (button.getId() == 2) {
			if (this.select) {
				this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
				this.select = false;
			} else {
				this.title = Color.GREEN + OpenClassic.getGame().getTranslator().translate("gui.servers.select-fav");
				this.select = true;
			}
		}

		if (button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(new ServerURLScreen(this));
		}
		
		if (button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
		
		if (button.getId() == 5) {
			if(OpenClassic.getServer() != null) {
				OpenClassic.getServer().shutdown();
				this.getWidget(5, Button.class).setText(OpenClassic.getGame().getTranslator().translate("gui.servers.start-server"));
				this.getWidget(6, Button.class).setActive(false);
				this.getWidget(7, Button.class).setActive(false);
				this.getWidget(8, Button.class).setActive(false);
			} else {
				Thread thr = new Thread("Server") {
					public void run() {
						new ClassicServer(new File(OpenClassic.getClient().getDirectory(), "server")).start(new String[] { "embedded" });
					}
				};
				
				thr.setDaemon(true);
				thr.start();
				
				this.getWidget(5, Button.class).setText(OpenClassic.getGame().getTranslator().translate("gui.servers.stop-server"));
				this.getWidget(6, Button.class).setActive(true);
				this.getWidget(7, Button.class).setActive(true);
				this.getWidget(8, Button.class).setActive(false);
				this.getWidget(8, Button.class).setText(OpenClassic.getGame().getTranslator().translate("gui.servers.awaiting"));
			}
		}
		
		if (button.getId() == 6) {
			SettingsFrame frame = new SettingsFrame();
			
			frame.serverName.setText(OpenClassic.getServer().getConfig().getString("info.name"));
			frame.motd.setText(OpenClassic.getServer().getConfig().getString("info.motd"));
			frame.port.setText(String.valueOf(OpenClassic.getServer().getConfig().getInteger("options.port")));
			frame.chckbxShowOnServer.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.public"));
			frame.maxPlayers.setText(String.valueOf(OpenClassic.getServer().getConfig().getInteger("options.max-players")));
			frame.chckbxVerifyUsers.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.online-mode"));
			frame.chckbxUseWhitelist.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.whitelist"));
			frame.chckbxAllowFlying.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.allow-flight"));
			frame.defaultLevel.setText(OpenClassic.getServer().getConfig().getString("options.default-level"));
			frame.chckbxEnabled.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.enabled"));
			frame.chckbxFalling.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.falling"));
			frame.chckbxFlower.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.flower"));
			frame.chckbxMushroom.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.mushroom"));
			frame.chckbxTree.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.trees"));
			frame.chckbxSponge.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.sponge"));
			frame.chckbxLiquid.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.liquid"));
			frame.chckbxGrass.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.grass"));
			frame.changePhysics();
			
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			frame.setLocation((gd.getDisplayMode().getWidth() - frame.getWidth()) / 2, (gd.getDisplayMode().getHeight() - frame.getHeight()) / 2);
			frame.setVisible(true);
		}
		
		if (button.getId() == 7) {
			ConsoleScreen.get().setParent(this);
			OpenClassic.getClient().setCurrentScreen(ConsoleScreen.get());
		}
		
		if(button.getId() == 8) {
			OpenClassic.getClient().getProgressBar().setTitle("Multiplayer");
			OpenClassic.getClient().getProgressBar().setSubTitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
			OpenClassic.getClient().getProgressBar().setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
			OpenClassic.getClient().getProgressBar().setProgress(-1);
			OpenClassic.getClient().getProgressBar().setVisible(true);

			String play = HTTPUtil.fetchUrl(HeartbeatManager.getURL(), "", "http://minecraft.net/classic/list");
			String mppass = HTTPUtil.getParameterOffPage(play, "mppass");
			
			if (mppass.length() > 0) {
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
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.failed"), OpenClassic.getGame().getTranslator().translate("connecting.probably-down")));
				OpenClassic.getClient().getProgressBar().setVisible(false);
			}
		}
	}
	
	public void update() {
		if(!HeartbeatManager.getURL().equals("") && !this.getWidget(8, Button.class).isActive()) {
			this.getWidget(8, Button.class).setActive(true);
			this.getWidget(8, Button.class).setText(OpenClassic.getGame().getTranslator().translate("gui.servers.connect"));
		}
	}

	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		Server server = Storage.getServers().get(button.getText());
		
		if (this.select) {
			this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
			this.select = false;
			
			Storage.getFavorites().put(server.getName(), server.getUrl());
		} else {
			this.joinServer(server);
		}
	}

	private void joinServer(Server server) {
		if(server != null) {
			OpenClassic.getClient().getProgressBar().setTitle("Multiplayer");
			OpenClassic.getClient().getProgressBar().setSubTitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
			OpenClassic.getClient().getProgressBar().setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
			OpenClassic.getClient().getProgressBar().setProgress(-1);
			OpenClassic.getClient().getProgressBar().setVisible(true);

			String play = HTTPUtil.fetchUrl(server.getUrl(), "", "http://minecraft.net/classic/list");
			String mppass = HTTPUtil.getParameterOffPage(play, "mppass");
			
			if (mppass.length() > 0) {
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
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.failed"), OpenClassic.getGame().getTranslator().translate("connecting.probably-down")));
				OpenClassic.getClient().getProgressBar().setVisible(false);
			}
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 30);

		super.render();
	}
}
