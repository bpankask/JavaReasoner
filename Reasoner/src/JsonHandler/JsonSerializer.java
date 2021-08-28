package JsonHandler;

/**
 * Interface for classes used to serialize reasoner data into json format.
 */
public interface JsonSerializer {
    String serializeToJson();
    void writeJson(String fileName);
}
