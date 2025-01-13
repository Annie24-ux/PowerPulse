package wethinkcode.places.model;


import java.util.Collection;

/**
 * I am the "database" of place-names. You should write a class that implements me.
 * Your class might have other operations that I have not listed... that's OK.
 */
public interface Places
{
    Collection<String> provinces();

    Collection<Town> townsIn( String aProvince );

    int size();
}