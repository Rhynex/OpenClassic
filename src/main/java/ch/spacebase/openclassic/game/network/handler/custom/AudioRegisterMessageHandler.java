package ch.spacebase.openclassic.game.network.handler.custom;

import java.net.MalformedURLException;
import java.net.URL;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.custom.audio.AudioRegisterMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class AudioRegisterMessageHandler extends MessageHandler<AudioRegisterMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, AudioRegisterMessage message) {
		if(session == null || player == null) return;
		try {
			if(message.isMusic()) {
				OpenClassic.getClient().getAudioManager().registerMusic(message.getIdentifier(), new URL(message.getUrl()), false);
			} else {
				OpenClassic.getClient().getAudioManager().registerSound(message.getIdentifier(), new URL(message.getUrl()), false);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
