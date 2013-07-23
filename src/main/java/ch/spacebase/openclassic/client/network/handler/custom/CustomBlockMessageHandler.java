package ch.spacebase.openclassic.client.network.handler.custom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class CustomBlockMessageHandler extends MessageHandler<CustomBlockMessage> {

	@Override
	public void handle(ClassicSession session, Player player, CustomBlockMessage message) {
		for(Quad quad : message.getBlock().getModel().getQuads()) {
			if(!quad.getTexture().getParent().isInJar()) {
				File file = new File(OpenClassic.getGame().getDirectory(), "cache/" + GeneralUtils.getMinecraft().server + "/" + message.getBlock().getId() + ".png");
				if(!file.exists()) {
					if(!file.getParentFile().exists()) {
						try {
							file.getParentFile().mkdirs();
						} catch(SecurityException e) {
							e.printStackTrace();
							continue;
						}
					}

					try {
						file.createNewFile();
					} catch(Exception e) {
						e.printStackTrace();
						continue;
					}

					OpenClassic.getLogger().info(String.format(OpenClassic.getGame().getTranslator().translate("http.downloading"), file.getName()));

					byte[] data = new byte[4096];
					DataInputStream in = null;
					DataOutputStream out = null;

					try {
						in = new DataInputStream((new URL(quad.getTexture().getParent().getTexture())).openStream());
						out = new DataOutputStream(new FileOutputStream(file));

						int length = 0;
						while(OpenClassic.getGame().isRunning()) {
							length = in.read(data);
							if(length < 0) break;
							out.write(data, 0, length);
						}
					} catch(IOException e) {
						e.printStackTrace();
					} finally {
						IOUtils.closeQuietly(in);
						IOUtils.closeQuietly(out);
					}

					OpenClassic.getLogger().info(String.format(OpenClassic.getGame().getTranslator().translate("http.downloaded"), file.getName()));
				}

				quad.getTexture().getParent().setTexture(file.getPath());
			}
		}

		Blocks.register(message.getBlock());
	}

}
