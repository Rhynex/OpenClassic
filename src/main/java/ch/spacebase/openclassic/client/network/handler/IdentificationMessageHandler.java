package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.player.PlayerJoinEvent;
import ch.spacebase.openclassic.api.event.player.PlayerLoginEvent;
import ch.spacebase.openclassic.api.event.player.PlayerLoginEvent.Result;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.gui.ErrorScreen;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.ClassicSession.State;
import ch.spacebase.openclassic.game.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.game.network.MessageHandler;
import ch.spacebase.openclassic.game.util.InternalConstants;

import com.zachsthings.onevent.EventManager;

public class IdentificationMessageHandler extends MessageHandler<IdentificationMessage> {

	@Override
	public void handle(ClassicSession session, Player player, IdentificationMessage message) {
		if(session.getState() == State.IDENTIFYING) {
			PlayerLoginEvent event = EventManager.callEvent(new PlayerLoginEvent(OpenClassic.getClient().getPlayer(), session.getAddress()));
			if(event.getResult() != Result.ALLOWED) {
				OpenClassic.getClient().exitGameSession();
				OpenClassic.getClient().setActiveComponent(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("disconnect.plugin-disallow"), event.getKickMessage()));
				return;
			}
		}
		
		if(message.getVerificationKeyOrMotd().contains("-hax")) {
			GeneralUtils.getMinecraft().hacks = false;
		}

		OpenClassic.getClient().getProgressBar().setVisible(true);
		OpenClassic.getClient().getProgressBar().setSubtitleScaled(false);
		OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.multiplayer"));
		OpenClassic.getClient().getProgressBar().setSubtitle(message.getUsernameOrServerName());
		OpenClassic.getClient().getProgressBar().setText(message.getVerificationKeyOrMotd());
		byte op = message.getOpOrCustomClient();
		if(op == InternalConstants.OP) {
			player.setCanBreakBedrock(true);	
		} else if(op == InternalConstants.NOT_OP) {
			player.setCanBreakBedrock(false);
		}
		
		if(session.getState() == State.IDENTIFYING) {
			EventManager.callEvent(new PlayerJoinEvent(OpenClassic.getClient().getPlayer(), "Joined"));
		}

		session.setState(State.PREPARING);
	}

}
