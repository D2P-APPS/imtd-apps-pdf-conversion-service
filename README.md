# imtd-apps-pdf-conversion-service
PDF Conversion Service

## Introduction
PDF Conversion Service converts files to a pdf.

## Setup
TBD

## Starting the service
1. On the command line, enter this command: ./gradlew build
2. On the command line, enter this command: `java -jar build/libs/imtd-apps-pdf-conversion-service-<version>.jar`

## API Design
Microservice design should follow the accepted industry standards for REST responses to API calls.

## Spring Actuator
The Spring Boot Actuator provides production grade tools that exposes operational information about the running microservice - 
health, metrics, info, dump, environment, etc. The Actuator uses HTTP endpoints or JMX beans to enable interaction with the microservice.
* `/actuator` is the default url to access Actuator.
* Only the `/actuator/health` and `/actuator/info` endpoints are enabled by default
* `management.endpoints.web.exposure.include='*'` will enable all endpoints (see `src/main/resources/application.yaml`)
* `management.security.enabled=true` will allow all endpoints to be accessed. Disable once Spring Security is added.

### Health Check Indicator
TBD

### Info Check Indicator
TBD

### Metrics Check Indicator
TBD

### Trace Check Indicator
TBD

## Swagger Documentation
TBD

## Testing with CDC (Consumer Driven Contracts)
TBD

## Microservice Port Configurations
When microservices are run locally, they cannot run on the same port. Use the table below for ports when running locally.

| Microservice | Port |
|:---:|:---:|
|pdf-conversion|8082|
