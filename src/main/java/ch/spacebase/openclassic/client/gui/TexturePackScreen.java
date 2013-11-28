package ch.spacebase.openclassic.client.gui;

import java.io.File;
import java.util.Arrays;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.ButtonList;
import ch.spacebase.openclassic.api.gui.base.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.base.DefaultBackground;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class TexturePackScreen extends GuiComponent {

	private GuiComponent parent;
	private String[] textures = null;

	public TexturePackScreen(GuiComponent parent) {
		super("texturepackscreen");
		this.parent = parent;
	}

	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(oparent.getWidth(), oparent.getHeight());
		this.attachComponent(new DefaultBackground("bg"));
		ButtonList list = new ButtonList("packs", 0, 0, this.getWidth(), (int) (this.getHeight() * 0.8f));
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
		
		this.attachComponent(list);
		this.attachComponent(new Button("back", this.getWidth() / 2 - 150, (int) (this.getHeight() * 0.8f), 300, 40, OpenClassic.getGame().getTranslator().translate("gui.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, this.getHeight() / 4 - 80, OpenClassic.getGame().getTranslator().translate("gui.texture-packs.select"), true));
		
		String pack = OpenClassic.getClient().getConfig().getString("options.texture-pack");
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.texture-packs.current"), (!pack.equals("none") ? pack.substring(0, pack.indexOf('.')) : "Default"));
		this.attachComponent(new Label("current", this.getWidth() / 2, (int) (this.getHeight() * 0.8f) - 56, text, true));
		
		StringBuilder textures = new StringBuilder("Default");
		for(String file : (new File(OpenClassic.getClient().getDirectory(), "texturepacks").list())) {
			if(!file.endsWith(".zip")) continue;
			textures.append(";").append(file.substring(0, file.indexOf(".")));
		}

		this.textures = textures.toString().split(";");
		this.getComponent("packs", ButtonList.class).setContents(Arrays.asList(this.textures));
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		String pack = OpenClassic.getClient().getConfig().getString("options.texture-pack");
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.texture-packs.current"), (!pack.equals("none") ? pack.substring(0, pack.indexOf('.')) : "Default"));
		Label label = this.getComponent("current", Label.class);
		if(!label.getText().equals(text)) {
			label.setText(text);
		}
		
		super.update(mouseX, mouseY);
	}
	
}
