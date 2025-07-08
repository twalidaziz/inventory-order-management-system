# Inventory and Order Management System

A web application for managing inventory and order information build using Spring Boot and HTMX.

## Development

Update the local database connection in `application.properties` or create your own `application-local.properties` file to override
settings for development.

During development it is recommended to use the profile `local`. In IntelliJ `-Dspring.profiles.active=local` can be
added in the VM options of the Run Configuration after enabling this property in "Modify options".

In addition to the Spring Boot application, the DevServer must also be started - for this
[Node.js](https://nodejs.org/) version 20 is required. On first usage and after updates the dependencies have to be installed:

```
npm install
```

The DevServer can be started as follows:

```
npm run devserver
```

Using a proxy the whole application is now accessible under `localhost:8081`. All changes to the templates and JS/CSS
files are immediately visible in the browser.

## Build

The application can be built using the following command:

```
mvnw clean package
```

Node.js is automatically downloaded using the `frontend-maven-plugin` and the final JS/CSS files are integrated into the jar.

Start the application with the following command - here with the profile `production`:

```
java -Dspring.profiles.active=production -jar ./target/cms-0.0.1-SNAPSHOT.jar
```

If required, a Docker image can be created with the Spring Boot plugin. Add `SPRING_PROFILES_ACTIVE=production` as
environment variable when running the container.

```
mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=dev.twalidaziz/cms
```
## Features

* Inventory Management
  
  https://github.com/user-attachments/assets/71690e75-331e-4188-a473-abb25fa3c793
  
* Order Management

  https://github.com/user-attachments/assets/5035d0af-551a-412d-8f0a-7dd75ab19268
