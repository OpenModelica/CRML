package ctests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import crml.compiler.CompileSettings;
import crml.compiler.test.util.TestListener;
import crml.compiler.test.util.SharedParameter;

@ExtendWith({TestListener.class, SharedParameter.class}) // a hook for catching succesful test results in the test report
public class ParameterizedSuite {

    public static CompileSettings cs = new CompileSettings();

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


    /**
	 * Method for feeding the list of files into the parametrized test
	 * @return
	 * @throws IOException
	 */
	public static List<String> fileNameSource() throws IOException {
		List<String> fileList;
		
		try (Stream<Path> list = Files.list(Paths.get(cs.testFolderIn))) {
			fileList = list.map(path -> path.getFileName()
					.toString())
					.filter(name -> name.endsWith(".crml")).collect(Collectors.toList());
			fileList.forEach(System.out::println);
		}		
		return fileList;
	}
}