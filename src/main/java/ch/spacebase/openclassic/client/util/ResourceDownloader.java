package ch.spacebase.openclassic.client.util;

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

import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

import ch.spacebase.openclassic.api.OpenClassic;

public final class ResourceDownloader extends Thread {

	private File resource;
	private boolean running = true;

	public ResourceDownloader() {
		this.setName("ResourceDownloader");
		this.setDaemon(true);
		this.resource = new File(OpenClassic.getClient().getDirectory(), "resources/");
		if(!this.resource.exists() && !this.resource.mkdirs()) {
			JOptionPane.showMessageDialog(null, String.format(OpenClassic.getGame().getTranslator().translate("core.fail-resources"), this.resource));
			this.running = false;
		}
	}

	public void run() {
		if(!this.running) {
			return;
		}
		
		BufferedReader reader = null;
		ArrayList<String> list = new ArrayList<String>();

		URL base = null;
		try {
			base = new URL("https://dl.dropboxusercontent.com/u/40737374/openclassic/resources/");
			URL url = new URL(base, "resources/");

			URLConnection con = url.openConnection();
			con.setConnectTimeout(20000);
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;

			while((line = reader.readLine()) != null) {
				list.add(line);
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}

		for(String curr : list) {
			try {
				String split[] = curr.split(",");
				int size = Integer.parseInt(split[1]);
				File file = new File(this.resource, split[0]);

				if(!file.exists() || file.length() != size) {
					try {
						file.getParentFile().mkdirs();
					} catch(SecurityException e) {
						e.printStackTrace();
					}

					this.download(new URL(base, split[0].replaceAll(" ", "%20")), file, size);
				} else {
					int index = split[0].indexOf("/");
					if(split[0].substring(0, index).equalsIgnoreCase("sound")) {
						OpenClassic.getGame().getAudioManager().registerSound(split[0].substring(index + 1, split[0].length() - 4).replaceAll("[1-9]", "").replaceAll("/", "."), file.toURI().toURL(), true);
					} else if(split[0].substring(0, index).equalsIgnoreCase("music")) {
						if(split[0].contains("sweden")) {
							OpenClassic.getGame().getAudioManager().registerMusic("menu", file.toURI().toURL(), true);
						}

						OpenClassic.getGame().getAudioManager().registerMusic("bg", file.toURI().toURL(), true);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			if(!this.running) break;
		}

		this.running = false;
	}

	private void download(URL url, File file, int size) {
		OpenClassic.getLogger().info(String.format(OpenClassic.getGame().getTranslator().translate("http.downloading"), file.getName()));
		OpenClassic.getClient().getProgressBar().setText(file.getName());
		byte[] data = new byte[4096];
		DataInputStream in = null;
		DataOutputStream out = null;

		try {
			in = new DataInputStream(url.openStream());
			out = new DataOutputStream(new FileOutputStream(file));

			int length = 0;
			int done = 0;
			while(this.running) {
				length = in.read(data);
				if(length < 0) break;

				out.write(data, 0, length);
				done += length;
				OpenClassic.getClient().getProgressBar().setProgress((int) (((double) done / (double) size) * 100));
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}

		OpenClassic.getClient().getProgressBar().setText("");
		OpenClassic.getLogger().info(String.format(OpenClassic.getGame().getTranslator().translate("http.downloaded"), file.getName()));
	}

	public boolean isRunning() {
		return this.running;
	}
	
	public void stopThread() {
		this.running = false;
	}

}