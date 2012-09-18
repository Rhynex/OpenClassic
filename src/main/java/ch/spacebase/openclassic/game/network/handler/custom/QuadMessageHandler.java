package ch.spacebase.openclassic.game.network.handler.custom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.network.msg.custom.block.QuadMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class QuadMessageHandler extends MessageHandler<QuadMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, QuadMessage message) {
		if(session == null || player == null) return;
		if(Blocks.fromId(message.getBlock()) == null || Blocks.fromId(message.getBlock()).getModel() == null) return;
		if(!message.getQuad().getTexture().getParent().isInJar()) {
			File file = new File(OpenClassic.getClient().getDirectory(), "cache/" + session.getAddress().toString().replaceAll("/", "") + "/" + message.getBlock() + ".png");
			if(!file.exists()) {
				if(!file.getParentFile().exists()) {
					try {
						file.getParentFile().mkdirs();
					} catch(SecurityException e) {
						e.printStackTrace();
					}
				}

				try {
					file.createNewFile();
				} catch(IOException e) {
					e.printStackTrace();
					return;
				}

				System.out.println(String.format(OpenClassic.getGame().getTranslator().translate("http.downloading"), file.getName()));

				byte[] data = new byte[4096];
				DataInputStream in = null;
				DataOutputStream out = null;

				try {
					in = new DataInputStream((new URL(message.getQuad().getTexture().getParent().getTexture())).openStream());
					out = new DataOutputStream(new FileOutputStream(file));

					int length = 0;
					while (OpenClassic.getClient().isRunning()) {
						length = in.read(data);
						if (length < 0) break;
						out.write(data, 0, length);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				System.out.println(String.format(OpenClassic.getGame().getTranslator().translate("http.downloaded"), file.getName()));
			}

			message.getQuad().getTexture().getParent().setTexture(file.getPath());
		}
		
		((CustomBlock) Blocks.fromId(message.getBlock())).getModel().addQuad(message.getQuad());
	}

}
