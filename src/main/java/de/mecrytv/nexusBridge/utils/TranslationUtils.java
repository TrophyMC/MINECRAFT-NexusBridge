package de.mecrytv.nexusBridge.utils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.nexusBridge.NexusBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class TranslationUtils {


    public static String getLang(CommandSource source) {
        if (source instanceof Player player) {
            return NexusBridge.getInstance().getLanguageAPI()
                    .getProfile(player.getUniqueId(), "en_US").getLanguageCode();
        }
        return "en_US";
    }

    public static void sendTranslation(CommandSource source, String configKey, String... replacements) {
        String langCode = getLang(source);
        String message = getRawTranslation(langCode, configKey);

        message = applyRawReplacements(message, replacements);
        Component component = MiniMessage.miniMessage().deserialize(message);

        source.sendMessage(NexusBridge.getInstance().getPrefix().append(component));
    }

    public static Component getComponentTranslation(Player player, String configKey, String... replacements) {
        String langCode = getLang(player);
        String message = getRawTranslation(langCode, configKey);

        if (message.equals(configKey)) return Component.text(configKey);

        message = message.replaceFirst("(?i)(?:<[^>]*>)*Dynamic\\s*", "").trim();
        message = applyRawReplacements(message, replacements);

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static List<Component> getLoreTranslation(Player player, String configKey, String... replacements) {
        String langCode = getLang(player);
        String message = getRawTranslation(langCode, configKey);

        if (message.equals(configKey)) return List.of(Component.text(configKey));

        message = message.replaceFirst("(?i)(?:<[^>]*>)*Dynamic\\s*", "").trim();
        message = applyRawReplacements(message, replacements);

        List<Component> lore = new ArrayList<>();
        for (String line : message.split("\n")) {
            lore.add(MiniMessage.miniMessage().deserialize(line));
        }
        return lore;
    }

    private static String getRawTranslation(String langCode, String configKey) {
        String message = NexusBridge.getInstance().getLanguageAPI().getTranslation(langCode, configKey);

        // Fallback auf Englisch, wenn die Übersetzung fehlt
        if ((message == null || message.contains("Missing Lang")) && !langCode.equals("en_US")) {
            message = NexusBridge.getInstance().getLanguageAPI().getTranslation("en_US", configKey);
        }

        return (message == null || message.contains("Missing Lang")) ? configKey : message;
    }

    private static String applyRawReplacements(String input, String... replacements) {
        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                if (target != null && value != null) {
                    input = input.replace(target, value);
                }
            }
        }
        return input;
    }
}