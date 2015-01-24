package com.limpygnome.controllers.projects;

import com.limpygnome.jpa.ConnectionFactory;
import com.limpygnome.jpa.models.Project;
import com.limpygnome.jpa.providers.ProjectProvider;
import com.limpygnome.servlet.ExtendedHttpServlet;
import java.io.IOException;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * A filter for loading project models for statically loaded pages assumed to
 * be projects.
 * 
 * @author limpygnome
 */
@WebFilter(
    urlPatterns = {"/projects/*"},
    dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ERROR}
)
public class ProjectFilter implements Filter
{
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        EntityManager em = null;
        
        try
        {
            // Parse second part of URL
            HttpServletRequest req = (HttpServletRequest) request;
            String uri = req.getRequestURI();

            if(uri.length() > 1 && uri.startsWith("/"))
            {
                uri = uri.substring(1);
            }

            String[] segs = uri.split("/");

            // Attempt to load project model if the second part of the segment is available
            // i.e. /project/second segment used for loading model - of URI/URL
            if(segs.length >= 2)
            {
                // Fetch instance of em
                em = ConnectionFactory.getInstance().createMain();
                
                // Load project model
                ProjectProvider pp = new ProjectProvider(em);
                Project project = pp.projectFetchByUrl(segs[1]);

                if(project != null)
                {
                    // Put the model in the template data
                    HashMap<String, Object> templateData = ExtendedHttpServlet.retrieveOrSetupRequestTemplateData(request);
                    templateData.put("project", project);
                }
            }

            // We're done...
            chain.doFilter(request, response);
        }
        finally
        {
            if(em != null)
            {
                em.close();
            }
        }
        
    }
    
}