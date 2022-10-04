
package com.anf.core.servlets;

import com.anf.core.services.ContentService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/**
 * @author Sikha Vamsi
 * UserServlet for saving form data to aem node
 */
@Component(service = Servlet.class, property = { Constants.SERVICE_DESCRIPTION + "= Save User data",
		"sling.servlet.methods=" + HttpConstants.METHOD_POST, "sling.servlet.paths=" + "/bin/saveUserDetails" })
public class UserServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServlet.class);
	@Reference
	transient ContentService contentService;
	/**
	 * Resource Resolver Factory
	 */
	@Reference
	transient ResourceResolverFactory resolverFactory;

	/**
	 * Resource Resolver
	 */
	transient ResourceResolver resolver;
	private static final String SUBSERVICE_NAME = "eventingService";
    transient Session session;

	@Override
	protected void doPost(final SlingHttpServletRequest req, final SlingHttpServletResponse resp)
			throws ServletException, IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);
		try {
			resolver = resolverFactory.getServiceResourceResolver(params);
			session = resolver.adaptTo(Session.class);
			Node jcrContent;
			if (!session.nodeExists("/var/anf-code-challenge")) {
				jcrContent = resolver.getResource("/var").adaptTo(Node.class);
				jcrContent.addNode("anf-code-challenge", "nt:unstructured");

			}
			jcrContent = resolver.getResource("/var/anf-code-challenge").adaptTo(Node.class);
			Node ageNode = session.getNode("/etc/age");
			int minAge = Integer.parseInt(ageNode.getProperty("minAge").getString());
			int maxAge = Integer.parseInt(ageNode.getProperty("maxAge").getString());
			int age = Integer.parseInt(req.getRequestParameter("age").toString());
			if (age <= maxAge && age >= minAge) {
				Random rand = SecureRandom.getInstanceStrong();
				Node nextbuttonNode = jcrContent.addNode(String.valueOf(rand.nextInt(1000000)), "nt:unstructured");
				nextbuttonNode.setProperty("firstName", req.getRequestParameter("firstName").toString());
				nextbuttonNode.setProperty("lastName", req.getRequestParameter("lastName").toString());
				nextbuttonNode.setProperty("age", req.getRequestParameter("age").toString());
				session.save();
				session.logout();
				resp.setStatus(200);
			}
			else {
				resp.setStatus(404);
			}
		} catch (LoginException | RepositoryException | NoSuchAlgorithmException e) {
			LOGGER.error("UserServlet doPost Exception occurred {}", e.getMessage());
		}
	}

}
