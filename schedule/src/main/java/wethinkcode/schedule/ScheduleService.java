package wethinkcode.schedule;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MQ;
import wethinkcode.loadshed.common.transfer.DayDO;
import wethinkcode.loadshed.common.transfer.ScheduleDO;
import wethinkcode.loadshed.common.transfer.SlotDO;
import wethinkcode.loadshed.alerts.LoadshedLoggers;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.stage.StagePersistence;

import javax.jms.*;

/**
 * Provides a REST API for retrieving the current loadshedding schedule for a specific town
 * (within a given province) at a particular loadshedding stage.
 */
public class ScheduleService {
    public static final int DEFAULT_STAGE = 0;
    public static final int DEFAULT_PORT = 7002;
    public static final String MQ_TOPIC = "stage";

    private Javalin server;
    private int servicePort;
    private Connection connection;
    private static int currentStage;
    StagePersistence stagePersistence = new StagePersistence();



    public static void main(String[] args) {
        final ScheduleService svc = new ScheduleService().initialise();
        svc.start();
        System.out.println("Current on Schedule: " + currentStage);

    }

    /**
     * Initializes the service by setting up the HTTP server and message listener.
     *
     * @return the initialized ScheduleService instance
     */
    @VisibleForTesting
    ScheduleService initialise() {
        server = initHttpServer();
        loadPersistedStage();
        setUpMessageListener();
        return this;
    }

    /**
     * Starts the service on the default port.
     */
    public void start() {
        start(DEFAULT_PORT);
    }

    /**
     * Starts the service on a specified network port.
     *
     * @param networkPort the port number to start the server on
     */
    @VisibleForTesting
    void start(int networkPort) {
        servicePort = networkPort;
        run();
    }

    /**
     * Stops the server.
     */
    public void stop() {
        server.stop();
    }

    /**
     * Runs the server on the configured port.
     */
    public void run() {
        server.start(servicePort);
    }

    /**
     * Initializes the HTTP server with routes and CORS configurations.
     *
     * @return a configured Javalin instance
     */
    private Javalin initHttpServer() {
        return Javalin.create(config -> {
                    config.plugins.enableCors(cors -> {
                        cors.add(it -> {
                            it.allowHost("http://localhost:8080");
                            it.allowHost("http://127.0.0.1:7004");
                            it.allowHost("http://localhost:7002");
                            it.allowHost("http://localhost:7003");
                        });
                    });
                })
                .get("/{province}/{town}/{stage}", this::getSchedule)
                .get("/{province}/{town}", this::getDefaultSchedule);
    }

    /**
     * Handles requests for a specific loadshedding schedule.
     *
     * @param ctx the HTTP context
     * @return the updated HTTP context with the requested schedule or an error status
     */
    private Context getSchedule(Context ctx) {
        final String province = ctx.pathParam("province");
        final String townName = ctx.pathParam("town");
        final String stageStr = ctx.pathParam("stage");

        if (province.isEmpty() || townName.isEmpty() || stageStr.isEmpty()) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return ctx;
        }
        final int stage = Integer.parseInt(stageStr);
        if (stage < 0 || stage > 8) {
            return ctx.status(HttpStatus.BAD_REQUEST);
        }

        final Optional<ScheduleDO> schedule = getSchedule(province, townName, stage);
        ctx.status(schedule.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND);
        return ctx.json(schedule.orElseGet(ScheduleService::emptySchedule));
    }

    /**
     * Handles requests for the default loadshedding schedule based on the current stage.
     *
     * @param ctx the HTTP context
     * @return the updated HTTP context with the requested schedule or an error status
     */
    private Context getDefaultSchedule(Context ctx) {
        final String province = ctx.pathParam("province");
        final String townName = ctx.pathParam("town");

        if (province.isEmpty() || townName.isEmpty()) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return ctx;
        }

        final int stage = getCurrentStage();
        final Optional<ScheduleDO> schedule = getSchedule(province, townName, stage);

        ctx.status(schedule.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND);
        return ctx.json(schedule.orElseGet(ScheduleService::emptySchedule));
    }

    /**
     * Retrieves a schedule for a given province, town, and loadshedding stage.
     *
     * @param province the province name
     * @param town     the town name
     * @param stage    the loadshedding stage
     * @return an optional ScheduleDO, empty if no schedule is available
     */
    Optional<ScheduleDO> getSchedule(String province, String town, int stage) {
        return province.equalsIgnoreCase("Mars")
                ? Optional.empty()
                : Optional.of(mockSchedule());
    }

    /**
     * Generates a mock schedule for testing purposes.
     *
     * @return a mock ScheduleDO instance
     */
    private static ScheduleDO mockSchedule() {
        final List<SlotDO> slots = List.of(
                new SlotDO(LocalTime.of(2, 0), LocalTime.of(4, 0)),
                new SlotDO(LocalTime.of(10, 0), LocalTime.of(12, 0)),
                new SlotDO(LocalTime.of(18, 0), LocalTime.of(20, 0))
        );
        final List<DayDO> days = List.of(
                new DayDO(slots),
                new DayDO(slots),
                new DayDO(slots),
                new DayDO(slots)
        );
        return new ScheduleDO(days);
    }

    /**
     * Generates an empty schedule.
     *
     * @return an empty ScheduleDO instance
     */
    private static ScheduleDO emptySchedule() {
        final List<SlotDO> slots = Collections.emptyList();
        final List<DayDO> days = Collections.emptyList();
        return new ScheduleDO(days);
    }

    /**
     * Sets up a message listener for ActiveMQ to update the loadshedding stage.
     */
    private void setUpMessageListener() {
        try {
            String clientId = "ScheduleService-" + System.currentTimeMillis();
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);
            connection = factory.createConnection(MQ.USER, MQ.PASSWD);
            connection.setClientID(clientId);


            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination dest = session.createTopic(MQ_TOPIC);
            final MessageConsumer receiver = session.createConsumer(dest);

            System.out.println("Listening on schedule....");
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
                    } catch (JMSException e) {
                        LoadshedLoggers.severe("Received message of a different instance", e);
                        e.printStackTrace();
                    }
                }
            });
            connection.start();
        } catch (JMSException erk) {
            LoadshedLoggers.severe("Failed to set up Message Listener", erk);
            throw new RuntimeException(erk);
        }
    }

    /**
     * Converts a text string into a JSONObject.
     *
     * @param text the string to convert
     * @return the resulting JSONObject, or null if conversion fails
     */
    public JSONObject textToObject(String text) {
        try {
            return new JSONObject(text);
        } catch (JSONException err) {
            LoadshedLoggers.severe("Failed to convert to a JSONObject", err);
            err.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the current loadshedding stage.
     *
     * @return the current stage if valid, or the default stage otherwise
     */
    public static synchronized int getCurrentStage() {
        return (currentStage > 0 && currentStage <= 8) ? currentStage : DEFAULT_STAGE;
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
