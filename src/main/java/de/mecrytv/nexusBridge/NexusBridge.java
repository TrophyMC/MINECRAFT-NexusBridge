package de.mecrytv.nexusBridge;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusBridge.manager.ConfigManager;
import de.mecrytv.utils.DatabaseConfig;
import org.slf4j.Logger;

@Plugin(id = "nexus-bridge", name = "Nexus-Bridge", version = "1.0.0", authors = {"MecryTv"}, description = "A bridge plugin for Nexus-Core")
public class NexusBridge {

    private static NexusBridge instance;
    private final Logger logger;
    private final ProxyServer server;
    private final ConfigManager config;

    private DatabaseAPI databaseAPI;

    @Inject
    public NexusBridge(Logger logger, ProxyServer server, ConfigManager config) {
        instance = this;
        this.logger = logger;
        this.server = server;
        this.config = config;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

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
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (databaseAPI != null) databaseAPI.shutdown();
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
}
