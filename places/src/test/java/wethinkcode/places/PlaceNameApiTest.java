package wethinkcode.places;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import org.junit.jupiter.api.*;
import wethinkcode.places.model.Places;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * *Functional* tests of the PlaceNameService.
 */
public class PlaceNameApiTest
{
    public static final int TEST_PORT = 7777;

    private static PlaceNameService server;

    @BeforeAll
    public static void startServer() throws IOException{
        try(
            final LineNumberReader input = new LineNumberReader( new StringReader( PlacesTestData.CSV_DATA ) ); ){
            final PlacesCsvParser parser = new PlacesCsvParser();
            final Places testDb = parser.parseDataLines( input );
            server = new PlaceNameService().initialise( testDb );
            server.start( TEST_PORT );
        }
    }

    @AfterAll
    public static void stopServer(){
        server.stop();
    }

    @Test
    public void getProvincesJson(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/provinces" ).asJson();

        assertEquals( HttpStatus.OK, response.getStatus() );
        assertEquals( "application/json", response.getHeaders().getFirst( "Content-Type" ) );

        JSONArray jsonArray = response.getBody().getArray();
        assertEquals( 5, jsonArray.length() );
    }

    @Test
    public void getTownsInAProvince_provinceExistsInDb(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/towns/KwaZulu-Natal" ).asJson();

        assertEquals( HttpStatus.OK, response.getStatus() );
        assertEquals( "application/json", response.getHeaders().getFirst( "Content-Type" ) );

        JSONArray jsonArray = response.getBody().getArray();
        assertTrue( jsonArray.length() > 0 );

    }

    @Test
    public void getTownsInAProvince_noSuchProvinceInDb(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/towns/Oregon" ).asJson();

        assertEquals( HttpStatus.OK, response.getStatus() );
        assertEquals( "application/json", response.getHeaders().getFirst( "Content-Type" ) );

        JSONArray jsonArray = response.getBody().getArray();
        assertEquals( 0, jsonArray.length() );

    }

    private String serverUrl(){
        return "http://localhost:" + TEST_PORT;
    }
}
