package JSONHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonParser {

    //private backing variables
    private String ontologyName;
    private String[] subjects;
    private String[] predicates;
    private String[] objects;
    private Map<String, String> prefixMap;

    private File file;

    public String getOntologyName() {
        return ontologyName;
    }

    public String[] getSubjects() {
        return subjects;
    }

    public String[] getPredicates() {
        return predicates;
    }

    public String[] getObjects() {
        return objects;
    }

    public Map<String, String> getPrefixMap() {
        return prefixMap;
    }

    public File getFile() {
        return file;
    }

    /**
     * Public constructor for JsonParser
     * @param file
     * @throws IOException
     */
    public JsonParser(File file) throws IOException {
        this.file = file;
        parseJsonAndPopulate(file);
    }

    /**
     * Parses json file of specific format into data stored in JsonParser class
     * @param file
     * @throws IOException
     */
    public void parseJsonAndPopulate(File file) throws IOException {

        List<String> sub = new ArrayList<>();
        List<String> pred = new ArrayList<>();
        List<String> obj = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        //gets original axioms
        JsonNode arrNode = new ObjectMapper().readTree(file).get("OriginalAxioms");

        for(JsonNode objNode: arrNode){
            String triple= objNode.asText();

            triple = removeEscapeCharacters(triple);

            //splits triple into subject predicate object
            String[] spo = triple.split(" ", 3);
            sub.add(spo[0]);
            pred.add(spo[1]);
            obj.add(spo[2]);
        }

        subjects = sub.toArray(new String[sub.size()]);
        predicates = pred.toArray(new String[sub.size()]);
        objects = obj.toArray(new String[sub.size()]);

        //gets map of prefixes
        JsonNode mapNode = mapper.readTree(file).get("Prefixes");
        Map<String, String> map = mapper.convertValue(mapNode, new TypeReference<Map<String, String>>(){});
        prefixMap = map;

        //gets ontology name
        JsonNode nameNode = mapper.readTree(file).get("OntologyName");
        String ontName = nameNode.toString();
        ontologyName = ontName.substring(1,ontName.length()-1);

    }

    private String removeEscapeCharacters(String str){

        str = str.replace("\\\"", "\"").replace("\n", "").replace("\t","");

        return str;
    }

}
