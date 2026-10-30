package crml.compiler.translation;

public class CRMLTranslationException extends RuntimeException{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CRMLTranslationException(String errorMessage) {
		super(errorMessage);
	}

	public static class SIUnitMismatchException extends RuntimeException {
		public SIUnitMismatchException(String errorMessage) {
			super(errorMessage);
		}
	}

	public static class InvalidUserTypeOpException extends RuntimeException {
		public InvalidUserTypeOpException(String errorMessage) {
			super(errorMessage);
		}
	}
}