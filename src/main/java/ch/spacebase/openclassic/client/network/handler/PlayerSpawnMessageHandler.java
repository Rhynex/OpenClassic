package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.PlayerSpawnMessage;

import com.mojang.minecraft.entity.player.net.NetworkPlayer;

public class PlayerSpawnMessageHandler extends MessageHandler<PlayerSpawnMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerSpawnMessage message) {
		if(message.getPlayerId() >= 0) {
			ClientPlayer ocplayer = new ClientPlayer();
			ocplayer.setName(message.getName());
			NetworkPlayer p = new NetworkPlayer(GeneralUtils.getMinecraft(), message.getPlayerId(), ocplayer, message.getX(), message.getY() - 0.6875f, message.getZ(), message.getYaw(), message.getPitch());
			ocplayer.setHandle(p);
			GeneralUtils.getMinecraft().netPlayers.put(message.getPlayerId(), p);
			GeneralUtils.getMinecraft().level.addEntity(p);
		} else {
			OpenClassic.getClient().getLevel().setSpawn(new Position(OpenClassic.getClient().getLevel(), message.getX(), message.getY(), message.getZ(), message.getYaw(), message.getPitch()));
			GeneralUtils.getMinecraft().player.moveTo(message.getX(), message.getY(), message.getZ(), message.getYaw(), message.getPitch());
		}
	}

}
