package com.mojang.minecraft.entity.player;

import java.net.HttpURLConnection;
import java.net.URL;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.block.model.TextureFactory;
import ch.spacebase.openclassic.client.render.ClientTexture;

public class SkinDownloadTask implements Runnable {

	private Player player;

	public SkinDownloadTask(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
		HttpURLConnection conn = null;

		try {
			String name = this.player.getName();
			if(name.contains("@")) {
				name = name.substring(0, name.indexOf('@'));
			}

			ClientTexture texture = (ClientTexture) TextureFactory.getFactory().newTexture(new URL("http://s3.amazonaws.com/MinecraftSkins/" + Color.stripColor(name) + ".png"));
			if(texture.getImage() != null) {
				this.player.skin = texture;
			}
		} catch(Exception e) {
			if(e.getCause() == null || !e.getCause().getMessage().contains("response code: 403")) {
				e.printStackTrace();
			}
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
	}
	
}