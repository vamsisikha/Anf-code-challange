package com.anf.core.servlets;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

/**
 * @author Sikha Vamsi
 * AnfQueryBuilderServlet for fetching the pages 
 */
@Component(immediate = true, service = Servlet.class, property = { "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=" + "/bin/queryBuilder" })
public class AnfQueryBuilderServlet extends SlingAllMethodsServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6602840344355720741L;

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(AnfQueryBuilderServlet.class);
	
	/**
	 * Injecting the QueryBuilder dependency
	 */
	@Reference
	private transient QueryBuilder builder;	
	/**
	 * Session object
	 */
	private transient Session session;
	/**
	 * Constants
	 */
	private static final String PATH="path";
	private static final String PAGE_PATH="/content/anf-code-challenge/us/en";
	private static final String CQ_PAGE="cq:page";
	private static final String TYPE="type";
	private static final String PROPERTY="1_property";
	private static final String JCR_CONTENT="jcr:content/anfCodeChallenge";
	private static final String PROPERTY_VALUE="1_property.value";
	private static final String JCR_CREATED="@jcr:created";
	private static final String ORDER_BY="orderby";
	private static final String P_LIMIT="p.limit";
	private static final String TEN="10";
	private static final String TRUE="true";
	/**
	 * Overridden doGet() method which executes on HTTP GET request
	 */
	@Override
	protected void doGet(final SlingHttpServletRequest request,final SlingHttpServletResponse response) {
		try {
			 ResourceResolver resourceResolver = request.getResourceResolver();
			session = resourceResolver.adaptTo(Session.class);
			Map<String, String> predicate = new HashMap<>();
			predicate.put(PATH, PAGE_PATH);
			predicate.put(CQ_PAGE, TYPE);
			predicate.put(PROPERTY, JCR_CONTENT);
			predicate.put(PROPERTY_VALUE, TRUE);
			predicate.put(ORDER_BY, JCR_CREATED);
			predicate.put(P_LIMIT, TEN);
			/**
			 * Creating the Query instance
			 */
			Query query = builder.createQuery(PredicateGroup.create(predicate), session);
			/**
			 * Getting the search results from Query Builder
			 */
			SearchResult searchResult = query.getResult();

			for(final Hit hit : searchResult.getHits()) {
				String path = hit.getPath();
				response.getWriter().println(path);
			}
			/**
			 * Getting the search results using JCR-SQL2
			 */
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			final String sql2Query= "SELECT * FROM [cq:Page] AS s WHERE ISDESCENDANTNODE([/content/anf-code-challenge/us/en])  and  s.[jcr:content/anfCodeChallenge] = true order by s.[jcr:created]";
			javax.jcr.query.Query sqlQuery = queryManager.createQuery(sql2Query, javax.jcr.query.Query.JCR_SQL2);
			sqlQuery.setLimit(10);
			QueryResult queryResult = sqlQuery.execute();
			NodeIterator iterator=queryResult.getNodes();
			response.getWriter().println("********************JCR-SQL2 result*****************************");
			while(iterator.hasNext()) {
				Node node=(Node) iterator.next();
				response.getWriter().println(node.getPath());
			}
		} catch (Exception e) {
			log.error("AnfQueryBuilderServlet doGet Exception occurred {} ", e.getMessage());
		} finally {
			
			if(session != null) {
				
				session.logout();
			}
		}
	}
}
