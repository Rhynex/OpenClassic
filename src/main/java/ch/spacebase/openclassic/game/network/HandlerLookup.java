package ch.spacebase.openclassic.game.network;

import java.util.HashMap;
import java.util.Map;

import ch.spacebase.openclassic.api.network.msg.Message;

public abstract class HandlerLookup {

	private Map<Class<? extends Message>, MessageHandler<?>> handlers = new HashMap<Class<? extends Message>, MessageHandler<?>>();

	protected <T extends Message> void bind(Class<T> clazz, Class<? extends MessageHandler<T>> handlerClass) throws InstantiationException, IllegalAccessException {
		MessageHandler<T> handler = handlerClass.newInstance();
		this.handlers.put(clazz, handler);
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> MessageHandler<T> find(Class<T> clazz) {
		return (MessageHandler<T>) this.handlers.get(clazz);
	}

}
