package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.settings.Settings;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public final class OptionsScreen extends GuiScreen {

	private GuiScreen parent;
	private Settings settings;

	public OptionsScreen(GuiScreen parent, Settings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	@Override
	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.getWidget(0, ButtonList.class).setContents(this.buildContents());
		this.attachWidget(new Button(75, this.getWidth() / 2 - 100, this.getHeight() / 6 + 148, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.options.hacks")));
		this.attachWidget(new Button(100, this.getWidth() / 2 + 2, this.getHeight() / 6 + 148, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.options.controls")));
		this.attachWidget(new Button(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 172, this, OpenClassic.getGame().getTranslator().translate("gui.done")));
	}
	
	private List<String> buildContents() {
		List<String> contents = new ArrayList<String>();
		for(int count = 0; count < this.settings.getSettings().size(); count++) {
			if(GeneralUtils.getMinecraft().isInMultiplayer() && this.settings.getSetting(count).getName().equals("options.survival")) {
				continue;
			}
			
			if(GeneralUtils.getMinecraft().mipmapMode == 0 && this.settings.getSetting(count).getName().equals("options.smoothing")) {
				continue;
			}
			
			contents.add(OpenClassic.getGame().getTranslator().translate(this.settings.getSetting(count).getName()) + ": " + this.settings.getSetting(count).getStringValue());
		}
		
		return contents;
	}

	@Override
	public void onButtonClick(Button button) {
		if(button.getId() == 75) {
			GeneralUtils.getMinecraft().setCurrentScreen(new HacksScreen(this, GeneralUtils.getMinecraft().hackSettings));
		}

		if(button.getId() == 100) {
			GeneralUtils.getMinecraft().setCurrentScreen(new ControlsScreen(this, GeneralUtils.getMinecraft().bindings));
		}

		if(button.getId() == 200) {
			GeneralUtils.getMinecraft().setCurrentScreen(this.parent);
		}
	}
	
	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		int page = list.getCurrentPage();
		this.settings.getSetting((list.getCurrentPage() * 5) + button.getId()).toggle();
		this.getWidget(0, ButtonList.class).setContents(this.buildContents());
		list.setCurrentPage(page);
	}

	@Override
	public void render() {
		if(GeneralUtils.getMinecraft().ingame) {
			RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		} else {
			RenderHelper.getHelper().drawDefaultBG();
		}

		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.options.title"), this.getWidth() / 2, 20);
		super.render();
	}
}
