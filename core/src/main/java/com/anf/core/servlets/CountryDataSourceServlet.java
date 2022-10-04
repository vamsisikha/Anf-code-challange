package com.anf.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.Servlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static com.anf.core.constants.AppConstants.COUNTRY_LIST_PATH;
import static com.anf.core.constants.AppConstants.EQUALS;
import static com.anf.core.servlets.CountryDataSourceServlet.RESOURCE_TYPE;
import static com.anf.core.servlets.CountryDataSourceServlet.SERVICE_NAME;
/**
 * @author Sikha Vamsi
 * CountryDataSourceServlet for getting country json for dailogue
 */
@Component(service = Servlet.class, property = { Constants.SERVICE_ID + EQUALS + SERVICE_NAME,
		SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE })
public class CountryDataSourceServlet extends SlingSafeMethodsServlet {
	private static final long serialVersionUID = 6635403928775127450L;
	protected static final String SERVICE_NAME = "Country DataSource Servlet";
	protected static final String RESOURCE_TYPE = "/apps/anf/dropdowns";
	private static final Logger LOGGER = LoggerFactory.getLogger(CountryDataSourceServlet.class);

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
		try {
			ResourceResolver resourceResolver = request.getResourceResolver();
			Resource jsonResource = resourceResolver.getResource(COUNTRY_LIST_PATH);
			Asset asset = DamUtil.resolveToAsset(jsonResource);
			Rendition originalAsset = Objects.requireNonNull(asset).getOriginal();
			InputStream content = Objects.requireNonNull(originalAsset).adaptTo(InputStream.class);
			StringBuilder jsonContent = new StringBuilder();
			BufferedReader jsonReader = new BufferedReader(
					new InputStreamReader(Objects.requireNonNull(content), StandardCharsets.UTF_8));
			String line;
			while ((line = jsonReader.readLine()) != null) {
				jsonContent.append(line);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, String> data = objectMapper.readValue(jsonContent.toString(), Map.class);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			DataSource ds = new SimpleDataSource(new TransformIterator<>(data.keySet().iterator(), (Transformer) o -> {
				String dropValue = (String) o;
				ValueMap vm = new ValueMapDecorator(new HashMap<>());
				vm.put("text", dropValue);
				vm.put("value", data.get(dropValue));
				return new ValueMapResource(resourceResolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, vm);
			}));
			request.setAttribute(DataSource.class.getName(), ds);
		} catch (IOException e) {
			LOGGER.error("CountryDataSourceServlet exception occurred: {}", e.getMessage());
		}
	}

}
