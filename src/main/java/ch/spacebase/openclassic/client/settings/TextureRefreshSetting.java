package ch.spacebase.openclassic.client.settings;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.model.TextureFactory;
import ch.spacebase.openclassic.api.settings.BooleanSetting;
import ch.spacebase.openclassic.client.render.ClientTextureFactory;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class TextureRefreshSetting extends BooleanSetting {

	public TextureRefreshSetting(String name) {
		super(name);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		((ClientTextureFactory) TextureFactory.getFactory()).resetTextures();
		if(OpenClassic.getClient().isInGame()) {
			GeneralUtils.getMinecraft().levelRenderer.refresh();
		}
	}

}
