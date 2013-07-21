package ch.spacebase.openclassic.client.network.handler;

import com.mojang.minecraft.gui.ErrorScreen;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerJoinEvent;
import ch.spacebase.openclassic.api.event.player.PlayerLoginEvent;
import ch.spacebase.openclassic.api.event.player.PlayerLoginEvent.Result;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class IdentificationMessageHandler extends MessageHandler<IdentificationMessage> {

	@Override
	public void handle(ClassicSession session, Player player, IdentificationMessage message) {
		if(session.getState() == State.IDENTIFYING) {
			PlayerLoginEvent event = EventFactory.callEvent(new PlayerLoginEvent(OpenClassic.getClient().getPlayer(), session.getAddress()));
			if(event.getResult() != Result.ALLOWED) {
				GeneralUtils.getMinecraft().stopGame(false);
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("disconnect.plugin-disallow"), event.getKickMessage()));
			}
		}

		OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
		OpenClassic.getClient().getProgressBar().setSubtitle(message.getUsernameOrServerName());
		OpenClassic.getClient().getProgressBar().setText(message.getVerificationKeyOrMotd());
		GeneralUtils.getMinecraft().player.userType = message.getOpOrCustomClient();

		if(message.getUsernameOrServerName().indexOf("+hax") > -1 || message.getVerificationKeyOrMotd().indexOf("+hax") > -1) {
			GeneralUtils.getMinecraft().hacks = true;
		} else {
			GeneralUtils.getMinecraft().hacks = false;
		}

		if(GeneralUtils.getMinecraft().player.userType == Constants.OP && (message.getUsernameOrServerName().indexOf("+ophax") > -1 || message.getVerificationKeyOrMotd().indexOf("+ophax") > -1)) {
			GeneralUtils.getMinecraft().hacks = true;
		}

		if(message.getUsernameOrServerName().indexOf("+ctf") > -1 || message.getVerificationKeyOrMotd().indexOf("+ctf") > -1) {
			GeneralUtils.getMinecraft().ctf = true;
		}

		if(session.getState() == State.IDENTIFYING) {
			EventFactory.callEvent(new PlayerJoinEvent(OpenClassic.getClient().getPlayer(), "Joined"));
		}

		session.setState(State.PREPARING);
	}

}
