package InferenceTree;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.RuleDerivation;

import java.util.ArrayList;
import java.util.List;

public class InferenceNode implements TreeNode {

    public TreeNode support1 = null;
    public TreeNode support2 = null;
    public List<Triple> matchList;

    private String ruleUsed;
    private Triple inference;
    private int timeStep;
    private List<Double> supportEncoding;
    private List<Double> encoding;

    public InferenceNode(RuleDerivation rd, Model model){
        this.inference = rd.getConclusion();
        this.ruleUsed = rd.toString();
        generalizedTripleMatchCheck(rd, model);
        this.matchList.add(null);
    }

    @Override
    public Triple getValue() {
        return this.inference;
    }

    @Override
    public int getTimeStep() {
        return timeStep;
    }

    @Override
    public void setTimeStep(int x) {
        this.timeStep = x;
    }

    @Override
    public List<Double> getEncoding(){return encoding;}

    public void setEncoding(List<Double> enc){
        encoding = enc;
    }

    public List<Double> getSupportEncoding() {
        return supportEncoding;
    }

    /**
     * Sets the supportEncoding for this node based on the encoding of its supports.
     */
    public void setSupportEncoding() {
        this.supportEncoding = new ArrayList<>();

        if(this.support1 instanceof FactNode){
            this.supportEncoding.addAll(this.support1.getEncoding());
        }
        else if(this.support1 instanceof InferenceNode){
            this.supportEncoding.addAll(((InferenceNode) this.support1).getSupportEncoding());
        }

        if(this.support2 != null){
            if(this.support2 instanceof FactNode){
                this.supportEncoding.addAll(this.support2.getEncoding());
            }
            else if(this.support2 instanceof InferenceNode){
                this.supportEncoding.addAll(((InferenceNode) this.support2).getSupportEncoding());
            }
        }
    }//end method

    public String toString(){
        return inference.toString();
    }

    /**
     * May need to implement later to make sure that it doesn't accidentally use a generalized triple for support.
     * @param rd
     * @param model
     */
    private void generalizedTripleMatchCheck(RuleDerivation rd, Model model){
        this.matchList = rd.getMatches();
    }
}
