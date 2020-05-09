package argssearch.indexing.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringSplitter {

    private TokenCache cache;

    StringSplitter() {
        this.cache = TokenCacheFactory.getInstance().get(10000);
    }

    /**
     * to get the tokens from the text together with the tokenid and the positions within the text
     * @param text the text as Array, every word is an entry
     * @return String ist the token, the first Integer is the tokenId followed by the indices where the token occurs
     * in the given String
     */

    Map<String, List<Integer>> getSplittedString(String[] text){
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
