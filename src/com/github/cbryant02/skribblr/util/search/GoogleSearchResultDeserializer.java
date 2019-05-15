package com.github.cbryant02.skribblr.util.search;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Array deserializer for {@link GoogleSearchResult}.
 */
final class GoogleSearchResultDeserializer extends StdDeserializer<GoogleSearchResult[]> {
    /**
     * Construct a new {@code GoogleSearchResultDeserializer}.
     */
    GoogleSearchResultDeserializer() {
        super(GoogleSearchResult[].class);
    }

    @Override
    public GoogleSearchResult[] deserialize(JsonParser parser, DeserializationContext dc) throws IOException {
        ArrayList<GoogleSearchResult> results = new ArrayList<>();

        JsonNode root = parser.getCodec().readTree(parser);
        ArrayNode itemsArray = (ArrayNode)root.get("items");
        itemsArray.forEach(resultNode -> {
            JsonNode imageNode = resultNode.get("image");

            GoogleSearchResult result = new GoogleSearchResult();
            result.setMeta(new GoogleSearchResult.ResultMeta(imageNode.get("height").intValue(), imageNode.get("width").intValue(), imageNode.get("thumbnailLink").asText()));
            result.setTitle(resultNode.get("title").asText());
            result.setLink((resultNode.get("link").asText()).split("\\?")[0]);  // The link is split on '?' to remove extra parameters

            results.add(result);
        });

        return results.toArray(new GoogleSearchResult[0]);
    }
}
