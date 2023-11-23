# Docker Containers Setup

This documentation provides instructions building and running the docker containers in a dev environment.

## Building Docker Images

To set up the environment, you need to build the Docker images for the `tbh-freeradius` and `tbh-testcheck`. Here are the steps:

### 1. Build tbh-freeradius Image

Navigate to the `tbh-freeradius` directory and build the `tbh-freeradius` Docker image:

```bash
cd tbh-freeradius
docker build -t tbh-freeradius .
```

### 2. Build tbh-testcheck Image

Navigate to the `tbh-testcheck` directory and build the `tbh-testcheck` Docker image:

```bash
cd ../testcheck
docker build -t tbh-testcheck .
```

## Using Docker Compose

Now that you have built the required Docker images, you can set up the services using Docker Compose. Follow these steps:

### 1. Start the Services

To start the services defined in the `compose.yaml` file, run the following command:

```bash
docker-compose up -d
```

This command will start the services in detached mode, running them in the background.

### 2. Accessing the Services

You can access the services as follows:

- **PostgreSQL Database (db)**:
    - Database Name: radius
    - Username: radius
    - Password: radius
    - Host: localhost (since it's mapped to port 5432 on your host machine)
    - Port: 5432

- **FreeRADIUS Server (freeradius)**:
    - FreeRADIUS is running as a service and should be accessible via RADIUS client requests on ports 1812 (UDP) and 1813 (UDP).

- **Radiusclient (radiusclient)**:
    - This service depends on `freeradius` and is configured to communicate with it. It is just meant to `radclient` commands, so
      you might want to change the commands in the `continuous_check.sh` file and rebuild the image depending on your test scenario.

### 3. Stop the Services

To stop the services and remove the containers created by Docker Compose, run the following command:

```bash
docker-compose down
```

## Cleaning Up

To clean up Docker resources such as unused images and volumes, you can use the following commands:

```bash
# Remove all stopped containers
docker container prune

# Remove all unused images
docker image prune -a

# Remove all unused volumes
docker volume prune
```
