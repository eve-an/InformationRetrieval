package argssearch.retrieval.models;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class DisjunctiveRetrieval {

  private final PreparedStatement query;
  private final TokenCache cache;
  private final CoreNlpService nlpService;

  public DisjunctiveRetrieval(CoreNlpService nlpService, TokenCache cache, AbstractIndexTable table) {
      final String prequery ;
      final String postquery ;
      switch(table.getTableName().toLowerCase()){
          case "argument_index":
              prequery =
                      "SELECT crawlID AS doc, DENSE_RANK() OVER(ORDER BY weight,argument.argid) AS rank, weight AS score " +
                              "FROM argument INNER JOIN " +
                              "( ";
              postquery =
                      ") " +
                              "AS t_argument_index " +
                              "ON t_argument_index.refid = argument.argid " +
                              "ORDER BY rank ASC, score DESC, crawlID DESC;";
              break;
          case "premise_index":
              prequery =
                      "SELECT crawlID AS doc, DENSE_RANK() OVER(ORDER BY weight,argument.argid) AS rank, weight AS score " +
                              "FROM argument INNER JOIN " +
                              "( ";
              postquery =
                      ") " +
                              "AS t_premise_index " +
                              "ON t_premise_index.refid = argument.pid " +
                              "ORDER BY rank ASC, score DESC, crawlID DESC; ";
              break;
          case "discussion_index":
              prequery =
                      "SELECT crawlID AS doc, DENSE_RANK() OVER(ORDER BY weight,argument.argid) AS rank, weight AS score " +
                              "FROM argument INNER JOIN " +
                              "( " +
                              "   SELECT pid, weight " +
                              "   FROM premise INNER JOIN " +
                              "   (";
              postquery =
                      "   ) " +
                              "   AS t_discussion_index " +
                              "   ON t_discussion_index.refid = premise.did " +
                              ") " +
                              "AS t_premise " +
                              "ON t_premise.pid = argument.pid " +
                              "ORDER BY rank ASC, score DESC, crawlID DESC; ";
              break;
          default:
              prequery = "";
              postquery = ";";
              System.out.println("No matching tablename");
              break;
      }
          this.query = ArgDB.getInstance().prepareStatement(String.format(
            "%s SELECT %s AS refid, sum(%s) * ? AS weight "+
            "FROM %s " +
            "WHERE %s >= ? AND %s = ANY(?::int[]) " +
            "GROUP BY refid %s",
            prequery,
            table.getRefId(),
            table.getWeight(),
            table.getTableName(),
            table.getWeight(),
            table.getTokenID(),
          postquery));

    this.nlpService = nlpService;
    this.cache = cache;
  }

  public void execute(final String text, final int tokenMinWeight, final int weightMultiplier) {
    String tokenArray = nlpService.lemmatize(text)
        .stream()
        .distinct()
        .map(this.cache::get)
        .map(String::valueOf)
        .collect(Collectors.joining(", ", "{", "}"));

    try {
        this.query.setInt(1, weightMultiplier);
        this.query.setInt(2, tokenMinWeight);
        this.query.setString(3, tokenArray);

        ResultSet resultSet = this.query.executeQuery();
        //System.out.println("output: ");
        while (resultSet.next()) {
            //System.out.println(resultSet.getString(1) + " " + resultSet.getInt(2)+ " " + resultSet.getDouble(3));
            //todo triconsumer
        }
        resultSet.close();
    } catch (SQLException sqlE) {
        //System.out.println("exep");
        System.out.println(sqlE.getMessage());
        sqlE.printStackTrace();
    }
  }
}
