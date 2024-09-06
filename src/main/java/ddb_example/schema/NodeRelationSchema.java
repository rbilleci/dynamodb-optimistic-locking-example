package ddb_example.schema;

import ddb_example.model.NodeRelation;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticImmutableTableSchema;

import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.*;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.updateBehavior;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior.WRITE_IF_NOT_EXISTS;

@Component
public class NodeRelationSchema {

    private final TableSchema<NodeRelation> schema =
            StaticImmutableTableSchema.builder(NodeRelation.class, NodeRelation.NodeRelationBuilder.class)
                    .addAttribute(String.class, a -> a
                            .name("pk")
                            .getter(NodeRelation::parentId)
                            .setter(NodeRelation.NodeRelationBuilder::parentId)
                            .tags(primaryPartitionKey(), updateBehavior(WRITE_IF_NOT_EXISTS)))
                    .addAttribute(String.class, a -> a
                            .name("sk")
                            .getter(NodeRelation::childId)
                            .setter(NodeRelation.NodeRelationBuilder::childId)
                            .tags(primarySortKey(), updateBehavior(WRITE_IF_NOT_EXISTS)))
                    .newItemBuilder(NodeRelation::builder, NodeRelation.NodeRelationBuilder::build)
                    .build();

    public TableSchema<NodeRelation> schema() {
        return schema;
    }
}
