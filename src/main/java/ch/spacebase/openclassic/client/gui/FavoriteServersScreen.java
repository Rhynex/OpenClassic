package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import com.mojang.minecraft.SessionData;

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
		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new AddFavoriteScreen(this));
		}

		if(button.getId() == 2) {
			if(this.delete) {
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
		if(this.delete) {
			OpenClassic.getClient().setCurrentScreen(new ConfirmDeleteServerScreen(this, button.getText()));
			this.title = OpenClassic.getGame().getTranslator().translate("gui.favorites.select");
			this.delete = false;
		} else {
			OpenClassic.getClient().joinServer(SessionData.favorites.get(button.getText()));
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15);

		super.render();
	}
}
