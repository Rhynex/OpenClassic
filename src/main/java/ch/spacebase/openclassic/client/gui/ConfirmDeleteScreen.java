package ch.spacebase.openclassic.client.gui;

import java.io.File;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.DefaultBackground;
import ch.spacebase.openclassic.api.gui.base.Label;

public class ConfirmDeleteScreen extends GuiComponent {

	private GuiComponent parent;
	private String name;
	private File file;

	public ConfirmDeleteScreen(GuiComponent parent, String name, File file) {
		super("confirmdeletescreen");
		this.parent = parent;
		this.name = name;
		this.file = file;
	}
	
	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(parent.getWidth(), parent.getHeight());
		this.attachComponent(new DefaultBackground("bg"));
		this.attachComponent(new Button("yes", this.getWidth() / 2 - 204, this.getHeight() / 6 + 264, 200, 40, OpenClassic.getGame().getTranslator().translate("gui.yes")).setCallback(new ButtonCallback() {
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
				
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Button("no", this.getWidth() / 2 + 4, this.getHeight() / 6 + 264, 200, 40, OpenClassic.getGame().getTranslator().translate("gui.no")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, (this.getHeight() / 2) - 64, String.format(OpenClassic.getGame().getTranslator().translate("gui.delete.level"), this.file.getName().substring(0, this.file.getName().indexOf("."))), true));
	}
	
}
