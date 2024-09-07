package nl.iverium.cobbledbank;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import nl.iverium.cobbledbank.commands.PokeBankCommand;
import nl.iverium.cobbledbank.config.DBConfig;
import nl.iverium.cobbledbank.config.LanguageConfig;
import nl.iverium.cobbledbank.data.external.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class CobbledBank implements ModInitializer {
    public static CobbledBank instance;
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger("CobbledBank");
    private static File configDir;
    private static MinecraftServer server;

    public static Logger getLog() {
        return log;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static void setServer(MinecraftServer server) {
        CobbledBank.server = server;
    }

    public static File getConfigDir() {
        return configDir;
    }

    public static void setConfigDir(File configDir) {
        CobbledBank.configDir = configDir;
    }

    public DBConfig dbConfig;
    public LanguageConfig languageConfig;
    public Database database;

    @Override
    public void onInitialize() {
        instance = this;

        PlatformEvents.SERVER_STARTED.subscribe(Priority.NORMAL, t -> {
            setServer(t.getServer());
            this.load();
            return Unit.INSTANCE;
        });
        PlatformEvents.SERVER_STOPPING.subscribe(Priority.NORMAL, t -> {
            this.shutdown();
            return Unit.INSTANCE;
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            //command registration
            dispatcher.register(PokeBankCommand.getCommand());
        });
    }

    public void load() {
        log.info("Loading CobbledBank");
        //load config
        setConfigDir(new File("config/CobbledBank"));
        getConfigDir().mkdirs();

        DBConfig.writeConfig();
        LanguageConfig.writeConfig();
        dbConfig = DBConfig.getConfig();
        languageConfig = LanguageConfig.getConfig();
        //load database
        if (dbConfig != null)
        {
            if (dbConfig.enabled)
            {
                this.database = new Database();

            } else {
                log.info("Database configuration found, but database is disabled");
            }
        } else {
            log.error("Database configuration not found, disabling database");
        }
    }

    public void shutdown() {
        log.info("Shutting down CobbledBank");
        //save config
        //save database
        if (this.database != null) {
            this.database.shutdown();
        } else {
            log.error("Database is null, cannot shut down a database. Was it ever properly connected?");
        }
    }
}
