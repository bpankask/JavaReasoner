package JsonHandler;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.file.Paths;
import java.util.Map;

public abstract class JsonWriter {

    /**
     * Method to take all the triples from graph created by reasoning over original graph and write them to json file in specific format
     */
    public static void writeToJson(String ontName, Map<String, String> map,
                                   String[] originalAxioms, String[] reasonedAxioms, String fileName)  {

        //creates instance of the class used in writing to Json file
        JsonFileContent jsonFileContent = new JsonFileContent(ontName,map,originalAxioms,reasonedAxioms);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

            // convert book object to JSON file
            mapper.writeValue(Paths.get(fileName).toFile(), jsonFileContent);


        } catch (Exception ex) {
            System.out.println("...Error in Method writeToJSON in OutputWriter class...");
        }
    }
}

