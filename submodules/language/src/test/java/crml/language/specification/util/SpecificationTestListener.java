package crml.language.specification.util;

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.join;
import static j2html.TagCreator.p;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import crml.language.util.ErrorListener.CRMLSyntaxResults;

import static com.aventstack.extentreports.Status.FAIL;

public class SpecificationTestListener implements TestExecutionListener, AfterEachCallback{
    private final ExtentSparkReporter reporter = new ExtentSparkReporter("build"+ java.io.File.separator+ "specification_test_report.html");
    private final ExtentReports extentReport = new ExtentReports();

    private static final Map<TestIdentifier, TestExecutionResult> RESULTS = new HashMap<>();
    private static final Map<String, Map<String,? extends Object>> SHARED = new HashMap<>();

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        this.extentReport.attachReporter(reporter);
        this.extentReport.setAnalysisStrategy(AnalysisStrategy.SUITE);
        testPlan.getChildren(getRoot(testPlan)).forEach(testIdentifier -> {
            RESULTS.put(testIdentifier, null);
        });
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        testPlan.getChildren(getRoot(testPlan)).forEach(klass -> {
            final ExtentTest testKlass = extentReport.createTest(getKlassName(klass.getUniqueId()));
            testPlan.getDescendants(klass).forEach(test -> processTestNode(testKlass, test));
        });
        extentReport.flush();
    }

    private void processTestNode(ExtentTest testKlass, TestIdentifier test) {
        if(test.getDisplayName().equals("simulateTestFile(Path, Boolean, Boolean)"))
            return;

        String name = "";
        Pattern pattern_idx = Pattern.compile("^(\\[\\d+\\])");
        Matcher matcher_idx = pattern_idx.matcher(test.getDisplayName());
        if(matcher_idx.find()) {
            name += matcher_idx.group(1)+" ";
        }
        //Pattern pattern_path = Pattern.compile("((?:[ A-Za-z0-9_@.#&+-]+[\\\\\\/])?[^\\\\\\/]+\\.crml(?:\\.disabled)?), ");
        Pattern pattern_path = Pattern.compile("([^\\\\\\/]+\\.crml(?:\\.disabled)?), ");
        Matcher matcher_path = pattern_path.matcher(test.getDisplayName());
        if(matcher_path.find()) {//Find must be called.
            name += matcher_path.group(1);
        }
    

        final ExtentTest node = testKlass.createNode(name);
        System.err.println("Hello there " +SHARED.get(test.getDisplayName()));
        for(Entry<String, ? extends Object> entry : SHARED.getOrDefault(test.getDisplayName(), new HashMap<String, Object>()).entrySet()){
            System.err.println("\t"+entry.getClass());
            if (entry.getValue() instanceof Path path){
                node.info(
                    join(
                        p(join(entry.getKey(), br())),
                        p(a(path.toString()).withHref(path.toUri().toString()))
                    ).render()
                );
            } else if (entry.getValue() instanceof CRMLSyntaxResults syntax) {
                node.info(
                    join(
                        p(join(entry.getKey(), br())),
                        p(
                            join(syntax.errors().stream().map(Object::toString).toArray()) )
                    ).render());
            } else {
                node.info(
                    join(
                        p(join(entry.getKey(), br())),
                        p(entry.getValue().toString())
                    ).render()
                );
            }
        }
        
        final TestExecutionResult testResult = RESULTS.get(test);
        switch (testResult.getStatus()) {
            case SUCCESSFUL:
                node.pass(testResult.toString());
                break;
            case FAILED:
                node.fail(testResult.toString());
                if(testResult.getThrowable().isPresent())
                    node.log(FAIL, testResult.getThrowable().get());
                break;
            case ABORTED:
                node.skip(testResult.toString());
                break;
        }
    
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        RESULTS.put(testIdentifier, testExecutionResult);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        SHARED.put(context.getDisplayName(), SharedParameter.asMap(context));
    }
    
    private TestIdentifier getRoot(TestPlan testPlan) {
        return testPlan.getRoots().stream().findFirst().get();
    }
    
    private String getKlassName(String uniqueId) {
        return getKlassName(UniqueId.parse(uniqueId));
    }

    private String getKlassName(UniqueId uniqueId) {
        return uniqueId
                .getSegments().stream()
                .filter(segment -> segment.getType().equals("class"))
                .map(UniqueId.Segment::getValue)
                .collect(Collectors.joining());
    }
}
