//package wethinkcode.schedule;
//
//import java.util.Optional;
//
//import org.junit.jupiter.api.*;
//import wethinkcode.loadshed.common.transfer.ScheduleDO;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class ScheduleServiceTest
//{
//    private ScheduleService testSvc;
//
//    @BeforeEach
//    public void initTestScheduleFixture(){
//        testSvc = new ScheduleService();
//    }
//
//    @AfterEach
//    public void destroyTestFixture(){
//        testSvc = null;
//    }
//
//    @Test
//    public void testSchedule_someTown(){
//        final Optional<ScheduleDO> schedule = testSvc.getSchedule( "Eastern Cape", "Gqeberha", 4 );
//        assertThat( schedule.isPresent() );
//        assertEquals( 4, schedule.get().numberOfDays() );
//    }
//
//    @Test
//    public void testSchedule_nonexistentTown(){
//        final Optional<ScheduleDO> schedule = testSvc.getSchedule( "Mars", "Elonsburg", 2 );
//        assertThat( schedule.isEmpty() );
//    }
//
//        @Test
//    void testGetCurrentStageThreadSafety() throws InterruptedException {
//        ScheduleService scheduleService = new ScheduleService();
//
//        Runnable task = () -> {
//            for (int i = 0; i < 1000; i++) {
//                scheduleService.setCurrentStage(i % 9);
//                int currentStage = ScheduleService.getCurrentStage();
//                assertTrue(currentStage >= 0 && currentStage <= 8, "Stage out of bounds");
//            }
//        };
//
//        Thread thread1 = new Thread(task);
//        Thread thread2 = new Thread(task);
//
//        thread1.start();
//        thread2.start();
//
//        thread1.join();
//        thread2.join();
//
//        int finalStage = ScheduleService.getCurrentStage();
//        assertTrue(finalStage >= 0 && finalStage <= 8);
//    }
//}
