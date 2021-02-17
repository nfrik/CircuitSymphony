package org.circuitsymphony.rest.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ufl.cise.klu.common.KLU_common;
import edu.ufl.cise.klu.common.KLU_symbolic;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import org.circuitsymphony.manager.CircuitManager;
import org.circuitsymphony.rest.App;
import org.circuitsymphony.rest.controllers.SymphonyController;
import org.circuitsymphony.rest.exceptions.SymphonyRestServiceException;
import org.circuitsymphony.rest.models.dto.SimulationDTO;
import org.circuitsymphony.rest.services.SimulationService;
import org.circuitsymphony.util.MathUtil;
import org.circuitsymphony.util.NicsluSolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Test suite for {@link SymphonyController} API endpoints.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@WebAppConfiguration
public class SymphonyControllerTest {
    private static final String TEST_CIRCUIT = "lrc.txt";
    private static final String TEST_GRAPH_CIRCUIT = "test_graph.json";
    private static final String TEST_GRAPH_CIRCUIT_PART_A = "test_graph_a.json";
    private static final String TEST_GRAPH_CIRCUIT_PART_B = "test_graph_b.json";

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webAppCtx;
    @Autowired
    private SimulationService simulationService;
    @Autowired
    private SymphonyController symphonyController;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(webAppCtx).build();
    }

    @Test
    public void testSimulationLifecycle() throws Exception {
        mockMvc.perform(post("/symphony/simulations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", notNullValue()))
                .andDo(result -> {
                    String key = objectMapper.readValue(result.getResponse().getContentAsString(), SimulationDTO.class).getKey();

                    mockMvc.perform(post("/symphony/simulations/{key}/kill", key))
                            .andExpect(status().isOk());

                    exception.expect(SymphonyRestServiceException.class);
                    simulationService.getSimIfExist(key);
                });
    }

    @Test
    public void testGetSimulationStates() throws Exception {
        mockMvc.perform(post("/symphony/simulations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", notNullValue()))
                .andDo(result -> {
                    String key = objectMapper.readValue(result.getResponse().getContentAsString(), SimulationDTO.class).getKey();
                    mockMvc.perform(get("/symphony/simulations"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$[0].dataSubmitted", notNullValue()))
                            .andExpect(jsonPath("$[0].status", is("paused")));
                    killSimulation(key);
                });
    }

    @Test
    public void testLoadInternalCircuit() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/loadInternalCircuit", key).param("filename", TEST_CIRCUIT))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadInternalCircuit: " + TEST_CIRCUIT)));
        killSimulation(key);
    }

    @Test(expected = Exception.class)
    public void testLoadMissingInternalCircuit() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/loadInternalCircuit", key)
                .param("filename", "not_existing_cmf_file.test"));
        killSimulation(key);
    }

    @Test
    public void testLoadCircuit() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuit", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuit")));
        killSimulation(key);
    }

    @Test
    public void testLoadCircuitFromGraph() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        killSimulation(key);
    }

    @Test
    public void testAddGraphElementsFromFile() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT_PART_A))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/addGraphElementsFromFile", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT_PART_B))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("addGraphElementsFromFile")));
        killSimulation(key);
    }

    @Test
    public void testAddGraphElements() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT_PART_A))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        String json = new String(StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT_PART_B)), "UTF-8");
        mockMvc.perform(post("/symphony/simulations/{key}/addGraphElements", key).content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("addGraphElements")));
        killSimulation(key);
    }

    @Test
    public void testDeleteGraphElements() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT_PART_A))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/addGraphElementsFromFile", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT_PART_B))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("addGraphElementsFromFile")));
        mockMvc.perform(post("/symphony/simulations/{key}/deleteGraphElement", key)
                .param("elementId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("deleteGraphElement")));
        killSimulation(key);
    }

    @Test
    public void testSimulationResume() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/loadInternalCircuit", key).param("filename", TEST_CIRCUIT))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadInternalCircuit")));
        mockMvc.perform(post("/symphony/simulations/{key}/start", key))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("start")));
        mockMvc.perform(post("/symphony/simulations/{key}/stop", key))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("stop")));
        mockMvc.perform(post("/symphony/simulations/{key}/start", key))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("start")));
        killSimulation(key);
    }

    @Test
    public void testSimulationStartFor() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/loadInternalCircuit", key).param("filename", TEST_CIRCUIT))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadInternalCircuit")));
        mockMvc.perform(post("/symphony/simulations/{key}/startFor", key)
                .param("seconds", "1.0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("startFor: 1")));
        mockMvc.perform(post("/symphony/simulations/{key}/time", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time", not(0.0)));
        killSimulation(key);
    }

    @Test
    public void testSimulationStartForAndWait() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/loadInternalCircuit", key)
                .param("filename", TEST_CIRCUIT))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadInternalCircuit")));
        mockMvc.perform(post("/symphony/simulations/{key}/startForAndWait", key)
                .param("seconds", "1.0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("startForAndWait: 1")));
        mockMvc.perform(post("/symphony/simulations/{key}/time", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time", greaterThan(1.0)));
        killSimulation(key);
    }

    @Test
    public void testRetrieveMeasurements() throws Exception {
        String key = setupSimulation();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("peekInterval", "0.001");
        params.add("pokeInterval", "0.001");
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(patch("/symphony/simulations/{key}/settings", key)
//                .param("peekInterval", "0.001"))
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setPeekInterval")));
        mockMvc.perform(post("/symphony/simulations/{key}/setMeasurableElements", key).content("1"))
//                .param("json", "[1,2,3,4,5,6,7,8,9]"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setMeasurableElements")));
        mockMvc.perform(post("/symphony/simulations/{key}/startForAndWait", key)
                .param("seconds", "0.5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("startForAndWait")));
        mockMvc.perform(get("/symphony/simulations/{key}/measurements", key))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0.05")));
        killSimulation(key);
    }

    @Test
    public void testSetPeekInterval() throws Exception {
        String key = setupSimulation();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("peekInterval", "0.001");
        params.add("pokeInterval", "0.001");
        mockMvc.perform(patch("/symphony/simulations/{key}/settings", key)
//                .param("peekInterval", "0.001"))
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setPeekInterval: 0.001")));
        killSimulation(key);
    }

    @Test
    public void testGetGraphAsCmf() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(get("/symphony/simulations/{key}/graphAsCmf", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cmf", notNullValue()))
                .andExpect(jsonPath("$.cmf", containsString("10.20027730826997 50 5.0 50")));
        killSimulation(key);
    }

    @Test
    public void testGetGraph() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(get("/symphony/simulations/{key}/currentCircuitAsJSONGraph", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.graph", notNullValue()))
                .andExpect(content().string(containsString("\"graph\":{\"0\":[\"$\",\"1\",\"5.0E-6\"")));
        killSimulation(key);
    }

    @Test
    public void testGetElements() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(get("/symphony/simulations/{key}/graphElements", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elements", notNullValue()))
                .andExpect(content().string(containsString("\"elements\":{\"1\":{\"x\"")));
        killSimulation(key);
    }

    @Test
    public void testSimulationGetTime() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/time", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time", equalTo(0.0)));
        killSimulation(key);
    }

    @Test
    public void testSimulationGetTimeAfterStart() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/loadInternalCircuit", key).param("filename", TEST_CIRCUIT))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadInternalCircuit")));
        mockMvc.perform(post("/symphony/simulations/{key}/start", key))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("start")));
        mockMvc.perform(post("/symphony/simulations/{key}/time", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time", not(0.0)));
        killSimulation(key);
    }

    @Test
    public void testSimulationPeekTime() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(post("/symphony/simulations/{key}/peekTime", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time", equalTo(-1.0)));
        killSimulation(key);
    }

    @Test
    public void testGetGraphElements() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(get("/symphony/simulations/{key}/elements", key, 1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"elementId\":0,\"type\":\"WireElm\",\"current\":0.0")));
        killSimulation(key);
    }

    @Test
    public void testGetPropertiesList() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(post("/symphony/simulations/{key}/element/{elementId}", key, 1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"properties\":[\"resistance\"]")));
        killSimulation(key);
    }

    @Test
    public void testGetProperty() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(post("/symphony/simulations/{key}/element/{elementId}/property", key, 1)
                .param("propertyKey", "resistance"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"propertyKey\":\"resistance\",\"propertyValue\":")));
        killSimulation(key);
    }

    @Test
    public void testSetProperty() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(patch("/symphony/simulations/{key}/element/{elementId}/property", key, 1)
                .param("propertyKey", "beta")
                .param("newValue", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"propertyKey\":\"beta\",\"propertyValue\":123")));
        killSimulation(key);
    }

    @Test
    public void testGetVoltageDiff() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(post("/symphony/simulations/{key}/element/{elementId}/voltageDiff", key, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(0.0)));
        killSimulation(key);
    }

    @Test
    public void testGetCurrent() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(post("/symphony/simulations/{key}/element/{elementId}/current", key, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(0.0)));
        killSimulation(key);
    }

    @Test
    public void testPeekVoltageDiff() throws Exception {
        String key = setupSimulation();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("peekInterval", "0.001");
        params.add("pokeInterval", "0.001");
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(patch("/symphony/simulations/{key}/settings", key)
//                .param("peekInterval", "0.001"))
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setPeekInterval")));
        mockMvc.perform(post("/symphony/simulations/{key}/setMeasurableElements", key).content("1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setMeasurableElements")));
        mockMvc.perform(post("/symphony/simulations/{key}/startForAndWait", key)
                .param("seconds", "0.5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("startForAndWait")));
        mockMvc.perform(post("/symphony/simulations/{key}/element/{elementId}/peekVoltageDiff", key, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", not(0.0)));
        killSimulation(key);
    }

    @Test
    public void testPeekCurrent() throws Exception {
        String key = setupSimulation();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("peekInterval", "0.001");
        params.add("pokeInterval", "0.001");
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(patch("/symphony/simulations/{key}/settings", key)
//                .param("peekInterval", "0.001"))
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setPeekInterval")));
        mockMvc.perform(post("/symphony/simulations/{key}/setMeasurableElements", key).content("1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setMeasurableElements")));
        mockMvc.perform(post("/symphony/simulations/{key}/startForAndWait", key)
                .param("seconds", "0.5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("startForAndWait")));
        mockMvc.perform(post("/symphony/simulations/{key}/element/{elementId}/peekCurrent", key, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", not(0.0)));
        killSimulation(key);
    }

    @Test
    public void testSetMeasurableElements() throws Exception {
        String key = setupSimulation();
        mockMvc.perform(fileUpload("/symphony/simulations/{key}/loadCircuitFromGraph", key)
                .file("file", StreamUtils.copyToByteArray(getClass().getResourceAsStream("/" + TEST_GRAPH_CIRCUIT))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadCircuitFromGraph")));
        mockMvc.perform(post("/symphony/simulations/{key}/setMeasurableElements", key).content("1"))
//                .param("json", "[1,2,3,4,5,6,7,8,9]"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setMeasurableElements")));
        killSimulation(key);
    }

    @Test
    public void kluLinearSolveTest(){
//        a = np.array([[3,1], [1,2]])
//        b = np.array([9,8])
//        x = np.linalg.solve(a, b)
//        x
        KLU_common Common = new KLU_common();
        KLU_symbolic Symbolic = new KLU_symbolic();
        FlexCompColMatrix circuitSparseMatrix = new FlexCompColMatrix(2,2);
        circuitSparseMatrix.set(0,0,3);
        circuitSparseMatrix.set(0,1,1);
        circuitSparseMatrix.set(1,0,1);
        circuitSparseMatrix.set(1,1,2);
        CompColMatrix circuitCCSM = new CompColMatrix(circuitSparseMatrix);

        Symbolic = MathUtil.klu_decomp(circuitCCSM,Common);
        double[] circuitRightSide={9,8};
        MathUtil.klu_backslash(circuitCCSM,circuitRightSide,Common,Symbolic);

        Assert.assertEquals(circuitRightSide[0],2.,1e-15);
        Assert.assertEquals(circuitRightSide[1],3.,1e-15);

        circuitCCSM.set(0,0,4);
        circuitCCSM.set(0,1,3);
        circuitCCSM.set(1,0,-5);
        circuitCCSM.set(1,1,9);


        circuitRightSide[0]=20;
        circuitRightSide[1]=26;

        Symbolic = MathUtil.klu_decomp(circuitCCSM,Common);

        MathUtil.klu_backslash(circuitCCSM,circuitRightSide,Common,Symbolic);

        Assert.assertEquals(circuitRightSide[0],2.,1e-15);
        Assert.assertEquals(circuitRightSide[1],4.,1e-15);
    }

    @Test
    public void nicsluLinearSolveTest(){

        NicsluSolver nicsluSolver = new NicsluSolver();

        FlexCompColMatrix circuitSparseMatrix = new FlexCompColMatrix(2,2);
        circuitSparseMatrix.set(0,0,3);
        circuitSparseMatrix.set(0,1,1);
        circuitSparseMatrix.set(1,0,1);
        circuitSparseMatrix.set(1,1,2);
        double[] circuitRightSide={9,8};
        //CompColMatrix circuitCCSM = new CompColMatrix(circuitSparseMatrix);
        CompRowMatrix circuitCSR = new CompRowMatrix(circuitSparseMatrix);

        nicsluSolver.SetMatrix(circuitCSR);

        nicsluSolver.AnalyzeAndFactorize();

        nicsluSolver.nicslu_solve(circuitRightSide);

        Assert.assertEquals(circuitRightSide[0],2.,1e-15);
        Assert.assertEquals(circuitRightSide[1],3.,1e-15);

        circuitCSR.set(0,0,4);
        circuitCSR.set(0,1,3);
        circuitCSR.set(1,0,-5);
        circuitCSR.set(1,1,9);
        circuitRightSide[0]=20;
        circuitRightSide[1]=26;

        nicsluSolver.SetMatrix(circuitCSR);

        nicsluSolver.AnalyzeAndFactorize();               

        nicsluSolver.nicslu_solve(circuitRightSide);
        System.out.println(circuitRightSide);
        Assert.assertEquals(circuitRightSide[0],2.,1e-15);
        Assert.assertEquals(circuitRightSide[1],4.,1e-15);
    }

    @Test
    public void nicsluSingularityTest(){

        NicsluSolver nicsluSolver = new NicsluSolver();

        FlexCompColMatrix circuitSparseMatrix = new FlexCompColMatrix(2,2);
        circuitSparseMatrix.set(0,0,0);
        circuitSparseMatrix.set(0,1,0);
        circuitSparseMatrix.set(1,0,1);
        circuitSparseMatrix.set(1,1,2);

        CompRowMatrix circuitCSR = new CompRowMatrix(circuitSparseMatrix);

        nicsluSolver.SetMatrix(circuitCSR);

        boolean ret = nicsluSolver.AnalyzeAndFactorize();

        Assert.assertEquals(false, ret);
    }

    @Test
    public void nonlinearTest() throws ExecutionException, InterruptedException {
        // Create circuit manager for managing single simulation. You can have as many CircuitManager as needed, each
        // represents single simulation
        CircuitManager simulation = new CircuitManager();
        // Load circuit, wait for load to finish
        simulation.loadInternalCircuit("../research/tunnel-osc.cmf").get();
        // Specify elements which need to be measured
        simulation.setMeasurableElements(new ArrayList<>((Arrays.asList(123))));
        // Specify to measure parameters each 1 ms, wait for command to finish
        simulation.setPeekInterval(0.001).get();
        // Run simulation for 5 ms
        simulation.startForAndWait(.005);
        // Disable measuring, wait for command to finish

        // Peek some measurements, simulation may be running at this point.
        double time = simulation.peekTime();
        double current = simulation.peekCurrent(123);
        double voltage = simulation.peekVoltageDiff(123);
        Assert.assertEquals(time,0.00402,1e-5);
        Assert.assertEquals(current,3.9339664599972E-9,1e-12);
        Assert.assertEquals(voltage,0.039339664599972686,1e-12);
        System.out.println(time);
        System.out.println(current);
        System.out.println(voltage);


        simulation.startForAndWait(.5);
        // Peek some measurements, simulation may be running at this point.

        time = simulation.peekTime();
        current = simulation.peekCurrent(123);
        voltage = simulation.peekVoltageDiff(123);
        Assert.assertEquals(time,0.50492,1e-5);
        Assert.assertEquals(current,6.654451328739555E-9,1e-9);
        Assert.assertEquals(voltage,0.06654451328739555,1e-9);
        System.out.println(time);
        System.out.println(current);
        System.out.println(voltage);

        simulation.setPeekInterval(-1).get();

        simulation.stop().get();
        // Permanently stop simulation, simulation thread will exit
        simulation.kill();
//        System.out.println(simulation.peekVoltageDiff(1000));
    }

    private String setupSimulation() throws Exception {
        final String[] key = new String[1];
        mockMvc.perform(post("/symphony/simulations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", notNullValue()))
                .andDo(result -> key[0] = objectMapper.readValue(result.getResponse().getContentAsString(), SimulationDTO.class).getKey());
        return key[0];
    }

    private void killSimulation(String key) throws Exception {
        mockMvc.perform(post("/symphony/simulations/{key}/kill", key))
                .andExpect(status().isOk());
    }
}
