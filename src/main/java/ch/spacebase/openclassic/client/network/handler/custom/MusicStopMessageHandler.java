package ch.spacebase.openclassic.client.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.custom.audio.MusicStopMessage;

public class MusicStopMessageHandler extends MessageHandler<MusicStopMessage> {

	@Override
	public void handle(ClassicSession session, Player player, MusicStopMessage message) {
		if(message.getIdentifier().equals("all_music")) {
			OpenClassic.getClient().getAudioManager().stopMusic();
		} else {
			OpenClassic.getClient().getAudioManager().stop(message.getIdentifier());
		}
	}

}
