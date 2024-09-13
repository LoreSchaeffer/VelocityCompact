package network.multicore.vc;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;
import com.zaxxer.hikari.HikariConfig;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.*;
import network.multicore.vc.dependencies.LibraryLoader;
import network.multicore.vc.events.*;
import network.multicore.vc.persistence.Database;
import network.multicore.vc.persistence.HibernateHbm2DdlAutoMode;
import network.multicore.vc.persistence.PrefixNamingStrategy;
import network.multicore.vc.persistence.datasource.DataSourceProvider;
import network.multicore.vc.persistence.entity.entities.PackageEntities;
import network.multicore.vc.utils.*;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = VelocityCompact.PLUGIN_ID,
        name = "VelocityCompact",
        version = VelocityCompact.PLUGIN_VERSION,
        description = "All you need for your velocity server",
        url = "https://github.com/LoreSchaeffer/VelocityCompact",
        authors = {VelocityCompact.PLUGIN_AUTHOR},
        dependencies = {
                @Dependency(id = "premiumvanish", optional = true)
        }
)
public class VelocityCompact {
    public static final String PLUGIN_ID = "velocitycompact";
    public static final String PLUGIN_VERSION = "1.0.0";
    public static final String PLUGIN_AUTHOR = "LoreSchaeffer";

    private static VelocityCompact instance;
    private final Logger logger;
    private final ProxyServer proxy;
    private final File pluginDir;
    private final Set<AbstractCommand> commands = new HashSet<>();
    private YamlDocument config;
    private Database db;
    private UserRepository userRepository;
    private BanRepository banRepository;
    private KickRepository kickRepository;
    private MuteRepository muteRepository;
    private WarnRepository warnRepository;
    private boolean premiumVanishSupport;
    private ScheduledTask announcerTask;
    private boolean loading = false;

    // TODO Optimize moderation commands to reduce duplicates

    @Inject
    private VelocityCompact(ProxyServer proxy, @DataDirectory Path dataDirectory, Logger logger) {
        instance = this;

        this.proxy = proxy;
        this.pluginDir = dataDirectory.toFile();
        this.logger = logger;

        Text.setProxy(proxy);

        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            if (!pluginDir.mkdirs()) {
                logger.error("Failed to create plugin directory");
                shutdown();
            }
        }
    }

    public void enable() {
        loading = true;

        try {
            LibraryLoader libLoader = new LibraryLoader(proxy.getPluginManager(), new File(pluginDir, "libraries"), logger);
            libLoader.downloadDependencies();
        } catch (IOException | DependencyResolutionException e) {
            logger.error("Failed to download dependencies", e);
            shutdown();
            return;
        }

        try {
            loadConfig();
        } catch (IOException e) {
            logger.error("Failed to load config", e);
            shutdown();
            return;
        }

        try {
            Messages.init(new File(pluginDir, "messages.yml"));
        } catch (IOException e) {
            logger.error("Failed to load messages", e);
            shutdown();
            return;
        }

        try {
            initStorage();
        } catch (IllegalArgumentException | IOException e) {
            shutdown();
        }

        Cache.get();
        CensureUtils.init(config);

        premiumVanishSupport = config.getBoolean("premium-vanish-support", false);
        if (premiumVanishSupport) {
            try {
                VelocityVanishAPI.getInvisiblePlayers();
            } catch (Throwable t) {
                premiumVanishSupport = false;
                logger.error("Failed to load premium vanish support", t);
            }
        }

        proxy.getEventManager().register(this, new CommandExecuteListener());
        proxy.getEventManager().register(this, new PlayerChatListener());
        proxy.getEventManager().register(this, new PlayerConnectToServerListener());
        proxy.getEventManager().register(this, new PlayerDisconnectListener());
        proxy.getEventManager().register(this, new PlayerLoginListener());
        proxy.getEventManager().register(this, new PlayerPreLoginListener());

        registerCommands();

        if (config.getBoolean("modules.announcer", false)) startAnnouncer();

        loading = false;
    }

    public void disable() {
        if (announcerTask != null) stopAnnouncer();
        proxy.getEventManager().unregisterListeners(this);
        unregisterCommands();

        try {
            if (db != null) db.close();
        } catch (Throwable ignored) {
        }

        try {
            Cache.get().clear();
        } catch (Throwable ignored) {
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        enable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        disable();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        disable();
        enable();
    }

    public static VelocityCompact getInstance() {
        return instance;
    }

    public Logger logger() {
        return logger;
    }

    public ProxyServer proxy() {
        return proxy;
    }

    public YamlDocument config() {
        return config;
    }

    public File pluginDir() {
        return pluginDir;
    }

    public boolean hasPremiumVanishSupport() {
        return premiumVanishSupport;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public BanRepository banRepository() {
        return banRepository;
    }

    public KickRepository kickRepository() {
        return kickRepository;
    }

    public MuteRepository muteRepository() {
        return muteRepository;
    }

    public WarnRepository warnRepository() {
        return warnRepository;
    }

    public boolean isLoading() {
        return loading;
    }

    public void shutdown() {
        Optional<PluginContainer> container = proxy.getPluginManager().getPlugin(PLUGIN_ID);
        container.ifPresent(c -> c.getExecutorService().shutdown());
    }

    private void loadConfig() throws IOException {
        File configFile = new File(pluginDir, "config.yml");

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            config = YamlDocument.create(
                    configFile,
                    Objects.requireNonNull(is),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder()
                            .setAutoUpdate(true)
                            .setCreateFileIfAbsent(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );

            config.update();
            config.save();
        }
    }

    private void initStorage() throws IllegalArgumentException, IOException {
        DataSourceProvider<?> provider;

        String storageType = config.getString("storage-type");

        switch (storageType) {
            case "MySQL", "MariaDB", "PostgreSQL" -> {
                String address = config.getString("data.address");
                String database = config.getString("data.database");
                String username = config.getString("data.username");
                String password = config.getString("data.password");
                boolean usePool = config.getBoolean("data.pool.enabled");

                Preconditions.checkNotNull(address, "address");
                Preconditions.checkNotNull(database, "database");
                Preconditions.checkNotNull(username, "username");
                Preconditions.checkNotNull(password, "password");
                Preconditions.checkArgument(!address.isBlank(), "address must not be blank");
                Preconditions.checkArgument(!database.isBlank(), "database must not be blank");
                Preconditions.checkArgument(!username.isBlank(), "username must not be blank");

                String host;
                int port;

                if (address.contains(":")) {
                    String[] parts = address.split(":");

                    try {
                        host = parts[0];
                        port = Integer.parseInt(parts[1]);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid address format");
                    }
                } else {
                    host = address;
                    port = storageType.equals("MySQL") || storageType.equals("MariaDB") ? DataSourceProvider.DEF_MYSQL_PORT : DataSourceProvider.DEF_POSTGRESQL_PORT;
                }

                if (usePool) {
                    HikariConfig poolConfig = new HikariConfig();
                    poolConfig.setMaximumPoolSize(config.getInt("data.pool.maximum-pool-size"));
                    poolConfig.setMinimumIdle(config.getInt("data.pool.minimum-idle"));
                    poolConfig.setMaxLifetime(config.getLong("data.pool.maximum-lifetime"));
                    poolConfig.setKeepaliveTime(config.getLong("data.pool.keepalive-time"));
                    poolConfig.setConnectionTimeout(config.getLong("data.pool.connection-timeout"));

                    switch (storageType) {
                        case "MySQL" -> provider = DataSourceProvider.newMysqlHikariDataSourceProvider(host, port, database, username, password, poolConfig);
                        case "MariaDB" -> provider = DataSourceProvider.newMariaDbHikariDataSourceProvider(host, port, database, username, password, poolConfig);
                        case "PostgreSQL" -> provider = DataSourceProvider.newPostgreSqlHikariDataSourceProvider(host, port, database, username, password, poolConfig);
                        default -> throw new IllegalArgumentException("Invalid storage type");
                    }
                } else {
                    switch (storageType) {
                        case "MySQL" -> provider = DataSourceProvider.newMysqlDataSourceProvider(host, port, database, username, password);
                        case "MariaDB" -> provider = DataSourceProvider.newMariaDbDataSourceProvider(host, port, database, username, password);
                        case "PostgreSQL" -> provider = DataSourceProvider.newPostgreSqlDataSourceProvider(host, port, database, username, password);
                        default -> throw new IllegalArgumentException("Invalid storage type");
                    }
                }
            }
            case "H2" -> {
                File dbFile = new File(pluginDir, "velocitycompact-h2.db");
                if (!dbFile.exists() || !dbFile.isFile()) {
                    if (!dbFile.createNewFile()) {
                        throw new IOException("Failed to create H2 database file");
                    }
                }

                provider = DataSourceProvider.newH2DataSourceProvider(dbFile);
            }
            case "SQLite" -> {
                File dbFile = new File(pluginDir, "velocitycompact-sqlite.db");
                if (!dbFile.exists() || !dbFile.isFile()) {
                    if (!dbFile.createNewFile()) {
                        throw new IOException("Failed to create SQLite database file");
                    }
                }

                provider = DataSourceProvider.newSQLiteDataSourceProvider(dbFile);
            }
            default -> {
                logger.error("Invalid storage type");
                throw new IllegalArgumentException("Invalid storage type");
            }
        }

        Database.Builder builder = new Database.Builder();
        builder.persistenceUnitName(PLUGIN_ID)
                .hbm2ddlAuto(HibernateHbm2DdlAutoMode.UPDATE)
                .dataSourceProvider(provider)
                .entities(new PackageEntities(User.class.getPackageName()));

        if (storageType.equals("MySQL") || storageType.equals("MariaDB") || storageType.equals("PostgreSQL")) {
            String tablePrefix = config.getString("data.table-prefix");
            if (tablePrefix != null && !tablePrefix.isBlank()) {
                if (!tablePrefix.endsWith("_")) tablePrefix.concat("_");

                builder.tablesPrefix(tablePrefix);
                PrefixNamingStrategy.setTablePrefix(tablePrefix);
            }
        }

        db = builder.build();

        userRepository = db.createRepository(UserRepository.class, User.class);
        banRepository = db.createRepository(BanRepository.class, Ban.class);
        kickRepository = db.createRepository(KickRepository.class, Kick.class);
        muteRepository = db.createRepository(MuteRepository.class, Mute.class);
        warnRepository = db.createRepository(WarnRepository.class, Warn.class);
    }

    private void registerCommands() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(AbstractCommand.class.getPackageName())
                .addScanners(Scanners.SubTypes));

        reflections.getSubTypesOf(AbstractCommand.class).forEach(clazz -> {
            try {
                AbstractCommand cmd = clazz.getDeclaredConstructor().newInstance();
                cmd.register();
                commands.add(cmd);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void unregisterCommands() {
        commands.forEach(AbstractCommand::unregister);
        commands.clear();
    }

    private void startAnnouncer() {
        List<AnnouncerMessage> messages = new ArrayList<>();

        config.getSection("announcer.messages").getKeys().forEach(key -> {
            AnnouncerMessage message = new AnnouncerMessage(
                    config.getStringList("announcer.messages." + key + ".server-list", List.of()),
                    config.getBoolean("announcer.messages." + key + ".list-is-whitelist", false),
                    config.getStringList("announcer.messages." + key + ".lines", List.of())
            );
            if (!message.lines().isEmpty()) messages.add(message);
        });

        if (messages.isEmpty()) {
            logger.warn("No announcer messages found");
            return;
        }

        long interval = config.getLong("announcer.interval", 600L);
        if (interval <= 0) {
            logger.warn("Invalid announcer interval");
            return;
        }

        announcerTask = proxy.getScheduler()
                .buildTask(this, new AnnouncerTask(messages))
                .delay(interval, TimeUnit.SECONDS)
                .repeat(interval, TimeUnit.SECONDS)
                .schedule();
    }

    private void stopAnnouncer() {
        if (!announcerTask.status().equals(TaskStatus.CANCELLED) || announcerTask.status().equals(TaskStatus.FINISHED)) {
            announcerTask.cancel();
            announcerTask = null;
        }
    }

    private class AnnouncerTask implements Runnable {
        private final List<AnnouncerMessage> messages;
        private final boolean random;
        private int index = 0;

        public AnnouncerTask(List<AnnouncerMessage> messages) {
            this.messages = messages;
            this.random = config.getBoolean("announcer.random", false);
        }

        @Override
        public void run() {
            if (random) {
                AnnouncerMessage message = messages.get(new Random().nextInt(messages.size()));

                if (message.isWhitelist()) {
                    message.servers().forEach(serverName -> {
                        RegisteredServer server = proxy.getServer(serverName).orElse(null);
                        if (server == null) {
                            logger.warn("Announcer not sent to server {}. Server not found", serverName);
                            return;
                        }

                        Text.broadcast(message.lines(), server);
                    });
                } else {
                    proxy.getAllServers()
                            .stream()
                            .filter(server -> !message.servers().contains(server.getServerInfo().getName()))
                            .forEach(server -> Text.broadcast(message.lines(), server));
                }
            } else {
                AnnouncerMessage message = messages.get(index);

                if (message.isWhitelist()) {
                    message.servers().forEach(serverName -> {
                        RegisteredServer server = proxy.getServer(serverName).orElse(null);
                        if (server == null) {
                            logger.warn("Announcer not sent to server {}. Server not found", serverName);
                            return;
                        }

                        Text.broadcast(message.lines(), server);
                    });
                } else {
                    proxy.getAllServers()
                            .stream()
                            .filter(server -> !message.servers().contains(server.getServerInfo().getName()))
                            .forEach(server -> Text.broadcast(message.lines(), server));
                }

                index++;
                if (index >= messages.size()) index = 0;
            }
        }
    }
}
