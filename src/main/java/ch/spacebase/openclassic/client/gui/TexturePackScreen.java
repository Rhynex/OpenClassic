package ch.spacebase.openclassic.client.gui;

import java.io.File;
import java.util.Arrays;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class TexturePackScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] textures = null;

	public TexturePackScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 75, this.getHeight() / 6 + 156, 150, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));

		StringBuilder textures = new StringBuilder("Default");
		for(String file : (new File(OpenClassic.getClient().getDirectory(), "texturepacks").list())) {
			if(!file.endsWith(".zip")) continue;
			textures.append(";").append(file.substring(0, file.indexOf(".")));
		}

		this.textures = textures.toString().split(";");
		this.getWidget(0, ButtonList.class).setContents(Arrays.asList(this.textures));
	}

	public final void onButtonClick(Button button) {
		if(button.isActive()) {
			if(button.getId() == 1) {
				OpenClassic.getClient().setCurrentScreen(this.parent);
			}
		}
	}

	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		if(button.isActive()) {
			if(button.getText().equals("Default")) {
				OpenClassic.getClient().getConfig().setValue("options.texture-pack", "none");
			} else {
				OpenClassic.getClient().getConfig().setValue("options.texture-pack", button.getText() + ".zip");
			}

			OpenClassic.getClient().getConfig().save();
			GeneralUtils.getMinecraft().textureManager.clear();
		}
	}

	public void render() {
		String pack = OpenClassic.getClient().getConfig().getString("options.texture-pack");
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.texture-packs.select"), this.getWidth() / 2, 15, 16777215);
		RenderHelper.getHelper().renderText(String.format(OpenClassic.getGame().getTranslator().translate("gui.texture-packs.current"), (!pack.equals("none") ? pack.substring(0, pack.indexOf('.')) : "Default")), this.getWidth() / 2, this.getHeight() / 2 + 48, 16777215);
		super.render();
	}
}
