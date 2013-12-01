package ch.spacebase.openclassic.client.render;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.block.model.TextureFactory;

public class ClientTextureFactory extends TextureFactory {

	private List<ClientTexture> textures = new ArrayList<ClientTexture>();
	
	@Override
	public Texture newTexture(URL url) {
		for(ClientTexture tex : this.textures) {
			if(tex.getURL().equals(url)) {
				return tex;
			}
		}
		
		ClientTexture texture = new ClientTexture(url);
		this.textures.add(texture);
		return texture;
	}
	
	@Override
	public Texture newTexture(URL url, int frameWidth, int frameHeight, int frameSpeed) {
		for(ClientTexture tex : this.textures) {
			if(tex.getURL().equals(url)) {
				return tex;
			}
		}
		
		ClientTexture texture = new ClientTexture(url, frameWidth, frameHeight, frameSpeed);
		this.textures.add(texture);
		return texture;
	}
	
	public void updateTextures() {
		List<ClientTexture> disposed = new ArrayList<ClientTexture>();
		for(ClientTexture texture : this.textures) {
			if(texture.isDisposed()) {
				disposed.add(texture);
			}
			
			texture.update();
		}
		
		this.textures.remove(disposed);
	}
	
	public void resetTextures() {
		for(ClientTexture texture : this.textures) {
			texture.resetTextureId();
		}
	}
	
	public void reloadTextures() {
		for(ClientTexture texture : this.textures) {
			texture.reload();
		}
	}

}
