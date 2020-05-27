package argssearch.retrieval.models;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ConjunctiveRetrieval {

  private final PreparedStatement query;
  private final TokenCache cache;
  private final CoreNlpService nlpService;

  public ConjunctiveRetrieval(CoreNlpService nlpService, TokenCache cache, AbstractIndexTable table) {
    this.query = ArgDB.getInstance().prepareStatement(String.format(
      "SELECT %s AS refid, sum(%s) * ? AS weight, SUM(%s) AS occurrence_count "+
      "FROM %s " +
      "WHERE %s >= ? AND %s = ANY(?::int[]) " +
      "GROUP BY refid " +
      "HAVING COUNT(%s) = ? " +
      "ORDER BY weight DESC, occurrence_count DESC;",
      table.getRefId(),
      table.getWeight(),
      table.getOccurrences(),
      table.getTableName(),
      table.getWeight(),
      table.getTokenID(),
      table.getTokenID()));

    this.nlpService = nlpService;
    this.cache = cache;
  }

  public void execute(final String text, final int tokenMinWeight, final int weightMultiplier, final BiConsumer<Integer, Integer> argumentProcessor) {
    List<String> preprocessedText = nlpService.lemmatize(text);
    String tokenArray = preprocessedText.stream().map(this.cache::get).map(String::valueOf).collect(Collectors.joining(", ", "{", "}"));

    try {
        this.query.setInt(1, weightMultiplier);
        this.query.setInt(2, tokenMinWeight);
        this.query.setString(3, tokenArray);
        this.query.setInt(4, preprocessedText.size());

        ResultSet resultSet = this.query.executeQuery();
        while (resultSet.next()) {
          argumentProcessor.accept(resultSet.getInt(1), resultSet.getInt(2));
        }
        resultSet.close();
    } catch (SQLException sqlE) {
      if (ArgDB.isException(sqlE)) {
        sqlE.printStackTrace();
      }
    }
  }
}
