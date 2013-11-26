package ch.spacebase.openclassic.client.network.handler;

import com.mojang.minecraft.entity.player.net.NetworkPlayer;
import com.zachsthings.onevent.EventManager;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.event.player.PlayerChatEvent;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.network.msg.PlayerChatMessage;

public class PlayerChatMessageHandler extends MessageHandler<PlayerChatMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerChatMessage message) {
		if(message.getPlayerId() < 0) {
			player.sendMessage(Color.YELLOW + message.getMessage());
		} else {
			NetworkPlayer sender = GeneralUtils.getMinecraft().netPlayers.get(message.getPlayerId());
			if(sender != null) {
				PlayerChatEvent event = EventManager.callEvent(new PlayerChatEvent(sender.openclassic, message.getMessage()));
				if(event.isCancelled()) {
					return;
				}
			}
			
			player.sendMessage(message.getMessage());
		}
	}

}
