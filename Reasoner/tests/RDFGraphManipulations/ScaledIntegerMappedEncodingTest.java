package RDFGraphManipulations;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;

public class ScaledIntegerMappedEncodingTest {

    private Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM).read("conceptRoleInfoListCheck.ttl");

    @Test
    public void getConceptRoleInfo_ReturnsCorrectListCount() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read("conceptRoleCountTest.ttl");

        Method method = ScaledIntegerMappedEncoding.class.getDeclaredMethod("getConceptRoleInfo");
        method.setAccessible(true);

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);
        method.invoke(sie);
        List<RDFNode>[] info = sie.conceptRoleInfoArray;

        assertEquals(17, info[0].size());
        assertEquals(4, info[1].size());
    }

    @Test
    public void getConceptRoleInfo_ReturnsCorrectConceptValues() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, FileNotFoundException {

        Method method = ScaledIntegerMappedEncoding.class.getDeclaredMethod("getConceptRoleInfo");
        method.setAccessible(true);

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);
        method.invoke(sie);
        List<RDFNode>[] info = sie.conceptRoleInfoArray;

        List<RDFNode> expected = new ArrayList<>();
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#TransitiveProperty"));
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#equivalentClass"));
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#cardinality"));
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#ObjectProperty"));
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#Class"));
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#equivalentClass"));
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#Restriction"));
        expected.add(model.getResource("http://www.w3.org/2002/07/owl#FunctionalProperty"));

        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#goal_of"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Relator"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Amount_of_substrate"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Item"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Set"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Configuration"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Abstract"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#has_left_time_boundary"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#spatial_boundary_of"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#function_determinant_of"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#has_goal"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Entity"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Individual"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Property"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Concrete"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Presential"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Continuous"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Mass_entity"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Category"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#instantiated_by"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Material_object"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Material_boundary"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Space_time"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#has_boundary"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#has_time_boundary"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#left_boundary_of"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Spatial_boundary"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#Space"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#depends_on"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#boundary_of"));
        expected.add(model.getResource("http://www.onto-med.de/ontologies/gfo.owl#has_spatial_boundary"));

        expected.add(model.getResource("http://www.w3.org/2000/01/rdf-schema#Resource"));
        expected.add(model.getResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"));
        expected.add(model.getResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"));
        expected.add(model.getResource("http://www.w3.org/2001/XMLSchema#string"));

        for(RDFNode node : expected){
            info[0].remove(node);
        }

        int numLiterals = 0;
        int numBlank = 0;

        for(RDFNode node : info[0]){
            if(node.isAnon())
                numBlank++;
            if(node.isLiteral())
                numLiterals++;
        }

        assertEquals(13, numBlank);
        assertEquals(5, numLiterals);
    }

    //Do above for roles

    @Test
    public void createEncodingMap_EncodedMapMatchesConcepts() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getInfo = ScaledIntegerMappedEncoding.class.getDeclaredMethod("getConceptRoleInfo");
        getInfo.setAccessible(true);

        Method createEncoding = ScaledIntegerMappedEncoding.class.getDeclaredMethod("createEncodingMap");
        createEncoding.setAccessible(true);

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);

        getInfo.invoke(sie);
        List<RDFNode>[] info = sie.conceptRoleInfoArray;

        List<String> concepts = new ArrayList<>();
        for(RDFNode node : info[0]){
            concepts.add(node.toString());
        }

        HashMap<Double, String> map = (HashMap<Double, String>) createEncoding.invoke(sie);

        for(Map.Entry<Double, String> entry : map.entrySet()){
            concepts.remove(entry.getValue());
        }

        assertTrue(concepts.isEmpty());
    }

    @Test
    public void createEncodingMap_EncodedMapMatchesRoles() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getInfo = ScaledIntegerMappedEncoding.class.getDeclaredMethod("getConceptRoleInfo");
        getInfo.setAccessible(true);

        Method createEncoding = ScaledIntegerMappedEncoding.class.getDeclaredMethod("createEncodingMap");
        createEncoding.setAccessible(true);

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);

        getInfo.invoke(sie);
        List<RDFNode>[] info = sie.conceptRoleInfoArray;

        List<String> roles = new ArrayList<>();
        for(RDFNode node : info[1]){
            roles.add(node.toString());
        }

        HashMap<Double, String> map = (HashMap<Double, String>) createEncoding.invoke(sie);

        for(Map.Entry<Double, String> entry : map.entrySet()){
            roles.remove(entry.getValue());
        }

        assertTrue(roles.isEmpty());
    }

    @Test
    public void createEncodingMap_KeysAreAppropriatelyPositiveAndNegative() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getInfo = ScaledIntegerMappedEncoding.class.getDeclaredMethod("getConceptRoleInfo");
        getInfo.setAccessible(true);

        Method createEncoding = ScaledIntegerMappedEncoding.class.getDeclaredMethod("createEncodingMap");
        createEncoding.setAccessible(true);

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);

        getInfo.invoke(sie);
        List<RDFNode>[] info = sie.conceptRoleInfoArray;

        List<String> concepts = new ArrayList<>();
        for(RDFNode node : info[0]){
            concepts.add(node.toString());
        }

        List<String> roles = new ArrayList<>();
        for(RDFNode node : info[1]){
            roles.add(node.toString());
        }

        HashMap<Double, String> map = (HashMap<Double, String>) createEncoding.invoke(sie);

        for(Map.Entry<Double, String> entry : map.entrySet()){
            if(entry.getKey() < 0){
                roles.remove(entry.getValue());
            }
            else{
                concepts.remove(entry.getValue());
            }
        }

        assertTrue(roles.isEmpty());
        assertTrue(concepts.isEmpty());
    }

    @Test
    public void createEncodingMap_AllKeysAreBetweenNegOneAndPositiveOne() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getInfo = ScaledIntegerMappedEncoding.class.getDeclaredMethod("getConceptRoleInfo");
        getInfo.setAccessible(true);

        Method createEncoding = ScaledIntegerMappedEncoding.class.getDeclaredMethod("createEncodingMap");
        createEncoding.setAccessible(true);

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);

        getInfo.invoke(sie);

        HashMap<Double, String> map = (HashMap<Double, String>) createEncoding.invoke(sie);
        //map.put(12.0, "Hello");

        for(Map.Entry<Double, String> entry : map.entrySet()){
            if(entry.getKey() < -1 || entry.getKey() > 1){
                assertTrue(false);
            }
        }
    }

    @Test
    public void labelMap_ConceptsAreMappedToPosativeInt(){
        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);
        sie.getEncodedMapAndPopLabelMap();

        HashMap<Integer, String> labelMap = sie.labelMap;

        // Creating a list of concept strings to remove from.
        List<RDFNode> concepts = sie.conceptRoleInfoArray[0];
        List<String> conceptsStrings = new ArrayList<>();
        for(RDFNode node : concepts){
            conceptsStrings.add(node.toString());
        }

        for(Map.Entry<Integer, String> entry : labelMap.entrySet()){
            if(entry.getKey() > 0){
                conceptsStrings.remove(entry.getValue());
            }
        }

        assertTrue(conceptsStrings.isEmpty());
    }

    @Test
    public void labelMap_RolesAreMappedToNegativeInt(){
        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);
        sie.getEncodedMapAndPopLabelMap();

        HashMap<Integer, String> labelMap = sie.labelMap;

        // Creating a list of concept strings to remove from.
        List<RDFNode> roles = sie.conceptRoleInfoArray[1];
        List<String> roleStrings = new ArrayList<>();
        for(RDFNode node : roles){
            roleStrings.add(node.toString());
        }

        for(Map.Entry<Integer, String> entry : labelMap.entrySet()){
            if(entry.getKey() < 0){
                roleStrings.remove(entry.getValue());
            }
        }

        assertTrue(roleStrings.isEmpty());
    }

    @Test
    public void labelMap_AllRolesAndConceptsAreInMap(){
        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);
        sie.getEncodedMapAndPopLabelMap();

        HashMap<Integer, String> labelMap = sie.labelMap;
        List<RDFNode> concepts = sie.conceptRoleInfoArray[0];
        List<RDFNode> roles = sie.conceptRoleInfoArray[1];

        int numConceptsInMap = 0;
        int numRolesInMap = 0;

        for(Map.Entry<Integer, String> entry : labelMap.entrySet()){
            if(entry.getKey() > 0)
                numConceptsInMap++;
            else
                numRolesInMap++;
        }

        assertEquals(concepts.size() + roles.size(), labelMap.size());
        assertEquals(concepts.size(), numConceptsInMap);
        assertEquals(roles.size(), numRolesInMap);
    }

    @Test
    public void labelMapAndEncodedMapAreSameSize(){
        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);

        HashMap<Double, String> encodedMap = sie.getEncodedMapAndPopLabelMap();
        HashMap<Integer, String> labelMap = sie.labelMap;

        assertEquals(labelMap.size(), encodedMap.size());
    }

    @Test
    public void canGoBackAndForthBetweenLabelMapAndEncodedMap(){
        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(model);

        HashMap<Double, String> encodedMap = sie.getEncodedMapAndPopLabelMap();
        HashMap<Integer, String> labelMap = sie.labelMap;
        int numConcepts = sie.conceptRoleInfoArray[0].size();
        int numRoles = sie.conceptRoleInfoArray[1].size();

        for(Map.Entry<Double, String> entry : encodedMap.entrySet()){
            if(entry.getKey() > 0){
                int key = (int) (entry.getKey() * numConcepts);
                assertEquals(labelMap.get(key), entry.getValue());
            }
            else{
                int key = (int) (entry.getKey() * numRoles);
                assertEquals(labelMap.get(key), entry.getValue());
            }
        }
    }
}