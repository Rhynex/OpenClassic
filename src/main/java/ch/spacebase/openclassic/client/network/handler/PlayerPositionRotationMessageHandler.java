package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.PlayerPositionRotationMessage;

import com.mojang.minecraft.entity.player.net.NetworkPlayer;

public class PlayerPositionRotationMessageHandler extends MessageHandler<PlayerPositionRotationMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerPositionRotationMessage message) {
		if(message.getPlayerId() >= 0) {
			NetworkPlayer moving = GeneralUtils.getMinecraft().netPlayers.get(message.getPlayerId());
			if(moving != null) {
				moving.queue((byte) (message.getXChange() * 32), (byte) (message.getYChange() * 32), (byte) (message.getZChange() * 32), message.getYaw(), message.getPitch());
			}
		}
	}

}
