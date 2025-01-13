package wethinkcode.loadshed.spikes;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.alerts.LoadshedLoggers;
import wethinkcode.loadshed.common.mq.MQ;
import wethinkcode.loadshed.common.transfer.StageDO;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The TopicReceiver class subscribes to a message topic from the Stage Service
 * and processes incoming messages. The receiver listens for messages, extracts
 * stage data, and stores it in a queue.
 */
public class TopicReceiver implements Runnable {

    /**
     * The time (in milliseconds) to pause between operations.
     */
    private static long NAP_TIME = 2000;

    /**
     * The name of the MQ topic to subscribe to.
     */
    public static final String MQ_TOPIC_NAME = "stage";

    /**
     * The stage number received from the MQ messages.
     */
    public int stageRn;

    /**
     * A queue to store the stage numbers received from the messages.
     */
    public final ConcurrentLinkedQueue<Integer> stageRnQueue = new ConcurrentLinkedQueue<>();

    /**
     * The main method that initializes the TopicReceiver and starts the message listening process.
     */
    public static void main(String[] args) {
        final TopicReceiver app = new TopicReceiver();
        app.run();
    }

    private boolean running = true;
    private Connection connection;

    /**
     * The run method that starts the message listener and processes incoming messages.
     * It continuously listens for messages while the receiver is running.
     */
    @Override
    public void run() {
        setUpMessageListener();
        while (running) {
            System.out.println("Still doing stuff from SPIKES...");
            snooze();
        }
        closeConnection();
        System.out.println("Bye...");
    }

    /**
     * Sets up the message listener to receive messages from the topic.
     * This listener processes each incoming message by extracting stage data and storing it.
     */
    private void setUpMessageListener() {
        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);
            connection = factory.createConnection(MQ.USER, MQ.PASSWD);

            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination dest = session.createTopic(MQ_TOPIC_NAME);

            final MessageConsumer receiver = session.createConsumer(dest);
            receiver.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message m) {
                    try {
                        String bodyText = ((TextMessage) m).getText();
                        JSONObject stageData = textToObject(bodyText);
                        int stage = stageData.getInt("stage");
                        stageRn = stage;
                        System.out.println("Int stage: " + stage);

                        System.out.println("Receiving message from topic: " + bodyText);
                    } catch (JMSException e) {
                        LoadshedLoggers.severe("Received message of a different instance", e);
                        e.printStackTrace();
                    }
                }
            });
            connection.start();

        } catch (JMSException erk) {
            LoadshedLoggers.severe("Failed to set up Activemq listener", erk);
            throw new RuntimeException(erk);
        }
    }

    /**
     * Converts the text message into a JSON object.
     *
     * @param text The text message to convert.
     * @return The corresponding JSONObject.
     */
    private JSONObject textToObject(String text) {
        try {
            return new JSONObject(text);
        } catch (JSONException err) {
            err.printStackTrace();
            LoadshedLoggers.severe("Failed to convert into a JSONObject", err);

        }
        return null;
    }

    /**
     * Returns the queue containing the stage numbers that were received.
     *
     * @return The queue containing the stage numbers.
     */
    public Object sendStageQueue() {
        System.out.println("This is stage to be sent: " + stageRnQueue);
        return stageRnQueue;
    }

    /**
     * Pauses the thread for a specified amount of time.
     */
    private void snooze() {
        try {
            Thread.sleep(NAP_TIME);
        } catch (InterruptedException eek) {
            LoadshedLoggers.severe("Failed to snooze", eek);

        }
    }


    /**
     * Closes the connection to the MQ broker.
     */
    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                LoadshedLoggers.severe("Failed to close connection", ex);

            }
        }
    }
}
