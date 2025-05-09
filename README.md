# Interview Demo - MADANIA API (Spring Boot, Docker, Tomcat, MySQL)

This project is a Spring Boot application demonstrating a simple User Management API. It allows for the creation and retrieval of users, incorporating details such as their CIN (National Identity Number), CIN release date, and marital status. The application is designed for a containerized deployment using Docker and Docker Compose, featuring a MySQL database backend and deployment on an Apache Tomcat server.

## Table of Contents

1.  [Features](#features)
2.  [Technologies Used](#technologies-used)
3.  [Prerequisites](#prerequisites)
4.  [Project Setup](#project-setup)
    * [1. Clone the Repository](#1-clone-the-repository)
    * [2. Configure Environment Variables (`.env` file)](#2-configure-environment-variables-env-file)
5.  [Building the Application (WAR)](#building-the-application-war)
6.  [Running the Application with Docker Compose](#running-the-application-with-docker-compose)
7.  [Accessing the Application](#accessing-the-application)
    * [API Endpoints](#api-endpoints)
    * [Example Postman Requests](#example-postman-requests)
8.  [Running Automated Tests](#running-automated-tests)
9. [Database Management](#database-management)

## 1. Features

* Create new users with name, CIN, CIN release date, and marital status.
* Retrieve users by their CIN and CIN release date.
* Input validation for user creation (CIN format, required fields, date logic).
* Containerized deployment using Docker Compose for:
    * MySQL 8.0 database.
    * Apache Tomcat 10.1 server running the application WAR.

## 2. Technologies Used

* **Java:** 17
* **Spring Boot:** (e.g., 3.2.x - *Adjust based on your `build.gradle`*)
    * Spring MVC (for REST APIs)
    * Spring Data JPA (for database interaction)
    * Spring Boot Starter Test (for testing framework)
* **Hibernate:** (JPA implementation)
* **Lombok:** To reduce boilerplate code.
* **Database:** MySQL 8.0 (run via Docker)
* **Servlet Container:** Apache Tomcat 10.1 (run via Docker)
* **Containerization:** Docker & Docker Compose
* **Build Tool:** Gradle
* **Testing:** JUnit 5, Mockito, AssertJ (via `spring-boot-starter-test`), Testcontainers (for integration tests against Dockerized MySQL).
* **Logging:** SLF4J & Logback (default with Spring Boot).

## 3. Prerequisites

Before you begin, ensure you have the following installed and running on your system:

* **Java Development Kit (JDK):** Version 17 or later.
* **Docker Desktop:** Latest stable version for your operating system (macOS or Windows). This includes Docker Engine and Docker Compose.
    * *Ensure Docker Desktop is running.*
* **Git:** For cloning the repository.
* **API Client (Optional):** An API client like [Postman](https://www.postman.com/downloads/) for manually testing the API endpoints.

*(Gradle is managed by the Gradle Wrapper (`gradlew`) included in the project, so no global Gradle installation is required.)*

## 4. Project Setup

#### 4.1. Clone the Repository

Open your terminal or command prompt, navigate to your desired workspace directory, and run:

```$
git clone <your-repository-url>
cd <project-directory-name>
```

#### 4.2. Configure Environment Variables (.env file)

This application uses an .env file to manage sensitive database credentials for the local Docker Compose development environment.

* In the root directory of the cloned project, create a new file rename the provided .env-example to .env.

* Replace the placeholder values with the credentials you intend to use for your local MySQL setup

### 5. Building the Application (WAR)

* Navigate to the root directory of the project
* Run the Gradle build command:

On macOS/Linux:
```$ ./gradlew clean build ```

On Windows
```$ gradlew.bat clean build ```

### 6. Running the Application with Docker Compose

Docker Compose orchestrates the startup of the MySQL database container and the Tomcat container (which includes your application).

* Ensure Docker Desktop is running on your system.
* Navigate to the root directory of the project (where docker-compose.yml is located).
* Run the following command:

```$ docker-compose up --build ```

### 7. Building the Application (WAR)
Once the Docker Compose services are up and running:

* Your application's API will be accessible on your host machine at http://localhost:8080.

API Endpoints
Create User: ```$  POST /api/v1/users ```

Get User by CIN and Release Date: ```$ GET /api/v1/users/cin/{cin}?releaseDate=YYYY-MM-DD```

 **Example Postman Requests**

**1. Create a New User:**
```$
Method: POST
URL: http://localhost:8080/api/v1/users
Headers: Content-Type: application/json
Body (raw JSON):
```
```$
{
    "name": "Mourad Tlili",
    "cin": "12345678",
    "cinReleaseDate": "2023-01-15",
    "marriedStatus": true
}
```

* Expected Response: HTTP Status 201 Created with the created user object in the response body.

** 2. Get User by CIN and Release Date: **

```$
Method: GET
URL: http://localhost:8080/api/v1/users/cin/12345678
Params (Query Parameters in Postman):
Key: releaseDate
Value: 2023-01-15
```
* Expected Response: HTTP Status 200 OK with the user object in the response body. 

### 8. Running Automated Tests

The project includes unit and integration tests. To run them:

* Open your terminal.
* Navigate to the root directory of the project.
* Ensure Docker Desktop is running (for integration tests that use Testcontainers).
* Run the Gradle test command:\

    * On macOS/Linux:

    ```$
    ./gradlew test
    ```

    * On Windows 
    ```$
    gradlew.bat test
    ```

*Test results will be displayed in the console. A detailed HTML report can be found in build/reports/tests/test/index.html.

### 9. Database Management
Ensure services are running: docker-compose up\
Connect to the MySQL container: ```$docker exec -it mysql_for_user_app mysql -u root -p```\
 (root password will be requested)\
 
**Now you are able to manage the database from terminal**

