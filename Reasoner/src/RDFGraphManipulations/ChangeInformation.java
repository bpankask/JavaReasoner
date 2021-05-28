package RDFGraphManipulations;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.List;

public abstract class ChangeInformation {

    /**
     * Deletes unwanted comment statements (triples) from the KG.
     * @param model
     */
    public static void delCommmentTriples(OntModel model){

        StmtIterator iter = model.listStatements();
        //List of triples to be deleted.
        List<Statement> statementsToDel = new ArrayList<>();

        while(iter.hasNext()) {
            Statement stmt      = iter.nextStatement();// get next statement
            Property predicate = stmt.getPredicate(); // get the predicate

            //Adds triples with rdfs:comment to list for deletion.
            if(predicate.toString().contentEquals("http://www.w3.org/2000/01/rdf-schema#comment")){
                statementsToDel.add(stmt);
            }
        }//end of while loop

        //Deletes list of triples
        model.remove(statementsToDel);
    }

    /**
     * Creates and returns a list of OntModel from dividing larger model into smaller ones.
     * @param originalModel
     * @return
     */
    public static List<OntModel> createMiniKGs(OntModel originalModel, int desiredSize){

        //Iterator on all statements including ones from imported models.
        StmtIterator iter = originalModel.listStatements();

        List<List<Statement>> miniTripleSets = new ArrayList<>();
        miniTripleSets.add(new ArrayList<Statement>());

        int numTriples = 0;
        int numKGs = 0;

        while(iter.hasNext()) {
            Statement stmt = iter.nextStatement();// get next statement
            numTriples++;
            miniTripleSets.get(numKGs).add(stmt);
            if(numTriples == desiredSize){
                numTriples = 0;
                numKGs++;
                miniTripleSets.add(new ArrayList<Statement>());
            }
        }//end of while loop

        List<OntModel> miniKGs = new ArrayList<>(numKGs+1);
        for(List<Statement> tripleSet: miniTripleSets){
            if(tripleSet.size() > 0){
                OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
                model.setNsPrefixes(originalModel.getNsPrefixMap());
                model.add(tripleSet);
                miniKGs.add(model);
            }
        }

        return miniKGs;
    }
}
