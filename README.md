# Project title
Kitty Cards

## Technologies
- [Springboot](https://spring.io/) - Java framework to create a service
- [Gradle](https://gradle.org/) - Automated building and management tool
- [MySQL](https://www.mysql.com/) - Database
- [React](https://reactjs.org/docs/getting-started.html) - Javascript library for the frontend
- [Github Projects](https://github.com/explore) - Project Management
- [Figma](https://figma.com/) - Mockups
- [Google Cloud](https://cloud.google.com/) - Deployment
- [SonarCloud](https://sonarcloud.io/) - Testing & Feedback of code quality

## High-level components

### User

Users can eidt their username, password, birthday and avatar save them to the database, the primary key ID will be automatically stored upon registration.

[User](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/User.java)

[UserService](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/UserService.java)

### Game

Game is the core part and takes players, cardpile, winner/loser, etc.. as components.

[Game](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Game.java)

[GameService](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/GameService.java)

### Board

Board is the most basic element in a game, gridsquares and cardpile are initialized within a board.

[Board](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Board.java)

[BoardService](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/BoardService.java)


### ChatMessage

ChatMessage can be sent within the game, we offer translation function to every message.

[ChatMessage](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/ChatMessage.java)

[ChatService](https://github.com/sopra-fs24-group-08/sopra-fs24-group-08-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/ChatService.java)


### WebSocket

In order to make our Application more real-time, we use WebSockets in key areas.



## Deployment and Database

### Deployment on Google Cloud

Our application is hosted on [Google Cloud URL](https://sopra-fs23-group-38-client.oa.r.appspot.com/). Also our server status is available in this link [Google Cloud URL](https://sopra-fs23-group-38-server.oa.r.appspot.com/). All cloud deployments are now complete and can be accessed directly via the link above.

### Cloud SQL Database

This application use Cloud SQL database to store data.

## Launch & Development

For your local development environment, you may need gradle to build this application and create your own database:

### Create your own database of Cloud SQL:



### Building with Gradle

You can use the local Gradle Wrapper to build the application.

- macOS: `./gradlew`
- Linux: `./gradlew`
- Windows: `./gradlew.bat`

More Information about [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and [Gradle](https://gradle.org/docs/).

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your browser. You also have to check whether your server URL is set properly on `localhost:8080`.

### Testing

Testing is optional, and you can run the tests with

```bash
./gradlew test
```
## Roadmap
1. Improve game feature: add random events to the game for more fun
2. Improve Friend system; add chat function outside the game and store the history
3. Improve UI; structure the CSS files better, replace the static images with better resource
## Authors and acknowledgement
SoPra Group 08 2024 members: 
- **David Tanner** - [Github](https://github.com/Davtan00)
- **Jingxuan Tian** - [Github](https://github.com/xuanjt)
- **Yiyang Chen** - [Github](https://github.com/CindyChen-1999)
- **Zixian Pang** - [Github](https://github.com/Dennis-Pang)
- **Luis Schmid** - [Github](https://github.com/LooPyt)

>Firstly, we want to thank our TA Sven Fabian Ringger for the help throughout the whole project. Secondly, we want to thank any official documents/online tutorials that provide us with help at any part of the project. During this semester, we encountered so many challenges, which also offered us chances to grow and gain knowledge and experience in the software field. Furthermore, we also realized that not only coding skills but also communication matters, as we fell short in this part but eventually overcame it. 
In a nutshell, we appreciate this journey at Sopra and thank for anyone who helps us directly or indirectly.

## License
[MIT License](LICENSE)



 

