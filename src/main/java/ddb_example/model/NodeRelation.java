package ddb_example.model;

import lombok.Builder;

@Builder
public record NodeRelation(String parentId, String childId) {
}
