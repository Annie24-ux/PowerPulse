package wethinkcode.stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StageServiceTest {

    private StageService stageService;

    @BeforeEach
    void setUp() {
        stageService = new StageService().initialise();
    }

    @Test
    void testInitialise() {
        assertNotNull(stageService, "StageService should be initialized");
    }


}
