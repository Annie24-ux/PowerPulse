package wethinkcode.web;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MQ;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.places.PlaceNameService;
import wethinkcode.schedule.ScheduleService;
import wethinkcode.stage.StagePersistence;
import wethinkcode.stage.StageService;
import java.net.http.HttpClient;
import javax.jms.*;
import wethinkcode.loadshed.alerts.LoadshedLoggers;

/**
 * The front-end web server for the LightSched project.
 * <p>
 * This class is primarily focused on how the web server interacts with back-end services, such as receiving and processing
 * messages from the stage service, setting up an HTTP server, and managing stage updates.
 */
public class WebService {

    public static final int DEFAULT_PORT = 8080;
    private static final String PAGES_DIR = "/templates";
    private Connection connection;
    private static int currentStage;
    private static long NAP_TIME = 2000;//ms

    /**
     * Main method to initialize and start the web service.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final WebService svc = new WebService().initialise();
        svc.start();
        System.out.println("Current Stage on webService: " + currentStage);
    }

    private Javalin server;
    private HttpClient client;
    public static final String MQ_TOPIC_NAME = "stage";
    private int servicePort;
    StagePersistence stagePersistence = new StagePersistence();

    /**
     * Initializes the web service by configuring the HTTP server, HTTP client, and message listener.
     *
     * @return the initialized web service instance
     */
    @VisibleForTesting
    WebService initialise() {
        server = configureHttpServer();
        configureHttpClient();
        loadPersistedStage();
        setUpMessageListener();
        return this;
    }

    /**
     * Starts the web service on the default port.
     */
    public void start() {
        start(DEFAULT_PORT);
    }

    /**
     * Starts the web service on a specified network port.
     *
     * @param networkPort the port to bind the server to
     */
    @VisibleForTesting
    void start(int networkPort) {
        servicePort = networkPort;
        run();
    }

    /**
     * Stops the web server.
     */
    public void stop() {
        server.stop();
    }


    /**
     * Runs the web server, starting it on the configured port.
     */
    public void run() {
        server.start(servicePort);
    }

    /**
     * Configures the HTTP client for the web service.
     */
    private void configureHttpClient() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Configures the HTTP server to serve static files and handle incoming requests.
     *
     * @return the configured Javalin server instance
     */
    private Javalin configureHttpServer() {
        Javalin javalin = Javalin.create(javalinConfig -> javalinConfig.addStaticFiles(PAGES_DIR, Location.CLASSPATH));
        return javalin;
    }

    /**
     * Sets up the message listener to subscribe to the MQ topic and handle incoming messages.
     * Listens for updates from the stage service and processes them accordingly.
     */
    private void setUpMessageListener() {
        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);
            connection = factory.createConnection(MQ.USER, MQ.PASSWD);


            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination dest = session.createTopic(MQ_TOPIC_NAME);

            final MessageConsumer receiver = session.createConsumer(dest);

            System.out.println("Listening on web....");
            receiver.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message m) {
                    try {
                        String bodyText = ((TextMessage) m).getText();
                        JSONObject stageData = textToObject(bodyText);
                        int stage = stageData.getInt("stage");
                        currentStage = stage;
                        System.out.println("Loadshedding stage has been updated to: " + currentStage);
                        StageDO stageDO = new StageDO(currentStage);
                        stageDO.setStage(currentStage);
                        stagePersistence.saveStage(stageDO);
                        System.out.println("Receiving message from topic: " + bodyText);
                    } catch (JMSException e) {
                        LoadshedLoggers.severe("Could not process message", e);
                        e.printStackTrace();
                    }
                }
            });
            connection.start();

        } catch (JMSException erk) {
            LoadshedLoggers.severe("MessageListener was not set up", erk);
            throw new RuntimeException(erk);
        }
    }

    /**
     * Converts the text of a message into a JSON object.
     *
     * @param text the text message to convert
     * @return the JSON object representing the message text
     */
    private JSONObject textToObject(String text) {
        try {
            return new JSONObject(text);
        } catch (JSONException err) {
            err.printStackTrace();
            LoadshedLoggers.severe("Failed to convert to a JSONObject", err);
        }
        return null;
    }

    /**
     * Pauses the execution of the listener for a specified duration.
     */
    private void snooze() {
        try {
            Thread.sleep(NAP_TIME);
        } catch (InterruptedException eek) {
            LoadshedLoggers.severe("Program could not sleep.", eek);
        }
    }

    /**
     * Gets the current stage value.
     *
     * @return the current stage
     */
    public static int getCurrentStage() {
        return currentStage;
    }

    /**
     * Updates the current loadshedding stage.
     *
     * @param stage the new stage value
     */
    public static synchronized void setCurrentStage(int stage) {
        currentStage = stage;
    }

    private void loadPersistedStage() {
        StageDO savedStage = stagePersistence.loadStage();
        if (savedStage != null) {
            System.out.println("Persisted stage found: " + savedStage.getStage());
            setCurrentStage(savedStage.getStage());
        } else {
            System.out.println("No persisted stage found, using default stage.");
        }
    }


}
