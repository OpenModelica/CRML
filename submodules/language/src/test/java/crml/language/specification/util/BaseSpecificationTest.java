package crml.language.specification.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.junit.jupiter.params.provider.Arguments;

import crml.language.util.Parser;

@ExtendWith({SpecificationTestListener.class, SharedParameter.class})
public class BaseSpecificationTest {
	public static final Path RESOURCES;
	public static final Path OUT;

	private ExtensionContext.Store context;

	@BeforeEach
	public void beforeEach(ExtensionContext.Store context){
		this.context = context;
	}

	public void emit(Object message, String key){
		SharedParameter.registerKey(key, context);

		System.out.println("Emit: "+key);
		context.put(key, message);
	}

	static {
		try {
			RESOURCES = Paths.get(Thread.currentThread().getContextClassLoader().getResource("specs/specRoot.txt").toURI()).getParent();
			System.out.println("Resources: "+RESOURCES);
			OUT = Path.of("build","testSuiteGenerated");
			System.out.println("Output:"+OUT);
		} catch (URISyntaxException e) {
			// Test configuration is expected to be correct, 
			// therefore, we just wrap potential errors in a runtime exception to hide it in code.
			throw new RuntimeException("Incorrect project setup. Resources not found.");
		}
	}

    protected Parser.ParserResult parse(Path model) throws IOException {
        return new Parser().parse(model);
    }

	public static List<Arguments> fileNameSourceHelper2(Path source) {
		try (Stream<Path> list = Files.list(source)) {
			return list
				.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".crml") || p.toString().endsWith(".crml.disabled"))
				.sorted(new NaturalCompare())
				.map(p -> {
					String name = p.getFileName().toString().toLowerCase();

					boolean isFaulty = name.contains(".faulty");
					boolean isDisabled = name.contains(".disabled");

					return Arguments.of(p, !isFaulty, isDisabled);
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
