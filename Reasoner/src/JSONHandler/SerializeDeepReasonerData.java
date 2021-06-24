package JSONHandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class SerializeDeepReasonerData implements JsonSerializer{
    private List<ArrayList<Double>> kB;
    private List<Double[][]> supports;
    private List<Double[][]> outputs;
    private List<Hashtable<Double, String>> vectorMap;

    public SerializeDeepReasonerData(List<ArrayList<Double>> kB, List<Double[][]> supports, List<Double[][]> outputs, List<Hashtable<Double, String>> vectorMap) {
        this.kB = kB;
        this.supports = supports;
        this.outputs = outputs;
        this.vectorMap = vectorMap;
    }

    public SerializeDeepReasonerData(){
        super();
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
    public List<Double[][]> getSupports() {
        return supports;
    }
    @JsonSetter("supports")
    public void setSupports(List<Double[][]> supports) {
        this.supports = supports;
    }

    @JsonProperty("outputs")
    public List<Double[][]> getOutputs() {
        return outputs;
    }
    @JsonSetter("outputs")
    public void setOutputs(List<Double[][]> outputs) {
        this.outputs = outputs;
    }

    @JsonProperty("vectorMap")
    public List<Hashtable<Double, String>> getVectorMap() {
        return vectorMap;
    }
    @JsonSetter("vectorMap")
    public void setVectorMap(List<Hashtable<Double, String>> vectorMap) {
        this.vectorMap = vectorMap;
    }

    /**
     * Takes all the necessary deep reasoner data and converts to json.
     * @return
     */
    public String serializeJson(){
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
     * @param json
     */
    public void writeJson(String fileName, String json)  {

    }
}
