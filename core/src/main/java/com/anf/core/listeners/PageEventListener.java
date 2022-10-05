package com.anf.core.listeners;

import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sikha Vamsi
 * 
 *         Event Listener that listens to JCR events
 */
@Component(service = EventListener.class, immediate = true)
public class PageEventListener implements EventListener {
	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(PageEventListener.class);

	/**
	 * Resource Resolver Factory
	 */
	@Reference
	private ResourceResolverFactory resolverFactory;

	/**
	 * Resource Resolver
	 */
	private ResourceResolver resolver;

	@Reference
	private SlingRepository repository;

	/**
	 * Session object
	 */
	private Session session;

	/**
	 * Constants
	 */
	private static final String JCR_CONTENT="jcr:content";
	
	private static final String PAGE_CREATED="pageCreated";
	
	private static final String CQ_PAGE="cq:Page";
	
	private static final String PAGE_PATH="/content/anf-code-challenge/us/en";
	
	private static final String SUBSERVICE_NAME="anfuser";
	
	/**
	 * Activate method to initialize stuff
	 */
	@Activate
	protected void activate(ComponentContext componentContext) {

		try {
			Map<String, Object> params = new HashMap<>();
			params.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);
			resolver = resolverFactory.getServiceResourceResolver(params);
			session = resolver.adaptTo(Session.class);
			session.getWorkspace().getObservationManager().addEventListener(this, Event.NODE_ADDED,PAGE_PATH, true, null, new String[] { CQ_PAGE }, false);
		} catch (Exception e) {
			log.error("PageEventListener activate Exception occurred {}", e.getMessage());
		}
	}

	@Deactivate
	protected void deactivate() {
		if (session != null) {
			session.logout();
		}
	}

	@Override
	public void onEvent(EventIterator events) {
		try {
			while (events.hasNext()) {
				String path = events.nextEvent().getPath();
				if (path.contains(JCR_CONTENT)) {
					Node node = session.getNode(path);
					node.setProperty(PAGE_CREATED, Boolean.TRUE);
					session.save();
				}
			}
		} catch (Exception e) {
			log.error("PageEventListener onEvent Exception occurred {} ", e.getMessage());
		}
	}

}
