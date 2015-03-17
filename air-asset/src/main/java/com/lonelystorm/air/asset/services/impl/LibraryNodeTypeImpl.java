package com.lonelystorm.air.asset.services.impl;

import java.util.Arrays;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
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

import com.lonelystorm.air.asset.util.LibraryConstants;

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
            registerIndexes(session);
        } catch (RepositoryException e) {
                LOGGER.error("Unable to setup the repository namespace, node types and indexes", e);
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

    /**
     * Register the indexes for oak repositories.
     *
     * @param session
     * @throws RepositoryException
     */
    private void registerIndexes(Session session) throws RepositoryException {
        Node node = session.getNode("/oak:index/nodetype");
        if (node != null) {
            registerIndex(session, node, "declaringNodeTypes", LibraryConstants.ASSET_TYPE_NAME);
        }
    }

    /**
     * Register an index for oak repositories.
     *
     * @param session
     * @param node
     * @param name
     * @param object
     * @throws RepositoryException
     */
    private void registerIndex(Session session, Node node, String name, String object) throws RepositoryException {
        Property nodeTypes = node.getProperty(name);
        Value[] values = nodeTypes.getValues();
        boolean found = false;
        for (Value value : values) {
            if (value.getString().equals(object)) {
                found = true;
            }
        }
        if (!found) {
            ValueFactory factory = session.getValueFactory();
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = factory.createValue(object, PropertyType.NAME);
            node.setProperty(name, values);
            node.setProperty("reindex", true);
            session.save();
        }
    }

}
