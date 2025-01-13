package wethinkcode.loadshed.common.transfer;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Object representing the schedule of load shedding, including the start date
 * and a list of load shedding days.
 * This class models the schedule of load shedding for a particular period.
 *
 * Note: This model is a simplified version and does not handle more complex scenarios.
 */
public class ScheduleDO {

    // Using LocalDate.now() is probably a bug! Is the server in the
    // same timezone as the client? Maybe not.
    // What if the server creates an instance one nanosecond before midnight
    // before sending it to the client who then gets it "the next day"?
    // What could you do about all that?
    private LocalDate startDate = LocalDate.now();

    private List<DayDO> loadSheddingDays;

    /**
     * Default constructor for ScheduleDO.
     * This constructor is used to create an empty ScheduleDO object.
     */
    public ScheduleDO(){
    }

    /**
     * Constructs a ScheduleDO object with the provided list of load shedding days.
     *
     * @param days the list of load shedding days for the schedule.
     */
    @JsonCreator
    public ScheduleDO(
            @JsonProperty( value = "days" ) List<DayDO> days ){
        loadSheddingDays = days;
    }

    /**
     * Gets the list of load shedding days for the schedule.
     *
     * @return the list of load shedding days.
     */
    public List<DayDO> getDays(){
        return loadSheddingDays;
    }

    /**
     * Returns the number of load shedding days in the schedule.
     *
     * @return the size of the load shedding days list.
     */
    public int numberOfDays(){
        return getDays().size();
    }

    /**
     * Returns the start date of the load shedding schedule.
     *
     * @return the start date of the schedule.
     */
    public LocalDate getStartDate(){
        return startDate;
    }
}
