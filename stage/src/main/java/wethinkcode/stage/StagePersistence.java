package wethinkcode.stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import wethinkcode.loadshed.common.transfer.StageDO;

import java.io.File;
import java.io.IOException;

public class StagePersistence {

    private static final String FILE_PATH = "stage.json";

    private ObjectMapper objectMapper;

    public StagePersistence() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Load the persisted stage from the JSON file.
     * @return the loaded StageDO object, or null if there's an error or no file.
     */
    public StageDO loadStage() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                return objectMapper.readValue(file, StageDO.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Save the current stage to the JSON file.
     * @param stage the StageDO object to save.
     */
    public void saveStage(StageDO stage) {
        try {
            objectMapper.writeValue(new File(FILE_PATH), stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
