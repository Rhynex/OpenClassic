package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.DefaultBackground;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.client.util.ServerDataStore;

public class ConfirmDeleteServerScreen extends GuiComponent {

	private GuiComponent parent;
	private String name;

	public ConfirmDeleteServerScreen(GuiComponent parent, String name) {
		super("confirmdeleteserverscreen");
		this.parent = parent;
		this.name = name;
	}
	
	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(oparent.getWidth(), oparent.getHeight());
		this.attachComponent(new DefaultBackground("bg"));
		this.attachComponent(new Button("yes", this.getWidth() / 2 - 204, this.getHeight() / 2 + 104, 200, 40, "Yes").setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				ServerDataStore.removeFavorite(name);
				ServerDataStore.saveFavorites();
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Button("no", this.getWidth() / 2 + 4, this.getHeight() / 2 + 104, 200, 40, "No").setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, this.getHeight() / 4 - 60, String.format(OpenClassic.getGame().getTranslator().translate("gui.delete.server"), this.name), true));
	}
	
}
