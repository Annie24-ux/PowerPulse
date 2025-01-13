package wethinkcode.loadshed.common.util;

/**
 * TODO: javadoc Checks
 */
public interface Checks
{
    static String checkNotEmpty( String aString ){
        if( aString == null || aString.isEmpty() ){
            throw new IllegalArgumentException();
        }
        return aString;
    }
}
