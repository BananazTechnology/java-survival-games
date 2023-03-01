[![SOFT](https://github.com/BananazTechnology/java-survival-games/actions/workflows/SOFT.yml/badge.svg?branch=develop)](https://github.com/BananazTechnology/java-survival-games/actions/workflows/SOFT.yml)[![RELEASE](https://github.com/BananazTechnology/java-survival-games/actions/workflows/RELEASE.yml/badge.svg?branch=main)](https://github.com/BananazTechnology/java-survival-games/actions/workflows/RELEASE.yml)

# Java Survival Game
* Description: A Discord bot made with @Javacord!
* Version: (Check main for release or develop for dev)
* Creator: Aaron Renner
* THIS BRANCH IS ONLY FOR GAMES

### Table of Contents
* [Introduction](#introduction)
* Setup *"How to"*
  * [Run Spring-Boot](#running-the-project)
* Help
  * [Setup Libraries and Examples](#libraries)
  
## Introduction

This Java application is built on the Spring-Boot framework! This project interacts with Discord commands or startup objects in the application.yml to play games in Discord channels, see *How-TO* below for more details.

THIS BRANCH IS ONLY FOR GAMES

## Setup
### Properties
The following document formatting MUST REMAIN THE SAME, replace or add only where noted to!
Tips:
* THIS BRANCH IS ONLY FOR GAMES
* The position of the `discord` below `nft-bot` is essential!

``` yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: com.aaronrenner.discordbot.models.TableNameStrategy
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://aaronrenner.com:8306/gamebot?createDatabaseIfNotExist=true
    username: USERNAME
    password: PASSWORD
    hikari:
      connectionTimeout: 120000
      idleTimeout: 600000
      # 15 minutes recommended for lifetime
      maxLifetime: 900000
      maximumPoolSize: 25
# Custom
nft-bot:
  customName: <GAME-NAME-AS-STRING>
  discord:
    adminChannel: <Admin channel (Ex. "admins")>
    commandPrefix: <Command prefix for all (Ex. "!")>
    gameChannel: <Name of the game channel (Ex. "survival-game")>
    gameCommand: <The trigger to be combined with prefix for the game (Ex. "survive")>
    generalChannel*: <Name of an extra output channel (Ex. "general")>
    generalOutput*: <Boolean to control generic output (Ex. true)>
    token: <DISCORD-TOKEN>
    winnerRole: <The role ID (Ex. "885357681773277204")>
  game:
    disableWinlock*: <Can toggle to allow winners to re-roll, default false (Ex. true)>
    adminContact: <The textual @ for admin (Ex. "<@!885316615212769281>")>
    loserMessages:
    - <Loser msgs get one %s that will auto fill the user>
    - <(Ex. "%s you did not win!")>
    dropScale: <A value of 1:? where ? is the drop scale (Ex. 35)>
    maxWinners: <The maximum amount of spots (Ex. 200)>
    userCooldown: <The wait-period between entries, in seconds (Ex. 43200 equals 12hrs)>
    winnerMessages:
    - <Winner msgs resolve with the first %s being user, second is the adminContact>
    - <This field can also take server channels as its raw mention (Ex. <#930256345414905967>)>
    - <(Ex. "Congrats %s, enter wallet in <#930256345414905967> or reach out to %s with questions!")>
    dispatchInterval: <The message wait for sending to Discord, in Milis (Ex. 1000 equals 1 seconds)>

(*) = is optional. DOES NOT NEED TO BE INCLUDED
```

### Running PROD
Setup the `SPRING_APPLICATION_JSON` value in the Docker-Compose. See example docker-compose.yaml in this project.

### Running the Project

Executing the project can be done in two ways, the first is by initializing using Maven which the second produces a traditional Jar file. Before attempting to run the program some setup must be done inside of the [src/main/resources/application.properties](src/main/resources/application.yml), you can follow the guides.

### Build with Maven

If you have Maven installed on your machine you can navigate to the root project directory with this README file and execute the following. Remember to follow the above Database setup procedures first.
```sh
mvn -B -DskipTests clean package
```
You can also use the built in Maven wrapper and execute the project by following this command.
```sh
./mvnw -B -DskipTests clean package
```
### Setting up in IDE

Download Lombok to your IDE or VS Code Extension!

Use the IDE "Run Configuration" to setup the `-Dspring.application.json` (eclipse example) in the Environment Properties

### Creating a Docker Image

To build a container that can execute the application from a safe location you can use my supplied [Dockerfile](Dockerfile) to do so. You should follow the guides first to better understand some of these arguments.

```Dockerfile
CMD [ "java", \
        "-jar", \
        "discord-nft-bot.jar"]
```