import org.jspecify.annotations.NullMarked;

@NullMarked
module ddb.example.main {
    requires org.jspecify;
    requires static lombok;
    requires software.amazon.awssdk.enhanced.dynamodb;
    requires spring.context;
}


