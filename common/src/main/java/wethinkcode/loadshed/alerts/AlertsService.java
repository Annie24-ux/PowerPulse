package wethinkcode.loadshed.alerts;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MQ;
import wethinkcode.loadshed.alerts.LoadshedLoggers;

/**
 * The AlertsService class is responsible for consuming MQ messages from a specified
 * queue (alert) and processing them. It listens for incoming messages and logs
 * their contents. The service runs continuously until manually stopped.
 *
 * This service is designed to be simple, focusing primarily on receiving and processing
 * alerts through the MQ connection.
 */
public class AlertsService implements Runnable {

    /**
     * The time (in milliseconds) to pause between checks.
     */
    private static long NAP_TIME = 2000;

    /**
     * The name of the MQ queue the service listens to.
     */
    public static final String MQ_QUEUE_NAME = "alert";

    /**
     * The main method to start the AlertsService. It creates an instance of the
     * AlertsService and calls the `run()` method to initiate the listening process.
     *
     * @param args Command line arguments (unused).
     */
    public static void main( String[] args ){
        final AlertsService app = new AlertsService();
        app.run();
    }

    private boolean running = true;
    private Connection connection;

    /**
     * Runs the AlertsService. It sets up the message listener and enters a loop
     * to handle other tasks while continuously listening for MQ messages.
     * The service keeps running until manually stopped, at which point the
     * MQ connection is closed.
     */
    @Override
    public void run(){
        setUpMessageListener();
        while( running ){
            System.out.println( "Still doing other things..." );
            snooze();
        }

        closeConnection();
        System.out.println( "Bye..." );
    }

    /**
     * Sets up the MQ connection, session, and message consumer. It hooks up a
     * MessageListener to the queue to listen for incoming messages. When a message
     * is received, the listener processes the message and logs its content.
     * The method also starts the connection to begin receiving messages.
     *
     * The message listener handles incoming messages asynchronously.
     */
    private void setUpMessageListener(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ.URL );
            connection = factory.createConnection( MQ.USER, MQ.PASSWD );

            final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            final Destination queueId = session.createQueue( MQ_QUEUE_NAME );

            final MessageConsumer receiver = session.createConsumer( queueId );
            receiver.setMessageListener( new MessageListener() {
                /**
                 * Callback method for processing messages received from the queue.
                 * It checks the message type, processes it, and logs the message content.
                 *
                 * @param m The received JMS message.
                 */
                @Override
                public void onMessage( Message m ){
                    try{
                        if(m instanceof TextMessage){
                            String queueMessage = ((TextMessage) m).getText();
                            System.out.println("Received queue message: "+ queueMessage);
                        } else {
                            System.out.println("Received a non-text message");
                        }
                    }catch (JMSException e){
                        LoadshedLoggers.severe("Received message of another type", e);
                        e.printStackTrace();
                    }
                }
            });

            connection.start();

        }catch( JMSException erk ){
            System.out.println("Throwing JMS exception...");
            LoadshedLoggers.severe("Failed to set up MessageListener", erk);
            throw new RuntimeException( erk );
        }
    }

    /**
     * Pauses the service for a specified amount of time to simulate
     * doing other work or avoiding busy-waiting in the main loop.
     *
     * The method handles interruptions during sleep and logs any encountered errors.
     */
    private void snooze(){
        try{
            Thread.sleep( NAP_TIME );
        }catch( InterruptedException eek ){
            LoadshedLoggers.severe("Failed to sleep.", eek);
        }
    }

    /**
     * Closes the connection to the MQ broker. Ensures that the connection is
     * properly closed when the service is stopped.
     *
     * The method logs any errors that occur during the connection closure.
     */
    private void closeConnection(){
        if( connection != null ) try{
            connection.close();
        }catch( JMSException ex ){
            LoadshedLoggers.severe("Failed to close connection.", ex);
        }
    }
}
