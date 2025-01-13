package wethinkcode.places;

/**
 * Test data common to several of the test suites.
 */
public interface PlacesTestData
{
    public static final String CSV_DATA =
        // First line in the data is just headers and must be ignored.
        // In total, 13 lines including the header line, 5 of these are towns/urban areas.
        //
        // Amatikulu: we want the town, not the station.
        // Botrivier: "Urban Area" counts as a town, but ignore other features.
        // Brakpan: We have 3 Brakpans, one in Gauteng, one in NC, one in FS. We want all the towns.
        // Sibabe : Should not show up at all.
        //
        // Note that this test data is NOT directly from the Places database, but has been
        // modified for test purposes.
        """
        Name,Feature_Description,pklid,Latitude,Longitude,Date,MapInfo,Province,fklFeatureSubTypeID,Previous_Name,fklMagisterialDistrictID,ProvinceID,fklLanguageID,fklDisteral,Local Municipality,Sound,District Municipality,fklLocalMunic,Comments,Meaning
        Amatikulu,Station,95756,-29.05111111,31.53138889,31-05-1989,,KwaZulu-Natal,79,,237,4,16,DC28,uMlalazi,,,KZ284,,
        Amatikulu,Town,95757,-29.04666667,31.52805556,31-05-1989,,KwaZulu-Natal,111,,237,4,16,DC28,uMlalazi,,,KZ284,,
        Botrivier,Station,92996,-34.22666667,19.20611111,31-05-1979,,Western Cape,79,,15,9,16,DC3,Theewaterskloof,,,WC031,,
        Botrivier,Urban Area,92997,-34.22583333,19.205,31-05-1979,,Western Cape,114,,15,9,16,DC3,Theewaterskloof,,,WC031,,
        Botrivier,Mouth,92998,-34.36805556,19.09888889,31-05-1982,,Western Cape,87,,359,9,16,DC3,Overstrand,,,WC032,,
        Brakpan,Non_Perennial,92797,-26.60444444,26.34,01-06-1992,,North West,66,,262,8,16,DC40,Matlosana,,,NW403,,
        Brakpan,Station,92798,-26.24,28.36111111,31-05-1995,,Gauteng,79,,280,3,16,EKU,Ekurhuleni Metro,,,EKU,,
        Brakpan,Urban Area,92799,-26.23527778,28.37,31-05-1995,,Gauteng,114,,280,3,16,EKU,Ekurhuleni Metro,,,EKU,,
        Brakpan,Dry,92800,-27.04583333,20.44638889,01-06-1992,,Northern Cape,65,,136,6,16,DC8,Mier,,,NC081,,
        Brakpan,Urban Area,92801,-27.00583333,20.57805556,01-06-1992,,Northern Cape,65,,136,6,16,DC8,Mier,,,NC081,,
        Brakpan,Town,92802,-27.95111111,26.53333333,30-05-1975,,Free State,68,,155,2,16,DC18,Matjhabeng,,,FS184,,
        Sibabe,Mouth,70814,-27.35666667,30.44444444,01-06-1987,,KwaZulu-Natal,87,,228,4,16,DC25,Utrecht,,,KZ253,,
        """ ;
}
