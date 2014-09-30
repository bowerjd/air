package com.lonelystorm.aem.air.asset.services.impl;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lonelystorm.aem.air.asset.util.LibraryConstants;

/**
 * Registers the namespace and node types required for the asset library functionality.
 */
@Component
public class LibraryNodeTypeImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNodeTypeImpl.class);

    private static final String NAMESPACE_URI = "http://opensource.lonelystorm.com/aem";

    @Reference
    private SlingRepository repository;

    @Activate
    protected void activate(ComponentContext context) {
        Session session = null;

        try {
            session = repository.loginAdministrative(null);

            final Workspace workspace = session.getWorkspace();
            registerNamespace(workspace.getNamespaceRegistry());
            registerLibraryTemplate(workspace.getNodeTypeManager());
            registerLibraryThemeTemplate(workspace.getNodeTypeManager());
        } catch (RepositoryException e) {
                LOGGER.error("Unable to setup the repository namespace and node types", e);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    /**
     * Register the namespace with the repository.
     *
     * @param registry
     * @throws RepositoryException
     */
    private void registerNamespace(NamespaceRegistry registry) throws RepositoryException {
        registry.registerNamespace("ls", NAMESPACE_URI);
    }

    /**
     * Register the library node template.
     *
     * @param manager
     * @throws RepositoryException
     */
    @SuppressWarnings("unchecked")
    private void registerLibraryTemplate(NodeTypeManager manager) throws RepositoryException {
        NodeTypeTemplate nt = manager.createNodeTypeTemplate();
        nt.setName(LibraryConstants.ASSET_TYPE_NAME);
        nt.setAbstract(false);
        nt.setQueryable(true);
        nt.setDeclaredSuperTypeNames(new String[] { "sling:Folder" });

        PropertyDefinitionTemplate property = manager.createPropertyDefinitionTemplate();
        property.setName("categories");
        property.setMultiple(true);
        nt.getPropertyDefinitionTemplates().add(property);

        manager.registerNodeType(nt, true);
    }

    /**
     * Register the library theme node template
     *
     * @param manager
     * @throws RepositoryException
     */
    @SuppressWarnings("unchecked")
    private void registerLibraryThemeTemplate(NodeTypeManager manager) throws RepositoryException {
        NodeTypeTemplate nt = manager.createNodeTypeTemplate();
        nt.setName(LibraryConstants.ASSET_THEME_TYPE_NAME);
        nt.setAbstract(false);
        nt.setQueryable(true);
        nt.setDeclaredSuperTypeNames(new String[] { "sling:Folder" });

        PropertyDefinitionTemplate property = manager.createPropertyDefinitionTemplate();
        property.setName("theme");
        property.setMultiple(true);
        nt.getPropertyDefinitionTemplates().add(property);

        manager.registerNodeType(nt, true);
    }

}
