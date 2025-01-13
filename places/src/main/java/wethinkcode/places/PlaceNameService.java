package wethinkcode.places;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;
import wethinkcode.loadshed.alerts.LoadshedLoggers;

/**
 * Provides a Place-names Service for locations in South Africa.
 * <p>
 * Reads place-name data from a CSV file, parses it into domain objects, and serves
 * place-name data as JSON to clients via HTTP endpoints.
 * <p>
 * Clients can request:
 * <ul>
 *     <li>A list of available provinces.</li>
 *     <li>A list of towns within a specific province.</li>
 *     <li>A list of neighborhoods within a town.</li>
 * </ul>
 * Supports configuration via command-line arguments or configuration files.
 */
@Command(name = "PlaceNameService", mixinStandardHelpOptions = true)
public class PlaceNameService implements Runnable {

    public static final int DEFAULT_PORT = 7000;

    // Configuration keys
    public static final String CFG_CONFIG_FILE = "config.file";
    public static final String CFG_DATA_DIR = "data.dir";
    public static final String CFG_DATA_FILE = "data.file";
    public static final String CFG_SERVICE_PORT = "server.port";

    /**
     * Main entry point for the application.
     * Initializes and runs the PlaceNameService.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        final PlaceNameService svc = new PlaceNameService().initialise();
        final int exitCode = new CommandLine(svc).execute(args);
        while (true) {
            int i = 0;
        }
    }

    private final Properties config;
    private Javalin server;
    private Places places;

    @Option(names = { "-c", "--config" }, description = "Configuration file path")
    private File configFile;

    @Option(names = { "-d", "--datadir" },
            description = "Directory pathname where datafiles may be found")
    private File dataDir;

    @Option(names = { "-f", "--datafile" }, description = "CSV Data file path")
    private File dataFile;

    @Option(names = { "-p", "--port" }, description = "Service network port number")
    private int svcPort;

    public PlaceNameService() {
        config = initConfig();
    }

    /**
     * Starts the service on the default port.
     */
    public void start() {
        start(servicePort());
    }

    /**
     * Starts the service on the specified port.
     *
     * @param port The port number to start the service on.
     */
    @VisibleForTesting
    void start(int port) {
        server.start(port);
    }

    /**
     * Stops the running service.
     */
    public void stop() {
        server.stop();
    }

    /**
     * Initializes the PlaceNameService with the necessary dependencies.
     *
     * @return The initialized PlaceNameService instance.
     */
    @VisibleForTesting
    PlaceNameService initialise() {
        places = initPlacesDb();
        server = initHttpServer();
        return this;
    }

    /**
     * Initializes the service with a specific Places database for testing.
     *
     * @param aPlaceDb The Places database to use.
     * @return The initialized PlaceNameService instance.
     */
    @VisibleForTesting
    PlaceNameService initialise(Places aPlaceDb) {
        places = aPlaceDb;
        server = initHttpServer();
        return this;
    }

    @Override
    public void run() {
        server.start(servicePort());
    }

    /**
     * Initializes service configuration from the configuration file or default settings.
     *
     * @return The initialized Properties instance containing the configuration.
     */
    private Properties initConfig() {
        try (FileReader in = new FileReader(configFile())) {
            final Properties p = new Properties(defaultConfig());
            p.load(in);
            return p;
        } catch (IOException ex) {
            LoadshedLoggers.severe("Failed to read and load file", ex);
            return defaultConfig();
        }
    }

    /**
     * Initializes the Places database by parsing the specified CSV file.
     *
     * @return The Places database.
     */
    private Places initPlacesDb() {
        try {
            return new PlacesCsvParser().parseCsvSource(dataFile());
        } catch (IOException ex) {
            LoadshedLoggers.severe("Failed to initialize places database", ex);
            System.err.println("Error reading CSV file " + dataFile + ": " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Initializes the HTTP server with the configured routes and settings.
     *
     * @return The initialized Javalin server instance.
     */
    private Javalin initHttpServer() {
        System.out.println("Server running...");
        return Javalin.create(config -> {
                    config.plugins.enableCors(cors -> {
                        cors.add(it -> {
                            it.allowHost("http://localhost:8080");
                            it.allowHost("http://127.0.0.1:5500/");
                            it.allowHost("http://localhost:7002");
                            it.allowHost("http://localhost:7003");
                        });
                    });
                }).get("/", ctx -> ctx.json(places.provinces()))
                .get("/provinces", ctx -> ctx.json(places.provinces()))
                .get("/towns/{province}", this::getTowns);
    }

    /**
     * Handles HTTP GET requests for towns in a given province.
     *
     * @param ctx The HTTP context containing the request details.
     * @return The HTTP context after adding the response.
     */
    private Context getTowns(Context ctx) {
        final String province = ctx.pathParam("province");
        final Collection<Town> towns = places.townsIn(province);
        return ctx.json(towns);
    }

    @VisibleForTesting
    File configFile() {
        return configFile != null
                ? configFile
                : new File(defaultConfig().getProperty(CFG_CONFIG_FILE));
    }

    @VisibleForTesting
    File dataFile() {
        return dataFile != null
                ? dataFile
                : new File(getConfig(CFG_DATA_FILE));
    }

    @VisibleForTesting
    File dataDir() {
        return dataDir != null
                ? dataDir
                : new File(getConfig(CFG_DATA_DIR));
    }

    /**
     * Retrieves the configured service port.
     *
     * @return The service port number.
     */
    int servicePort() {
        return svcPort > 0
                ? svcPort
                : Integer.parseInt(getConfig(CFG_SERVICE_PORT));
    }

    /**
     * Retrieves the value for a specific configuration key.
     *
     * @param aKey The configuration key.
     * @return The value associated with the key.
     */
    @VisibleForTesting
    String getConfig(String aKey) {
        return config.getProperty(aKey);
    }

    /**
     * Retrieves the Places database.
     *
     * @return The Places database.
     */
    @VisibleForTesting
    Places getDb() {
        return places;
    }

    /**
     * Provides default configuration properties.
     *
     * @return The default Properties instance.
     */
    private static Properties defaultConfig() {
        final Properties p = new Properties();
        p.setProperty(CFG_CONFIG_FILE, System.getProperty("user.dir") + "/places.properties");
        p.setProperty(CFG_DATA_DIR, System.getProperty("user.dir"));
        p.setProperty(CFG_DATA_FILE, System.getProperty("user.dir") + "/places.csv");
        p.setProperty(CFG_SERVICE_PORT, Integer.toString(DEFAULT_PORT));
        return p;
    }
}