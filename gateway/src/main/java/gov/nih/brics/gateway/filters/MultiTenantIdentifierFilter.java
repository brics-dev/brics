package gov.nih.brics.gateway.filters;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import gov.nih.brics.gateway.configuration.BricsConfiguration;

@Component
public class MultiTenantIdentifierFilter extends ZuulFilter {
	
	@Autowired
	BricsConfiguration conf;
	
	public static final String TENANT_ID_HEADER = "X-TenantID";

	@Override
	public int filterOrder() {
		return 5 - 1; // run before PreDecoration
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRequest().getHeader(TENANT_ID_HEADER) == null;
		
	}
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		
		if (request.getHeader(TENANT_ID_HEADER) == null) {
			ctx.addZuulRequestHeader(TENANT_ID_HEADER, conf.getOrgName());
		}

        return null;
    }
}