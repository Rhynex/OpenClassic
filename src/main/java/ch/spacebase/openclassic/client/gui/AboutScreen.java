package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonCallback;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.util.Constants;

public class AboutScreen extends GuiScreen {

	private GuiScreen parent;

	public AboutScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		this.attachWidget(WidgetFactory.getFactory().newButton(1, this.getWidth() / 2 - 100, this.getHeight() / 6 + 120 + 12, this, OpenClassic.getGame().getTranslator().translate("gui.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setCurrentScreen(parent);
			}
		}));
		this.attachWidget(WidgetFactory.getFactory().newBlockPreview(2, this.getWidth() / 2 - 10, (this.getHeight() / 2) - 56, this, VanillaBlock.STONE, 2));
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, (this.getHeight() / 2) - 32, this, "OpenClassic " + Constants.VERSION, true));
		this.attachWidget(WidgetFactory.getFactory().newLabel(4, this.getWidth() / 2, (this.getHeight() / 2) - 21, this, "Modded By Steveice10 (Steveice10@gmail.com)", true));
	}
	
}
