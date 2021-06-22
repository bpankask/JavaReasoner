package InferenceTree;

import org.apache.jena.graph.Triple;

import java.util.List;

public class FactNode implements TreeNode {

    private Triple fact;
    private int timeStep = 0;
    private List<Double> encoding;

    public FactNode(Triple fact){
        this.fact = fact;
    }

    @Override
    public Triple getValue() {
        return this.fact;
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
    public List<Double> getEncoding() {
        return encoding;
    }

    public void setEncoding(List<Double> encoding) {
        this.encoding = encoding;
    }

    public String toString(){
        return getValue().toString();
    }
}
