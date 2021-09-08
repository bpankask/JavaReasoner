package JsonHandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.file.Paths;
import java.util.*;

/**
 * Class holding all relevent data to train a deep reasoner.  Uses Jackson Library to serialize this classes fields into json.
 */
public class SerializeDeepReasonerData implements JsonSerializer{
    private List<ArrayList<Double>> kB;
    private List<ArrayList[]> supports;
    private List<ArrayList[]> outputs;
    private int concepts = 0;
    private int roles = 0;

    public SerializeDeepReasonerData(List<ArrayList<Double>> kB, List<ArrayList[]> supports, List<ArrayList[]> outputs,
                                     int concepts, int roles) {
        this.kB = kB;
        this.supports = supports;
        this.outputs = outputs;
        this.concepts = concepts;
        this.roles = roles;
    }

    public SerializeDeepReasonerData(){
    }

    @JsonProperty("kB")
    public List<ArrayList<Double>> getkB() {
        return kB;
    }
    @JsonSetter("kB")
    public void setkB(List<ArrayList<Double>> kB) {
        this.kB = kB;
    }

    @JsonProperty("supports")
    public List<ArrayList[]> getSupports() {
        return supports;
    }
    @JsonSetter("supports")
    public void setSupports(List<ArrayList[]> supports) {
        this.supports = supports;
    }

    @JsonProperty("outputs")
    public List<ArrayList[]> getOutputs() {
        return outputs;
    }
    @JsonSetter("outputs")
    public void setOutputs(List<ArrayList[]> outputs) {
        this.outputs = outputs;
    }

    @JsonProperty("concepts")
    public int getConcepts() {
        return this.concepts;
    }
    @JsonSetter("concepts")
    public void setConcepts(int concepts) {
        this.concepts = concepts;
    }

    @JsonProperty("roles")
    public int getRoles() {
        return this.roles;
    }
    @JsonSetter("roles")
    public void setRoles(int roles) {
        this.roles = roles;
    }

    /**
     * Takes all the necessary deep reasoner data and converts to json.
     * @return
     */
    public String serializeToJson(){
        String value = "";

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

            value = writer.writeValueAsString(this);

        } catch (Exception ex) {
            System.out.println("...Error serializing Deep Reasoner Data...");
        }

        return value;
    }

    /**
     * Writes this classes data to a json file.
     * @param fileName
     */
    public void writeJson(String fileName)  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

            // convert book object to JSON file
            writer.writeValue(Paths.get(fileName).toFile(), this);


        } catch (Exception ex) {
            System.out.println("...Error in Method writeToJSON in OutputWriter class...");
        }
    }
}
