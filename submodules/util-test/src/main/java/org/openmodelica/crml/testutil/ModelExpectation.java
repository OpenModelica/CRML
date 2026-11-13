package org.openmodelica.crml.testutil;

/**
 * What a test model is expected to do, read from the marker in its file name:
 * {@code name.<marker>.crml}, optionally followed by {@code .disabled}.
 * A file without a marker is {@link #CORRECT}.
 */
public enum ModelExpectation {
	/** Parses and passes validation. Marker {@code .correct}. */
	CORRECT(".correct"),
	/** Rejected by the parser. Marker {@code .syntax-error}. */
	SYNTAX_ERROR(".syntax-error"),
	/**
	 * Parses, but is rejected by validation on the DOM (types, name
	 * resolution and similar rules). Marker {@code .semantic-error}.
	 */
	SEMANTIC_ERROR(".semantic-error");

	/** The marker used before this change; rejected so that stale names are noticed. */
	private static final String LEGACY_MARKER = ".faulty";

	private final String marker;

	ModelExpectation(String marker) {
		this.marker = marker;
	}

	public String marker() {
		return marker;
	}

	/** Whether the parser is expected to accept the model. */
	public boolean parses() {
		return this != SYNTAX_ERROR;
	}

	public static ModelExpectation fromFileName(String fileName) {
		String name = fileName.toLowerCase();
		if (name.contains(LEGACY_MARKER)) {
			throw new IllegalArgumentException(fileName + ": the '" + LEGACY_MARKER
				+ "' marker is no longer used; use '" + SYNTAX_ERROR.marker + "' or '"
				+ SEMANTIC_ERROR.marker + "'");
		}
		for (ModelExpectation expectation : values()) {
			if (name.contains(expectation.marker + ".")) {
				return expectation;
			}
		}
		return CORRECT;
	}
}
