package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.LevelIO;

public final class LevelDumpScreen extends GuiScreen {

	private GuiScreen parent;
	private TextBox widget;

	public LevelDumpScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public final void onOpen() {
		this.widget = new TextBox(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 30, this, 30);
		
		this.clearWidgets();
		this.attachWidget(this.widget);
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.level-dump.dump")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")));
		
		this.getWidget(1, Button.class).setActive(false);
	}

	public final void onButtonClick(Button button) {
		Minecraft mc = GeneralUtils.getMinecraft();
		if (button.getId() == 1 && this.widget.getText().trim().length() > 0) {
			mc.level.name = this.widget.getText();
			LevelIO.save(mc.level);
			mc.setCurrentScreen(this.parent);
		}

		if (button.getId() == 2) {
			mc.setCurrentScreen(this.parent);
		}
	}

	public final void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(1, Button.class).setActive(this.widget.getText().trim().length() > 0);
	}

	public final void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.level-dump.name"), this.getWidth() / 2, 40);

		super.render();
	}
}
