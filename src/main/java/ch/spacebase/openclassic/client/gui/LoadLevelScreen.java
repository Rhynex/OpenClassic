package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.gui.ConfirmDeleteScreen;

import java.io.File;
import java.util.Arrays;


public class LoadLevelScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] levels = null;
	private String title = "Load level";

	private boolean delete = false;

	public LoadLevelScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 312, this.getHeight() / 6 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.load-level.new")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 104, this.getHeight() / 6 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.load-level.delete")));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 104, this.getHeight() / 6 + 288, 200, 40, this, OpenClassic.getGame().getTranslator().translate("gui.back")));

		StringBuilder levels = new StringBuilder();
		File dir = new File(OpenClassic.getClient().getDirectory(), "levels");
		for(String file : dir.list()) {
			File f = new File(dir, file);
			if(f.isDirectory()) {
				if(levels.length() != 0) levels.append("/");
				levels.append(file);
				continue;
			}
		}

		this.levels = levels.toString().split("/");
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

	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		if(this.delete) {
			File file = null;
			for(File f : (new File(OpenClassic.getClient().getDirectory(), "levels")).listFiles()) {
				if(f != null && f.isDirectory() && f.getName().equals(button.getText())) {
					file = f;
					break;
				}
			}

			if(file == null) return;
			OpenClassic.getClient().setCurrentScreen(new ConfirmDeleteScreen(this, file));
			this.delete = false;
			this.title = OpenClassic.getGame().getTranslator().translate("gui.load-level.title");
		} else {
			OpenClassic.getClient().getProgressBar().setTitle("Singleplayer");
			OpenClassic.getClient().getProgressBar().setSubTitle("Loading Level");
			OpenClassic.getClient().getProgressBar().setText("Loading chunks...");
			OpenClassic.getClient().getProgressBar().setProgress(0);
			OpenClassic.getClient().getProgressBar().setVisible(true);
			OpenClassic.getClient().openLevel(button.getText());
			OpenClassic.getClient().getProgressBar().setVisible(false);
			OpenClassic.getClient().setCurrentScreen(null);
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 30);
		super.render();
	}
}
