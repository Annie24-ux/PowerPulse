package wethinkcode.places;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.*;
import picocli.CommandLine;
import wethinkcode.places.model.Places;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration/Functional tests for the PlaceNameService.
 */
public class PlaceNameServiceTest
{
    @Test
    public void getACsvFileIntoTheServer(){
        try{

            final File csvFile = createTestCsvFile();
            final String[] args = {"-f", csvFile.getPath()};
            final PlaceNameService svc = new PlaceNameService();
            new CommandLine( svc ).parseArgs( args );
            svc.initialise();

            final Places db = svc.getDb();
            assertEquals( 5, db.size() );

        }catch( IOException ex ){
            fail( ex );
        }

    }

    private File createTestCsvFile() throws IOException{
        final File f = File.createTempFile( "places", "csv" );
        f.deleteOnExit();

        try(  FileWriter out = new FileWriter( f ) ){
            out.write( PlacesTestData.CSV_DATA );
            return f;
        }
    }
}
