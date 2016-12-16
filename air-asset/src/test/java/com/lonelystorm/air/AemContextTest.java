package com.lonelystorm.air;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
import com.lonelystorm.air.asset.services.LibraryAdapterManager;

import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public abstract class AemContextTest {

    @Rule
    public final AemContext context = new AemContext();

    protected ResourceResolver resolver;

    protected Bindings bindings = new SimpleBindings();

    @Mock
    private LibraryAdapterManager mockLibAdapter;

    @Before
    public void setUp() throws Exception {
        // Resource resolver
        resolver = context.resourceResolver();

        // Sling Models setups
        context.addModelsForPackage(Asset.class.getPackage().getName());

        // Script Bindings setup
        bindings.put("pageManager", context.pageManager());
        bindings.put("request", context.request());
        bindings.put("response", context.response());
        bindings.put("sling", context.slingScriptHelper());

        when(mockLibAdapter.library(any(Resource.class))).thenAnswer(new Answer<AssetLibrary>() {
            @Override
            public AssetLibrary answer(InvocationOnMock invocation) throws Throwable {
                return ((Resource)invocation.getArguments()[0]).adaptTo(AssetLibrary.class);
            }
        });

        when(mockLibAdapter.theme(any(Resource.class))).thenAnswer(new Answer<AssetTheme>() {
            @Override
            public AssetTheme answer(InvocationOnMock invocation) throws Throwable {
                return ((Resource)invocation.getArguments()[0]).adaptTo(AssetTheme.class);
            }
        });

        when(mockLibAdapter.themeConfiguration(any(Resource.class))).thenAnswer(new Answer<AssetThemeConfiguration>() {
            @Override
            public AssetThemeConfiguration answer(InvocationOnMock invocation) throws Throwable {
                return ((Resource)invocation.getArguments()[0]).adaptTo(AssetThemeConfiguration.class);
            }
        });

        context.registerService(LibraryAdapterManager.class, mockLibAdapter);
    }

}
