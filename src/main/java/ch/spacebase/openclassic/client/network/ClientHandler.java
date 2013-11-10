package ch.spacebase.openclassic.client.network;

import java.util.logging.Level;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutException;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.client.gui.ErrorScreen;

public class ClientHandler extends SimpleChannelUpstreamHandler {

	private ClientSession session;

	public ClientHandler(ClientSession session) {
		this.session = session;
	}

	@Override
	public void channelConnected(ChannelHandlerContext context, ChannelStateEvent event) {
		Channel channel = event.getChannel();
		context.setAttachment(this.session);

		OpenClassic.getLogger().info("Channel connected: " + channel + ".");
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext context, ChannelStateEvent event) {
		Channel channel = event.getChannel();
		GuiScreen screen = OpenClassic.getClient().getCurrentScreen();
		if(!this.session.isDisconnected()) {
			if(screen instanceof ErrorScreen) {
				OpenClassic.getClient().setCurrentScreen(screen);
			} else {
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen("Disconnected!", "You lost connection!"));
			}
		}

		OpenClassic.getClient().getProgressBar().setVisible(false);
		OpenClassic.getLogger().info("Channel disconnected: " + channel + ".");
	}

	@Override
	public void messageReceived(ChannelHandlerContext context, MessageEvent event) {
		ClientSession session = (ClientSession) context.getAttachment();
		session.messageReceived((Message) event.getMessage());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event) {
		Channel channel = event.getChannel();
		if(event.getCause() instanceof ReadTimeoutException) {
			if(!this.session.isDisconnected()) {
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen("Disconnected!", "Connection timed out."));
			}
		} else if(channel.isOpen()) {
			if(!(event.getCause().getMessage() != null && (event.getCause().getMessage().equals("Connection reset by peer") || event.getCause().getMessage().equals("Connection timed out")))) {
				OpenClassic.getLogger().log(Level.WARNING, "Exception caught, closing channel: " + channel + "...", event.getCause());
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen("Disconnected!", event.getCause().getMessage()));
			}

			channel.close();
		}
	}

}
