package JsonHandler;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to provide necessary objects for Jackson library to convert to JSON in the OutputWriter class.
 * @author Brayden Pankaskie
 *
 */
public class JsonFileContent {

    private String OntologyName;
    private Map<String,String> Prefixes;
    private String[] OriginalAxioms;
    private String[] InferredAxioms;

    public JsonFileContent(String OntologyName, Map<String, String> prefixes, String[] OriginalAxioms, String[] ReasonedAxioms) {
        super();
        this.OntologyName = OntologyName;
        Prefixes = prefixes;
        this.OriginalAxioms = OriginalAxioms;
        this.InferredAxioms = ReasonedAxioms;
    }

    public JsonFileContent() {
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

}