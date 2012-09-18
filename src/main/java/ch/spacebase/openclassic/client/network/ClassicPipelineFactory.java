package ch.spacebase.openclassic.client.network;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.StaticChannelPipeline;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import ch.spacebase.openclassic.game.network.ClassicDecoder;
import ch.spacebase.openclassic.game.network.ClassicEncoder;

public class ClassicPipelineFactory implements ChannelPipelineFactory {

	private Timer timer = new HashedWheelTimer();
	private ClientSession session;
	
	public ClassicPipelineFactory(ClientSession session) {
		this.session = session;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return new StaticChannelPipeline(
			new ClassicDecoder(),
			new ClassicEncoder(),
			new ReadTimeoutHandler(this.timer, 40),
			new ClassicHandler(this.session)
		);
	}

}
