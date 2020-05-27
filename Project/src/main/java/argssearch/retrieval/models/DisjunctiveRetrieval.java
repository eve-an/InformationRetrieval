package argssearch.retrieval.models;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DisjunctiveRetrieval {

  private final PreparedStatement query;
  private final TokenCache cache;
  private final CoreNlpService nlpService;

  public DisjunctiveRetrieval(CoreNlpService nlpService, TokenCache cache, AbstractIndexTable table) {
    this.query = ArgDB.getInstance().prepareStatement(String.format(
      "SELECT %s AS refid, sum(%s) * ? AS weight, count(%s) AS tid_matches, SUM(%s) AS occurrence_count "+
      "FROM %s " +
      "WHERE %s >= ? AND %s = ANY(?::int[]) " +
      "GROUP BY refid " +
      "ORDER BY weight DESC, tid_matches DESC, occurrence_count DESC;",
      table.getRefId(),
      table.getWeight(),
      table.getTokenID(),
      table.getOccurrences(),
      table.getTableName(),
      table.getWeight(),
      table.getTokenID()));

    this.nlpService = nlpService;
    this.cache = cache;
  }

  public void execute(final String text, final int tokenMinWeight, final int weightMultiplier, final BiConsumer<Integer, Integer> argumentProcessor) {
    String tokenArray = nlpService.lemmatize(text).stream().map(this.cache::get).map(String::valueOf).collect(Collectors.joining(", ", "{", "}"));

    try {
        this.query.setInt(1, weightMultiplier);
        this.query.setInt(2, tokenMinWeight);
        this.query.setString(3, tokenArray);

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
