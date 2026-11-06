package crml.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import crml.compiler.translation.LibraryResolver;

/**
 * Finds a library in the .crml files of a list of directories (the directory of
 * the model and --library-path), else in the libraries shipped in the compiler
 * jar (libraries/crml).
 */
public class FileLibraryResolver implements LibraryResolver {
	private final List<Path> dirs = new ArrayList<Path>();

	public FileLibraryResolver(List<Path> dirs) {
		for (Path d : dirs)
			if (d != null && !this.dirs.contains(d))
				this.dirs.add(d);
	}

	@Override
	public Library find(String name) throws IOException {
		Pattern header = Pattern.compile("(^|\\s)library\\s+'?" + Pattern.quote(name) + "'?\\s+is\\b");
		for (Path dir : dirs) {
			if (!Files.isDirectory(dir))
				continue;
			List<Path> files = new ArrayList<Path>();
			// a directory stream also works for a model inside a jar (zip file system)
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.crml")) {
				for (Path f : stream)
					files.add(f);
			}
			Collections.sort(files);
			for (Path f : files) {
				String text = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
				if (header.matcher(text).find())
					return new Library(f.toString(), text);
			}
		}
		InputStream shipped = FileLibraryResolver.class.getResourceAsStream("/crml/libraries/" + name + ".crml");
		if (shipped == null)
			return null;
		try (InputStream in = shipped) {
			ByteArrayOutputStream text = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			for (int n; (n = in.read(buffer)) > 0;)
				text.write(buffer, 0, n);
			return new Library(name + ".crml (shipped with the compiler)",
					new String(text.toByteArray(), StandardCharsets.UTF_8));
		}
	}

	@Override
	public String describe() {
		return "the library path " + dirs + " (use --library-path) nor in the libraries of the compiler";
	}
}
