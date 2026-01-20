package de.mecrytv.nexusBridge.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusBridge.NexusBridge;
import de.mecrytv.nexusBridge.models.TeleportModel;
import dev.httpmarco.polocloud.sdk.java.Polocloud;
import dev.httpmarco.polocloud.shared.player.PolocloudPlayer;
import dev.httpmarco.polocloud.shared.player.SharedPlayerProvider;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Optional;
import java.util.UUID;

public class ReportTeleportEvent {

    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("nexus:bridge");
    SharedPlayerProvider<PolocloudPlayer> playerProvider = (SharedPlayerProvider<PolocloudPlayer>) Polocloud.instance().playerProvider();

     @Subscribe
    public void onPluginMessage(PluginMessageEvent event){
         if (!event.getIdentifier().equals(IDENTIFIER)) return;

         DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

         try {
             String subChannel = in.readUTF();
             if (subChannel.equals("TeleportRequest")){
                 UUID staffUUID = UUID.fromString(in.readUTF());
                 UUID targetUUID = UUID.fromString(in.readUTF());
                 String targetName = in.readUTF();

                 Optional<Player> staffPlayerOpt = NexusBridge.getInstance().getServer().getPlayer(staffUUID);
                 if (staffPlayerOpt.isEmpty()) {
                     NexusBridge.getInstance().getLogger().warn("Staff player not found: " + staffUUID);
                     return;
                 }
                 Player staffPlayer = staffPlayerOpt.get();

                playerProvider.findByNameAsync(targetName).thenAccept(player -> {
                    if (player == null) return;

                    TeleportModel teleportModel = new TeleportModel(staffUUID.toString(), targetUUID.toString(), targetName);

                    DatabaseAPI.set("reportteleport", teleportModel);
                    String targetServerName = player.getCurrentServerName();

                    Optional<RegisteredServer> targetServerOpt = NexusBridge.getInstance().getServer().getServer(targetServerName);
                    RegisteredServer targetServer = targetServerOpt.get();
                    staffPlayer.createConnectionRequest(targetServer).fireAndForget();

                });
             }
         } catch (Exception e) {
             NexusBridge.getInstance().getLogger().error("Error while handling plugin message: " + e.getMessage());
         }
     }
}
