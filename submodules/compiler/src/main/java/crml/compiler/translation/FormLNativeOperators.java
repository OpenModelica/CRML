package crml.compiler.translation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * FORM-L operators that are translated to blocks of the CRML Modelica library
 * (CRML.TimeLocators.Continuous and CRML.Requirements) instead of to models
 * generated from their CRML definitions.
 *
 * The definitions of the FORM-L library build these operators from ETL
 * operators on single periods ('check' phi 'over' P, 'count' C 'inside' P,
 * 'duration' b 'on' P). The CRMLtoModelica runtime evaluates them only on one
 * CRMLPeriod, so they cannot work on the Periods that the FORM-L time locators
 * return. The CRML library implements every FORM-L time locator and
 * requirement check as a block over a vector of time periods, so the translator
 * instantiates these blocks.
 */
public class FormLNativeOperators {

	/** How an operand of a FORM-L operator is passed to the block. */
	public enum Kind {
		/** Boolean condition, passed as a Boolean4 input. */
		BOOLEAN,
		/** Clock: its events are passed as the Boolean4 input whose rising edges they are. */
		CLOCK,
		/** Periods built by a FORM-L time locator, passed as the time locator input. */
		PERIODS,
		/** Real expression, passed as a continuous input. */
		REAL_INPUT,
		/** Constant Integer, passed as a parameter. */
		INTEGER_PARAMETER,
		/** Constant Real, passed as a parameter. */
		REAL_PARAMETER
	}

	public static class Operand {
		public final Kind kind;
		public final String port;

		Operand(Kind kind, String port) {
			this.kind = kind;
			this.port = port;
		}
	}

	public static class NativeOperator {
		/** The Modelica block. */
		public final String block;
		/** The CRML return type: Periods or Boolean. */
		public final String returnType;
		/** The operands, in the order of the operator definition. */
		public final List<Operand> operands;

		NativeOperator(String block, String returnType, Operand... operands) {
			this.block = block;
			this.returnType = returnType;
			this.operands = Arrays.asList(operands);
		}

		/** The output of the block. */
		public String output() {
			return "y";
		}
	}

	private static final String LOCATORS = "CRML.TimeLocators.Continuous.";
	private static final String REQUIREMENTS = "CRML.Requirements.";

	private static final HashMap<String, NativeOperator> operators = new HashMap<>();

	private static void periods(String name, String block, Operand... operands) {
		operators.put(name, new NativeOperator(LOCATORS + block, "Periods", operands));
	}

	private static void check(String name, String block, Operand... operands) {
		operators.put(name, new NativeOperator(REQUIREMENTS + block, "Boolean", operands));
	}

	private static Operand op(Kind kind, String port) {
		return new Operand(kind, port);
	}

	static {
		// Time periods, keyed by the operator name the translator builds from
		// the keywords of the definition ('from' Clock ev 'for' Real d -> 'fromfor').
		periods("'during'", "During", op(Kind.BOOLEAN, "u"));
		periods("'when'", "When", op(Kind.CLOCK, "u"));
		periods("'from'", "From", op(Kind.CLOCK, "u1"));
		periods("'after'", "After", op(Kind.CLOCK, "u1"));
		periods("'before'", "Before", op(Kind.CLOCK, "u1"));
		periods("'until'", "Until", op(Kind.CLOCK, "u1"));
		periods("'afterbefore'", "AfterBefore", op(Kind.CLOCK, "u1"), op(Kind.CLOCK, "u2"));
		periods("'afteruntil'", "AfterUntil", op(Kind.CLOCK, "u1"), op(Kind.CLOCK, "u2"));
		periods("'frombefore'", "FromBefore", op(Kind.CLOCK, "u1"), op(Kind.CLOCK, "u2"));
		periods("'fromuntil'", "FromUntil", op(Kind.CLOCK, "u1"), op(Kind.CLOCK, "u2"));
		periods("'afterfor'", "AfterFor", op(Kind.CLOCK, "u"), op(Kind.REAL_INPUT, "duration"));
		periods("'fromfor'", "FromFor", op(Kind.CLOCK, "u"), op(Kind.REAL_INPUT, "duration"));

		// Requirement checks: Periods P <keywords> ...
		check("'check at end'", "CheckAtEnd", op(Kind.PERIODS, "tl"), op(Kind.BOOLEAN, "u"));
		check("'check anytime'", "CheckAnytime", op(Kind.PERIODS, "tl"), op(Kind.BOOLEAN, "u"));
		check("'ensure'", "Ensure", op(Kind.PERIODS, "tl"), op(Kind.BOOLEAN, "u"));

		String[][] comparisons = { { "<", "Lower" }, { "<=", "LowerEqual" }, { ">", "Greater" },
				{ ">=", "GreaterEqual" }, { "==", "Equal" } };
		for (String[] c : comparisons) {
			check("'check count" + c[0] + "'", "CheckCount" + c[1], op(Kind.PERIODS, "tl"), op(Kind.CLOCK, "u"),
					op(Kind.INTEGER_PARAMETER, "threshold"));
			if (!c[0].equals("=="))
				check("'check duration" + c[0] + "'", "CheckDuration" + c[1], op(Kind.PERIODS, "tl"),
						op(Kind.BOOLEAN, "u"), op(Kind.REAL_PARAMETER, "threshold"));
		}
	}

	/** The native translation of a user operator, or null if it has none. */
	public static NativeOperator get(String operatorName) {
		return operators.get(operatorName);
	}
}
