package RDFGraphManipulations;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ScaledIntegerMappedEncoding implements MapEncoding {

    private Model model;
    public ArrayList<RDFNode>[] conceptRoleInfoArray = new ArrayList[2];
    public HashMap<Integer, String> labelMap;

    public ScaledIntegerMappedEncoding(Model model){
        this.model = model;
        getConceptRoleInfo();
    }

    private void getConceptRoleInfo(){
        StmtIterator si = this.model.listStatements();
        ArrayList<RDFNode> concepts = new ArrayList<>();
        ArrayList<RDFNode> roles = new ArrayList<>();

        while(si.hasNext()){
            Statement s = si.nextStatement();

            if(!concepts.contains(s.getObject())){
                concepts.add(s.getObject());
            }
            if(!concepts.contains(s.getSubject())){
                concepts.add(s.getSubject());
            }
            if(!roles.contains(s.getPredicate())){
                roles.add((s.getPredicate()));
            }
        }
        conceptRoleInfoArray[0] = concepts;
        conceptRoleInfoArray[1] = roles;
    }

    private HashMap<Double, String> createEncodingMap(){
        List<RDFNode> concepts = conceptRoleInfoArray[0];
        List<RDFNode> roles = conceptRoleInfoArray[1];

        int numConcepts = concepts.size();
        int numRoles = roles.size();

        //Map to hold encoding as key and string representing role/concept as string.
        HashMap<Double, String> map = new HashMap<>();
        HashMap<Integer, String> labelMap = new HashMap<>();

        Random random = new Random();

        int key = 1;
        //Map concepts.
        for(RDFNode concept : concepts){

            while(labelMap.containsKey(key)){
                key =  random.nextInt(numConcepts) + 1;
            }

            map.put((double)key/numConcepts, concept.toString());
            labelMap.put(key, concept.toString());
        }

        key = -1;
        //Map roles
        for(RDFNode role : roles){
            while(labelMap.containsKey(key)){
                key = -1 * (random.nextInt(numRoles) + 1);
            }

            map.put((double)key/numRoles, role.toString());
            labelMap.put(key, role.toString());
        }

        this.labelMap = labelMap;
        return map;
    }

    @Override
    public HashMap<Double, String> getEncodedMapAndPopLabelMap() {
        return createEncodingMap();
    }

}
