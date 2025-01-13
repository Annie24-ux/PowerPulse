package stageProducer;


import org.junit.jupiter.api.*;
import topic.StageProducer;

import javax.jms.*;

import static org.junit.jupiter.api.Assertions.*;

class StageProducerIntegrationTest {

    private StageProducer stageProducer;

    @BeforeEach
    void setUp() throws JMSException {
        stageProducer = new StageProducer();
    }

    @AfterEach
    void tearDown() throws JMSException {
        stageProducer.close();
    }

    @Test
    void testSendMessage() {
        String message = "Integration Test Message";
        assertDoesNotThrow(() -> stageProducer.send(message));
    }
}

