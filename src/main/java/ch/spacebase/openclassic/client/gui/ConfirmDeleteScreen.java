package ch.spacebase.openclassic.client.gui;

import java.io.File;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;

public class ConfirmDeleteScreen extends GuiScreen {

	private GuiScreen parent;
	private String name;
	private File file;

	public ConfirmDeleteScreen(GuiScreen parent, String name, File file) {
		this.parent = parent;
		this.name = name;
		this.file = file;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 102, this.getHeight() / 6 + 132, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.yes")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				try {
					file.delete();
				} catch(SecurityException e) {
					e.printStackTrace();
				}

				File file = new File(new File(OpenClassic.getClient().getDirectory(), "levels"), name + ".nbt");
				if(file.exists()) {
					try {
						file.delete();
					} catch(SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 + 2, this.getHeight() / 6 + 132, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.no")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, (this.getHeight() / 2) - 32, this, String.format(OpenClassic.getGame().getTranslator().translate("gui.delete.level"), this.file.getName().substring(0, this.file.getName().indexOf("."))), true));
	}
	
}
