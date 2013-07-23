package com.mojang.minecraft;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.ClientProgressBar;

public final class ResourceDownloadThread extends Thread {

	private File resource;
	private Minecraft mc;
	public boolean running = true;
	private boolean finished = false;

	public ResourceDownloadThread(File location, Minecraft mc, ClientProgressBar progress) {
		this.mc = mc;
		this.setName("Client-Resource Download Thread");
		this.setDaemon(true);
		
		this.resource = new File(location, "resources/");
		if (!this.resource.exists() && !this.resource.mkdirs()) {
			throw new RuntimeException(String.format(OpenClassic.getGame().getTranslator().translate("core.fail-resources"), this.resource));
		}
	}

	public final void run() {
		BufferedReader reader = null;
		ArrayList<String> list = new ArrayList<String>();
		
		URL base = null;
		try {
			base = new URL("https://dl.dropboxusercontent.com/u/40737374/minecraft_resources/");
			URL url = new URL(base, "resources/");
			
			URLConnection con = url.openConnection();
			con.setConnectTimeout(20000);
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;

			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}
		
		for (String curr : list) {
			try {
				String split[] = curr.split(",");
				int size = Integer.parseInt(split[1]);
				File file = new File(this.resource, split[0]);

				if (!file.exists() || file.length() != size) {
					try {
						file.getParentFile().mkdirs();
					} catch(SecurityException e) {
						e.printStackTrace();
					}
					
					this.download(new URL(base, split[0].replaceAll(" ", "%20")), file, size);
				} else {
					int index = split[0].indexOf("/");
					if (split[0].substring(0, index).equalsIgnoreCase("sound")) {
						this.mc.audio.registerSound(split[0].substring(index + 1, split[0].length() - 4).replaceAll("[1-9]", "").replaceAll("/", "."), file.toURI().toURL(), true);
					} else if (split[0].substring(0, index).equalsIgnoreCase("music")) {
						if(split[0].contains("sweden"))  {
							this.mc.audio.registerMusic("menu", file.toURI().toURL(), true);
						}
						
						this.mc.audio.registerMusic("bg", file.toURI().toURL(), true);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!this.running) return;
		}
		
		this.finished = true;
	}
	
	private void download(URL url, File file, int size) {
		System.out.println(String.format(OpenClassic.getGame().getTranslator().translate("http.downloading"), file.getName()));
		this.mc.progressBar.setText(file.getName());
		byte[] data = new byte[4096];
		DataInputStream in = null;
		DataOutputStream out = null;
		
		try {
			in = new DataInputStream(url.openStream());
			out = new DataOutputStream(new FileOutputStream(file));

			int length = 0;
			int done = 0;
			while (this.running) {
				length = in.read(data);
				if (length < 0) break;

				out.write(data, 0, length);
				done += length;
				this.mc.progressBar.setProgress((int) (((double) done / (double) size) * 100));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		
		this.mc.progressBar.setText("");
		System.out.println(String.format(OpenClassic.getGame().getTranslator().translate("http.downloaded"), file.getName()));
	}
	
	public boolean isFinished() {
		return this.finished;
	}
	
}