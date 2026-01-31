package de.mecrytv.nexusBridge;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.databaseapi.utils.DatabaseConfig;
import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.nexusBridge.events.ReportTeleportEvent;
import de.mecrytv.nexusBridge.manager.ConfigManager;
import de.mecrytv.nexusapi.NexusAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "nexus-bridge", name = "Nexus-Bridge", version = "1.0.0", authors = {"MecryTv"}, description = "A bridge plugin for Nexus-Core")
public class NexusBridge {

    private static NexusBridge instance;
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final ConfigManager config;

    private DatabaseAPI databaseAPI;
    private LanguageAPI languageAPI;
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("nexus:bridge");

    @Inject
    public NexusBridge(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        instance = this;
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.config = new ConfigManager(dataDirectory, "config.json");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Path langDir = Path.of("/home/minecraft/languages/");
        this.languageAPI = new LanguageAPI(langDir);

        DatabaseConfig dbConfig = new DatabaseConfig(
                config.getString("mariadb.host"),
                config.getInt("mariadb.port"),
                config.getString("mariadb.database"),
                config.getString("mariadb.username"),
                config.getString("mariadb.password"),
                config.getString("redis.host"),
                config.getInt("redis.port"),
                config.getString("redis.password")
        );

        this.databaseAPI = new DatabaseAPI(dbConfig);

        server.getChannelRegistrar().register(IDENTIFIER);
        server.getEventManager().register(this, new ReportTeleportEvent());

        registerGlobalStaffListener();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (databaseAPI != null) databaseAPI.shutdown();
    }

    private void registerGlobalStaffListener() {
        NexusAPI.getInstance().getGlobalNotifyer().listen(this.languageAPI, (permission, component) -> {

            Component finalMessage = getPrefix().append(component);

            for (Player player : server.getAllPlayers()) {
                if (player.hasPermission(permission)) {
                    player.sendMessage(finalMessage);
                }
            }

            logger.info("[GlobalNotify] Nachricht an Team gesendet: " + permission);
        });
    }

    public static NexusBridge getInstance() {
        return instance;
    }
    public ProxyServer getServer() {
        return server;
    }
    public Logger getLogger() {
        return logger;
    }
    public ConfigManager getConfig() {
        return config;
    }
    public DatabaseAPI getDatabaseAPI() {
        return databaseAPI;
    }
    public LanguageAPI getLanguageAPI() {
        return languageAPI;
    }
    public Path getDataDirectory() {
        return dataDirectory;
    }
    public Component getPrefix() {
        if (!this.config.contains("prefix")) {
            return MiniMessage.miniMessage().deserialize("<dark_grey>[<gold>Moderation<dark_grey>] ");
        }

        String prefixRaw = this.config.getString("prefix");

        if (prefixRaw == null || prefixRaw.isEmpty()) {
            return MiniMessage.miniMessage().deserialize("<dark_grey>[<gold>Moderation<dark_grey>] ");
        }

        return MiniMessage.miniMessage().deserialize(prefixRaw);
    }
}
