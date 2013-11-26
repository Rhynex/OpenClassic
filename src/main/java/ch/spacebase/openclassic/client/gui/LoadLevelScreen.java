package ch.spacebase.openclassic.client.gui;

import java.io.File;
import java.util.Arrays;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;

public class LoadLevelScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] levels = null;

	private boolean delete = false;

	public LoadLevelScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		ButtonList list = new ButtonList(1, this, true);
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				if(delete) {
					File file = null;
					for(File f : (new File(OpenClassic.getGame().getDirectory(), "levels")).listFiles()) {
						if(f != null && (f.getName().equals(button.getText() + ".mine") || f.getName().equals(button.getText() + ".map") || f.getName().equals(button.getText() + ".oclvl") || f.getName().equals(button.getText() + ".lvl") || f.getName().equals(button.getText() + ".dat") || f.getName().equals(button.getText() + ".mclevel"))) {
							file = f;
							break;
						}
					}

					if(file == null) return;

					OpenClassic.getClient().setCurrentScreen(new ConfirmDeleteScreen(LoadLevelScreen.this, button.getText(), file));
					delete = false;
					getWidget(5, Label.class).setText(OpenClassic.getGame().getTranslator().translate("gui.load-level.title"));
				} else {
					OpenClassic.getClient().openLevel(getWidget(1, ButtonList.class).getButton(button.getId()).getText());
				}
			}
		});
		
		this.attachWidget(list);
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 156, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.load-level.new")));
		this.attachWidget(WidgetFactory.getFactory().newButton(3, this.getWidth() / 2 - 52, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.load-level.delete")));
		this.attachWidget(WidgetFactory.getFactory().newButton(4, this.getWidth() / 2 + 52, this.getHeight() / 6 + 156, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
		this.attachWidget(WidgetFactory.getFactory().newLabel(5, this.getWidth() / 2, 15, this, OpenClassic.getGame().getTranslator().translate("gui.load-level.title"), true));
		
		StringBuilder levels = new StringBuilder();
		for(String file : (new File(OpenClassic.getGame().getDirectory(), "levels").list())) {
			if(!file.endsWith(".map") && !file.endsWith(".mine") && !file.endsWith(".mclevel") && !file.endsWith(".oclvl") && !file.endsWith(".dat") && !file.endsWith(".lvl")) continue;
			if(levels.length() != 0) levels.append(";");
			levels.append(file.substring(0, file.indexOf(".")));
		}

		this.levels = levels.toString().split(";");
		this.getWidget(1, ButtonList.class).setContents(Arrays.asList(this.levels));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(new LevelCreateScreen(this));
		}

		if(button.getId() == 3) {
			if(this.delete) {
				this.getWidget(5, Label.class).setText(OpenClassic.getGame().getTranslator().translate("gui.load-level.title"));
				this.delete = false;
			} else {
				this.getWidget(5, Label.class).setText(Color.RED + OpenClassic.getGame().getTranslator().translate("gui.load-level.title-delete"));
				this.delete = true;
			}
		}

		if(button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}
	
}
