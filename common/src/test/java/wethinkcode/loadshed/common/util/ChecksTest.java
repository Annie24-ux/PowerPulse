package wethinkcode.loadshed.common.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Mike Morris <mikro2nd@gmail.com>
 */
public class ChecksTest
{
    @Test
    public void checkNotEmpty_nullStringThrows(){
        assertThrows( IllegalArgumentException.class,
            () -> Checks.checkNotEmpty( null ));
    }

    @Test
    public void checkNotEmpty_emptyStringThrows(){
        assertThrows( IllegalArgumentException.class,
            () -> Checks.checkNotEmpty( "" ));
    }

    @Test
    public void checkNotEmpty_nonEmptyStringIsOk(){
        assertDoesNotThrow( () -> Checks.checkNotEmpty( "a" ) );
    }
}
