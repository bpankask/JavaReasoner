package Reasoner;

import InferenceTree.InferenceNode;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ReasonerLogicTest {

    @Test
    public void runTimeStepReasoner_NoDerivationsFromPreprocessing() {
        String filePath = "C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\TestFiles\\gfo-1.0.owl";
        String rulePath = "src\\RulesTimeStep.txt";
        String tracePath = "OutputFiles\\traceTest";
        String preProcessingPath = "src\\PreprocessingTimeStep.txt";

        InfModel[] arr = ReasonerLogic.runTimeStepReasoner(filePath, rulePath, preProcessingPath, tracePath);

        List<String> postRuleDer = new ArrayList<>();



        for (StmtIterator i = arr[1].listStatements(); i.hasNext(); ) {

            Statement s = i.nextStatement();

            //Gets all the Derivations from a given statement/triple.
            for (Iterator id = arr[1].getDerivation(s); id.hasNext(); ) {

                //Creates node from derivation/inference.
                RuleDerivation deriv = (RuleDerivation) id.next();
                if(!postRuleDer.contains(deriv.getRule().toString()))
                    postRuleDer.add(deriv.getRule().toString());
            }//end for
        }

        assertFalse(postRuleDer.contains("[rdfs_container: (?u ?a ?x), propertyCheck(?a) -> (?a rdf:type rdf:Property), (?a rdf:type rdfs:ContainerMembershipProperty)]"));
        assertFalse(postRuleDer.contains("[rdf_container: (?u ?a ?x), propertyCheck(?a) -> (?a rdf:type rdf:Property)]"));
    }

    @Test
    public void runTimeStepReasoner_NoDerivationsExceptFromContainerRules(){
        String filePath = "C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\TestFiles\\gfo-1.0.owl";
        String rulePath = "src\\RulesTimeStep.txt";
        String tracePath = "OutputFiles\\traceTest";
        String preProcessingPath = "src\\PreprocessingTimeStep.txt";

        InfModel[] arr = ReasonerLogic.runTimeStepReasoner(filePath, rulePath, preProcessingPath, tracePath);

        List<String> preRuleDer = new ArrayList<>();

        for (StmtIterator i = arr[0].listStatements(); i.hasNext(); ) {

            Statement s = i.nextStatement();

            //Gets all the Derivations from a given statement/triple.
            for (Iterator id = arr[0].getDerivation(s); id.hasNext(); ) {

                //Creates node from derivation/inference.
                RuleDerivation deriv = (RuleDerivation) id.next();
                if(!preRuleDer.contains(deriv.getRule().toString()))
                    preRuleDer.add(deriv.getRule().toString());
            }//end for
        }
        assertTrue(preRuleDer.contains("[rdfs_container: (?u ?a ?x), propertyCheck(?a) -> (?a rdf:type rdf:Property), (?a rdf:type rdfs:ContainerMembershipProperty)]")
        || preRuleDer.contains("[rdf_container: (?u ?a ?x), propertyCheck(?a) -> (?a rdf:type rdf:Property)]") || preRuleDer.isEmpty());
    }
}