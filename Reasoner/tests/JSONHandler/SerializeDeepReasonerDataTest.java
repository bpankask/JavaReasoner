package JSONHandler;

import JsonHandler.SerializeDeepReasonerData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class SerializeDeepReasonerDataTest {

    private SerializeDeepReasonerData serializeClass = null;

    @Before
    public void setUp(){
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

        List<ArrayList[]> supp = new ArrayList();
        ArrayList[] suppArr1 = new ArrayList[2];
        suppArr1[0] = new ArrayList(Arrays.asList(.01, .02));
        suppArr1[1] = new ArrayList(Arrays.asList(.11, .12));
        ArrayList[] suppArr2 = new ArrayList[2];
        suppArr2[0] = new ArrayList(Arrays.asList(.11, .12));
        suppArr2[1] = new ArrayList(Arrays.asList(.21, .22));
        supp.add(suppArr1);
        supp.add(suppArr2);

        List<ArrayList[]> outs = new ArrayList();
        ArrayList[] outArr1 = new ArrayList[2];
        outArr1[0] = new ArrayList(Arrays.asList(.01, .02));
        outArr1[1] = new ArrayList(Arrays.asList(.11, .12));
        ArrayList[] outArr2 = new ArrayList[2];
        outArr2[0] = new ArrayList(Arrays.asList(.11, .12));
        outArr2[1] = new ArrayList(Arrays.asList(.21, .22));
        outs.add(suppArr1);
        outs.add(suppArr2);

        this.serializeClass = new SerializeDeepReasonerData(kb, supp, outs, 10, 5);
    }

    @Test
    public void serializeToJson_CorrectJsonConversion() throws JsonProcessingException {

        String json = this.serializeClass.serializeToJson();

        SerializeDeepReasonerData expected = new ObjectMapper().readerFor(SerializeDeepReasonerData.class).readValue(json);

        assertEquals(expected.getkB(), this.serializeClass.getkB());
        assertArrayEquals(expected.getOutputs().toArray(), this.serializeClass.getOutputs().toArray());
        assertArrayEquals(expected.getSupports().toArray(), this.serializeClass.getSupports().toArray());
    }

    @Test
    public void writeJson_CorrectReadingAndWritingToFile() throws IOException {
        this.serializeClass.writeJson("C:\\Users\\Brayden Pankaskie\\Desktop\\JavaReasoner\\Reasoner\\tests\\jsonWriterTest.json");

        File jsonFile = new File("C:\\Users\\Brayden Pankaskie\\Desktop\\JavaReasoner\\Reasoner\\tests\\jsonWriterTest.json");

        SerializeDeepReasonerData actual = new ObjectMapper().readerFor(SerializeDeepReasonerData.class).readValue(jsonFile);
        assertEquals(this.serializeClass.serializeToJson(), actual.serializeToJson());
    }
}