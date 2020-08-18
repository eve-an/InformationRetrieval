package argssearch.retrieval.models;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.ArgDB;
import argssearch.shared.interfaces.TriConsumer;
import argssearch.shared.nlp.CoreNlpService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DisjunctiveRetrievalOnAllTables {

    private final PreparedStatement query;
    private final TokenCache cache;
    private final CoreNlpService nlpService;

    public DisjunctiveRetrievalOnAllTables(CoreNlpService nlpService, TokenCache cache) {
        this.query = ArgDB.getInstance().prepareStatement(
                "SELECT end_t.crawlID AS doc, DENSE_RANK() over (ORDER BY SUM(sumW),argid) AS rank, SUM(sumW) as score " +
                        "FROM " +
                        "( " +
                        "    SELECT t_arg.crawlID, content, ispro, argID, t_arg.pID, dID, sumW, tid_matches, occurrence_count " +
                        "    FROM premise INNER JOIN " +
                        "    ( " +
                        "        SELECT argument.crawlID, content, ispro, argument.argID, argument.pID, sumW, tid_matches, occurrence_count " +
                        "        FROM argument INNER JOIN " +
                        "        ( " +
                        "            SELECT argument_Index.argID, SUM(weight) * ? AS sumW, COUNT(tID) AS tid_matches, SUM(occurrences) AS occurrence_count " +
                        "            FROM argument_Index " +
                        "            WHERE weight >= ? AND tID = ANY(?::int[]) " +
                        "            GROUP BY argument_Index.argID " +
                        "            HAVING COUNT(tid) = ? "  +
                        "            ORDER BY sumW DESC, tid_matches DESC, occurrence_count DESC " +
                        "            LIMIT ? " +
                        "        ) " +
                        "        AS t_arg_Index " +
                        "        ON t_arg_Index.argID = argument.argID " +
                        "    ) " +
                        "    AS t_arg " +
                        "    ON t_arg.pID = premise.pID " +
                        "UNION ALL " +
                        "    SELECT argument.crawlID, content, ispro, argID, t_premise.pID, dID, sumW, tid_matches, occurrence_count " +
                        "    FROM argument INNER JOIN " +
                        "    ( " +
                        "        SELECT premise.pID, dID, sumW, tid_matches, occurrence_count " +
                        "        FROM premise INNER JOIN " +
                        "        ( " +
                        "            SELECT premise_Index.pID, SUM(weight) * ? AS sumW, COUNT(tID) AS tid_matches, SUM(occurrences) AS occurrence_count " +
                        "            FROM premise_Index " +
                        "            WHERE weight >= ? AND tID = ANY(?::int[]) " +
                        "            GROUP BY premise_Index.pID " +
                        "            HAVING COUNT(tid) = ? "  +
                        "            ORDER BY sumW DESC, tid_matches DESC, occurrence_count DESC " +
                        "            LIMIT ? " +
                        "        ) " +
                        "        AS t_premise_index " +
                        "        ON t_premise_index.pID = premise.pID " +
                        "    ) " +
                        "    AS t_premise " +
                        "    ON t_premise.pID = argument.pID " +
                        "UNION ALL " +
                        "    SELECT argument.crawlID, content, ispro, argID, t_premise.pID, dID, sumW, tid_matches, occurrence_count " +
                        "    FROM argument INNER JOIN " +
                        "    ( " +
                        "        SELECT pID, t_discussion.dID, sumW, tid_matches, occurrence_count " +
                        "        FROM premise INNER JOIN " +
                        "        ( " +
                        "            SELECT discussion.dID, sumW, tid_matches, occurrence_count " +
                        "            FROM discussion INNER JOIN " +
                        "            ( " +
                        "                SELECT discussion_index.dID, SUM(weight) * ? AS sumW, COUNT(tID) AS tid_matches, SUM(occurrences) AS occurrence_count " +
                        "                FROM discussion_index " +
                        "                WHERE weight >= ? AND tID = ANY(?::int[]) " +
                        "                GROUP BY discussion_index.dID " +
                        "                HAVING COUNT(tid) = ? "  +
                        "                ORDER BY sumW DESC, tid_matches DESC, occurrence_count DESC " +
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
                        "Group by end_t.crawlID, end_t.argid " +
                        "ORDER BY rank ASC, score DESC, crawlID DESC " +
                        "LIMIT ?; "
        );
        this.nlpService = nlpService;
        this.cache = cache;
    }

    public void execute(final String text
            , final int tokenMinWeightDiscussion
            , final int tokenMinWeightPremise
            , final int tokenMinWeightArgument
            , final double tokenWeightMultipierDiscussion
            , final double tokenWeightMultipierPremise
            , final double tokenWeightMultipierArgument
            , final int limitDiscussion
            , final int limitPremise
            , final int limitArgument
            , final int limitFinal
            , TriConsumer<String,Integer,Double> triConsumer
    )
    { List<String> preprocessedText = nlpService.lemmatize(text);
        String tokenArray = preprocessedText.stream().map(this.cache::get).map(String::valueOf).collect(Collectors.joining(", ", "{", "}"));

        try{
            this.query.setDouble(1, tokenWeightMultipierArgument);
            this.query.setInt(2, tokenMinWeightArgument);
            this.query.setString(3, tokenArray);
            this.query.setInt(4, preprocessedText.size());
            this.query.setInt(5, limitArgument);
            this.query.setDouble(6, tokenWeightMultipierPremise);
            this.query.setInt(7, tokenMinWeightPremise);
            this.query.setString(8, tokenArray);
            this.query.setInt(9, preprocessedText.size());
            this.query.setInt(10, limitPremise);
            this.query.setDouble(11, tokenWeightMultipierDiscussion);
            this.query.setInt(12, tokenMinWeightDiscussion);
            this.query.setString(13, tokenArray);
            this.query.setInt(14, preprocessedText.size());
            this.query.setInt(15, limitDiscussion);
            this.query.setInt(16, limitFinal);

            ResultSet resultSet = this.query.executeQuery();
            //System.out.println("output: ");
            while (resultSet.next()) {
                triConsumer.accept(resultSet.getString(1),resultSet.getInt(2),resultSet.getDouble(3));
            }
            resultSet.close();
        } catch (SQLException sqlE) {
            //System.out.println("exep");
            System.out.println(sqlE.getMessage());
            sqlE.printStackTrace();
        }
    }
}
