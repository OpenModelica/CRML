package org.openmodelica.crml.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.junit.jupiter.params.provider.Arguments;
import org.openmodelica.crml.util.NaturalCompare;

@ExtendWith({SpecificationTestListener.class, SharedParameter.class})
public class ReportedTest {
	private ExtensionContext.Store context;

	@BeforeEach
	public void beforeEach(ExtensionContext.Store context){
		this.context = context;
	}

	public void emit(Object message, String key){
		SharedParameter.registerKey(key, context);

		context.put(key, message);
	}
	
	/**
	 * The models in {@code source} as {@code (Path, ModelExpectation, Boolean isDisabled)}
	 * arguments; see {@link ModelExpectation} for the file name markers.
	 */
	public static List<Arguments> fileNameSourceHelper2(Path source) {
		try (Stream<Path> list = Files.list(source)) {
			return list
				.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".crml") || p.toString().endsWith(".crml.disabled"))
				.sorted(new NaturalCompare.PathCompare())
				.map(p -> {
					String name = p.getFileName().toString();
					boolean isDisabled = name.toLowerCase().endsWith(".disabled");
					return Arguments.of(p, ModelExpectation.fromFileName(name), isDisabled);
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Same as {@link #fileNameSourceHelper2(Path)}, but models with a syntax
	 * error are dropped entirely, as they never reach the DOM.
	 */
	public static List<Arguments> fileNameSourceHelperDom(Path source) {
		return fileNameSourceHelper2(source).stream()
			.filter(args -> ((ModelExpectation) args.get()[1]).parses())
			.collect(Collectors.toList());
	}
}
