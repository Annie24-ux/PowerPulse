package topic;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * A JMS producer that connects to an ActiveMQ broker and sends messages to a topic.
 */
public class StageProducer {
    private static final String MQ_URL = "tcp://localhost:61616";
    private static final String MQ_USER = "admin";
    private static final String MQ_PASSWD = "admin";
    private static final String MQ_TOPIC_NAME = "stage";

    private Connection connection;
    private Session session;
    private MessageProducer producer;

    /**
     * Initializes the producer by creating a connection to the ActiveMQ broker,
     * starting a session, and preparing a message producer for the topic.
     *
     * @throws JMSException if an error occurs during initialization.
     */
    public StageProducer() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ_URL);
        connection = factory.createConnection(MQ_USER, MQ_PASSWD);
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(MQ_TOPIC_NAME);
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        System.out.println("Connection established...");
    }

    /**
     * Sends a text message to the ActiveMQ topic.
     *
     * @param message the message to be sent.
     * @throws JMSException if an error occurs during message transmission.
     */
    public void send(String message) throws JMSException {
        System.out.println("Entering send body....");
        System.out.println("send body message: " + message);
        TextMessage textMessage = session.createTextMessage(message);
        System.out.println("Sending message: " + message);
        producer.send(textMessage);
    }

    /**
     * Closes the ActiveMQ connection, session, and producer to release resources.
     *
     * @throws JMSException if an error occurs while closing resources.
     */
    public void close() throws JMSException {
        producer.close();
        session.close();
        connection.close();
        System.out.println("Closing all connection");
    }
}
