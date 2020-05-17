package argssearch.indexing.index;

import argssearch.shared.db.AbstractTextEntity;
import argssearch.shared.db.ArgDB;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringSplitter {

    private TokenCache cache;
    PreparedStatement ps;
    int counter = 0;

    StringSplitter(AbstractTextEntity entry) {
        this.cache = TokenCacheFactory.getInstance().get(10000);

        ps = ArgDB.getInstance().prepareStatement(String.format(
                "INSERT INTO %s_index (tID, %s, occurences, offsets) VALUES (?,?,?,?)",
                entry.getTableName(),
                entry.getTableName()
        ));
        addIndicesInDB(entry);
    }

    protected void addIndicesInDB(AbstractTextEntity entry){
        Map<String, List<Integer>> splittedString = getSplittedString(entry.getTextAttributeName().split(" "));
        for(Map.Entry<String, List<Integer>> token : splittedString.entrySet()){
            insertNew(token,entry.getPrimaryKeyAttributeName() //TODO entry.getID oÄ
                    );
        }
    }

    private void insertNew(Map.Entry<String, List<Integer>> entry, String id){
        if(counter > 99){
            executeBatch();
        }
        String token = entry.getKey();
        Integer tId = entry.getValue().remove(0);
        List<Integer> offsets = entry.getValue();
        Integer[] array = (Integer[]) offsets.toArray();
        try {
            ps.setInt(1, tId);
            ps.setString(2, id);
            ps.setInt(3, offsets.size());
            ps.setArray(4, ArgDB.getInstance().getConnection().createArrayOf("SMALLINT",array));
            ps.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        counter++;
    }


    protected void executeBatch(){
        try {
            int[] results = ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
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
