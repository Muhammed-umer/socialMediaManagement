package com.monitor.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class NavigationInjectionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
            
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(res);

        chain.doFilter(request, responseWrapper);

        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.startsWith("text/html")) {
            String content = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
            
            String activeClass = req.getRequestURI().equals("/twitter") ? " active" : "";
            String twitterNav = "<a href=\"/twitter\" class=\"nav-item" + activeClass + "\">Twitter</a>";

            if (content.contains("</nav>") && !content.contains("href=\"/twitter\"")) {
                content = content.replace("</nav>", twitterNav + "\n</nav>");
                res.setContentLength(content.getBytes(responseWrapper.getCharacterEncoding()).length);
                res.getWriter().write(content);
                return; // Response is committed
            }
        }
        
        responseWrapper.copyBodyToResponse();
    }
}
