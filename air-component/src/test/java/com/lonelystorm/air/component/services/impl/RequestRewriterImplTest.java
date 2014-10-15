package com.lonelystorm.air.component.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.resourceresolver.MockHelper;
import org.apache.sling.testing.resourceresolver.MockResourceResolverFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.component.ComponentContext;

import com.day.cq.rewriter.linkchecker.Link;
import com.day.cq.rewriter.linkchecker.LinkCheckerSettings;
import com.lonelystorm.air.component.services.impl.RequestRewriterImpl;

@RunWith(MockitoJUnitRunner.class)
public class RequestRewriterImplTest {

    @Mock
    private Link link;

    @Mock
    private LinkCheckerSettings linkCheckerSettings;

    @Mock
    private ComponentContext componentContext;

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @InjectMocks
    private RequestRewriterImpl requestRewriterImpl;

    private ResourceResolver resolver;

    @Before
    public void setUp() throws Exception {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("requestrewriter.paths", new String[] { "/content/aem-air/" });
        when(componentContext.getProperties()).thenReturn(properties);

        requestRewriterImpl.activate(componentContext);

        ResourceResolverFactory factory = new MockResourceResolverFactory();
        resolver = spy(factory.getResourceResolver(null));

        MockHelper.create(resolver)
            .resource("/content")
            .resource("/content/aem-air")
            .resource("/content/aem-air/page")
            .resource("/content/aem-air2")
            .resource("/content/aem-air2/page")
        .commit();
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resolver);
    }

    @Test
    public void rewrite() {
        assertNull(requestRewriterImpl.rewrite(null, null, null));
    }

    @Test
    public void rewriteLink() {
        when(link.getHref()).thenReturn("/content/aem-air/page");
        doReturn("/page").when(resolver).map("/content/aem-air/page");

        String result = requestRewriterImpl.rewriteLink(link, linkCheckerSettings);

        assertEquals("/page.html", result);
        verify(resolver, times(1)).close();
    }

    @Test
    public void rewriteLinkNotMatchingPage() {
        when(link.getHref()).thenReturn("/content/aem-air2/page");

        String result = requestRewriterImpl.rewriteLink(link, linkCheckerSettings);

        assertNull(result);
        verify(resolver, times(1)).close();
    }

    @Test
    public void rewriteLinkNonExistingResource() {
        when(link.getHref()).thenReturn("/content/aem-air/nonexisting");
        doReturn("/page").when(resolver).map("/content/aem-air/nonexisting");

        String result = requestRewriterImpl.rewriteLink(link, linkCheckerSettings);

        assertNull(result);
        verify(resolver, times(1)).close();
    }

    @Test
    public void rewriteLinkWithExtension() {
        when(link.getHref()).thenReturn("/content/aem-air/page.html");

        String result = requestRewriterImpl.rewriteLink(link, linkCheckerSettings);

        assertNull(result);
        verify(resolver, never()).close();
    }

    @Test
    public void rewriteLinkThrowsLoginException() throws Exception {
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenThrow(new LoginException());

        when(link.getHref()).thenReturn("/content/aem-air/page");
        doReturn("/page").when(resolver).map("/content/aem-air/page");

        String result = requestRewriterImpl.rewriteLink(link, linkCheckerSettings);

        assertNull(result);
        verify(resolver, never()).close();
    }

}
