//package wethinkcode.schedule;
//
//
//import org.junit.jupiter.api.Test;
//import kong.unirest.json.JSONObject;
//import static org.junit.jupiter.api.Assertions.*;
//
//class ScheduleAdditionalTests {
//
//    @Test
//    void testTextToObjectValidJson() {
//        ScheduleService scheduleService = new ScheduleService();
//        String validJson = "{\"stage\": 2}";
//        JSONObject jsonObject = scheduleService.textToObject(validJson);
//
//        assertNotNull(jsonObject);
//        assertEquals(2, jsonObject.getInt("stage"));
//    }
//
//    @Test
//    void testTextToObjectInvalidJson() {
//        ScheduleService scheduleService = new ScheduleService();
//        String invalidJson = "Invalid json string";
//        JSONObject jsonObject = scheduleService.textToObject(invalidJson);
//
//        assertNull(jsonObject);
//    }
//}
//
