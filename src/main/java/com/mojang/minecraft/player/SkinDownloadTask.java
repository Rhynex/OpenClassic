package com.mojang.minecraft.player;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.util.Constants;

public final class SkinDownloadTask implements Runnable {

	private Player player;

	public SkinDownloadTask(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
			HttpURLConnection conn = null;

			try {
				String name = this.player.getName();
				if (name.contains("@")) {
					name = name.substring(0, name.indexOf('@'));
				}

				conn = (HttpURLConnection) new URL(Constants.MINECRAFT_URL_HTTP + "skin/" + Color.stripColor(name) + ".png").openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(false);
				conn.connect();

				if (conn.getResponseCode() != 404 && conn.getResponseCode() != 403) {
					this.player.newTexture = ImageIO.read(conn.getInputStream());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(conn != null) {
					conn.disconnect();
				}
			}
	}
}