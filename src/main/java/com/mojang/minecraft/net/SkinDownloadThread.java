package com.mojang.minecraft.net;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.util.HTTPUtil;

import com.mojang.minecraft.net.NetworkPlayer;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

final class SkinDownloadThread extends Thread {

	private NetworkPlayer player;

	public SkinDownloadThread(NetworkPlayer player) {
		super("Client-Skin Download Thread");
		this.player = player;
	}

	public void run() {
		HttpURLConnection conn = null;

		try {
			conn = (HttpURLConnection) (new URL(Constants.MINECRAFT_URL + "skin/" + Color.stripColor(this.player.name) + ".png")).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.connect();
			if (conn.getResponseCode() != 404 && conn.getResponseCode() != 403) {
				this.player.newTexture = ImageIO.read(conn.getInputStream());
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
