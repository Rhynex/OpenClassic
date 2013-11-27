package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.BlockPreview;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.DefaultBackground;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.api.util.Constants;

public class AboutScreen extends GuiComponent {

	private GuiComponent parent;

	public AboutScreen(GuiComponent parent) {
		super("aboutscreen");
		this.parent = parent;
	}

	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(oparent.getWidth(), oparent.getHeight());
		this.attachComponent(new DefaultBackground("bg"));
		this.attachComponent(new Button("back", this.getWidth() / 2 - 200, this.getHeight() / 6 + this.getHeight() / 2, OpenClassic.getGame().getTranslator().translate("gui.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new BlockPreview("stone", this.getWidth() / 2 - 18, (this.getHeight() / 2) - 112, VanillaBlock.STONE, 2));
		this.attachComponent(new Label("name", this.getWidth() / 2, (this.getHeight() / 2) - 64, "OpenClassic " + Constants.VERSION, true));
		this.attachComponent(new Label("author", this.getWidth() / 2, (this.getHeight() / 2) - 42, "Modded By Steveice10 (Steveice10@gmail.com)", true));
	}
	
}
