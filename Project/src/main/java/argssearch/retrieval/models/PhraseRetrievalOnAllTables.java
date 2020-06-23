package argssearch.retrieval.models;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PhraseRetrievalOnAllTables {

    private final PreparedStatement query;
    private final TokenCache cache;
    private final CoreNlpService nlpService;

    public PhraseRetrievalOnAllTables(CoreNlpService nlpService, TokenCache cache) {
        this.query = ArgDB.getInstance().prepareStatement(
                "SELECT content, ispro, argID, pID, dID, SUM(phraseMatches) as phraseCount, SUM(weightSum) as sum, COUNT(*) as count " +
                        "FROM " +
                        "( " +
                        "    SELECT content, ispro, argID, t_arg.pID, dID, phraseMatches, weightSum " +
                        "    FROM premise INNER JOIN " +
                        "    ( " +
                        "        SELECT content, ispro, argument.argID, argument.pID, phraseMatches, weightSum " +
                        "        FROM argument INNER JOIN " +
                        "        ( " +
                        "            SELECT argument_Index.argID, phraseCount(?::INT[], array_agg(tID), array_agg(occurrences), array_cat_agg(offsets)) as phraseMatches, SUM(weight) * ? AS weightSum " +
                        "            FROM argument_index " +
                        "            WHERE weight >= ? AND tid = ANY(?::INT[]) " +
                        "            GROUP BY argument_Index.argID " +
                        "            HAVING COUNT(tid) = ? AND phraseCount(?::INT[], array_agg(tid), array_agg(occurrences), array_cat_agg(offsets)) > 0 " +
                        "            ORDER BY phraseMatches DESC, weightSum DESC " +
                        "            LIMIT ? " +
                        "        ) " +
                        "        AS t_arg_Index " +
                        "        ON t_arg_Index.argID = argument.argID " +
                        "    ) " +
                        "    AS t_arg " +
                        "    ON t_arg.pID = premise.pID " +
                        "UNION ALL " +
                        "    SELECT content, ispro, argID, t_premise.pID, dID, phraseMatches, weightSum " +
                        "    FROM argument INNER JOIN " +
                        "    ( " +
                        "        SELECT premise.pID, dID, phraseMatches, weightSum " +
                        "        FROM premise INNER JOIN " +
                        "        ( " +
                        "            SELECT premise_Index.pID, phraseCount(?::INT[], array_agg(tID), array_agg(occurrences), array_cat_agg(offsets)) as phraseMatches, SUM(weight) * ? AS weightSum " +
                        "            FROM premise_index " +
                        "            WHERE weight >= ? AND tid = ANY(?::INT[]) " +
                        "            GROUP BY premise_Index.pID " +
                        "            HAVING COUNT(tid) = ? AND phraseCount(?::INT[], array_agg(tid), array_agg(occurrences), array_cat_agg(offsets)) > 0 " +
                        "            ORDER BY phraseMatches DESC, weightSum DESC " +
                        "            LIMIT ? " +
                        "        ) " +
                        "        AS t_premise_index " +
                        "        ON t_premise_index.pID = premise.pID " +
                        "    ) " +
                        "    AS t_premise " +
                        "    ON t_premise.pID = argument.pID " +
                        "UNION ALL " +
                        "    SELECT content, ispro, argID, t_premise.pID, dID, phraseMatches, weightSum " +
                        "    FROM argument INNER JOIN " +
                        "    ( " +
                        "        SELECT pID, t_discussion.dID, phraseMatches, weightSum " +
                        "        FROM premise INNER JOIN " +
                        "        ( " +
                        "            SELECT discussion.dID, phraseMatches, weightSum " +
                        "            FROM discussion INNER JOIN " +
                        "            ( " +
                        "                SELECT discussion_index.dID, phraseCount(?::INT[], array_agg(tID), array_agg(occurrences), array_cat_agg(offsets)) as phraseMatches, SUM(weight) * ? AS weightSum " +
                        "                FROM discussion_index " +
                        "                WHERE weight >= ? AND tid = ANY(?::INT[]) " +
                        "                GROUP BY discussion_index.dID " +
                        "                HAVING COUNT(tid) = ? AND phraseCount(?::INT[], array_agg(tid), array_agg(occurrences), array_cat_agg(offsets)) > 0 " +
                        "                ORDER BY phraseMatches DESC, weightSum DESC " +
                        "                LIMIT ? " +
                        "            ) " +
                        "            AS t_discussion_index " +
                        "            ON t_discussion_index.dID = discussion.dID " +
                        "        ) " +
                        "        AS t_discussion " +
                        "        ON t_discussion.dID = premise.dID " +
                        "    ) " +
                        "    AS t_premise " +
                        "    ON t_premise.pID = argument.pID " +
                        ") " +
                        "AS end_t " +
                        "Group by end_t.content, end_t.pid, end_t.did, end_t.argID, end_t.ispro " +
                        "ORDER BY phraseCount desc, sum desc " +
                        "LIMIT ?; "
        );
        this.nlpService = nlpService;
        this.cache = cache;
    }

    public void execute(final String text
            , final int tokenMinWeightDiscussion
            , final int tokenMinWeightPremise
            , final int tokenMinWeightArgument
            , final int tokenWeightMultipierDiscussion
            , final int tokenWeightMultipierPremise
            , final int tokenWeightMultipierArgument
            , final int limitDiscussion
            , final int limitPremise
            , final int limitArgument
            , final int limitFinal
    )
    {   List<Integer> preprocessedText = nlpService.lemmatize(text).stream().map(this.cache::get).collect(Collectors.toList());
        if (preprocessedText.size() == 0) {
            return;
        }

        // A B A C
        String tokenOrder = preprocessedText.stream().map(String::valueOf).collect(
                Collectors.joining(", ", "{", "}"));

        // A B C
        String tokenArray = preprocessedText.stream().distinct().map(String::valueOf).collect(
                Collectors.joining(", ", "{", "}"));

        int input = (int)preprocessedText.stream().distinct().count();

        try{
            this.query.setString(1, tokenOrder);
            this.query.setInt(2, tokenWeightMultipierArgument);
            this.query.setInt(3, tokenMinWeightArgument);
            this.query.setString(4, tokenArray);
            this.query.setInt(5, input);
            this.query.setString(6, tokenOrder);
            this.query.setInt(7, limitArgument);

            this.query.setString(8, tokenOrder);
            this.query.setInt(9, tokenWeightMultipierPremise);
            this.query.setInt(10, tokenMinWeightPremise);
            this.query.setString(11, tokenArray);
            this.query.setInt(12, input);
            this.query.setString(13, tokenOrder);
            this.query.setInt(14, limitPremise);

            this.query.setString(15, tokenOrder);
            this.query.setInt(16, tokenWeightMultipierDiscussion);
            this.query.setInt(17, tokenMinWeightDiscussion);
            this.query.setString(18, tokenArray);
            this.query.setInt(19, input);
            this.query.setString(20, tokenOrder);
            this.query.setInt(21, limitDiscussion);

            this.query.setInt(22, limitFinal);

            ResultSet resultSet = this.query.executeQuery();
            int i = 0;

            while (resultSet.next()) {
                //todo Output

                //System.out.println(resultSet.getString(1)+" | "+resultSet.getBoolean(2)+" | "+resultSet.getInt(3)+" | "+resultSet.getInt(4)+" | "+resultSet.getInt(5)+" | "+resultSet.getInt(6)+" | "+resultSet.getInt(8));
            }
            resultSet.close();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }
}
