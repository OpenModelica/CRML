package crml.compiler.util;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import crml.compiler.omc.OMCUtil.OMCFilesLog;

import static com.aventstack.extentreports.Status.FAIL;
import static com.aventstack.extentreports.Status.INFO;
import static com.aventstack.extentreports.Status.WARNING;

import static j2html.TagCreator.*;

public class TestListener implements TestExecutionListener, AfterEachCallback  {
  private final ExtentSparkReporter reporter = new ExtentSparkReporter("build"+ java.io.File.separator+ "test_report.html");
  private final ExtentReports extentReport = new ExtentReports();
  private static final Map<TestIdentifier, TestExecutionResult> RESULTS = new HashMap<>();
  private static final Map<TestIdentifier, String> SKIPPED = new HashMap<>();
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
      if (SKIPPED.containsKey(klass)) {
        extentReport.createTest(getKlassName(klass.getUniqueId())).skip(SKIPPED.get(klass));
      } else if (RESULTS.containsKey(klass)) {
        final ExtentTest testKlass = extentReport.createTest(getKlassName(klass.getUniqueId()));
        testPlan.getDescendants(klass).forEach(test -> processTestNode(testKlass, test));
      }
    });
    extentReport.flush();
  }

  private void processTestNode(ExtentTest testKlass, TestIdentifier test) {
    if(test.isContainer())
            return;

    // remove the parametrized test case signature 
    if(test.getDisplayName().equals("simulateTestFile(String)"))
      return;
    
    String name = "";
    Pattern pattern_idx = Pattern.compile("^(\\[\\d+\\])");
    Matcher matcher_idx = pattern_idx.matcher(test.getDisplayName());
    if(matcher_idx.find()) {
      name += matcher_idx.group(1)+" ";
    }
    Pattern pattern_path = Pattern.compile("((?:[ A-Za-z0-9_@.#&+-]+[\\\\\\/])?[^\\\\\\/]+)$");
    Matcher matcher_path = pattern_path.matcher(test.getDisplayName());
    if(matcher_path.find()) {//Find must be called.
      name += matcher_path.group(1);
    }
    

    final ExtentTest node = testKlass.createNode(name);

    for(Entry<String, ? extends Object> entry : SHARED.getOrDefault(test.getDisplayName(), new HashMap<String, Object>()).entrySet()){
      if(entry.getValue() instanceof OMCFilesLog files){
        node.info(
          join(
            p(join(entry.getKey(), br())),
            p(join(
              Stream.<Path>of(files.files()).map(f -> 
                join(a(f.toString()).withHref(f.toUri().toString()), br())
              ).toArray())
            )
          ).render()
        );
      } else if (entry.getValue() instanceof Path path){
        node.info(
          join(
            p(join(entry.getKey(), br())),
            p(a(path.toString()).withHref(path.toUri().toString()))
          ).render()
        );
      } else {
        node.info(
          join(
            p(join(entry.getKey(), br())),
            p(entry.getValue().toString())
          ).render()
        );
      }
    }
      
    
    if (SKIPPED.containsKey(test)) {
      node.skip(SKIPPED.get(test));
      return;
    }
    
    final TestExecutionResult testResult = RESULTS.get(test);
        if (testResult == null) {
            node.log(INFO, "No test results found");
            return;
        } 
        if(testResult.getStatus()==Status.SUCCESSFUL)        
            node.pass(testResult.toString());
        else if(testResult.getStatus()==Status.ABORTED)
            node.log(WARNING, testResult.toString());
        else if(testResult.getStatus()==Status.FAILED){
            node.fail(testResult.toString());
            if(testResult.getThrowable().isPresent())
            node.log(FAIL, testResult.getThrowable().get());
        }
        else 
            throw new PreconditionViolationException("Unsupported execution status:" + testResult.getStatus());
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        SKIPPED.put(testIdentifier, reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        RESULTS.put(testIdentifier, testExecutionResult);
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

    private TestIdentifier getRoot(TestPlan testPlan) {
        return testPlan.getRoots().stream().findFirst().get();
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        SHARED.put(context.getDisplayName(), SharedParameter.asMap(context));
    }
}
