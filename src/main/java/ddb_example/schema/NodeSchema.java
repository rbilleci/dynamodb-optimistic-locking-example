package ddb_example.schema;


import ddb_example.model.Node;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.extensions.VersionedRecordExtension;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticImmutableTableSchema;

import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.*;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior.WRITE_IF_NOT_EXISTS;

@Component
public class NodeSchema {

    private final TableSchema<Node> schema =
            StaticImmutableTableSchema.builder(Node.class, Node.NodeBuilder.class)
                    .addAttribute(String.class, a -> a
                            .name("pk")
                            .getter(Node::id)
                            .setter(Node.NodeBuilder::id)
                            .tags(primaryPartitionKey(), updateBehavior(WRITE_IF_NOT_EXISTS)))
                    .addAttribute(String.class, a -> a
                            .name("p")
                            .getter(Node::parentId)
                            .setter(Node.NodeBuilder::parentId))
                    .addAttribute(Long.class, a -> a
                            .name("v")
                            .getter(Node::version)
                            .setter(Node.NodeBuilder::version)
                            .tags(VersionedRecordExtension.AttributeTags.versionAttribute()))
                    .newItemBuilder(Node::builder, Node.NodeBuilder::build)
                    .build();

    public TableSchema<Node> schema() {
        return schema;
    }

}
