package com.bearnation.bridgebot;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.MsaAuthenticationService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.network.session.NetworkSession;

import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BridgeBot {

    public static void main(String[] args) {
        while (true) {
            try {
                runBot();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("[BridgeBot] Disconnected. Reconnecting in 5 seconds...");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {}
        }
    }

    private static void runBot() throws Exception {
        JsonObject config;
        try (FileReader reader = new FileReader("config.json")) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
        }
        String refreshToken = config.get("refreshToken").getAsString();

        MsaAuthenticationService authService = new MsaAuthenticationService("BridgeBot");
        authService.setRefreshToken(refreshToken);

        try {
            authService.login();
        } catch (RequestException e) {
            throw new RuntimeException("Failed to refresh Microsoft login: " + e.getMessage(), e);
        }

        GameProfile profile = authService.getSelectedProfile();
        String accessToken = authService.getAccessToken();
        System.out.println("[BridgeBot] Logged in as " + profile.getName());

        UUID uuid = profile.getId();
        String username = profile.getName();
        GameProfile authData = new GameProfile("")

        MinecraftProtocol protocol = new MinecraftProtocol(authData);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        ClientNetworkSession session = new ClientNetworkSession(
                new InetSocketAddress("play.mcbearnation.net", 25565),
                protocol,
                executor,
                (SocketAddress) null,
                (ProxyInfo) null
        );

        session.addListener(new SessionAdapter() {
            public void packetReceived(NetworkSession sess, Object packet) {
                if (packet instanceof ClientboundLoginPacket) {
                    System.out.println("[BridgeBot] Logged in, sending /server ctf");
                    sess.send(new ServerboundChatCommandPacket("server ctf"));
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                System.out.println("[BridgeBot] Disconnected: " + event.getReason());
                if (event.getCause() != null) {
                    event.getCause().printStackTrace();
                }
                executor.shutdown();
            }
        });

        System.out.println("[BridgeBot] Connecting to proxy...");
        session.connect(true);
    }
}
