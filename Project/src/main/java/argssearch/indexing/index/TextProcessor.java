package argssearch.indexing.index;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.AbstractTextTable;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import edu.stanford.nlp.simple.Token;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextProcessor {

    private static final int MAX_BATCH_SIZE = 99;

    private TokenCache cache;
    private CoreNlpService nlpService;
    private PreparedStatement ps;
    private int counter = 0;

    TextProcessor(AbstractIndexTable table, TokenCache cache, CoreNlpService nlpService) {
        this.cache = cache;
        this.nlpService = nlpService;

        // TODO integrate this with AbstractIndexTable
        ps = ArgDB.getInstance().prepareStatement(String.format(
                "INSERT INTO %s (tID, %s, occurrences, offsets) VALUES (?,?,?,?)",
                table.getTableName(),
                table.getRefId()
        ));

    }

    void process(final int id, final String text){
        Map<String, List<Integer>> splittedString = getSplittedString(nlpService.lemmatize(text));
        for(Map.Entry<String, List<Integer>> token : splittedString.entrySet()){

            Integer tId = token.getValue().remove(0);
            List<Integer> offsets = token.getValue();
            Integer[] array = offsets.toArray(new Integer[0]);

            try {
                ps.setInt(1, tId);
                ps.setInt(2, id);
                ps.setInt(3, offsets.size());
                ps.setArray(4, ArgDB.getInstance().createArrayOf("SMALLINT", array));
                ps.addBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(++counter > MAX_BATCH_SIZE){
                executeBatch();
            }
        }
    }


    private void executeBatch(){
        try {
            int[] results = ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void finish() {
        if (counter > 0) {
            executeBatch();
        }
    }

    /**
     * to get the tokens from the text together with the tokenid and the positions within the text
     * @param text the text as Array, every word is an entry
     * @return String ist the token, the first Integer is the tokenId followed by the indices where the token occurs
     * in the given String
     */
    private Map<String, List<Integer>> getSplittedString(List<String> text){
        Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();
        for(int i = 0; i < text.size(); i++){
            if(result.containsKey(text.get(i))){
                result.get(text.get(i)).add(i);
            }else{
                ArrayList<Integer> l = new ArrayList<Integer>();
                l.add(this.cache.get(text.get(i)));
                l.add(i);
                result.put(text.get(i), l);
            }
        }
        return result;
    }
}
