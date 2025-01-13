package wethinkcode.places;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.*;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Mike Morris <mikro2nd@gmail.com>
 */
public class PlaceNameConfigTest
{
    @BeforeEach
    public void setUp(){
    }

    @AfterEach
    public void tearDown(){
    }

    @Test
    public void missingConfigFileFallsBackToDefaults() throws IOException {
        final File testConfig = File.createTempFile( "nosuchfile", "properties" );
        final String fileName = testConfig.getPath();
        assertTrue( testConfig.delete() );

        final String[] args = { "-c", fileName };
        final PlaceNameService svc = new PlaceNameService();
        new CommandLine( svc ).parseArgs( args );

        final String defaultDir = System.getProperty( "user.dir" );
        assertThat( svc.getConfig( PlaceNameService.CFG_CONFIG_FILE ))
            .isEqualTo( defaultDir + "/places.properties" );
        assertThat( svc.getConfig( PlaceNameService.CFG_DATA_DIR ))
            .isEqualTo( defaultDir );
        assertThat( svc.getConfig( PlaceNameService.CFG_DATA_FILE ))
            .isEqualTo( defaultDir + "/places.csv" );
    }

    @Test
    public void cmdLineDataConfigOverridesDefaultConfig(){
        final String testConfigFileName = "/tmp/places.properties";
        final String[] args = { "-c", testConfigFileName };
        final PlaceNameService svc = new PlaceNameService();
        new CommandLine( svc ).parseArgs( args );

        assertEquals( testConfigFileName, svc.configFile().getPath() );
    }

    @Test
    public void cmdLineDataFileOverridesConfig(){
        final String testFilename = "/tmp/testData.csv";
        final String[] args = { "-f", testFilename };

        final PlaceNameService svc = new PlaceNameService();
        assertThat( svc.dataFile().getPath() )
            .isNotEqualTo( testFilename );

        new CommandLine( svc ).parseArgs( args );
        assertThat( svc.dataFile().getPath() )
            .isEqualTo( testFilename );
    }

    @Test
    public void cmdLineDataFileOverridesCmdLineDataDir(){
        final String testDirName = "/tmp";
        final String[] args = { "-d", testDirName };

        final PlaceNameService svc = new PlaceNameService();
        assertThat( svc.dataDir().getPath() )
            .isNotEqualTo( testDirName );

        new CommandLine( svc ).parseArgs( args );
        assertThat( svc.dataDir().getPath() )
            .isEqualTo( testDirName );
    }
}
