package ch.spacebase.openclassic.game.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.custom.audio.MusicStopMessage;
import ch.spacebase.openclassic.client.network.ClientSession;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.handler.MessageHandler;

public class MusicStopMessageHandler extends MessageHandler<MusicStopMessage> {

	@Override
	public void handleClient(ClientSession session, final ClientPlayer player, MusicStopMessage message) {
		if(session == null || player == null) return;
		if(message.getIdentifier().equals("all_music")) {
			OpenClassic.getClient().getAudioManager().stopMusic();
		} else {
			OpenClassic.getClient().getAudioManager().stop(message.getIdentifier());
		}
	}

}
