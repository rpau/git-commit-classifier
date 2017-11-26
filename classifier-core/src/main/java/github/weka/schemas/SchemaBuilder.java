package github.weka.schemas;

import github.weka.readers.InstancesReader;
import github.weka.writers.FileInstancesWriter;
import github.weka.writers.InstancesWriter;

import java.util.stream.Stream;

public class SchemaBuilder {

  static String[] DEFAULT_CATEGORIES =
          Stream.of(Category.values())
          .map(Category::getText)
          .toArray(String[]::new);

  InstancesReader reader;

  InstancesWriter saver = new FileInstancesWriter();

  String[] categories = DEFAULT_CATEGORIES;

  private SchemaBuilder() {
  }

  public static SchemaBuilder emptySchema() {
    return new SchemaBuilder();
  }

  public static SchemaBuilder from(InstancesReader reader) {
    SchemaBuilder builder = new SchemaBuilder();
    builder.reader = reader;
    return builder;
  }

  public SchemaBuilder withSaver(InstancesWriter saver) {
    this.saver = saver;
    return this;
  }

  public SchemaBuilder withCategories(String[] categories) {
    this.categories = categories;
    return this;
  }

  public Schema build() throws Exception {
    if (reader != null) {
      new Schema(reader.read(), this);
    }
    return new Schema( this);
  }
}
