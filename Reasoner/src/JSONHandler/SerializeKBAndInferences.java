package JSONHandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.file.Paths;
import java.util.Map;

public class SerializeKBAndInferences implements JsonSerializer{
    private String OntologyName;
    private Map<String,String> Prefixes;
    private String[] OriginalAxioms;
    private String[] InferredAxioms;

    public SerializeKBAndInferences(String OntologyName, Map<String, String> prefixes, String[] OriginalAxioms, String[] ReasonedAxioms) {
        super();
        this.OntologyName = OntologyName;
        Prefixes = prefixes;
        this.OriginalAxioms = OriginalAxioms;
        this.InferredAxioms = ReasonedAxioms;
    }

    public SerializeKBAndInferences() {
        super();
    }

    @JsonProperty("OntologyName")
    public String getOntologyName() {
        return OntologyName;
    }
    public void setOntologyName(String ontologyName) {
        OntologyName = ontologyName;
    }

    @JsonProperty("Prefixes")
    public Map<String, String> getPrefixes() {
        return Prefixes;
    }
    public void setPrefixes(Map<String, String> prefixes) {
        Prefixes = prefixes;
    }

    @JsonProperty("OriginalAxioms")
    public String[] getOriginalAxioms() {
        return OriginalAxioms;
    }
    public void setOriginalAxioms(String[] OriginalAxioms) {
        this.OriginalAxioms = OriginalAxioms;
    }

    @JsonProperty("InferredAxioms")
    public String[] getReasonedAxioms() {
        return InferredAxioms;
    }
    public void setReasonedAxioms(String[] ReasonedAxioms) {
        this.InferredAxioms = ReasonedAxioms;
    }

    /**
     * Take all the triples from graph created by reasoning over original graph and converts them to a json format.
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

    }
}
