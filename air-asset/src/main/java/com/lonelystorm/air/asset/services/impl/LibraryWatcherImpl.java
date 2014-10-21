package com.lonelystorm.air.asset.services.impl;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.services.CacheManager;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.asset.util.LibraryConstants;

/**
 * Watches the repository to detect changes to the client library and invalidates the cache
 */
@Component(immediate = true)
@Service
public class LibraryWatcherImpl implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryWatcherImpl.class);

    @Reference(target = "(models.adapter.implementationClass=com.lonelystorm.air.asset.models.AssetLibrary)")
    private AdapterFactory libraryAdaptorFactory;

    @Reference(target = "(models.adapter.implementationClass=com.lonelystorm.air.asset.models.AssetTheme)")
    private AdapterFactory themeAdaptorFactory;

    @Reference
    private SlingRepository repository;

    @Reference
    private LibraryResolver libraryResolver;

    @Reference
    private CacheManager cacheManager;

    private Session session = null;

    @Activate
    public void activate(ComponentContext context) {
        try {
            session = repository.loginAdministrative(null);

            ObservationManager manager = session.getWorkspace().getObservationManager();
            manager.addEventListener(this, Event.NODE_ADDED | Event.NODE_MOVED | Event.NODE_REMOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED, "/", true, null, null, true);
            loadExistingLibraries();
        } catch (RepositoryException e) {
            LOGGER.error("Error installing watcher for {} nodes.", LibraryConstants.ASSET_TYPE_NAME, e);
        }
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        if (session != null) {
            try {
                ObservationManager manager = session.getWorkspace().getObservationManager();
                manager.removeEventListener(this);
            } catch (RepositoryException e) {
                LOGGER.error("Error removing watcher for {} nodes.", LibraryConstants.ASSET_TYPE_NAME, e);
            } finally {
                if (session != null) {
                    session.logout();
                }
            }
        }
    }

    // TODO: Ensure exception needs to be bubbled up
    /**
     * Finds existing asset libraries.
     *
     * @throws RepositoryException
     */
    private void loadExistingLibraries() throws RepositoryException {
        String queryString = String.format("/jcr:root//element(*, %s)", LibraryConstants.ASSET_TYPE_NAME);

        Query query = session.getWorkspace().getQueryManager().createQuery(queryString, "xpath");
        QueryResult result = query.execute();

        NodeIterator iterator = result.getNodes();
        libraryResolver.clear();
        cacheManager.clear();
        while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            libraryResolver.load(node.getPath());
        }
    }

    /**
     * Watches for changes to the repository relating to asset libraries.
     */
    @Override
    public void onEvent(EventIterator it) {
        Set<String> added = new HashSet<>();
        Set<String> changed = new HashSet<>();
        Set<String> removed = new HashSet<>();

        while (it.hasNext()) {
            Event event = it.nextEvent();

            try {
                int type = event.getType();
                String path = event.getPath();

                // Remove property name from the path
                if (type == Event.PROPERTY_ADDED || type == Event.PROPERTY_REMOVED || type == Event.PROPERTY_CHANGED) {
                    path = Text.getRelativeParent(path, 1);
                }

                // Remove jcr:content from the path
                if (path.endsWith(JcrConstants.JCR_CONTENT)) {
                    path = Text.getRelativeParent(path, 1);
                }

                if (type == Event.NODE_ADDED) {
                    added.add(path);
                } else if (type == Event.NODE_REMOVED) {
                    removed.add(path);
                    return;
                } else {
                    changed.add(path);
                }
            } catch (RepositoryException e) {
                // TODO: Improve error handling
                LOGGER.error("Error processing jcr repository event", e);
            }
        }

        for (String path : removed) {
            Asset asset = getAssetFromPath(path);
            if (asset != null) {
                try {
                    loadExistingLibraries();
                } catch (RepositoryException e) {
                    LOGGER.error("Unable to reload asset libraries ({})", path, e);
                }
                break;
            }
        }

        for (String path : added) {
            try {
                Node node = session.getNode(path);

                while (node != null && node.getDepth() > 0 && !node.isNodeType(LibraryConstants.ASSET_TYPE_NAME)) {
                    node = node.getParent();
                }

                if (node != null && node.isNodeType(LibraryConstants.ASSET_TYPE_NAME)) {
                    libraryResolver.load(node.getPath());
                }
            } catch (RepositoryException e) {
                LOGGER.error("Unable to find asset library ({})", path, e);
            }
        }

        // TODO: Enable caching of the asset library (instead of always generating at runtime)
        Set<String> all = new HashSet<>();
        all.addAll(changed);
        all.addAll(added);
        for (String path : all) {
            Asset asset = getAssetFromPath(path);
            if (asset != null) {
                cacheManager.clear();
                break;
            }
        }
    }

    private Asset getAssetFromPath(String path) {
        Asset asset = null;

        try {
            Node node = session.getNode(path);

            while (node != null && node.getDepth() > 0 && !node.isNodeType(LibraryConstants.ASSET_TYPE_NAME)) {
                node = node.getParent();
            }

            if (node != null && node.isNodeType(LibraryConstants.ASSET_TYPE_NAME)) {
                asset = libraryResolver.findLibraryByPath(node.getPath());
            }
        } catch (RepositoryException e) {
            LOGGER.error("Unable to find asset library ({})", path, e);
        }

        return asset;
    }

}
