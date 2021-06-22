package InferenceTree;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class InferenceNodeTest {

    @Test
    public void testIfInferenceNodeIsCreatedCorrectly_GetValueIsCorrect(){
        Model model = ModelFactory.createDefaultModel();
        Triple t1 = model.createStatement(model.createResource("R1"), model.createProperty("P1"), model.createResource("R1")).asTriple();
        Triple t2 = model.createStatement(model.createResource("R2"), model.createProperty("P2"), model.createResource("R2")).asTriple();
        Triple t3 = model.createStatement(model.createResource("R3"), model.createProperty("P3"), model.createResource("R3")).asTriple();

        InferenceNode in = new InferenceNode(new RuleDerivation(null, t1, new ArrayList<Triple>() {{
            add(t2);
            add(t3);
        }}, null), model);

        assertEquals(t1.toString(), in.getValue().toString());
    }

    @Test
    public void testIfInferenceNodeIsCreatedCorrectly_SupportsAreCorrect(){
        Model model = ModelFactory.createDefaultModel();
        Triple t1 = model.createStatement(model.createResource("R1"), model.createProperty("P1"), model.createResource("R1")).asTriple();
        Triple t2 = model.createStatement(model.createResource("R2"), model.createProperty("P2"), model.createResource("R2")).asTriple();
        Triple t3 = model.createStatement(model.createResource("R3"), model.createProperty("P3"), model.createResource("R3")).asTriple();

        InferenceNode in = new InferenceNode(new RuleDerivation(null, t1, new ArrayList<Triple>() {{
            add(t2);
            add(t3);
        }}, null), model);

        assertEquals(t2.toString(), in.matchList.get(0).toString());
        assertEquals(t3.toString(), in.matchList.get(1).toString());
    }


}