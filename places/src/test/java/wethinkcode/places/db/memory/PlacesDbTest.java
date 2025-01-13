package wethinkcode.places.db.memory;

import java.util.Set;

import org.junit.jupiter.api.*;
import wethinkcode.places.model.Town;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Mike Morris <mikro2nd@gmail.com>
 */
public class PlacesDbTest
{
    public static final Set<Town> TOWNS = Set.of(
        new Town( "Cape Town", "Western Cape" ),
        new Town( "Worcester", "Western Cape" ),
        new Town( "Riversdale", "Western Cape" ),
        new Town( "Gqeberha", "Eastern Cape" ),
        new Town( "Queenstown", "Eastern Cape" ),
        new Town( "Sandton-East", "Gauteng" ),
        new Town( "Riversdale", "Gauteng" ),
        new Town( "Mabopane", "Gauteng" ),
        new Town( "Brakpan", "Gauteng" )
    );

    @Test
    public void testProvinces(){
        final PlacesDb db = new PlacesDb( TOWNS );
        assertThat( db.provinces().size() ).isEqualTo( 3 );
    }

    @Test
    public void testTownsInProvince(){
        final PlacesDb db = new PlacesDb( TOWNS );
        assertThat( db.townsIn( "Gauteng" ).size() ).isEqualTo( 4 );
        assertThat( db.townsIn( "Eastern Cape" ).size() ).isEqualTo( 2 );
        assertThat( db.townsIn( "Western Cape" ).size() ).isEqualTo( 3 );
        assertThat( db.townsIn( "Northern Cape" ) ).isEmpty();
    }
}
