package ch.spacebase.openclassic.client.gui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.HTTPUtil;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.gui.ErrorScreen;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ServerURLScreen extends GuiScreen {

	private GuiScreen parent;

	public ServerURLScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.servers.connect")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")));
		this.attachWidget(new TextBox(2, this.getWidth() / 2 - 100, this.getHeight() / 2 - 10, this));
		this.getWidget(2, TextBox.class).setFocus(true);

		this.getWidget(0, Button.class).setActive(false);
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			Minecraft mc = GeneralUtils.getMinecraft();

			mc.progressBar.setVisible(true);
			mc.progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
			mc.progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
			mc.progressBar.setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
			mc.progressBar.setProgress(-1);
			mc.progressBar.render();
			String play = HTTPUtil.fetchUrl(this.getWidget(2, TextBox.class).getText(), "", Constants.MINECRAFT_URL_HTTPS + "classic/list");
			String mppass = HTTPUtil.getParameterOffPage(play, "mppass");

			if(mppass.length() > 0) {
				String user = HTTPUtil.getParameterOffPage(play, "username");
				if(mc.data == null) {
					mc.data = new SessionData(user);
				} else {
					mc.data.username = user;
				}
				
				mc.data.key = mppass;

				try {
					mc.data.haspaid = Boolean.valueOf(HTTPUtil.fetchUrl(Constants.MINECRAFT_URL_HTTPS + "haspaid.jsp", "user=" + URLEncoder.encode(user, "UTF-8")));
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
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.fail-connect"), OpenClassic.getGame().getTranslator().translate("connecting.check")));
				mc.progressBar.setVisible(false);
				return;
			}

			mc.progressBar.setVisible(false);
			mc.initGame();
			OpenClassic.getClient().setCurrentScreen(null);
		}

		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(0, Button.class).setActive(this.getWidget(2, TextBox.class).getText().length() > 0);
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();

		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.add-favorite.enter-url"), this.getWidth() / 2, 40);
		super.render();
	}
}
