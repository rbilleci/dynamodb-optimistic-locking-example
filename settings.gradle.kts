rootProject.name = "ddb-example"


val awsVersion = "2.27.14"
val caffeineVersion = "3.1.8"
val localstackVersion = "1.20.1"
val micrometerVersion = "1.13.2"
val r4jVersion = "2.2.0"
val springBootVersion = "3.3.3"
val uuidCreatorVersion = "6.0.0"


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Spring
            library("spring.boot", "org.springframework.boot:spring-boot-starter:$springBootVersion")
            library("spring.boot.test", "org.springframework.boot:spring-boot-starter-test:$springBootVersion")

            // Libraries for DynamoDB
            library("dynamodb", "software.amazon.awssdk:dynamodb:$awsVersion")
            library("dynamodb.enhanced", "software.amazon.awssdk:dynamodb-enhanced:$awsVersion")
            library("netty.nio.client", "software.amazon.awssdk:netty-nio-client:$awsVersion")

            // Libraries for localstack testing
            library("localstack", "org.testcontainers:localstack:$localstackVersion")
            library("localstack.junit.jupiter", "org.testcontainers:junit-jupiter:$localstackVersion")
        }
    }
}