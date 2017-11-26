package github.weka.schemas;

public enum Category {
  BUGS("bugs", 0),
  FEATURES("features", 1),
  CLEAN_UPS("cleanups", 2),
  RELEASE("release", 3),
  MERGE("merge", 4);

  private String text;

  private int id;

  Category(String text, int id) {
    this.text = text;
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public int getId() {
    return id;
  }
}
