package com.lonelystorm.air.util;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Escalated Resolver.
 *
 * This class creates administrative resource resolvers and automatically closes
 * them quietly once the session has been finished with.
 *
 */
public class EscalatedResolver {

    private ResourceResolverFactory factory;

    private Logger logger;

    public EscalatedResolver(ResourceResolverFactory factory, Class<?> callee) {
        this.factory = factory;
        logger = LoggerFactory.getLogger(callee);
    }

    private void closeQuietly(ResourceResolver resolver) {
        if (resolver != null) {
            resolver.close();
        }
    }

    public <T> T doSession(Session<T> session) {
        ResourceResolver resolver = null;
        T result = null;

        try {
            resolver = factory.getAdministrativeResourceResolver(null);
            result = session.run(resolver);
        } catch (LoginException e) {
            logger.error("Unable to create administrative resource resolver.", e);
        } finally {
            closeQuietly(resolver);
        }

        return result;
    }

    public interface Session<T> {

        public T run(ResourceResolver resolver);

    }

}
