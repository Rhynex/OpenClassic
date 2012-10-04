package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.LoginInfo;


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
			OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("connecting.connect"));
			OpenClassic.getClient().getProgressBar().setText(OpenClassic.getGame().getTranslator().translate("connecting.getting-info"));
			OpenClassic.getClient().getProgressBar().setProgress(-1);
			OpenClassic.getClient().getProgressBar().setVisible(true);
			String play = HTTPUtil.fetchUrl(this.getWidget(2, TextBox.class).getText(), "", "http://minecraft.net/classic/list");
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
				OpenClassic.getClient().getProgressBar().setVisible(false);
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("connecting.failed"), OpenClassic.getGame().getTranslator().translate("connecting.check")));
			}
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
