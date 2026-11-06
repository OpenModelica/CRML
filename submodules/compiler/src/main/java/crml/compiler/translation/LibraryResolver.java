package crml.compiler.translation;

import java.io.IOException;

/**
 * Finds the CRML libraries a model depends on ('ETL' union ...).
 *
 * The translator itself does not read files, so that it also runs where there
 * is no file system (the web build of the compiler).
 */
public interface LibraryResolver {

	/** The text of a library and where it comes from, for messages. */
	public static class Library {
		public final String source;
		public final String text;

		public Library(String source, String text) {
			this.source = source;
			this.text = text;
		}
	}

	/** The library name, or null if it is not found. */
	Library find(String name) throws IOException;

	/** Where libraries are searched, for the message when one is not found. */
	String describe();
}
