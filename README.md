# FreeRADIUS management backend

This project provides a backend service for managing and configuring FreeRADIUS servers. It is built using Kotlin and integrates with a PostgreSQL database for data storage.
The application supports localization.

## Prerequisites

Before you begin, ensure you have:
- Docker and Docker Compose installed on your machine.
- JDK 11+.
- Maven 3.

## Setup the docker containers

Follow these steps to get your development environment running:

1. **Clone the Repository**

   git clone [repository-url]
   cd [repository-name]


2. **Build Docker Images**

   Navigate to the `tbh-freeradius` and `tbh-testcheck` directories to build their respective Docker images.

   cd tbh-freeradius

   docker build -t tbh-freeradius .

   cd ../tbh-testcheck

   docker build -t tbh-testcheck .


3. **Using Docker Compose**
   Start the services using Docker Compose.

   docker-compose up -d

## Build the project

At the root directory, run the following command:

mvn clean install -T 1C

## Usage

The application is comprised of several services:

- **PostgreSQL Database (db)**
  - `Database Name`: radius
  - `Username`: radius
  - `Password`: radius
  - `Host`: localhost (mapped to port 5432 on your host machine)
  - `Port`: 5432

- **FreeRADIUS Server (freeradius)**
  - Accessible via RADIUS client requests on ports 1812 (UDP) and 1813 (UDP).

- **Radiusclient (radiusclient)**
  - Configured to communicate with the FreeRADIUS server. Modify the `continuous_check.sh` file for specific test scenarios.

To stop the services:
  
docker-compose down


## Cleaning Up

To remove unused Docker resources, use the following commands:

    docker container prune
    docker image prune -a
    docker volume prune


## Contributing

Contributions to this project are welcome. Please follow the standard fork, branch, and pull request workflow.

## License

This project is licensed under the MIT License.
