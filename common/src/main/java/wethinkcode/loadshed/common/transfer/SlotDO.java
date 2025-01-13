package wethinkcode.loadshed.common.transfer;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Object representing a time slot for load shedding, with a start and end time.
 * This class models a specific time period during which load shedding occurs.
 */
public class SlotDO {

    private LocalTime start;

    private LocalTime end;

    /**
     * Default constructor for SlotDO.
     * This constructor is used to create an empty SlotDO object.
     */
    public SlotDO(){
    }

    /**
     * Constructs a SlotDO object with the provided start and end times for load shedding.
     *
     * @param from the start time of the load shedding slot.
     * @param to the end time of the load shedding slot.
     */
    @JsonCreator
    public SlotDO(
            @JsonProperty( value = "from" ) LocalTime from,
            @JsonProperty( value = "to" ) LocalTime to ){
        start = from;
        end = to;
    }

    /**
     * Returns the start time of the load shedding slot.
     *
     * @return the start time of the slot.
     */
    public LocalTime getStart(){
        return start;
    }

    /**
     * Returns the end time of the load shedding slot.
     *
     * @return the end time of the slot.
     */
    public LocalTime getEnd(){
        return end;
    }
}
