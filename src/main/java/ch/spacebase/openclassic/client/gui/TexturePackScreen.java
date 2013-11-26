package ch.spacebase.openclassic.client.gui;

import java.io.File;
import java.util.Arrays;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class TexturePackScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] textures = null;

	public TexturePackScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		ButtonList list = new ButtonList(1, this);
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				if(button.getText().equals("Default")) {
					OpenClassic.getClient().getConfig().setValue("options.texture-pack", "none");
				} else {
					OpenClassic.getClient().getConfig().setValue("options.texture-pack", button.getText() + ".zip");
				}

				OpenClassic.getClient().getConfig().save();
				GeneralUtils.getMinecraft().textureManager.clear();
			}
		});
		
		this.attachWidget(list);
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 75, this.getHeight() / 6 + 156, 150, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, 15, this, OpenClassic.getGame().getTranslator().translate("gui.texture-packs.select"), true));
		
		String pack = OpenClassic.getClient().getConfig().getString("options.texture-pack");
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.texture-packs.current"), (!pack.equals("none") ? pack.substring(0, pack.indexOf('.')) : "Default"));
		this.attachWidget(WidgetFactory.getFactory().newLabel(4, this.getWidth() / 2, this.getHeight() / 2 + 48, this, text, true));
		
		StringBuilder textures = new StringBuilder("Default");
		for(String file : (new File(OpenClassic.getClient().getDirectory(), "texturepacks").list())) {
			if(!file.endsWith(".zip")) continue;
			textures.append(";").append(file.substring(0, file.indexOf(".")));
		}

		this.textures = textures.toString().split(";");
		this.getWidget(1, ButtonList.class).setContents(Arrays.asList(this.textures));
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		String pack = OpenClassic.getClient().getConfig().getString("options.texture-pack");
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.texture-packs.current"), (!pack.equals("none") ? pack.substring(0, pack.indexOf('.')) : "Default"));
		Label label = this.getWidget(4, Label.class);
		if(!label.getText().equals(text)) {
			label.setText(text);
		}
	}
	
}
