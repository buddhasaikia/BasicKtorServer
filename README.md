# BasicKtorServer 🚀

Welcome to **BasicKtorServer**! This project serves as a clear, minimal, and fully-functional example of a backend service built with [Ktor](https://ktor.io/), the asynchronous framework for creating microservices and web applications in Kotlin.

Whether you're just starting out with Ktor or looking for a solid foundation to build upon, you're in the right place!

## 📖 What's inside?

This project was generated using the [Ktor Project Generator](https://start.ktor.io) and includes:
- **Routing**: Clean, structured API endpoints.
- **Tutorials**: A step-by-step guide to building REST APIs with Ktor.

## 🛠️ Prerequisites

Before you begin, ensure you have the following installed:
- [Java Development Kit (JDK)](https://adoptium.net/) (version 11 or higher)
- [Docker](https://www.docker.com/) (optional, for running containerized builds)

## 🚀 Getting Started

To get the server up and running on your local machine, run the following command in your terminal:

```bash
./gradlew run
```

If the server starts successfully, you'll see output similar to this:

```text
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
You can now interact with the API at `http://localhost:8080`!

## 📦 Useful Commands

We use Gradle to manage our build and tasks. Here are some of the most useful commands:

| Command                                 | What it does                                                         |
| --------------------------------------- | -------------------------------------------------------------------- |
| `./gradlew run`                         | **Start the server locally**                                         |
| `./gradlew test`                        | Run all automated tests                                              |
| `./gradlew build`                       | Compile the code and run tests                                       |
| `./gradlew buildFatJar`                 | Package the server into a single executable JAR file                 |
| `./gradlew buildImage`                  | Build a Docker image containing your application                     |
| `./gradlew runDocker`                   | Run the application using the local Docker image                     |

## 📚 Learning Resources

If you'd like to dive deeper, check out these excellent resources:
- 📖 **Local Guide**: [Building a REST API with Ktor: A Complete Tutorial](ktor_tutorial.md)
- 🌐 [Official Ktor Documentation](https://ktor.io/docs/home.html)
- 🐙 [Ktor GitHub Repository](https://github.com/ktorio/ktor)
- 💬 [Ktor Slack Community](https://app.slack.com/client/T09229ZC6/C0A974TJ9) (Get an invite [here](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up))

Happy coding! ✨
