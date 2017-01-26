/**
 * Copyright 2016-eternity audrey
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package date.willnot.amy.adblocker;

import date.willnot.amy.adblocker.ServerListPing.StatusResponse;
import lombok.Getter;

import java.net.InetSocketAddress;

/**
 * Not actually mine. Used for reading information about a remote MC server.
 *
 * @author unknown
 * @since ???
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

