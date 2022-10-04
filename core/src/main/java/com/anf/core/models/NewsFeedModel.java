package com.anf.core.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anf.core.bean.NewsFeedBean;


/**
 * @author Sikha Vamsi
 * NewsFeedModel for fetching the news feed stored in node
 */
@Model(adaptables = { SlingHttpServletRequest.class,
		Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class NewsFeedModel {

	private List<NewsFeedBean> listNewsFeedBean;
	/** The resource resolver. */
	@SlingObject
	private ResourceResolver resourceResolver;
	@Reference
	private Session session;
	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(NewsFeedModel.class);
	private static final String PATH="/var/commerce/products/anf-code-challenge/newsData";
	private static final String AUTHOR="author";
	private static final String CONTENT="content";
	private static final String DESCRIPTION="description";
	private static final String TITLE="title";
	private static final String URL="url";
	private static final String URL_IMAGE="urlImage";
	private static final String DATE_FORMAT="dd.MM.yyyy";
	public List<NewsFeedBean> getListNewsFeedBean() {
		try {
			Node node = resourceResolver.getResource(PATH).adaptTo(Node.class);
			java.util.Iterator<javax.jcr.Node> levelOnechildren = node.getNodes();
			listNewsFeedBean=new ArrayList<>();
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			Date date = new Date();
			while (levelOnechildren.hasNext()) {
				Node path = levelOnechildren.next();
				NewsFeedBean newsFeedBean = new NewsFeedBean();
				newsFeedBean.setAuthor(path.getProperty(AUTHOR).getString());
				newsFeedBean.setContent(path.getProperty(CONTENT).getString());
				newsFeedBean.setDescription(path.getProperty(DESCRIPTION).getString());
				newsFeedBean.setTitle(path.getProperty(TITLE).getString());
				newsFeedBean.setUrl(path.getProperty(URL).getString());
				newsFeedBean.setUrlImage(path.getProperty(URL_IMAGE).getString());
				newsFeedBean.setFeedDate(dateFormat.format(date));
				listNewsFeedBean.add(newsFeedBean);
			}
		} catch (RepositoryException e) {
			log.error("NewsFeedModel getListNewsFeedBean Exception occurred {} ", e.getMessage());
		}
		return listNewsFeedBean;
	}

}
