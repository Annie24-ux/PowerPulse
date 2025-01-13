package topic;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import wethinkcode.loadshed.alerts.LoadshedLoggers;
import wethinkcode.loadshed.common.mq.MQ;
import wethinkcode.loadshed.common.transfer.StageDO;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import wethinkcode.loadshed.spikes.TopicReceiver;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service by
 * subscribing to a Topic.
 */
public class listener implements Runnable {

    private static long NAP_TIME = 2000; //ms

    public static final String MQ_TOPIC_NAME = "stage";
    public int stageRn;

    /**
     * Main method to run the listener.
     * Initializes the listener and starts it.
     */
    public static void main(String[] args) {
        final listener app = new listener();
        app.run();
    }

    private boolean running = true;

    private Connection connection;

    /**
     * Starts the listener and listens for incoming messages from the MQ.
     * The listener runs in a loop and keeps receiving messages until stopped.
     */
    @Override
    public void run() {
        setUpMessageListener();
        while (running) {
            System.out.println("Still doing stuff...");
            snooze();
        }
        closeConnection();
        System.out.println("Bye...");
    }

    /**
     * Sets up the message listener to subscribe to the MQ topic.
     * It creates a connection to the message queue and listens for new messages.
     */
    private void setUpMessageListener() {
        System.out.println("Setting up listener....");
        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);
            connection = factory.createConnection(MQ.USER, MQ.PASSWD);

            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination dest = session.createTopic(MQ_TOPIC_NAME); // <-- NB: Topic, not Queue!

            final MessageConsumer receiver = session.createConsumer(dest);
            receiver.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message m) {
                    try {
                        String bodyText = ((TextMessage) m).getText();
                        JSONObject stageData = textToObject(bodyText);
                        int stage = stageData.getInt("stage");
                        stageRn = stage;
                        System.out.println("Stage: " + stageRn);
                        System.out.println("Receiving message from topic: " + bodyText);
                    } catch (JMSException e) {
                        e.printStackTrace();
                        LoadshedLoggers.severe("Failed to read message from producer", e);
                    }
                }
            });

            connection.start();

        } catch (JMSException erk) {
            LoadshedLoggers.severe("Activemq set up failed to initialize", erk);
            throw new RuntimeException(erk);
        }
    }

    /**
     * Converts the received text message into a JSON object.
     *
     * @param text the text of the message to be converted
     * @return the JSON object representation of the text
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
     * Pauses the listener for a specified amount of time before checking for more messages.
     */
    private void snooze() {
        try {
            Thread.sleep(NAP_TIME);
        } catch (InterruptedException eek) {
            LoadshedLoggers.severe("Listener thread failed to sleep/snooze", eek);
        }
    }

    /**
     * Closes the connection to the MQ when the listener is stopped.
     */
    private void closeConnection() {
        System.out.println("Connections closing now..");
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                LoadshedLoggers.severe("Listener connection failed to close", ex);
            }
        }
    }
}
