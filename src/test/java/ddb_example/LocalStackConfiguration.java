package ddb_example;

import ddb_example.model.Node;
import ddb_example.model.NodeRelation;
import ddb_example.schema.NodeRelationSchema;
import ddb_example.schema.NodeSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class LocalStackConfiguration {

    @Container
    private static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                    .withEnv("DYNAMODB_IN_MEMORY", "1")
                    .withEnv("DYNAMODB_SHARE_DB", "1")
                    .withServices(LocalStackContainer.Service.DYNAMODB);

    static {
        LOCALSTACK.start();
    }

    @Bean
    public DynamoDbClient client() {
        final var credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(
                LOCALSTACK.getAccessKey(),
                LOCALSTACK.getSecretKey()));
        return DynamoDbClient.builder()
                .endpointOverride(LOCALSTACK.getEndpoint())
                .credentialsProvider(credentials)
                .region(Region.of(LOCALSTACK.getRegion()))
                .overrideConfiguration(ClientOverrideConfiguration.builder().build())
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient enhancedClient(final DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }


    @Bean
    public NodeSchema nodeSchema() {
        return new NodeSchema();
    }

    @Bean
    public NodeRelationSchema nodeRelationSchema() {
        return new NodeRelationSchema();
    }


    @Bean
    public DynamoDbTable<Node> nodes(final DynamoDbEnhancedClient enhancedClient,
                                     final NodeSchema nodeSchema) {
        final var table = enhancedClient.table("nodes", nodeSchema.schema());
        table.createTable();
        return table;
    }


    @Bean
    public DynamoDbTable<NodeRelation> nodeRelations(final DynamoDbEnhancedClient enhancedClient,
                                                     final NodeRelationSchema nodeRelationSchema) {
        final var table = enhancedClient.table("node_relations", nodeRelationSchema.schema());
        table.createTable();
        return table;
    }


}
