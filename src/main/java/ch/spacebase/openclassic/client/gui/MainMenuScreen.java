package ch.spacebase.openclassic.client.gui;

import java.net.URL;
import java.util.List;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.GuiTextures;

public class MainMenuScreen extends GuiScreen {
	
	private TextBox bg = new TextBox(7, 324, this.getHeight() / 4 + 144, 515, 320, this);
	private String title = "Loading post...";
	private String text = "Please wait...";
	
	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, -8, this.getHeight() / 4 + 19, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.singleplayer")));
		this.attachWidget(new Button(1, -8, this.getHeight() / 4 + 67, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.multiplayer")));
		this.attachWidget(new Button(2, -8, this.getHeight() / 4 + 115, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.options")));
		this.attachWidget(new Button(3, -8, this.getHeight() / 4 + 163, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.resource-packs")));
		this.attachWidget(new Button(4, -8, this.getHeight() / 4 + 211, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.language")));
		this.attachWidget(new Button(5, -8, this.getHeight() / 4 + 259, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.about")));
		this.attachWidget(new Button(6, -8, this.getHeight() / 4 + 307, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.main-menu.quit")));
		this.bg.setText("Tumblr Feed");
		new TumblrLoadThread().start();
	}

	public void onButtonClick(Button button) {
		if (button.getId() == 0) {
			OpenClassic.getClient().setCurrentScreen(new LoadLevelScreen(this));
		}

		if (button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new ServerListScreen(this));
		}

		if (button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(new OptionsScreen(this));
		}
		
		if (button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(new ResourcePackScreen(this));
		}
		
		if (button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(new LanguageScreen(this));
		}

		if (button.getId() == 5) {
			OpenClassic.getClient().setCurrentScreen(new AboutScreen(this));
		}

		if (button.getId() == 6) {
			OpenClassic.getClient().shutdown();
		}
	}
	
	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().drawSubTex(GuiTextures.LOGO.getSubTexture(0, GuiTextures.LOGO.getWidth(), GuiTextures.LOGO.getHeight()), 0, 20, 0, 0.75f, 1);
		this.bg.render();
		RenderHelper.getHelper().renderText(this.title, this.bg.getX() + 16, this.bg.getY() + 40, false);
		String str = "";
		int y = this.bg.getY() + 64;
		int space = 0;
		int last = 0;
		for(int pos = 0; pos < this.text.length(); pos++) {
			if(y > this.bg.getY() + 272) break;
			String add = String.valueOf(this.text.charAt(pos));
			if(add.equals(" ")) space = pos;
			if(RenderHelper.getHelper().getStringWidth(str + add) >= 476) {
				String cutoff = str.substring(space - last);
				RenderHelper.getHelper().renderText(str.substring(0, space - last), this.bg.getX() + 32, y, false);
				y += 26;
				str = cutoff + add;
				last = pos;
			} else {
				str += add;
			}
		}
		
		if(str.length() > 0 && y <= this.bg.getY() + 272) {
			RenderHelper.getHelper().renderText(str, this.bg.getX() + 32, y, false);
		}
		
		super.render();
	}
	
	private class TumblrLoadThread extends Thread {
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				URL feedSource = new URL("http://oclassicupdate.tumblr.com/rss");
				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = input.build(new XmlReader(feedSource));
				List<SyndEntry> entries = feed.getEntries();
				title = entries.get(0).getTitle();
				text = entries.get(0).getDescription().getValue().replaceAll("<p>", "").replaceAll("</p>", "");
			} catch (Exception e) {
				title = "Couldn't load the tumblr feed.";
				text = "";
				e.printStackTrace();
			}
		}
	}
	
}
