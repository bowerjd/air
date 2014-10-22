package org.apache.sling.models.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;

import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.impl.injectors.OSGiServiceInjector;
import org.apache.sling.models.impl.injectors.SelfInjector;
import org.apache.sling.models.impl.injectors.ValueMapInjector;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.services.LibraryAdapterManager;

public class MockAdapter {

    public static void setUp() throws Exception {
        ComponentContext componentContext = mock(ComponentContext.class);
        BundleContext bundleContext = mock(BundleContext.class);

        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        when(componentContext.getProperties()).thenReturn(new Hashtable<String, Object>());

        final ModelAdapterFactory factory = new ModelAdapterFactory();
        factory.activate(componentContext);

        factory.bindInjector(new SelfInjector(), new ServicePropertiesMap(1, 1));
        factory.bindInjector(new ValueMapInjector(), new ServicePropertiesMap(2, 2));

        ServiceReference librayReference = mock(ServiceReference.class);
        LibraryAdapterManager libraryAdapterManager = mock(LibraryAdapterManager.class);
        String libraryName = LibraryAdapterManager.class.getName();
        when(bundleContext.getServiceReferences(libraryName, null)).thenReturn(new ServiceReference[] { librayReference });
        when(bundleContext.getService(librayReference)).thenReturn(libraryAdapterManager);
        when(libraryAdapterManager.library(any(Resource.class))).thenAnswer(new Answer<AssetLibrary>() {
            @Override
            public AssetLibrary answer(InvocationOnMock invocation) throws Throwable {
                return ((Resource) invocation.getArguments()[0]).adaptTo(AssetLibrary.class);
            }
        });
        when(libraryAdapterManager.theme(any(Resource.class))).thenAnswer(new Answer<AssetTheme>() {
            @Override
            public AssetTheme answer(InvocationOnMock invocation) throws Throwable {
                return ((Resource) invocation.getArguments()[0]).adaptTo(AssetTheme.class);
            }
        });

        OSGiServiceInjector injectorFactory = new OSGiServiceInjector();
        injectorFactory.activate(componentContext);
        factory.bindInjector(injectorFactory, new ServicePropertiesMap(3, 3));

        SlingAdaptable.setAdapterManager(new AdapterManager() {
            @Override
            public <AdapterType> AdapterType getAdapter(Object resource, Class<AdapterType> cls) {
                return factory.getAdapter(resource, cls);
            }
        });
    }

}
