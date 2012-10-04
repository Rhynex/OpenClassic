package ch.spacebase.openclassic.game.component.player;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.component.Component;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;

public class ClientInfoComponent extends Component {

	private boolean custom;
	private String version;
	private String language = "";
	private List<RemotePluginInfo> plugins = new ArrayList<RemotePluginInfo>();

	@Override
	public boolean canDetach() {
		return false;
	}
	
	@Override
	public boolean canTick() {
		return false;
	}
	
	public boolean isCustom() {
		return this.custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void addPlugin(RemotePluginInfo info) {
		this.plugins.add(info);
	}
	
	public List<RemotePluginInfo> getPlugins() {
		return this.plugins;
	}
	
	public String getLanguage() {
		return this.language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
}
