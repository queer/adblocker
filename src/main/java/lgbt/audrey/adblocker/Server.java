package lgbt.audrey.adblocker;

import lgbt.audrey.adblocker.ServerListPing.StatusResponse;
import lombok.Getter;

import java.net.InetSocketAddress;

/**
 * Not actually mine. Used for reading information about a remote MC server.
 *
 * @author unknown
 * @since 8/30/15.
 */
class Server {
    @Getter
    private final String ip;
    private final int port;
    private String data;
    @Getter
    private StatusResponse lastResponse;
    
    Server(final String ip, final String port) {
        this.ip = ip;
        this.port = Integer.parseInt(port);
    }
    
    boolean isOnline() {
        try {
            final ServerListPing ping = new ServerListPing();
            final InetSocketAddress address = new InetSocketAddress(ip, port);
            ping.setAddress(address);
            ping.setTimeout(500);
            final StatusResponse response = ping.fetchData();
            if(response == null) {
                return false;
            }
            lastResponse = response;
            data = String.format("Ver: %s, Players: %s/%s", response.getVersion().getName(), response.getPlayers().getOnline(), response.getPlayers().getMax());
        } catch(final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @SuppressWarnings("unused")
    public String getData() {
        return data;
    }
}

