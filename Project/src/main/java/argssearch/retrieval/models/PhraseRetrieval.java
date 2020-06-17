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

public class PhraseRetrieval {

  private final PreparedStatement query;
  private final TokenCache cache;
  private final CoreNlpService nlpService;

  public PhraseRetrieval(CoreNlpService nlpService, TokenCache cache, AbstractIndexTable table) {
    this.query = ArgDB.getInstance().prepareStatement(String.format(
      "SELECT %s, phraseCount(?::INT[], array_agg(%s), array_agg(%s), array_cat_agg(%s)) as phraseMatches, SUM(%s) * ? AS weightSum " +
      "FROM %s " +
      "WHERE %s >= ? AND %s = ANY(?::INT[]) " +
      "GROUP BY %s " +
      "HAVING COUNT(%s) = ? AND phraseCount(?::INT[], array_agg(%s), array_agg(%s), array_cat_agg(%s)) > 0 " +
      "ORDER BY phraseMatches DESC, weightSum DESC",
      table.getRefId(),
      table.getTokenID(),
      table.getOccurrences(),
      table.getOffsets(),
      table.getWeight(),
      table.getTableName(),
      table.getWeight(),
      table.getTokenID(),
      table.getRefId(),
      table.getTokenID(),
      table.getTokenID(),
      table.getOccurrences(),
      table.getOffsets()
    ));

    this.nlpService = nlpService;
    this.cache = cache;
  }

  public void execute(final String text, final int tokenMinWeight, final int weightMultiplier, final BiConsumer<Integer, Integer> argumentProcessor) {
    List<Integer> preprocessedText = nlpService.lemmatize(text).stream().map(this.cache::get).collect(Collectors.toList());
    if (preprocessedText.size() == 0) {
      return;
    }

    // A B A C
    String tokenOrder = preprocessedText.stream().map(String::valueOf).collect(
        Collectors.joining(", ", "{", "}"));

    // A B C
    String tokenArray = preprocessedText.stream().distinct().map(String::valueOf).collect(
        Collectors.joining(", ", "{", "}"));

    try {
      query.setString(1, tokenOrder);
      query.setInt(2, weightMultiplier);
      query.setInt(3, tokenMinWeight);
      query.setString(4, tokenArray);
      query.setInt(5, (int)preprocessedText.stream().distinct().count());
      query.setString(6, tokenOrder);

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
