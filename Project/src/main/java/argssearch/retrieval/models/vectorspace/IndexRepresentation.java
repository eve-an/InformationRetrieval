package argssearch.retrieval.models.vectorspace;

import argssearch.shared.query.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IndexRepresentation {

    private final String crawlId;
    private final Map<Integer, Double> tokenWeightMap = new HashMap<>();
    private Result.DocumentType type;

    public IndexRepresentation(String crawlId, Integer[] tokenIds, Double[] weights, Result.DocumentType type) {
        this.crawlId = crawlId;
        this.type = type;
        loadMap(tokenIds, weights);
    }

    private void loadMap(Integer[] tokenIds, Double[] weights) {
        if (tokenIds.length != weights.length) {
            throw new IllegalStateException("Token array length and weight array length differ. Each token has exactly one weight!");
        }

        for (int i = 0; i < tokenIds.length; i++) {
            int tokenId = tokenIds[i];
            double tokenWeight = weights[i];

            if (tokenWeightMap.put(tokenId - 1, tokenWeight) != null) {
                throw new IllegalStateException("Token id " + tokenId + " must be unique.");
            }
        }
    }

    public String getCrawlId() {
        return crawlId;
    }

    public Vector toVector() {
        return new Vector(tokenWeightMap);
    }

    public Result.DocumentType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexRepresentation that = (IndexRepresentation) o;
        return crawlId.equals(that.crawlId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crawlId);
    }
}
