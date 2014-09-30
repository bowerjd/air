package org.apache.sling.models.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.impl.injectors.SelfInjector;
import org.apache.sling.models.impl.injectors.ValueMapInjector;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

public class MockAdapter {

    public static <T> T create(Resource resource, Class<T> cls) {
        ComponentContext componentContext = mock(ComponentContext.class);
        BundleContext bundleContext = mock(BundleContext.class);

        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        when(componentContext.getProperties()).thenReturn(new Hashtable<String, Object>());

        ModelAdapterFactory factory = new ModelAdapterFactory();
        factory.activate(componentContext);

        factory.bindInjector(new SelfInjector(), new ServicePropertiesMap(1, 1));
        factory.bindInjector(new ValueMapInjector(), new ServicePropertiesMap(2, 2));

        return factory.getAdapter(resource, cls);
    }

}
