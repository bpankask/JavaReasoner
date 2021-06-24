package JSONHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SerializeDeepReasonerDataTest {

    @Test
    public void serializeJson_CorrectJsonOutput() throws JsonProcessingException {
        List<ArrayList<Double>> kb = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> list1 = new ArrayList<Double>();
        list1.add(.1);
        list1.add(.2);
        list1.add(.3);
        ArrayList<Double> list2 = new ArrayList<Double>();
        list2.add(.7);
        list2.add(.8);
        list2.add(.9);
        kb.add(list1);
        kb.add(list2);

        List<Double[][]> supp = new ArrayList<Double[][]>();
        Double[][] supp1 = new Double[4][4];
        supp1[0][0] = .00;
        supp1[1][1] = .01;
        supp1[2][2] = .02;
        supp1[3][3] = .03;
        supp.add(supp1);

        List<Double[][]> outputs = new ArrayList<Double[][]>();
        Double[][] outputs1 = new Double[4][4];
        outputs1[0][0] = .00;
        outputs1[1][1] = .01;
        outputs1[2][2] = .02;
        outputs1[3][3] = .03;
        outputs.add(outputs1);

        List<Hashtable<Double, String>> vectorMap = new ArrayList<Hashtable<Double, String>>();
        Hashtable<Double, String> dict = new Hashtable<Double, String>();
        dict.put(.00, "rdf");
        dict.put(.01, "rdfs");
        dict.put(.02, "owl");
        vectorMap.add(dict);

        SerializeDeepReasonerData actual = new SerializeDeepReasonerData(kb, supp, outputs, vectorMap);
        String json = actual.serializeJson();

        SerializeDeepReasonerData expected = new ObjectMapper().readerFor(SerializeDeepReasonerData.class).readValue(json);

        assertEquals(expected.getkB(), actual.getkB());
        assertArrayEquals(expected.getOutputs().toArray(), actual.getOutputs().toArray());
        assertArrayEquals(expected.getSupports().toArray(), actual.getSupports().toArray());
        assertEquals(expected.getVectorMap(), actual.getVectorMap());
    }
}