package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.custom.audio.AudioPlayMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class AudioPlayMessageHandler extends MessageHandler<AudioPlayMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, AudioPlayMessage message) {
		if(session == null || player == null) return;
		if(message.isMusic()) {
			OpenClassic.getClient().getAudioManager().playMusic(message.getIdentifier(), message.isLooping());
		} else {
			OpenClassic.getClient().getAudioManager().playSound(message.getIdentifier(), message.getX(), message.getY(), message.getZ(), message.getVolume(), message.getPitch());
		}
	}

}
