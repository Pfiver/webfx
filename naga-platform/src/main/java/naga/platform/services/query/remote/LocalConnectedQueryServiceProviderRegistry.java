package naga.platform.services.query.remote;

import naga.platform.services.query.spi.QueryServiceProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public class LocalConnectedQueryServiceProviderRegistry {

    private static final Map</* dataSourceId */ Object, QueryServiceProvider> localConnectedProviders = new HashMap<>();

    public static void registerLocalConnectedProvider(Object dataSourceId, QueryServiceProvider localConnectedProvider) {
        localConnectedProviders.put(dataSourceId, localConnectedProvider);
    }

    public static QueryServiceProvider getLocalConnectedProvider(Object dataSourceId) {
        return localConnectedProviders.get(dataSourceId);
    }
}
