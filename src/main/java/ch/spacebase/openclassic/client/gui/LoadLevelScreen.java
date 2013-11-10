package ch.spacebase.openclassic.client.gui;

import java.io.File;
import java.util.Arrays;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;

public class LoadLevelScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] levels = null;
	private String title;

	private boolean delete = false;

	public LoadLevelScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.title = OpenClassic.getGame().getTranslator().translate("gui.load-level.title");
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 156, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.load-level.new")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.load-level.delete")));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 52, this.getHeight() / 6 + 144, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));

		StringBuilder levels = new StringBuilder();
		for(String file : (new File(OpenClassic.getGame().getDirectory(), "levels").list())) {
			if(!file.endsWith(".map") && !file.endsWith(".mine") && !file.endsWith(".mclevel") && !file.endsWith(".oclvl") && !file.endsWith(".dat") && !file.endsWith(".lvl")) continue;
			if(levels.length() != 0) levels.append(";");
			levels.append(file.substring(0, file.indexOf(".")));
		}

		this.levels = levels.toString().split(";");
		this.getWidget(0, ButtonList.class).setContents(Arrays.asList(this.levels));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new LevelCreateScreen(this));
		}

		if(button.getId() == 2) {
			if(this.delete) {
				this.title = OpenClassic.getGame().getTranslator().translate("gui.load-level.title");
				this.delete = false;
			} else {
				this.title = Color.RED + OpenClassic.getGame().getTranslator().translate("gui.load-level.title-delete");
				this.delete = true;
			}
		}

		if(button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void onButtonListClick(ButtonList list, Button button) {
		if(this.delete) {
			File file = null;
			for(File f : (new File(OpenClassic.getGame().getDirectory(), "levels")).listFiles()) {
				if(f != null && (f.getName().equals(button.getText() + ".mine") || f.getName().equals(button.getText() + ".map") || f.getName().equals(button.getText() + ".oclvl") || f.getName().equals(button.getText() + ".lvl") || f.getName().equals(button.getText() + ".dat") || f.getName().equals(button.getText() + ".mclevel"))) {
					file = f;
					break;
				}
			}

			if(file == null) return;

			OpenClassic.getClient().setCurrentScreen(new ConfirmDeleteScreen(this, button.getText(), file));
			this.delete = false;
			this.title = OpenClassic.getGame().getTranslator().translate("gui.load-level.title");
		} else {
			OpenClassic.getClient().openLevel(this.getWidget(0, ButtonList.class).getButton(button.getId()).getText());
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15, 16777215);
		super.render();
	}
	
}
