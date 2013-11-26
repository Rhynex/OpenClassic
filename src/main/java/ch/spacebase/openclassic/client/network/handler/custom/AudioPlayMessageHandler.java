package ch.spacebase.openclassic.client.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.custom.audio.AudioPlayMessage;

public class AudioPlayMessageHandler extends MessageHandler<AudioPlayMessage> {

	@Override
	public void handle(ClassicSession session, Player player, AudioPlayMessage message) {
		if(message.isMusic()) {
			OpenClassic.getClient().getAudioManager().playMusic(message.getIdentifier(), message.isLooping());
		} else {
			OpenClassic.getClient().getAudioManager().playSound(message.getIdentifier(), message.getX(), message.getY(), message.getZ(), message.getVolume(), message.getPitch());
		}
	}

}
