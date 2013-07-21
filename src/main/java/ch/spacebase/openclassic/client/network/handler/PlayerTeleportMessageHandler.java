package ch.spacebase.openclassic.client.network.handler;

import com.mojang.minecraft.player.net.NetworkPlayer;

import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class PlayerTeleportMessageHandler extends MessageHandler<PlayerTeleportMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerTeleportMessage message) {
		if(message.getPlayerId() < 0) {
			GeneralUtils.getMinecraft().player.moveTo(message.getX(), message.getY(), message.getZ(), message.getYaw(), message.getPitch());
		} else {
			NetworkPlayer moving = GeneralUtils.getMinecraft().netPlayers.get(message.getPlayerId());
			if (moving != null) {
				moving.teleport((short) (message.getX() * 32), (short) ((message.getY() * 32) - 22), (short) (message.getZ() * 32), message.getYaw(), message.getPitch());
			}
		}
	}

}
