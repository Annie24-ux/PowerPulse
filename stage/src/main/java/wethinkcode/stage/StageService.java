package wethinkcode.stage;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import topic.StageProducer;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.loadshed.alerts.LoadshedLoggers;

import javax.jms.JMSException;

/**
 * I provide a REST API that reports the current loadshedding "stage". I provide two endpoints:
 * <dl>
 * <dt>GET /stage
 * <dd>Report the current stage of loadshedding as a JSON serialization of a {@code StageDO} data/transfer object.
 * <dt>POST /stage
 * <dd>Set a new loadshedding stage/level by POSTing a JSON-serialized {@code StageDO} instance as the body of the request.
 * </dl>
 */
public class StageService {

    public static final int DEFAULT_STAGE = 0;
    public static final int DEFAULT_PORT = 7001;
    public static final String MQ_TOPIC_NAME = "stage";

    private StageProducer producer;
    private int loadSheddingStage;
    private Javalin server;
    private int servicePort;

    public static void main(String[] args) {
        final StageService svc = new StageService().initialise();
        svc.start();
        System.out.println("Entering main...");
    }

    @VisibleForTesting
    StageService initialise() {
        return initialise(DEFAULT_STAGE);
    }

    @VisibleForTesting
    StageService initialise(int initialStage) {
        StageDO persistedStage = new StagePersistence().loadStage();

        if (persistedStage != null) {
            loadSheddingStage = persistedStage.getStage();
        } else {
            loadSheddingStage = initialStage;
        }
        assert loadSheddingStage >= 0;

        server = initHttpServer();
        System.out.println("Server initialized successfully!");

        try {
            producer = new StageProducer();
            System.out.println("Producer initialized successfully!");
        } catch (JMSException e) {
            LoadshedLoggers.severe("StageProducer could not be initialized", e);
            throw new RuntimeException("Error initializing StageProducer", e);
        }

        return this;
    }

    public void start() {
        server = initHttpServer();
        if (server != null) {
            server.start(DEFAULT_PORT);
            System.out.println("STAGE SERVER STARTED!");
        } else {
            System.out.println("Server was not initialized properly!");
        }
    }

    @VisibleForTesting
    void start(int networkPort) {
        servicePort = networkPort;
        run();
    }

    public void stop() {
        server.stop();
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                LoadshedLoggers.severe("StageProducer failed to close", e);
                throw new RuntimeException(e);
            }
        }
    }

    public void run() {
        server.start(servicePort);
    }

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
                }).get("/", this::getCurrentStage)
                .get("/stage", this::getCurrentStage)
                .post("/stage", this::setNewStage);
    }

    /**
     * Retrieves the current loadshedding stage.
     *
     * @param ctx the context of the HTTP request
     * @return the context with the current stage as a JSON response
     */
    private Context getCurrentStage(Context ctx) {
        return ctx.json(new StageDO(loadSheddingStage));
    }

    /**
     * Sets a new load shedding stage.
     *
     * This method extracts the stage value from the request body, validates it, updates the internal stage value, broadcasts a stage change event, and returns the updated stage as a JSON response.
     *
     * @param ctx the current context of the request
     * @return the updated context with the new stage and HTTP status code
     * @throws JMSException if an error occurs while sending the message to the producer
     */
    private Context setNewStage(Context ctx) throws JMSException {
        final StageDO stageData = ctx.bodyAsClass(StageDO.class);
        final int newStage = stageData.getStage();

        System.out.println("New Stage: " + newStage);

        if (newStage >= 0) {
            loadSheddingStage = newStage;
            new StagePersistence().saveStage(new StageDO(loadSheddingStage));
            broadcastStageChangeEvent(ctx);
            ctx.status(HttpStatus.OK);
        } else {
            ctx.status(HttpStatus.BAD_REQUEST);
        }

        return ctx.json(new StageDO(loadSheddingStage));
    }

    /**
     * Broadcasts a stage change event to the JMS producer.
     *
     * @param ctx the context of the HTTP request containing the stage change data
     */
    private void broadcastStageChangeEvent(Context ctx) {
        try {
            String args = ctx.body();
            producer.send(args);
            System.out.println("New stage message sent to subscribers!");
        } catch (JMSException e) {
            LoadshedLoggers.severe("Message could not be sent to JMS producer.", e);
            throw new RuntimeException(e);
        }
    }
}
