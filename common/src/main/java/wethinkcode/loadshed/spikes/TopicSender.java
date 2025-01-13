package wethinkcode.loadshed.spikes;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.alerts.LoadshedLoggers;

/**
 * A small "maker" app for sending messages to a topic on an MQ server.
 * This application connects to a JMS topic named "stage" and sends messages
 * to the topic, which can be consumed by subscribers.
 */
public class TopicSender implements Runnable {

    private static long NAP_TIME = 2000; // ms

    public static final String MQ_URL = "tcp://localhost:61616";
    public static final String MQ_USER = "admin";
    public static final String MQ_PASSWD = "admin";
    public static final String MQ_TOPIC_NAME = "stage";

    /**
     * The main method for starting the TopicSender application.
     * It initializes the application, passes any command-line arguments,
     * and invokes the `run()` method to begin sending messages to the MQ topic.
     *
     * @param args Command-line arguments containing the messages to be sent.
     */
    public static void main(String[] args) {
        final TopicSender app = new TopicSender();
        app.cmdLineMsgs = args;
        app.run();
    }

    private String[] cmdLineMsgs;
    private Connection connection;
    private Session session;

    /**
     * The `run()` method connects to the MQ server, creates a session,
     * and sends messages to the specified MQ topic.
     * It also handles exceptions and cleans up resources in the `finally` block.
     */
    @Override
    public void run() {
        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ_URL);
            connection = factory.createConnection(MQ_USER, MQ_PASSWD);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sendMessages(cmdLineMsgs.length == 0
                    ? new String[]{ "{ \"stage\":17 }" }
                    : cmdLineMsgs);

        } catch (JMSException erk) {
            LoadshedLoggers.severe("Failed to set up Message Listener.", erk);
            throw new RuntimeException(erk);
        } finally {
            closeResources();
            System.out.println("Bye...");
        }
    }

    /**
     * Sets the command-line messages that will be sent to the MQ topic.
     *
     * @param cmdLineMsgs Array of messages passed from the command line.
     */
    public void setCmdLineMsgs(String[] cmdLineMsgs) {
        this.cmdLineMsgs = cmdLineMsgs;
    }

    /**
     * Sends a set of messages to the MQ topic.
     * The method creates a `MessageProducer`, sends the provided messages to
     * the MQ topic, and ensures the producer is closed after sending.
     *
     * @param messages Array of messages to be sent to the MQ topic.
     * @throws JMSException If an error occurs while sending the messages.
     */
    private void sendMessages(String[] messages) throws JMSException {
        Destination des = session.createTopic(MQ_TOPIC_NAME);
        MessageProducer messageProducer = session.createProducer(des);

        for (String message : messages) {
            TextMessage textMessage = session.createTextMessage(message + '\n');
            messageProducer.send(textMessage);
        }
        messageProducer.close();
        System.out.println("Topic message sent...");
    }

    /**
     * Closes the session and connection to the MQ server.
     * This method ensures that all resources are properly released and cleaned up.
     */
    private void closeResources() {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException ex) {
            LoadshedLoggers.severe("Failed to close connection.", ex);
        }
        session = null;
        connection = null;
    }
}
