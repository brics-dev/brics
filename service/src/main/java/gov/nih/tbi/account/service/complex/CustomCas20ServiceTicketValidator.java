
package gov.nih.tbi.account.service.complex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.XmlUtils;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;

/**
 * Implementation of the TicketValidator that will validate Service Tickets in compliance with the CAS 2.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class CustomCas20ServiceTicketValidator extends Cas20ServiceTicketValidator
{

    /** The CAS 2.0 protocol proxy callback url. */
    private String proxyCallbackUrl;

    /** The storage location of the proxy granting tickets. */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    /** Implementation of the proxy retriever. */
    private ProxyRetriever proxyRetriever;

    private String casServerUrlPrefix;

    /**
     * Constructs an instance of the CAS 2.0 Service Ticket Validator with the supplied CAS server url prefix.
     * 
     * @param casServerUrlPrefix
     *            the CAS Server URL prefix.
     */
    public CustomCas20ServiceTicketValidator(final String casServerUrlPrefix)
    {

        super(casServerUrlPrefix);
        this.casServerUrlPrefix = casServerUrlPrefix;
        this.proxyRetriever = new Cas20ProxyRetriever(casServerUrlPrefix, "UTF-8");
    }
    
    public String getCustomCasServerUrlPrefix() {
    	return this.casServerUrlPrefix;
    }


    protected String getUrlSuffix()
    {

        return "serviceValidate";
    }

    protected Map extractCustomAttributes(final String xml)
    {

        final int pos1 = xml.indexOf("<cas:attributes>");
        final int pos2 = xml.indexOf("</cas:attributes>");

        if (pos1 == -1)
        {
            return Collections.EMPTY_MAP;
        }

        final String attributesText = xml.substring(pos1 + 16, pos2);

        final Map attributes = new HashMap();
        final BufferedReader br = new BufferedReader(new StringReader(attributesText));

        String line;
        final List attributeNames = new ArrayList();
        try
        {
            while ((line = br.readLine()) != null)
            {
                final String trimmedLine = line.trim();
                if (trimmedLine.length() > 0)
                {
                    final int leftPos = trimmedLine.indexOf(":");
                    final int rightPos = trimmedLine.indexOf(">");
                    attributeNames.add(trimmedLine.substring(leftPos + 1, rightPos));
                }
            }
            br.close();
        }
        catch (final IOException e)
        {
            // ignore
        }

        for (final Iterator iter = attributeNames.iterator(); iter.hasNext();)
        {
            final String name = (String) iter.next();
            attributes.put(name, XmlUtils.getTextForElement(xml, name));
        }

        return attributes;
    }
}
