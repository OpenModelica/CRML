package ctests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import crml.compiler.CRMLC;
import crml.compiler.util.CompilerRoot;

/**
 * FORM-L time locators and requirement checks translate to the blocks of the
 * CRML Modelica library, and a model can use the ETL and FORM-L libraries of
 * libraries/crml ('ETL' union 'FORML' union { ... }).
 */
public class FormLNativeOperatorsTest {
	static Path repository;
	static Path libraries;
	static Path out = CompilerRoot.OUT.resolve("forml_native");

	@BeforeAll
	public static void findRepository() {
		Path dir = Paths.get("").toAbsolutePath();
		while (dir != null && !Files.isDirectory(dir.resolve("libraries").resolve("crml")))
			dir = dir.getParent();
		assertNotNull(dir, "libraries/crml not found above " + Paths.get("").toAbsolutePath());
		repository = dir;
		libraries = dir.resolve("libraries").resolve("crml");
	}

	static String translate(Path model, Path libraryPath) throws Exception {
		String name = model.getFileName().toString().replace(".crml", "");
		Path dir = out.resolve(name);
		CRMLC.parse_file(model, dir, true, false, false, "", false,
				libraryPath == null ? Arrays.asList() : Arrays.asList(libraryPath));
		return new String(Files.readAllBytes(dir.resolve(name + ".mo")), StandardCharsets.UTF_8);
	}

	static Path write(String name, String text) throws IOException {
		Path dir = out.resolve("sources");
		Files.createDirectories(dir);
		Path file = dir.resolve(name + ".crml");
		Files.write(file, text.getBytes(StandardCharsets.UTF_8));
		return file;
	}

	/** The text of model name inside a translated model. */
	static String model(String modelica, String name) {
		Matcher m = Pattern.compile("(?s)\\nmodel " + name + "\\b(.*?)\\nend " + name + ";").matcher(modelica);
		assertTrue(m.find(), "model " + name + " not found in:\n" + modelica);
		return m.group(1);
	}

	@Test
	@DisplayName("ICS educational requirements: FORM-L operators become CRML library blocks")
	void icsRequirements() throws Exception {
		String mo = translate(repository.resolve("resources").resolve("use_cases").resolve("cooling_system")
				.resolve("ics_reqs.crml"), libraries);

		// R1_T and R2_T at the top level
		assertTrue(mo.contains("CRML.TimeLocators.Continuous.During during"), mo);
		assertTrue(Pattern.compile("CRML.Requirements.Ensure ensure\\d+\\(tl = during\\d+\\.y, u = T_in_range\\)")
				.matcher(mo).find(), mo);
		assertTrue(Pattern.compile("CRML.TimeLocators.Continuous.FromFor fromFor\\d+\\(u = c\\d+\\.b, duration = 60.0\\)")
				.matcher(mo).find(), mo);
		assertTrue(Pattern.compile("CRML.Requirements.CheckAtEnd checkAtEnd\\d+\\(tl = fromFor\\d+\\.y, u = T_in_range\\)")
				.matcher(mo).find(), mo);

		// the blocks of a class are in the class model, with its variables
		String speed = model(mo, "Req_speed");
		assertTrue(speed.contains("CRML.Requirements.CheckDurationLowerEqual"), speed);
		assertTrue(speed.contains("u = v_too_high, threshold = 10.0"), speed);
		assertTrue(speed.contains("CRML.Requirements.CheckCountLowerEqual"), speed);
		assertTrue(speed.contains("u = v_too_high_clock.b, threshold = 2"), speed);
		String flow = model(mo, "Req_flow");
		assertTrue(Pattern.compile("CRML.Requirements.Ensure ensure\\d+\\(tl = during\\d+\\.y, u = f_over_fmin\\)")
				.matcher(flow).find(), flow);

		// instances keep the bindings of their external variables
		assertTrue(mo.contains("Req_flow Req_flow2(pump_in_service = pump_in_service2, f = flow2);"), mo);
		assertTrue(mo.contains("Req_speed Req_speed1(v = v1);"), mo);

		// the definitions of the mapped operators are not translated
		assertFalse(mo.contains("model 'ensure'"), mo);
		assertFalse(mo.contains("model 'check durationlower"), mo);
	}

	@Test
	@DisplayName("A Periods variable names the periods of its time locator")
	void periodsVariable() throws Exception {
		String mo = translate(write("PeriodsVariable", "model PeriodsVariable is 'FORML' union {\n"
				+ "  Boolean b is external;\n  Boolean c is external;\n"
				+ "  Periods P is 'during' b;\n  Boolean R is P 'check anytime' c;\n};\n"), libraries);
		assertTrue(Pattern.compile("CRML.Requirements.CheckAnytime checkAnytime\\d+\\(tl = during\\d+\\.y, u = c\\)")
				.matcher(mo).find(), mo);
		// no Modelica variable for P: the check uses the output of the During block
		assertFalse(mo.contains("CRMLPeriods P ="), mo);
	}

	@Test
	@DisplayName("A requirement check needs periods built by a FORM-L time locator")
	void checkNeedsTimeLocator() throws Exception {
		Path model = write("CheckOnPeriodsConstructor", "model CheckOnPeriodsConstructor is 'FORML' union {\n"
				+ "  Boolean b is external;\n"
				+ "  Boolean R is (new Periods [ new Clock b, new Clock false ]) 'ensure' b;\n};\n");
		Exception e = assertThrows(Exception.class, () -> translate(model, libraries));
		assertTrue(e.getMessage().contains("must be periods built by a FORM-L time locator"), e.getMessage());
	}

	@Test
	@DisplayName("A missing library is reported")
	void missingLibrary() throws Exception {
		Path model = write("MissingLibrary", "model MissingLibrary is 'NoSuchLibrary' union {\n"
				+ "  Boolean b is external;\n};\n");
		Exception e = assertThrows(Exception.class, () -> translate(model, null));
		assertTrue(e.getMessage().contains("Library NoSuchLibrary not found"), e.getMessage());
	}
}
