package wethinkcode.loadshed.common.transfer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Object representing a day of load shedding with associated slots.
 * This class is used to store the list of load shedding slots for a particular day.
 */
public class DayDO {

    private List<SlotDO> loadSheddingSlots;

    /**
     * Default constructor for DayDO.
     * This constructor is used to create an empty DayDO object.
     */
    public DayDO(){
    }

    /**
     * Constructs a DayDO object with the provided list of load shedding slots.
     *
     * @param slots the list of load shedding slots for the day.
     */
    @JsonCreator
    public DayDO(
            @JsonProperty( value = "slots" ) List<SlotDO> slots ){
        loadSheddingSlots = slots;
    }

    /**
     * Gets the list of load shedding slots for the day.
     *
     * @return the list of load shedding slots.
     */
    public List<SlotDO> getSlots(){
        return loadSheddingSlots;
    }

    /**
     * Returns the number of load shedding slots for the day.
     *
     * @return the size of the load shedding slots list.
     */
    public int numberOfSlots(){
        return getSlots().size();
    }
}
