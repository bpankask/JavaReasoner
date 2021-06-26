package JSONHandler;

/**
 * All Json Serializer classes must contain a method to write to a json file.
 */
public interface JsonSerializer {
    String serializeToJson();
    void writeJson(String fileName, String json);
}
