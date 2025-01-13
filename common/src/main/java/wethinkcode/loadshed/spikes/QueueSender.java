package wethinkcode.loadshed.spikes;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * The QueueSender class is responsible for sending MQ messages to the specified
 * queue in the Stage Service. The messages can be provided through command-line
 * arguments or default to a single message if none are provided.
 */
public class QueueSender implements Runnable {

    /**
     * The time (in milliseconds) to pause between operations.
     */
    private static long NAP_TIME = 2000;

    /**
     * The URL of the MQ broker.
     */
    public static final String MQ_URL = "tcp://localhost:61616";

    /**
     * The username for connecting to the MQ broker.
     */
    public static final String MQ_USER = "admin";

    /**
     * The password for connecting to the MQ broker.
     */
    public static final String MQ_PASSWD = "admin";

    /**
     * The name of the MQ queue to which messages will be sent.
     */
    public static final String MQ_QUEUE_NAME = "alert";

    /**
     * The main method that initializes the QueueSender and starts the message sending process.
     *
     * @param args Command line arguments that provide messages to send.
     */
    public static void main( String[] args ){
        final QueueSender app = new QueueSender(args);
        app.run();
    }

    private String[] cmdLineMsgs;
    private Connection connection;
    private Session session;

    /**
     * Constructor for the QueueSender class.
     * It initializes the list of messages to be sent based on command-line arguments.
     *
     * @param args Command line arguments that contain messages to be sent.
     */
    public QueueSender(String[] args){
        cmdLineMsgs = args;
    }

    /**
     * The run method that sets up the MQ connection and sends messages to the queue.
     * It creates a connection, sets up the session, and sends all provided messages.
     * After sending, it closes the connection and session resources.
     */
    @Override
    public void run(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ_URL );
            connection = factory.createConnection( MQ_USER, MQ_PASSWD );
            connection.start();

            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            sendAllMessages( cmdLineMsgs.length == 0
                    ? new String[]{ "{ \"stage\":17 }" }
                    : cmdLineMsgs );
        }catch( JMSException erk ){
            throw new RuntimeException( erk );
        }finally{
            closeResources();
        }
        System.out.println( "Bye..." );
    }

    /**
     * Sends all messages provided to the MQ queue.
     * Each message is sent as a TextMessage to the queue.
     *
     * @param messages The array of messages to send.
     * @throws JMSException If there is an issue sending the messages.
     */
    private void sendAllMessages( String[] messages ) throws JMSException {
        Destination destination = session.createQueue(MQ_QUEUE_NAME);
        MessageProducer producer = session.createProducer(destination);

        for(String message : messages){
            TextMessage textMessage = session.createTextMessage(message + '\n');
            producer.send(textMessage);
        }
        producer.close();
        System.out.println("Successfully sent!");
    }

    /**
     * Closes the session and connection resources. It ensures proper resource cleanup
     * after sending messages, regardless of any exceptions encountered.
     */
    private void closeResources(){
        try{
            if( session != null ) session.close();
            if( connection != null ) connection.close();
        }catch( JMSException ex ){
            // Ignore JMS exception during resource closure
        }
        session = null;
        connection = null;
    }
}
