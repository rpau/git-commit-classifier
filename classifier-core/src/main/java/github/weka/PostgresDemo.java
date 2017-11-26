package github.weka;

import github.weka.readers.DatabaseInstancesReader;
import github.weka.writers.DatabaseInstancesWriter;
import github.weka.schemas.Schema;
import github.weka.schemas.SchemaBuilder;

public class PostgresDemo {

  public static void main(String[] args) throws Exception {

    Schema schema = SchemaBuilder.from(new DatabaseInstancesReader())
            .withSaver(new DatabaseInstancesWriter())
            .build();

    for (int i = 0; i < 200; i++) {
      schema.put("features", "features");
      schema.put("bugs", "bugs");
      schema.put("cleanups", "cleanups");
      schema.put("release", "release");
      schema.put("merge", "merge");
    }
    schema.flush();

    System.out.println(schema.infer("bugs"));
  }
}
