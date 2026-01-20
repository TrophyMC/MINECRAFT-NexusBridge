package de.mecrytv.nexusBridge.utils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.nexusBridge.NexusBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.concurrent.CompletableFuture;

public class TranslationUtils {


    public static CompletableFuture<String> getLangCode(Player player) {
        return NexusBridge.getInstance().getDatabaseAPI()
                .getGenericAsync("language", "language", "id", "data", player.getUniqueId().toString())
                .thenApply(json -> {
                    if (json != null && json.has("languageCode")) {
                        return json.get("languageCode").getAsString();
                    }
                    return "en_US";
                });
    }


    public static void sendTranslation(CommandSource source, String configKey, String... replacements) {
        if (!(source instanceof Player player)) {
            String msg = NexusBridge.getInstance().getLanguageAPI().getTranslation("en_US", configKey);
            source.sendMessage(format(msg, replacements));
            return;
        }

        getLangCode(player).thenAccept(langCode -> {
            String message = NexusBridge.getInstance().getLanguageAPI().getTranslation(langCode, configKey);

            if ((message == null || message.contains("Missing Lang")) && !langCode.equals("en_US")) {
                message = NexusBridge.getInstance().getLanguageAPI().getTranslation("en_US", configKey);
            }

            if (message == null || message.contains("Missing Lang")) message = configKey;

            player.sendMessage(format(message, replacements));
        });
    }

    private static Component format(String message, String... replacements) {
        Component component = MiniMessage.miniMessage().deserialize(message);

        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                component = component.replaceText(builder -> builder.matchLiteral(target).replacement(value));
            }
        }

        return NexusBridge.getInstance().getPrefix().append(component);
    }
}
