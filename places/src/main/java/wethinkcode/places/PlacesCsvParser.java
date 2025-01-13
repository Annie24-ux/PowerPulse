package wethinkcode.places;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import wethinkcode.places.db.memory.PlacesDb;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

import static java.util.Objects.requireNonNull;

/**
 * PlacesCsvParser : Parses a CSV file containing place data. The CSV file is expected
 * to have fields (in order): Name, Feature_Description, pklid, Latitude, Longitude, Date,
 * MapInfo, Province, fklFeatureSubTypeID, Previous_Name, fklMagisterialDistrictID, ProvinceID,
 * fklLanguageID, fklDisteral, Local Municipality, Sound, District Municipality, fklLocalMunic,
 * Comments, and Meaning.
 *
 * <p>
 * Only the fields <code>Name</code>, <code>Feature_Description</code>, and <code>Province</code>
 * are relevant for parsing towns and urban areas.
 * </p>
 */
public class PlacesCsvParser {

    /**
     * Parses a CSV file to create a {@link Places} database object containing relevant place data.
     *
     * @param csvFile the CSV file to parse.
     * @return a {@link Places} object populated with towns and urban areas from the CSV.
     * @throws FileNotFoundException if the file does not exist or cannot be read.
     * @throws IOException if an I/O error occurs during parsing.
     */
    public Places parseCsvSource(File csvFile) throws FileNotFoundException, IOException {
        requireNonNull(csvFile);
        if (!(csvFile.exists() && csvFile.canRead())) {
            throw new FileNotFoundException("Required CSV input file " + csvFile.getPath() + " not found.");
        }

        return parseCsvSource(new LineNumberReader(new FileReader(csvFile)));
    }

    /**
     * Parses the CSV data using a {@link LineNumberReader}.
     *
     * @param reader the {@link LineNumberReader} containing the CSV data.
     * @return a {@link Places} object populated with relevant town data.
     * @throws IOException if an I/O error occurs during reading.
     */
    public Places parseCsvSource(LineNumberReader reader) throws IOException {
        try (final LineNumberReader in = Objects.requireNonNull(reader)) {
            in.readLine(); // Skip the header line.
            final Places db = parseDataLines(in);
            in.getLineNumber();
            return db;
        }
    }

    /**
     * Parses the data lines from the CSV to extract town information.
     *
     * @param in the {@link LineNumberReader} containing the CSV data lines.
     * @return a {@link Places} database object populated with towns.
     */
    @VisibleForTesting
    Places parseDataLines(final LineNumberReader in) {
        final Set<Town> allTowns = in.lines()
                .map(this::splitLineIntoValues)
                .filter(this::isLineAWantedFeature)
                .filter(this::isProvinceValid)
                .map(this::asTown)
                .collect(Collectors.toSet());
        return new PlacesDb(allTowns);
    }

    /**
     * Determines if a line represents a feature of interest (e.g., Urban Area, Town, Township).
     *
     * @param csvValue an array of values from the CSV line.
     * @return true if the line represents a feature of interest; false otherwise.
     */
    @VisibleForTesting
    boolean isLineAWantedFeature(String[] csvValue) {
        if (csvValue.length <= FEATURE_COLUMN) {
            return false;
        }
        return WANTED_FEATURES.contains(csvValue[FEATURE_COLUMN].toLowerCase());
    }

    /**
     * Returns a list of valid province names.
     *
     * @return a list of province names recognized as valid.
     */
    public List<String> validProvinces() {
        return List.of("Mpumalanga", "Limpopo", "Western Cape", "Eastern Cape", "North West",
                "KwaZulu-Natal", "Northern Cape", "Free State", "Gauteng");
    }

    /**
     * Validates if a province field in a CSV line contains a recognized province name.
     *
     * @param csvValue an array of values from the CSV line.
     * @return true if the province is valid; false otherwise.
     */
    private boolean isProvinceValid(String[] csvValue) {
        if (csvValue.length <= PROVINCE_COLUMN || csvValue[PROVINCE_COLUMN].isBlank()) {
            return false;
        }
        return validProvinces().contains(csvValue[PROVINCE_COLUMN]);
    }

    /**
     * Splits a CSV line into an array of string values.
     *
     * @param aCsvLine a single line from the CSV file.
     * @return an array of string values parsed from the line.
     */
    @VisibleForTesting
    String[] splitLineIntoValues(String aCsvLine) {
        final String[] listValues = aCsvLine.trim().split(",");
        return listValues;
    }

    /**
     * Converts a CSV line into a {@link Town} object.
     *
     * @param values an array of values from the CSV line.
     * @return a {@link Town} object representing the place.
     */
    @VisibleForTesting
    Town asTown(String[] values) {
        return new Town(values[NAME_COLUMN], values[PROVINCE_COLUMN]);
    }

    private static final Set<String> WANTED_FEATURES = Set.of(
            "Urban Area".toLowerCase(),
            "Town".toLowerCase(),
            "Township".toLowerCase()
    );

    static final int NAME_COLUMN = 0;
    static final int FEATURE_COLUMN = 1;
    static final int PROVINCE_COLUMN = 7;
    static final int MIN_COLUMNS = PROVINCE_COLUMN + 1;
    static final int MAX_COLUMNS = 20;
}