package scw.mvc.support;

import scw.mvc.Action;
import scw.mvc.ActionFactory;
import scw.mvc.Channel;
import scw.mvc.http.HttpChannel;

public abstract class HttpActionFactory implements ActionFactory {

	public Action getAction(Channel channel) {
		if (channel instanceof HttpChannel) {
			return getAction((HttpChannel) channel);
		}
		return null;
	}

	protected abstract Action getAction(HttpChannel httpChannel);

	public abstract void scanning(HttpAction httpAction, HttpControllerConfig httpControllerConfig);
}