package argssearch.acquisition;

import java.util.List;


class JsonArgument {

  private String id;
  private String conclusion;
  private List<Premise> premises;
  private Context context;

  public String getId() {
    return id;
  }

  public String getConclusion() {
    return conclusion;
  }

  public List<Premise> getPremises() {
    return premises;
  }

  public Context getContext() {
    return context;
  }

  @Override
  public String toString() {
    return "JsonArgument{" +
        "id='" + id + '\'' +
        ", conclusion='" + conclusion + '\'' +
        '}';
  }

  public static class Premise {

    private String text;
    private String stance;

    public String getText() {
      return text;
    }

    public String getStance() {
      return stance;
    }
  }

  public static class Context {

    private String sourceId;
    private String discussionTitle;
    private String sourceUrl;
    private String sourceDomain;

    public String getSourceId() {
      return sourceId;
    }

    public String getDiscussionTitle() {
      return discussionTitle;
    }

    public String getSourceUrl() {
      return sourceUrl;
    }

    public String getSourceDomain() {
      return sourceDomain;
    }
  }

}
