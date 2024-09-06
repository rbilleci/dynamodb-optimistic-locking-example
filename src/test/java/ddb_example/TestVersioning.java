package ddb_example;

import ddb_example.model.Node;
import ddb_example.model.NodeRelation;
import ddb_example.schema.NodeRelationSchema;
import ddb_example.schema.NodeSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactDeleteItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = {LocalStackConfiguration.class})
public class TestVersioning {


    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private NodeSchema nodeSchema;

    @Autowired
    private NodeRelationSchema nodeRelationSchema;

    @Autowired
    private DynamoDbTable<Node> nodes;

    @Autowired
    private DynamoDbTable<NodeRelation> nodeRelations;


    @Test
    public void testVersioning() {
        // Setup the initial state
        {
            // Create the parent
            enhancedClient.transactWriteItems(builder -> builder.addPutItem(
                    nodes,
                    Node.builder().id("p").build()));

            // Add the child, updating the parent to increment its version number
            final var parent = fetchNode("p");
            final var child = Node.builder().id("c1").parentId("p").build();
            final var relation = NodeRelation.builder()
                    .parentId(parent.id())
                    .childId(child.id())
                    .build();
            enhancedClient.transactWriteItems(builder -> builder
                    .addUpdateItem(nodes, parent) // increments the version number of the parent
                    .addPutItem(nodes, child)  // creates the child
                    .addPutItem(nodeRelations, relation)); // adds the relation
        }

        // BOB: delete the section, initially moving all children to the root
        enhancedClient.transactWriteItems(builder -> builder
                .addUpdateItem(nodes, fetchNode("p")) // increments the version number of the parent
                .addUpdateItem(nodes, fetchNode("c1").withParentId(null)) // move the child
                .addDeleteItem(nodeRelations, fetchNodeRelation("p", "c1"))); // delete the child relation
        final var bobsVersion = requireNonNull(fetchNode("p").version());
        // NOTE: at this point, we must check there are no children.
        // If children are found, then restart the move process.
        // After we finally get a version with no children, we can attempt
        // deletion of the parent.


        // ALICE: Meanwhile, Alice adds a new child
        {
            final var parent = fetchNode("p");
            final var child2 = Node.builder().id("c2").parentId("p").build();
            final var relation = NodeRelation.builder()
                    .parentId(parent.id())
                    .childId(child2.id())
                    .build();
            enhancedClient.transactWriteItems(builder -> builder
                    .addUpdateItem(nodes, parent) // increments the version number of the parent
                    .addPutItem(nodes, child2) // add the child
                    .addPutItem(nodeRelations, relation)); // add the relation
        }

        // BOB: attempt to delete the section,
        // using the OLD version number
        // We expect an exception here
        assertThrows(TransactionCanceledException.class, () ->
                enhancedClient.transactWriteItems(builder -> builder
                        .addDeleteItem(nodes, TransactDeleteItemEnhancedRequest.builder()
                                .key(Key.builder().partitionValue("p").build())
                                .conditionExpression(conditionExpressionFor(bobsVersion))
                                .build())));
        // BOB: attempt to delete with the most recent version number
        enhancedClient.transactWriteItems(builder -> builder.addDeleteItem(nodes,
                TransactDeleteItemEnhancedRequest.builder()
                        .key(Key.builder().partitionValue("p").build())
                        .conditionExpression(conditionExpressionFor(requireNonNull(fetchNode("p").version())))
                        .build()));

    }

    private Node fetchNode(final String id) {
        return nodes.getItem(Key.builder()
                .partitionValue(id)
                .build());
    }

    private NodeRelation fetchNodeRelation(final String parentId, final String childId) {
        return nodeRelations.getItem(Key.builder()
                .partitionValue(parentId)
                .sortValue(childId)
                .build());
    }

    private Expression conditionExpressionFor(final Long version) {
        return Expression.builder()
                .expression("v = :v")
                .expressionValues(Map.of(":v", AttributeValue.fromN(version.toString())))
                .build();
    }
}
