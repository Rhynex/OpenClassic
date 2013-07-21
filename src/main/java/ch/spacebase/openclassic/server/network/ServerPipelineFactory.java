package ch.spacebase.openclassic.server.network;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import ch.spacebase.openclassic.game.network.ClassicDecoder;
import ch.spacebase.openclassic.game.network.ClassicEncoder;

public class ServerPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return Channels.pipeline(
			new ClassicDecoder(),
			new ClassicEncoder(),
			new ServerHandler()
		);
	}

}
