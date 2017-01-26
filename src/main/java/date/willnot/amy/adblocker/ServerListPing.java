package date.willnot.amy.adblocker;

import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Not actually mine. Used for pinging a Minecraft server and reading the
 * response.
 *
 * NOTE: This class is NOT licensed under the same license as the rest of the
 * project. I don't know what license that zh32 has put this under, so use at
 * your own risk.
 *
 * <a href="https://gist.github.com/zh32/7190955">Source</a>
 *
 * @author zh32
 * @since ???
 *
 */
@SuppressWarnings("unused")
public class ServerListPing {
    private final Gson gson;
    private InetSocketAddress host;
    private int timeout;

    public ServerListPing() {
        timeout = 7000;
        gson = new Gson();
    }

    public InetSocketAddress getAddress() {
        return host;
    }

    public void setAddress(final InetSocketAddress host) {
        this.host = host;
    }

    int getTimeout() {
        return timeout;
    }

    void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public int readVarInt(final DataInput in) throws IOException {
        int i = 0;
        int j = 0;
        int k;
        do {
            k = in.readByte();
            i |= (k & 0x7F) << j * 7;
            j++;
            if(j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((k & 0x80) == 0x80);
        return i;
    }

    public void writeVarInt(final DataOutput out, int paramInt) throws IOException {
        while((paramInt & 0xFFFFFF80) != 0x0) {
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
        out.writeByte(paramInt);
    }

    public StatusResponse fetchData() throws IOException {
        try(Socket socket = new Socket()) {
            Bukkit.getLogger().info("Host: '" + host + '\'');
            socket.setSoTimeout(timeout);
            socket.connect(host, timeout);
            final OutputStream outputStream = socket.getOutputStream();
            final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            final InputStream inputStream = socket.getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final DataOutput handshake = new DataOutputStream(b);
            handshake.writeByte(0);
            writeVarInt(handshake, 4);
            writeVarInt(handshake, host.getHostString().length());
            handshake.writeBytes(host.getHostString());
            handshake.writeShort(host.getPort());
            writeVarInt(handshake, 1);
            writeVarInt(dataOutputStream, b.size());
            dataOutputStream.write(b.toByteArray());
            dataOutputStream.writeByte(1);
            dataOutputStream.writeByte(0);
            final DataInput dataInputStream = new DataInputStream(inputStream);
            final int size = readVarInt(dataInputStream);
            int id = readVarInt(dataInputStream);
            if(id == -1) {
                throw new IOException("Premature end of stream.");
            }
            if(id != 0) {
                throw new IOException("Invalid packetID");
            }
            final int length = readVarInt(dataInputStream);
            if(length == -1) {
                throw new IOException("Premature end of stream.");
            }
            if(length == 0) {
                throw new IOException("Invalid string length.");
            }
            final byte[] in = new byte[length];
            dataInputStream.readFully(in);
            final String json = new String(in);
            final long now = System.currentTimeMillis();
            dataOutputStream.writeByte(9);
            dataOutputStream.writeByte(1);
            dataOutputStream.writeLong(now);
            readVarInt(dataInputStream);
            id = readVarInt(dataInputStream);
            if(id == -1) {
                throw new IOException("Premature end of stream.");
            }
            if(id != 1) {
                throw new IOException("Invalid packetID");
            }
            final long pingtime = dataInputStream.readLong();
            final StatusResponse response = (StatusResponse) gson.fromJson(json, (Class) StatusResponse.class);
            response.setTime((int) (now - pingtime));
            dataOutputStream.close();
            outputStream.close();
            inputStreamReader.close();
            inputStream.close();
            socket.close();
            return response;
        } catch(final UnknownHostException e) {
            return null;
        }
    }

    public class StatusResponse {
        private String description;
        private Players players;
        private Version version;
        private String favicon;
        private int time;

        public String getDescription() {
            return description;
        }

        public Players getPlayers() {
            return players;
        }

        public Version getVersion() {
            return version;
        }

        public String getFavicon() {
            return favicon;
        }

        public int getTime() {
            return time;
        }

        public void setTime(final int time) {
            this.time = time;
        }
    }

    public class Players {
        private int max;
        private int online;
        private List<Player> sample;

        public int getMax() {
            return max;
        }

        public int getOnline() {
            return online;
        }

        public List<Player> getSample() {
            return sample;
        }
    }

    public class Player {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }

    public class Version {
        private String name;
        private String protocol;

        public String getName() {
            return name;
        }

        public String getProtocol() {
            return protocol;
        }
    }
}
