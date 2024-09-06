package ddb_example.model;


import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

@Builder
public record Node(String id,
                   @Nullable @With String parentId,
                   @Nullable Long version) {
}
