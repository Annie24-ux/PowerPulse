//package wethinkcode.stage;
//
//
//import java.util.concurrent.SynchronousQueue;
//
//import javax.jms.*;
//
//import kong.unirest.*;
//import kong.unirest.json.JSONException;
//import kong.unirest.json.JSONObject;
//import org.apache.activemq.ActiveMQConnectionFactory;
//import org.junit.jupiter.api.*;
//import wethinkcode.loadshed.common.transfer.StageDO;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * I test StageService message sending.
// */
//@Disabled
//@Tag( "expensive" )
//public class StageServiceMQTest
//{
//    public static final int TEST_PORT = 7777;
//
//    private static StageService server;
//
//    private static ActiveMQConnectionFactory factory;
//
//    private static Connection mqConnection;
//    private static int queuedStage;
//    private static String stageQueueBody;
//
//    @BeforeAll
//    public static void startInfrastructure() throws JMSException {
//        startMsgQueue();
//        startStageSvc();
//    }
//
//    @AfterAll
//    public static void cleanup() throws JMSException {
//        server.stop();
//        if(mqConnection != null){
//            mqConnection.close();
//        }
//    }
//
//    public void connectMqListener(MessageListener listener) throws JMSException {
//        mqConnection = factory.createConnection();
//        final Session session = mqConnection.createSession( false, Session.AUTO_ACKNOWLEDGE );
//        final Destination dest = session.createTopic( StageService.MQ_TOPIC_NAME );
//
//        final MessageConsumer receiver = session.createConsumer( dest );
//        receiver.setMessageListener(listener );
//
//        mqConnection.start();
//    }
//
//    @AfterEach
//    public void closeMqConnection() throws JMSException {
//        if(mqConnection != null){
//            mqConnection.close();
//        }
//        mqConnection = null;
//    }
//
//    public MessageConsumer connectMqConsumer() throws JMSException {
//        mqConnection = factory.createConnection();
//        final Session session = mqConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//        final Destination dest = session.createTopic(StageService.MQ_TOPIC_NAME);
//
//        final MessageConsumer receiver = session.createConsumer(dest);
//        mqConnection.start();
//
//        return receiver;
//    }
//
//
//    @Test
//    public void sendMqEventWhenStageChanges() {
//        MessageConsumer receiver;
//        try {
//            receiver = connectMqConsumer();
//        } catch (JMSException e) {
//            throw new RuntimeException("Failed to create MQ consumer", e);
//        }
//
//        final HttpResponse<StageDO> startStage = Unirest.get(serverUrl() + "/stage").asObject(StageDO.class);
//        assertEquals(HttpStatus.OK, startStage.getStatus());
//
//        final StageDO data = startStage.getBody();
//        final int newStage = data.getStage() + 1;
//
//        final HttpResponse<JsonNode> changeStage = Unirest.post(serverUrl() + "/stage")
//                .header("Content-Type", "application/json")
//                .body(new StageDO(newStage))
//                .asJson();
//        assertEquals(HttpStatus.OK, changeStage.getStatus());
//
//        Message message = null;
//        try {
//            message = receiver.receive(5000);
//        } catch (JMSException e) {
//            throw new RuntimeException(e);
//        }
//            if (message instanceof TextMessage) {
//                TextMessage queueMessage = (TextMessage) message;
//                try {
//                    stageQueueBody = queueMessage.getText();
//                } catch (JMSException e) {
//                    throw new RuntimeException(e);
//                }
//
//                JSONObject queueData = queueTextIsAnObject(stageQueueBody);
//                queuedStage = queueData.getInt("stage");
//
//                System.out.println("Posted new stage: " + newStage);
//                System.out.println("Received queued stage: " + queuedStage);
//            } else {
//                System.out.println("Received a non-text message");
//            }
//            assertNotNull(stageQueueBody);
//            assertEquals(newStage, queuedStage);
//        }
//
//
//    private static void startMsgQueue() throws JMSException {
//        factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
//    }
//
//    private static void startStageSvc(){
//        server = new StageService().initialise();
//        server.start( TEST_PORT );
//    }
//
//
//    private String serverUrl(){
//        return "http://localhost:" + TEST_PORT;
//    }
//
//    private JSONObject textToObject(String text){
//        try {
//            return new JSONObject(text);
//        }catch (JSONException err){
//            err.printStackTrace();
//        }
//        return null;
//    }
//
//
//    public JSONObject queueTextIsAnObject(String queueBody){
//        JSONObject expectedResult = textToObject(queueBody);
//        System.out.println("Res: "+expectedResult);
//        int x = expectedResult.getInt("stage");
//        System.out.println("Stage: "+ x);
//        return expectedResult;
//    }
//
//}
