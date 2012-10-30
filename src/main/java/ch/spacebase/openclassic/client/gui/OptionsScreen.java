package ch.spacebase.openclassic.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

public final class OptionsScreen extends GuiScreen {

	private GuiScreen parent;

	public OptionsScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		List<String> keys = new ArrayList<String>();
		keys.addAll(OpenClassic.getClient().getConfig().getKeys("options"));
		for(int count = 0; count < keys.size(); count++) {
			this.attachWidget(new StateButton(count, this.getWidth() / 2 - 310 + count % 2 * 320, this.getHeight() / 6 + 48 * (count >> 1), 310, 40, this, this.getName("options." + keys.get(count))));
			this.getWidget(count, StateButton.class).setState(this.getValue("options." + keys.get(count)));
		}
		
		this.attachWidget(new Button(100, this.getWidth() / 2 - 200, this.getHeight() / 6 + 280, this, OpenClassic.getGame().getTranslator().translate("gui.options.controls")));
		this.attachWidget(new Button(200, this.getWidth() / 2 - 200, this.getHeight() / 6 + 332, this, OpenClassic.getGame().getTranslator().translate("gui.done")));
	}
	
	private String getValue(String path) {
		if(path.equals("options.music")) return OpenClassic.getClient().getConfig().getBoolean(path, true) ? "ON" : "OFF";
		if(path.equals("options.sound")) return OpenClassic.getClient().getConfig().getBoolean(path, true) ? "ON" : "OFF";
		if(path.equals("options.show-info")) return OpenClassic.getClient().getConfig().getBoolean(path, false) ? "ON" : "OFF";
		if(path.equals("options.view-bobbing")) return OpenClassic.getClient().getConfig().getBoolean(path, true) ? "ON" : "OFF";
		if(path.equals("options.invert-mouse")) return OpenClassic.getClient().getConfig().getBoolean(path, false) ? "ON" : "OFF";
		if(path.equals("options.view-distance")) {
			int distance = OpenClassic.getClient().getConfig().getInteger(path, 0);
			switch(distance) {
			case 0: return "FAR";
			case 1: return "NORMAL";
			case 2: return "SHORT";
			case 3: return "TINY";
			default: return "UNKNOWN";
			}
		}
		
		if(path.equals("options.smoothing")) return OpenClassic.getClient().getConfig().getBoolean(path, false) ? "ON" : "OFF";
		if(path.equals("options.particles")) return OpenClassic.getClient().getConfig().getBoolean(path, true) ? "ON" : "OFF";
		return OpenClassic.getClient().getConfig().getValue(path).toString();
	}
	
	private String getName(String path) {
		if(path.equals("options.music")) return "Music";
		if(path.equals("options.sound")) return "Sound";
		if(path.equals("options.show-info")) return "Show Info";
		if(path.equals("options.view-bobbing")) return "View Bobbing";
		if(path.equals("options.invert-mouse")) return "Invert Mouse";
		if(path.equals("options.view-distance")) return "Render Distance";
		if(path.equals("options.smoothing")) return "Smoothing";
		if(path.equals("options.particles")) return "Particles";
		return path;
	}
	
	private String getPath(String name) {
		if(name.equals("Music")) return "options.music";
		if(name.equals("Sound")) return "options.sound";
		if(name.equals("Show Info")) return "options.show-info";
		if(name.equals("View Bobbing")) return "options.view-bobbing";
		if(name.equals("Invert Mouse")) return "options.invert-mouse";
		if(name.equals("Render Distance")) return "options.view-distance";
		if(name.equals("Smoothing")) return "options.smoothing";
		if(name.equals("Particles")) return "options.particles";
		return name;
	}
	
	private void toggleSetting(String name) {
		String path = this.getPath(name);
		if(path.equals("options.music")) {
			OpenClassic.getClient().getConfig().setValue(path, !OpenClassic.getClient().getConfig().getBoolean(path, true));
			if(!OpenClassic.getClient().getConfig().getBoolean(path, true)) OpenClassic.getClient().getAudioManager().stopMusic();
		}
		
		if(path.equals("options.sound")) OpenClassic.getClient().getConfig().setValue(path, !OpenClassic.getClient().getConfig().getBoolean(path, true));
		if(path.equals("options.show-info")) OpenClassic.getClient().getConfig().setValue(path, !OpenClassic.getClient().getConfig().getBoolean(path, false));
		if(path.equals("options.view-bobbing")) OpenClassic.getClient().getConfig().setValue(path, !OpenClassic.getClient().getConfig().getBoolean(path, true));
		if(path.equals("options.invert-mouse")) OpenClassic.getClient().getConfig().setValue(path, !OpenClassic.getClient().getConfig().getBoolean(path, false));
		if(path.equals("options.view-distance")) OpenClassic.getClient().getConfig().setValue(path, OpenClassic.getClient().getConfig().getInteger(path, 0) + 1 & 3);
		if(path.equals("options.smoothing")) {
			OpenClassic.getClient().getConfig().setValue(path, !OpenClassic.getClient().getConfig().getBoolean(path, false));
			ClientRenderHelper.getHelper().getTextureManager().clear();
			if(OpenClassic.getClient().getLevel() != null) ((ClientLevel) OpenClassic.getClient().getLevel()).getRenderer().queueAll();
		}
		
		if(path.equals("options.particles")) OpenClassic.getClient().getConfig().setValue(path, !OpenClassic.getClient().getConfig().getBoolean(path, true));
		try {
			OpenClassic.getClient().getConfig().save();
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Failed to save config!");
			e.printStackTrace();
		}
	}

	public void onButtonClick(Button button) {
		if(button.isActive()) {
			if (button.getId() < 75) {
				this.toggleSetting(button.getText());
				((StateButton) button).setState(this.getValue(this.getPath(button.getText())));
			}
			
			if(button.getId() == 100) {
				OpenClassic.getClient().setCurrentScreen(new ControlsScreen(this));
			}

			if(button.getId() == 200) {
				OpenClassic.getClient().setCurrentScreen(this.parent);
			}
		}
	}

	public void render() {
		if(OpenClassic.getClient().isInGame()) {
			RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		} else {
			RenderHelper.getHelper().drawDefaultBG();
		}
		
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.options.title"), this.getWidth() / 2, 40);
		super.render();
	}
}
