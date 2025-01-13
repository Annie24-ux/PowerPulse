package wethinkcode.places.db.memory;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

/**
 * In-memory implementation of the {@link Places} interface.
 * <p>
 * This class maintains a collection of {@link Town} objects and provides methods to:
 * <ul>
 * <li>Retrieve a list of unique provinces represented in the data.
 * <li>Retrieve all towns in a specific province.
 * <li>Get the total number of towns stored.
 * </ul>
 */
public class PlacesDb implements Places {
    private final Set<Town> towns = new TreeSet<>();

    /**
     * Constructs a new {@code PlacesDb} with the provided set of towns.
     *
     * @param places a {@code Set} of {@link Town} objects to initialize the database
     */
    public PlacesDb(Set<Town> places) {
        towns.addAll(places);
    }

    /**
     * Returns a collection of unique province names.
     *
     * @return a {@code Collection} of province names as {@code String}
     */
    @Override
    public Collection<String> provinces() {
        return towns.parallelStream()
                .map(town -> town.getProvince())
                .collect(Collectors.toSet());
    }

    /**
     * Returns a collection of towns located in the specified province.
     *
     * @param aProvince the name of the province
     * @return a {@code Collection} of {@link Town} objects in the specified province
     */
    @Override
    public Collection<Town> townsIn(String aProvince) {
        return towns.parallelStream()
                .filter(aTown -> aTown.getProvince().equals(aProvince))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the total number of towns stored in the database.
     *
     * @return the size of the town collection as an {@code int}
     */
    @Override
    public int size() {
        return towns.size();
    }
}