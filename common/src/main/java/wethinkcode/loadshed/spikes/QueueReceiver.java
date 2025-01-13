package wethinkcode.loadshed.spikes;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.alerts.LoadshedLoggers;
import wethinkcode.loadshed.common.mq.MQ;

/**
 * A simple application for receiving messages from a message queue.
 * This app connects to a queue named "stage", listens for incoming messages,
 * and processes them when received. It uses ActiveMQ for message communication.
 */
public class QueueReceiver implements Runnable {

    private static long NAP_TIME = 2000; // ms

    public static final String MQ_QUEUE_NAME = "stage";

    /**
     * Main method to start the QueueReceiver application.
     * It creates an instance of QueueReceiver and starts it.
     *
     * @param args Command-line arguments (not used in this case).
     */
    public static void main(String[] args) {
        final QueueReceiver app = new QueueReceiver();
        app.run();
    }

    private boolean running = true;
    private Connection connection;

    /**
     * The run method that continuously processes the queue messages.
     * It sets up the message listener and keeps the application running
     * until explicitly stopped. The application prints a message indicating
     * it is still doing other tasks.
     */
    @Override
    public void run() {
        setUpMessageListener();
        while (running) {
            // do other tasks...
            System.out.println("Still doing other things...");
            snooze();
        }

        // Close the MQ connection when done (unbalanced Open/Close discussed).
        closeConnection();
        System.out.println("Bye...");
    }

    /**
     * Sets up the message listener for the queue.
     * It connects to the message queue, creates a session, and sets up a
     * message consumer with a listener that processes incoming messages.
     * When a message is received, the `onMessage()` method is invoked.
     */
    private void setUpMessageListener() {
        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);
            connection = factory.createConnection(MQ.USER, MQ.PASSWD);

            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination queueId = session.createQueue(MQ_QUEUE_NAME);

            final MessageConsumer receiver = session.createConsumer(queueId);
            receiver.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message m) {
                    try {
                        if (m instanceof TextMessage) {
                            String queueMessage = ((TextMessage) m).getText();
                            System.out.println("Received queue message: " + queueMessage);
                        } else {
                            System.out.println("Received a non-text message");
                        }
                    } catch (JMSException e) {
                        LoadshedLoggers.severe("Received message of a different type.", e);
                        e.printStackTrace();
                    }
                }
            });
            connection.start();

        } catch (JMSException erk) {
            LoadshedLoggers.severe("Failed to set up Message Listener.", erk);
            throw new RuntimeException(erk);
        }
    }

    /**
     * Pauses the thread for a specified amount of time.
     * This method makes the current thread sleep for the duration of
     * `NAP_TIME` (in milliseconds).
     */
    private void snooze() {
        try {
            Thread.sleep(NAP_TIME);
        } catch (InterruptedException eek) {
            LoadshedLoggers.severe("Thread failed to sleep.", eek);
        }
    }

    /**
     * Closes the connection to the message queue.
     * This method ensures that the connection to the MQ server is closed
     * properly when the application finishes.
     */
    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                LoadshedLoggers.severe("Failed to close connection.", ex);
            }
        }
    }

}
