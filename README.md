# PowerPulse

PowerPulse is a comprehensive web application designed to provide accurate loadshedding schedules and power alerts for users based on their province and town. The project integrates a robust backend and an interactive frontend, making it a complete solution for navigating the complexities of loadshedding in South Africa.

## Features

1. **Loadshedding Stage Information:**
    - **GET /stage:** Retrieve the current loadshedding stage as a JSON object containing an integer value (0 to 8). Any stage outside the range should not go through.
      ```json
      {
        "stage": 4
      }
      ```

2. **Loadshedding Schedule Retrieval:**
    - **GET /{province}/{place}/{loadsheddingstage}:** Retrieve a detailed schedule for a specified town and loadshedding stage for today and the following three days as a JSON object.

3. **Stage Updates:**
    - **POST /stage:** Update the loadshedding stage by submitting a JSON object in the request body. This automatically notifies all services listening to the stage changes.

4. **Distributed Systems Integration:**
    - Built as a distributed system with communication facilitated through message queues and topics using ActiveMQ for load balancing and seamless service communication.

5. **Error Handling and Monitoring:**
    - **Alert Service:** Integrated with [ntfy.sh](https://ntfy.sh) to send real-time notifications to the developer’s phone when any service is down or malfunctioning.
    - **Logging:** Comprehensive logging mechanisms to track system activities and errors.

6. **Data Persistence:**
    - Stage and schedule data are stored in a SQLite database for reliability and quick access.

7. **User-Friendly Interface:**
    - Interactive web page with:
        - Current loadshedding stage display.
        - Form to select province and town to fetch the schedule.

## Technologies Used

- **Backend:** Java, SQLite, ActiveMQ
- **Frontend:** JavaScript, HTML, CSS
- **API Integration:** HTTP REST API
- **Error Handling:** ntfy.sh for notifications

## Project Structure

The project consists of the following key components:
- **Backend:** Handles HTTP requests, ActiveMQ integration, data persistence, and error monitoring.
- **Frontend:** Provides an interactive interface for users to view schedules and submit queries.
- **Database:** SQLite database for storing stage and schedule data.

## How It Works

1. **Loadshedding Stage Management:**
    - The current stage is fetched or updated using REST API endpoints.
    - Any change in stage triggers notifications to all listening services via ActiveMQ.

2. **Schedule Retrieval:**
    - Users input their province and town on the web page.
    - The backend retrieves the appropriate schedule based on the current stage and the user’s inputs.

3. **Error Handling:**
    - If a service fails, notifications are sent via ntfy.sh, and logs are generated for debugging.

4. **Message Queues and Topics:**
    - ActiveMQ ensures that services are load-balanced and updated efficiently in real time.

## Installation and Setup

1. **Prerequisites:**
    - Java Development Kit (JDK)
    - ActiveMQ installed and running
    - SQLite database
    - A modern web browser

2. **Clone the Repository:**
   ```bash
   git clone https://github.com/Annie24-ux/PowerPulse.git
   cd PowerPulse
   ```

3. **Run ActiveMQ:**
    - Start the ActiveMQ broker by running the following command in your terminal:
      ```bash
      activemq start
      ```

4. **Set Up the Database:**
    - Ensure the SQLite database (`powerpulse.db`) is in the correct location.

5. **Start the Backend Server:**
    - Compile and run the Java application:
      ```bash
      javac -cp .:activemq-all.jar Main.java
      java -cp .:activemq-all.jar Main
      ```

6. **Launch the Frontend:**
    - Open the `index.html` file in your browser.

7. **API Endpoints:**
    - Use tools like Postman or Thunder Client to test the endpoints.

## API Endpoints

1. **GET /stage**
    - Returns the current loadshedding stage.

2. **POST /stage**
    - Updates the current loadshedding stage.

3. **GET /{province}/{place}/{loadsheddingstage}**
    - Returns the schedule for the given province, town, and loadshedding stage.

## Example JSON Response

**Loadshedding Schedule:**
```json
{
  "stage": 4,
  "schedule": [
    { "day": "2025-01-15", "times": ["10:00-12:00", "16:00-18:00"] },
    { "day": "2025-01-16", "times": ["08:00-10:00", "14:00-16:00"] }
  ]
}
```

## Real-Time Notifications

- Notifications are sent via ntfy.sh when:
    - A service goes down.
    - Stage changes occur.

## Contributions

Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Submit a pull request with a detailed description of your changes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Author

Developed by [Sibiya Siphesihle](https://github.com/Annie24-ux).

---
A Beacon of light.
Needed at the darkest hour, literally.

For any questions or issues, feel free to open an issue on GitHub or contact the author directly.