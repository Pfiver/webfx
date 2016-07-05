package naga.core.spi.platform.vertx;

import io.vertx.core.Vertx;
import naga.core.datasource.ConnectionDetails;
import naga.core.services.update.UpdateService;
import naga.core.services.update.RemoteUpdateService;

/**
 * @author Bruno Salmon
 */
class VertxUpdateService extends RemoteUpdateService {

    private final Vertx vertx;

    public VertxUpdateService(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected UpdateService createConnectedUpdateService(ConnectionDetails connectionDetails) {
        return new VertxConnectedService(vertx, connectionDetails);
    }

}
