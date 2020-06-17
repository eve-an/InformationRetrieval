package argssearch.retrieval.models;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ConjunctiveRetrievalOnAllTables {

    private final PreparedStatement query;
    private final TokenCache cache;
    private final CoreNlpService nlpService;

    public ConjunctiveRetrievalOnAllTables(CoreNlpService nlpService, TokenCache cache) {
        this.query = ArgDB.getInstance().prepareStatement(
                "SELECT content, ispro, argID, pID, dID, SUM(sumW) as sum, SUM(tid_matches) as tid_count, SUM(occurrence_count) as occ_count, COUNT(*) as count " +
                        "FROM " +
                        "( " +
                        "    SELECT content, ispro, argID, t_arg.pID, dID, sumW, tid_matches, occurrence_count " +
                        "    FROM premise INNER JOIN " +
                        "    ( " +
                        "        SELECT content, ispro, argument.argID, argument.pID, sumW, tid_matches, occurrence_count " +
                        "        FROM argument INNER JOIN " +
                        "        ( " +
                        "            SELECT argument_Index.argID, SUM(weight) * ? AS sumW, COUNT(tID) AS tid_matches, SUM(occurrences) AS occurrence_count " +
                        "            FROM argument_Index " +
                        "            WHERE weight >= ? AND tID = ANY(?::int[]) " +
                        "            GROUP BY argument_Index.argID " +
                        "            ORDER BY sumW DESC, tid_matches DESC, occurrence_count DESC " +
                        "            LIMIT ? " +
                        "        ) " +
                        "        AS t_arg_Index " +
                        "        ON t_arg_Index.argID = argument.argID " +
                        "    ) " +
                        "    AS t_arg " +
                        "    ON t_arg.pID = premise.pID " +
                        "UNION ALL " +
                        "    SELECT content, ispro, argID, t_premise.pID, dID, sumW, tid_matches, occurrence_count " +
                        "    FROM argument INNER JOIN " +
                        "    ( " +
                        "        SELECT premise.pID, dID, sumW, tid_matches, occurrence_count " +
                        "        FROM premise INNER JOIN " +
                        "        ( " +
                        "            SELECT premise_Index.pID, SUM(weight) * ? AS sumW, COUNT(tID) AS tid_matches, SUM(occurrences) AS occurrence_count " +
                        "            FROM premise_Index " +
                        "            WHERE weight >= ? AND tID = ANY(?::int[]) " +
                        "            GROUP BY premise_Index.pID " +
                        "            ORDER BY sumW DESC, tid_matches DESC, occurrence_count DESC " +
                        "            LIMIT ? " +
                        "        ) " +
                        "        AS t_premise_index " +
                        "        ON t_premise_index.pID = premise.pID " +
                        "    ) " +
                        "    AS t_premise " +
                        "    ON t_premise.pID = argument.pID " +
                        "UNION ALL " +
                        "    SELECT content, ispro, argID, t_premise.pID, dID, sumW, tid_matches, occurrence_count " +
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
                        "Group by end_t.content, end_t.pid, end_t.did, end_t.argID, end_t.ispro " +
                        "ORDER BY sum desc, occ_count DESC, tid_count DESC " +
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
    { List<String> preprocessedText = nlpService.lemmatize(text);
        String tokenArray = preprocessedText.stream().map(this.cache::get).map(String::valueOf).collect(Collectors.joining(", ", "{", "}"));

        try{
            this.query.setInt(1, tokenWeightMultipierArgument);
            this.query.setInt(2, tokenMinWeightArgument);
            this.query.setString(3, tokenArray);
            this.query.setInt(4, limitArgument);
            this.query.setInt(5, tokenWeightMultipierPremise);
            this.query.setInt(6, tokenMinWeightPremise);
            this.query.setString(7, tokenArray);
            this.query.setInt(8, limitPremise);
            this.query.setInt(9, tokenWeightMultipierDiscussion);
            this.query.setInt(10, tokenMinWeightDiscussion);
            this.query.setString(11, tokenArray);
            this.query.setInt(12, limitDiscussion);
            this.query.setInt(13, limitFinal);

            ResultSet resultSet = this.query.executeQuery();
            while (resultSet.next()) {
                //todo Output

                // System.out.println(resultSet.getString(1)+" | "+resultSet.getBoolean(2)+" | "+resultSet.getInt(3)+" | "+resultSet.getInt(4)+" | "+resultSet.getInt(5)+" | "+resultSet.getInt(6)+" | "+resultSet.getInt(8)+" | "+resultSet.getInt(9));
            }
            resultSet.close();
        } catch (SQLException sqlE) {
            if (ArgDB.isException(sqlE)) {
                sqlE.printStackTrace();;
            }
        }
    }
}
