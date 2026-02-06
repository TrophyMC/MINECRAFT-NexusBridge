package de.mecrytv.nexusBridge.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.nexusBridge.utils.TimeUtils;
import de.mecrytv.nexusBridge.utils.TranslationUtils;
import de.mecrytv.nexusapi.models.MuteModel;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ViewMutesEvent {

    private final Map<UUID, Long> lastNotify = new HashMap<>();
    private static final long COOLDOWN_MILLIS = TimeUnit.MINUTES.toMillis(10);

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        DatabaseAPI.<MuteModel>get("mute", playerId.toString()).thenAccept(mute -> {
            if (mute != null && mute.isActive()) {
                event.setResult(PlayerChatEvent.ChatResult.denied());

                long now = System.currentTimeMillis();

                if (!lastNotify.containsKey(playerId) || (now - lastNotify.get(playerId)) > COOLDOWN_MILLIS) {
                    long remaining = mute.getMuteExpires() - now;
                    String timeString = (mute.getMuteExpires() == -1) ? "Permanent" : TimeUtils.formatDuration(remaining);
                    String reason = mute.getReason();

                    sendBoxMessage(player, "messages.blocked.mute",
                            "{reason}", reason,
                            "{time}", timeString);

                    lastNotify.put(playerId, now);
                }
            }
        });
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        lastNotify.remove(event.getPlayer().getUniqueId());
    }

    private void sendBoxMessage(Player player, String key, String... replacements) {
        Component message = TranslationUtils.getComponentTranslation(player, key, replacements);

        player.sendMessage(message);
    }
}