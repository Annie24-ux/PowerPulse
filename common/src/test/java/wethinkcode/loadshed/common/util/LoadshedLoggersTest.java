//package wethinkcode.loadshed.common.util;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import static com.google.common.base.CharMatcher.any;
//import static javax.management.Query.eq;
//import static jdk.internal.org.objectweb.asm.util.CheckClassAdapter.verify;
//import static org.mockito.Mockito.*;
//
//public class LoadshedLoggersTest {
//
//    @Test
//    void testSevereLogsCorrectly() {
//        // Mock the logger
//        Logger mockLogger = mock(Logger.class);
//        LoadshedLoggers.log(Level.SEVERE, "Test Message", new RuntimeException("Test Exception"));
//
//        // Verify that the logger was called with correct arguments
//        verify(mockLogger).log(eq(Level.SEVERE), eq("Test Message"), any(Throwable.class));
//    }
//}
//
//
//
//
//
//
//
//
