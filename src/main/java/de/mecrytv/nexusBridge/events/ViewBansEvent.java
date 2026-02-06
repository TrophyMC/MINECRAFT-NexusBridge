package de.mecrytv.nexusBridge.events;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.nexusBridge.utils.TimeUtils;
import de.mecrytv.nexusBridge.utils.TranslationUtils;
import de.mecrytv.nexusapi.models.BanModel;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class ViewBansEvent {

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        DatabaseAPI.<BanModel>get("ban", playerId.toString()).thenAccept(ban -> {
            try {
                if (ban != null && ban.isActive()) {
                    long now = System.currentTimeMillis();

                    if (ban.getBanExpires() != -1 && ban.getBanExpires() <= now) {
                        return;
                    }

                    long remaining = ban.getBanExpires() - now;
                    String timeString = (ban.getBanExpires() == -1) ? "Permanent" : TimeUtils.formatDuration(remaining);
                    String reason = ban.getReason();

                    Component kickMessage = TranslationUtils.getComponentTranslation(player, "messages.blocked.ban",
                            "{reason}", reason,
                            "{time}", timeString);

                    event.setResult(LoginEvent.ComponentResult.denied(kickMessage));
                }
            } finally {
                continuation.resume();
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            continuation.resume();
            return null;
        });
    }
}