package argssearch.indexing.index;

import argssearch.shared.db.AbstractTextTable;
import argssearch.shared.db.ArgDB;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextProcessor {

    private static final int MAX_BATCH_SIZE = 99;

    private TokenCache cache;
    private PreparedStatement ps;
    private int counter = 0;

    TextProcessor(AbstractTextTable entry) {
        this.cache = TokenCacheFactory.getInstance().get(10000);

        // TODO integrate this with AbstractIndexTable
        ps = ArgDB.getInstance().prepareStatement(String.format(
                "INSERT INTO %s_index (tID, %s, occurrences, offsets) VALUES (?,?,?,?)",
                entry.getTableName(),
                entry.getPrimaryKeyAttributeName()
        ));
    }

    void process(final int id, final String text){
        Map<String, List<Integer>> splittedString = getSplittedString(text.split(" "));
        for(Map.Entry<String, List<Integer>> token : splittedString.entrySet()){

            Integer tId = token.getValue().remove(0);
            List<Integer> offsets = token.getValue();
            Integer[] array = offsets.toArray(new Integer[0]);

            try {
                ps.setInt(1, tId);
                ps.setInt(2, id);
                ps.setInt(3, offsets.size());
                ps.setArray(4, ArgDB.getInstance().getConnection().createArrayOf("SMALLINT", array));
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
    private Map<String, List<Integer>> getSplittedString(String[] text){
        Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();
        for(int i = 0; i < text.length; i++){
            if(result.containsKey(text[i])){
                result.get(text[i]).add(i);
            }else{
                ArrayList<Integer> l = new ArrayList<Integer>();
                l.add(this.cache.get(text[i]));
                l.add(i);
                result.put(text[i], l);
            }
        }
        return result;
    }
}
