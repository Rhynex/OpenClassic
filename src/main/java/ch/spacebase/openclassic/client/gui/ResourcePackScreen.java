package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ResourcePackScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] packs = null;

	public ResourcePackScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 150, this.getHeight() / 6 + 312, 300, 40, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
	
		File dir = new File(OpenClassic.getClient().getDirectory(), "resourcepacks");
		if(!dir.exists()) dir.mkdirs();
		
		StringBuilder textures = new StringBuilder("Default");
		for(String file : (dir.list())) {
			if(!file.endsWith(".zip")) continue;
			textures.append(";").append(file.substring(0, file.indexOf(".")));
		}
		
		this.packs = textures.toString().split(";");
		this.getWidget(0, ButtonList.class).setContents(Arrays.asList(this.packs));
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			if (button.getId() == 1) {
				OpenClassic.getClient().setCurrentScreen(this.parent);
			}
		}
	}
	
	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		if(button.isActive()) {
			if(button.getText().equals("Default")) {
				OpenClassic.getClient().getConfig().setValue("settings.resourcepack", "none");
			} else {
				OpenClassic.getClient().getConfig().setValue("settings.resourcepack", button.getText() + ".zip");
			}
			
			try {
				OpenClassic.getClient().getConfig().save();
			} catch (IOException e) {
				OpenClassic.getLogger().severe("Failed to save config!");
				e.printStackTrace();
			}
			
			ClientRenderHelper.getHelper().getTextureManager().clear();
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.resource-packs.select"), this.getWidth() / 2, 30);
		
		String resourcePack = OpenClassic.getClient().getConfig().getString("settings.resourcepack", "none");
		RenderHelper.getHelper().renderText(String.format(OpenClassic.getGame().getTranslator().translate("gui.resource-packs.current"), (!resourcePack.equals("none") ? resourcePack.substring(0, resourcePack.indexOf('.')) : "Default")), this.getWidth() / 2, this.getHeight() / 2 + 96);
		super.render();
	}
}
