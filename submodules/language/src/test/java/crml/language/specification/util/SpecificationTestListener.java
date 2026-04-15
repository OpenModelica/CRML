package crml.language.specification.util;

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.code;
import static j2html.TagCreator.details;
import static j2html.TagCreator.join;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.summary;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.opentest4j.TestAbortedException;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import crml.language.util.ErrorListener.CRMLSyntaxResults;

import static com.aventstack.extentreports.Status.FAIL;

public class SpecificationTestListener implements TestExecutionListener, AfterEachCallback, AfterAllCallback {

    // Static report state shared across all instances
    private static ExtentSparkReporter reporter;
    private static ExtentReports extentReport;
    private static volatile boolean reportInitialized = false;

    // True once testPlanExecutionStarted fires (ServiceLoader path active)
    private static volatile boolean serviceLoaderActive = false;

    // TestExecutionListener path: results keyed by TestIdentifier
    private static final Map<TestIdentifier, TestExecutionResult> RESULTS = new HashMap<>();

    // Extension path: results keyed by "className#displayName"
    private static final Map<String, TestExecutionResult.Status> EXT_STATUSES = new HashMap<>();
    private static final Map<String, Throwable> EXT_THROWABLES = new HashMap<>();
    private static final Map<String, List<String>> RESULTS_BY_CLASS = new HashMap<>();

    // Shared parameters (both paths)
    private static final Map<String, Map<String, ? extends Object>> SHARED = new HashMap<>();

    private static synchronized void initReport() {
        if (!reportInitialized) {
            reporter = new ExtentSparkReporter("build" + java.io.File.separator + "specification_test_report.html");
            extentReport = new ExtentReports();
            extentReport.attachReporter(reporter);
            extentReport.setAnalysisStrategy(AnalysisStrategy.SUITE);
            reportInitialized = true;
        }
    }

    // -------------------------------------------------------------------------
    // TestExecutionListener path (ServiceLoader – works in VSCode, some IDEs)
    // -------------------------------------------------------------------------

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        serviceLoaderActive = true;
        initReport();
        testPlan.getChildren(getRoot(testPlan)).forEach(testIdentifier -> {
            RESULTS.put(testIdentifier, null);
        });
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        testPlan.getChildren(getRoot(testPlan)).forEach(klass -> {
            final ExtentTest testKlass = extentReport.createTest(getKlassName(klass.getUniqueId()));
            testPlan.getDescendants(klass).stream()
                    .sorted(Comparator.comparing(TestIdentifier::getDisplayName, NaturalCompare::compareStrings))
                    .forEach(test -> processTestNode(testKlass, test));
        });
        extentReport.flush();
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        RESULTS.put(testIdentifier, testExecutionResult);
    }

    private void processTestNode(ExtentTest testKlass, TestIdentifier test) {
        if (test.getDisplayName().equals("simulateTestFile(Path, Boolean, Boolean)"))
            return;

        final TestExecutionResult testResult = RESULTS.get(test);
        if (testResult == null)
            return;

        processNode(testKlass, test.getDisplayName(), testResult.getStatus(),
                testResult.getThrowable().orElse(null));
    }

    // -------------------------------------------------------------------------
    // Extension path (AfterEachCallback / AfterAllCallback – works in Gradle)
    // -------------------------------------------------------------------------

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        SHARED.put(context.getDisplayName(), SharedParameter.asMap(context));

        String className = context.getRequiredTestClass().getName();
        String displayName = context.getDisplayName();

        TestExecutionResult.Status status;
        Throwable throwable = null;
        if (context.getExecutionException().isEmpty()) {
            status = TestExecutionResult.Status.SUCCESSFUL;
        } else {
            throwable = context.getExecutionException().get();
            status = (throwable instanceof TestAbortedException)
                    ? TestExecutionResult.Status.ABORTED
                    : TestExecutionResult.Status.FAILED;
        }
        EXT_STATUSES.put(className + "#" + displayName, status);
        if (throwable != null)
            EXT_THROWABLES.put(className + "#" + displayName, throwable);
        RESULTS_BY_CLASS.computeIfAbsent(className, k -> new ArrayList<>()).add(displayName);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (serviceLoaderActive)
            return; // TestExecutionListener path handles reporting

        initReport();

        String className = context.getRequiredTestClass().getName();
        List<String> testNames = RESULTS_BY_CLASS.getOrDefault(className, List.of());
        if (testNames.isEmpty())
            return;

        final ExtentTest testKlass = extentReport.createTest(className);
        for (String displayName : testNames) {
            TestExecutionResult.Status status = EXT_STATUSES.get(className + "#" + displayName);
            Throwable throwable = EXT_THROWABLES.get(className + "#" + displayName);
            if (status == null)
                continue;
            processNode(testKlass, displayName, status, throwable);
        }
        extentReport.flush();
    }

    // -------------------------------------------------------------------------
    // Shared node rendering
    // -------------------------------------------------------------------------

    private void processNode(ExtentTest testKlass, String displayName,
            TestExecutionResult.Status status, Throwable throwable) {
        if (displayName.equals("simulateTestFile(Path, Boolean, Boolean)"))
            return;

        String name = "";
        Pattern pattern_idx = Pattern.compile("^(\\[\\d+\\])");
        Matcher matcher_idx = pattern_idx.matcher(displayName);
        if (matcher_idx.find())
            name += matcher_idx.group(1) + " ";

        Pattern pattern_path = Pattern.compile("([^\\\\\\/]+\\.crml(?:\\.disabled)?), ");
        Matcher matcher_path = pattern_path.matcher(displayName);
        if (matcher_path.find())
            name += matcher_path.group(1);

        if (name.isEmpty())
            return;

        final ExtentTest node = testKlass.createNode(name);
        for (Entry<String, ? extends Object> entry : SHARED.getOrDefault(displayName, new HashMap<>()).entrySet()) {
            if (entry.getValue() instanceof Path path) {
                try { 
                    String fileContent = Files.readString(path); 
                    node.info(join(
                        p(join(entry.getKey(), br())),
                        pre(code(fileContent)),
                        p(a(path.toString()).withHref(path.toUri().toString()))
                    ).render());
                } catch (Exception e) {
                    node.info(join(
                        p(join(entry.getKey(), br())),
                        p(a(path.toString()).withHref(path.toUri().toString()))
                    ).render());
                }     
            } else if (entry.getValue() instanceof CRMLSyntaxResults syntax) {
                node.info(join(
                    p(join(entry.getKey(), br())),
                    join(syntax.errors().stream().map(Object::toString).map(e -> p(e)).toArray())
                ).render());
            } else if (entry.getValue() instanceof String ast && "AST".equals(entry.getKey())) {
                    node.info(details(
                        summary(entry.getKey()),
                        pre(code(ast))
                    ).render());
            } else {
                node.info(join(p(join(entry.getKey(), br())),
                        p(entry.getValue().toString())).render());
            }
        }

        switch (status) {
            case SUCCESSFUL -> node.pass(status.toString());
            case FAILED -> {
                node.fail(status.toString());
                if (throwable != null)
                    node.log(FAIL, throwable);
            }
            case ABORTED -> node.skip(status.toString());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private TestIdentifier getRoot(TestPlan testPlan) {
        return testPlan.getRoots().stream().findFirst().get();
    }

    private String getKlassName(String uniqueId) {
        return getKlassName(UniqueId.parse(uniqueId));
    }

    private String getKlassName(UniqueId uniqueId) {
        return uniqueId.getSegments().stream()
                .filter(segment -> segment.getType().equals("class"))
                .map(UniqueId.Segment::getValue)
                .collect(Collectors.joining());
    }
}
