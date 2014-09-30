package com.lonelystorm.aem.air.asset.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.aem.air.asset.util.EscalatedResolver.Session;

@RunWith(MockitoJUnitRunner.class)
public class EscalatedResolverTest {

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Session<String> session;

    private EscalatedResolver escalated;

    @Before
    public void setUp() {
        escalated = new EscalatedResolver(resourceResolverFactory, getClass());
    }

    @Test
    public void run() throws Exception {
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);
        when(session.run(any(ResourceResolver.class))).thenReturn("Test");

        String result = escalated.doSession(session);

        assertEquals("Test", result);
        verify(session, times(1)).run(resourceResolver);
        verify(resourceResolver, times(1)).close();
    }

    @Test
    public void runReturnNull() throws Exception {
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);
        when(session.run(any(ResourceResolver.class))).thenReturn(null);

        String result = escalated.doSession(session);

        assertEquals(null, result);
        verify(session, times(1)).run(resourceResolver);
        verify(resourceResolver, times(1)).close();
    }

    @Test
    public void runThrowsException() throws Exception {
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenThrow(LoginException.class);

        String result = escalated.doSession(session);

        assertEquals(null, result);
        verify(session, never()).run(resourceResolver);
        verify(resourceResolver, never()).close();
    }

}
