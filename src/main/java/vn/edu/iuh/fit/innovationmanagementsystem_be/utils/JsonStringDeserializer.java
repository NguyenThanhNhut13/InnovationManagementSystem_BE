package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonStringDeserializer extends JsonDeserializer<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isTextual()) {
            // Nếu là string, parse thành JSON object
            try {
                return objectMapper.readValue(node.asText(), Object.class);
            } catch (Exception e) {
                // Nếu parse lỗi, trả về string gốc
                return node.asText();
            }
        } else {
            // Nếu đã là object, trả về trực tiếp
            return objectMapper.treeToValue(node, Object.class);
        }
    }
}
