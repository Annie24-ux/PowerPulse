// Base URLs for Services
const stageSvcUrl = "http://localhost:7001/stage";
const placesSvcUrl = "http://localhost:7000/";
const scheduleSvcUrl = "http://localhost:7002/";
const provinceDropdown = document.querySelector('#dropdown');
const townDropdown = document.querySelector('#towns-options');
 const scheduleForm = document.getElementById("schedule-form");


const provincesEndpoint = "provinces";
const provinceUrl = placesSvcUrl + provincesEndpoint;

window.addEventListener("load", () => {
    fetchStage();
    fetchProvinces();
});

provinceDropdown.addEventListener('change', () => {
    const chosenProvince = provinceDropdown.value;
    if (chosenProvince) {
        townDropdown.disabled = false;
        fetchTowns();
    } else {
        townDropdown.disabled = true;
        townDropdown.innerHTML = '<option value="">Select a Town</option>';
    }
});

scheduleForm.addEventListener('submit', (event) => {
    event.preventDefault();
    fetchSchedule();
});


/**
 * Fetches and displays the current stage from the stage service.
 */
async function fetchStage() {
    try {
        showLoadingState();
        const response = await fetch(stageSvcUrl);
        if (!response.ok) {
            sendErrorToQueue(`Failed to connect`, "PlaceName");
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const stageData = await response.json();
        updateStage(stageData);
    } catch (error) {
        handleFetchError(error, "Stage");
        console.error('Error fetching stage:', error);
    }
}

/**
 * Updates the stage display in the UI.
 * @param {Object} stageData - The stage data object.
 */
function updateStage(stageData) {
    const template = document.getElementById('stage');
    template.innerText = stageData.stage;
}

/**
 * Displays a loading message while data is being fetched.
 */
function showLoadingState() {
    const template = document.getElementById('stage');
    template.innerText = 'Loading...';
}

/**
 * Fetches a list of provinces from the places service and populates the dropdown.
 */
async function fetchProvinces() {
    try {
        const response = await fetch(provinceUrl);
        if (!response.ok) {
            sendErrorToQueue(`Failed to connect`, "PlaceName");
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const provinceData = await response.json();
        populateProvinces(provinceData);
    } catch (error) {
        handleFetchError(error, "PlaceName");
        console.error('Error fetching provinces:', error);
    }
}

/**
 * Populates the province dropdown with data.
 * @param {Array} provinceData - Array of province names.
 */
function populateProvinces(provinceData) {
    provinceData.forEach(province => {
        const newProvince = document.createElement('option');
        newProvince.textContent = province;
        provinceDropdown.appendChild(newProvince);
    });
}

/**
 * Fetches a list of towns based on the selected province and populates the town dropdown.
 */
async function fetchTowns() {
    const chosenProvince = provinceDropdown.value;
    const townEndpoint = "towns/";
    const townsUrl = placesSvcUrl + townEndpoint + chosenProvince;

    try {
        const response = await fetch(townsUrl);
        if (!response.ok) {
            sendErrorToQueue(`Failed to connect`, "Schedule");
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const townsData = await response.json();
        populateTowns(townsData);
    } catch (error) {
        handleFetchError(error, "PlaceName");
        console.error('Error fetching towns:', error);
    }
}

/**
 * Populates the town dropdown with data.
 * @param {Array} townsData - Array of town objects.
 */
function populateTowns(townsData) {
    townsData.forEach(town => {
        const newTown = document.createElement('option');
        newTown.textContent = town.name;
        townDropdown.appendChild(newTown);
    });
}

/**
 * Fetches the schedule for the selected province and town and displays it in the UI.
 */
async function fetchSchedule() {
    const selectedProvince = provinceDropdown.value;
    const selectedTown = townDropdown.value;
    if (!selectedProvince || !selectedTown) {
        console.log("No province or town was selected...");
        return;
    }

    const scheduleEndpoint = `${selectedProvince}/${selectedTown}`;
    const scheduleUrl = scheduleSvcUrl + scheduleEndpoint;

    try {
        const response = await fetch(scheduleUrl);
        if (!response.ok) {
            sendErrorToQueue(`Failed to connect`, "Schedule");
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const scheduleData = await response.json();
        displaySchedule(scheduleData);
    } catch (error) {
        handleFetchError(error, "Schedule");
        console.error('Error fetching schedule:', error);
    }
}

/**
 * Displays the schedule data in the UI.
 * @param {Object} scheduleData - Schedule data object.
 */
function displaySchedule(scheduleData) {
    const schedule = document.getElementById('schedule-div');
    schedule.innerHTML = '';

    const limitedDays = scheduleData.days.slice(0, 4);
    const startDateArray = scheduleData.startDate;
    let currentDate = new Date(startDateArray[0], startDateArray[1] - 1, startDateArray[2]);

    const rangedDays = [];
    for (let i = 0; i < 4; i++) {
        const nextDay = new Date(currentDate);
        nextDay.setDate(currentDate.getDate() + i);
        rangedDays.push(nextDay);
    }

    limitedDays.forEach((day, index) => {
        const dayContainer = document.createElement('div');
        dayContainer.classList.add('day');


        const dayHeader = document.createElement('h3');
        dayHeader.textContent = formatDate(rangedDays[index]);
        dayHeader.classList.add('date');
        dayContainer.appendChild(dayHeader);

        const slotList = document.createElement('ul');
        day.slots.forEach(slot => {
            const slotItem = document.createElement('li');
            slotItem.textContent = formatTime(slot.start, slot.end);
            slotList.appendChild(slotItem);
        });

        dayContainer.appendChild(slotList);
        schedule.appendChild(dayContainer);
    });
}

/**
 * Formats the time into a readable range.
 * @param {Array} start - Start time [hour, minute].
 * @param {Array} end - End time [hour, minute].
 * @returns {string} - Formatted time range.
 */
function formatTime(start, end) {
    const format = (num) => (num < 10 ? `0${num}` : num);
    return `${format(start[0])}:${format(start[1])} - ${format(end[0])}:${format(end[1])}`;
}

/**
 * Formats a date into a readable string.
 * @param {Date} date - Date object.
 * @returns {string} - Formatted date.
 */
function formatDate(date) {
    return date.toLocaleDateString('en-us', {
        weekday: "long",
        year: "numeric",
        month: "short",
        day: "numeric"
    });
}

/**
 * Sends an error message to a notification service.
 * @param {string} errorMessage - Error message to send.
 * @param {string} serviceName - Service name for context.
 */
async function sendErrorToQueue(errorMessage, serviceName) {
    const ntfyTopic = "siphesihle_alerts";
    const url = `https://ntfy.sh/${ntfyTopic}`;

    try {
        const errorDetails = `${serviceName} service is down: ${errorMessage}`;
        const response = await axios.post(url, errorDetails);
        console.log('Error sent to ntfy:', response.data);
    } catch (error) {
        console.error('Error sending error message:', error);
    }
}

/**
 * Handles fetch errors and sends them to the error queue.
 * @param {Error} error - Error object.
 * @param {string} context - Context for the error.
 */
function handleFetchError(error, context) {
    const message = `Failed to fetch data: ${error.message}`;
    sendErrorToQueue(message, context);
    console.error(message);
}

function toggleMenu() {
    const menu = document.querySelector('.navbar-links');
    menu.classList.toggle('active');
}

