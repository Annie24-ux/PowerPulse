package wethinkcode.stage;

import java.io.IOException;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONException;
import org.junit.jupiter.api.*;
import wethinkcode.loadshed.common.transfer.StageDO;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I contain functional tests of the Stage Service.
 */
@Tag( "expensive" )
public class StageServiceAPITest
{
    public static final int TEST_PORT = 7777;

    private static StageService server;

    @BeforeAll
    public static void startServer() throws IOException{
        server = new StageService().initialise();
        server.start( TEST_PORT );
    }

    @AfterAll
    public static void stopServer(){
        server.stop();
    }

    @Test
    public void setNewStage_validStage(){
        final int NEW_STAGE = 4;
        HttpResponse<JsonNode> post = Unirest.post( serverUrl() + "/stage" )
            .header( "Content-Type", "application/json" )
            .body( new StageDO( NEW_STAGE ))
            .asJson();
        assertEquals( HttpStatus.OK, post.getStatus() );

        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/stage" ).asJson();
        assertEquals( HttpStatus.OK, response.getStatus() );
        assertEquals( "application/json", response.getHeaders().getFirst( "Content-Type" ) );

        final int stage = getStageFromResponse( response );
        assertEquals( NEW_STAGE, stage );
    }

    @Test
    public void setNewStage_illegalStageValue(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/stage" ).asJson();
        assertEquals( HttpStatus.OK, response.getStatus() );
        assertEquals( "application/json", response.getHeaders().getFirst( "Content-Type" ) );
        final int oldStage = getStageFromResponse( response );

        final int NEW_STAGE = -1;
        final HttpResponse<JsonNode> post = Unirest.post( serverUrl() + "/stage" )
            .header( "Content-Type", "application/json" )
            .body( new StageDO( NEW_STAGE ))
            .asJson();
        assertEquals( HttpStatus.BAD_REQUEST, post.getStatus() );

        final HttpResponse<JsonNode> check = Unirest.get( serverUrl() + "/stage" ).asJson();
        assertEquals( HttpStatus.OK, check.getStatus() );
        assertEquals( "application/json", check.getHeaders().getFirst( "Content-Type" ) );

        final int stage = getStageFromResponse( check );
        assertEquals( oldStage, stage );
    }

    private static int getStageFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        return response.getBody().getObject().getInt( "stage" );
    }

    private String serverUrl(){
        return "http://localhost:" + TEST_PORT;
    }


}
