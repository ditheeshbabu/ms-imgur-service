# MS Imgur Service

A Spring Boot API application for managing images, equipped with configurations for local development, Docker-based deployment, and Kubernetes deployment.

---

## Table of Contents
1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Running Locally](#running-locally)
4. [Running with Docker](#running-with-docker)
5. [Deploying to Kubernetes](#deploying-to-kubernetes)

---

## Overview

The **MS Imgur Service** is a Spring Boot API application designed for managing image data. The service includes endpoints for:

- Register a User with basic information, username, and password
- Upload, view and delete images after authorizing the username/password.
- Associate the updated list of images with the user profile
- View the User Basic Information and the Images

### Documentation of services
- OpenAPI (Swagger) documentation and required Postman Collections are present in api-docs path.
- Once application is running, you may also access http://localhost:9090/swagger-ui/index.html to find details of the API and to try out some requests.
---
### Live instance: 

- The service is accessible at http://api.ditheesh.online/swagger-ui/index.html with the swagger API details.
- Update the URL in postman once the collection is imported to the above URL to test the live instance. The above swagger link can also be used to test the API.


## Getting Started

### Prerequisites
Ensure you have the following installed:
- **Java 23** (for running the application locally)
- **Docker** (for containerized deployment)
- **Kubernetes CLI (`kubectl`)** (for deploying to Kubernetes)
- **A running Kubernetes cluster** (if deploying to Kubernetes)

---

## Running Locally

Follow these steps to run the application directly on your local machine:

### 1. Build the Application
1. Clone the repository:
    ```bash
    git clone <repository-url>
    cd ms-imgur-service
    ```

2. Build the project using Gradle:
    ```bash
    ./gradlew clean build
    ```

### 2. Run the Application
Run the application with:
```bash
./gradlew bootRun
```

## Running with Docker

Follow these steps to run the application using Docker:

### 1. Build the Application
Build application using the same steps as above until gradlew clean build

### 2. Run the Application
Build and run the application using docker-compose
```bash
  docker-compose up --build
```

## Deploying to Kubernetes

Follow these steps to run the application using Kubernetes for autoscaling:

### 1. Build the Application
Build application using the same steps as `Running Locally` until gradlew clean build

### 2. Build docker image if not already present
Build and run the application using docker-compose
```bash
  docker build -t ms-imgur-service:latest .
```

### 3. Run in Kubernetes
```bash
  kubectl apply -f kubernetes.yml
```

