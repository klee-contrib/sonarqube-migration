package io.github.kbuntrock.dto.configuration;

/**
 * @author Kévin Buntrock
 */
public class MigrationConfiguration {

    private ServerConfiguration origin;
    private ServerConfiguration destination;

    public ServerConfiguration getOrigin() {
        return origin;
    }

    public void setOrigin(ServerConfiguration origin) {
        this.origin = origin;
    }

    public ServerConfiguration getDestination() {
        return destination;
    }

    public void setDestination(ServerConfiguration destination) {
        this.destination = destination;
    }
}
