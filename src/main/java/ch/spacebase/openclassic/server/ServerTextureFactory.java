package ch.spacebase.openclassic.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.block.model.TextureFactory;
import ch.spacebase.openclassic.game.GameTexture;

public class ServerTextureFactory extends TextureFactory {

	private List<GameTexture> textures = new ArrayList<GameTexture>();
	
	@Override
	public Texture newTexture(URL url) {
		GameTexture texture = new GameTexture(url);
		this.textures.add(texture);
		return texture;
	}
	
	@Override
	public Texture newTexture(URL url, int frameWidth, int frameHeight, int frameSpeed) {
		GameTexture texture = new GameTexture(url, frameWidth, frameHeight, frameSpeed);
		this.textures.add(texture);
		return texture;
	}

}
