package crml.compiler.translation;

import java.util.ArrayList;
import java.util.Comparator;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.Token;

public class Utilities {
    public static final String ANSI_RED = "\u001B[1;31m";
    public static final String ANSI_GREEN = "\u001B[1;32m";
    public static final String ANSI_YELLOW = "\u001B[1;33m";
    public static final String ANSI_BLUE = "\u001B[1;34m";
    public static final String ANSI_PURPLE = "\u001B[1;35m";
    public static final String ANSI_CYAN = "\u001B[1;36m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static String red(String input) {
        return ANSI_RED + input + ANSI_RESET;
    }

    public static String green(String input) {
        return ANSI_GREEN + input + ANSI_RESET;
    }

    public static String yellow(String input) {
        return ANSI_YELLOW + input + ANSI_RESET;
    }

    public static String blue(String input) {
        return ANSI_BLUE + input + ANSI_RESET;
    }

    public static String purple(String input) {
        return ANSI_PURPLE + input + ANSI_RESET;
    }

    public static String cyan(String input) {
        return ANSI_CYAN + input + ANSI_RESET;
    }

    public static String color(String input, String ANSI_COLOR) {
        return ANSI_COLOR + input + ANSI_RESET;
    }

    private static void reportMessage(Token token, String message, ArrayList<Range> ranges, String messageType, String ANSI_COLOR) {
        LineInfo lineInfo = getLineInfo(token);

        ranges.sort(Comparator.comparingInt(Range::getStart));

        int i;

        for (i = 0; i < ranges.size() - 1; ++i)
            if (ranges.get(i).end > ranges.get(i + 1).start)
                throw new ParseCancellationException("Color Ranges are overlapping");

        i = 0;
        String coloredLine = "";
        for (Range range : ranges) {
            if (i < range.start) {
                coloredLine += lineInfo.lineContent.substring(i, range.start);
            }
            coloredLine += color(lineInfo.lineContent.substring(range.start, range.end), ANSI_COLOR);
            i = range.end;
        }
        coloredLine += lineInfo.lineContent.substring(i);

        System.out.println(ANSI_BLUE + lineInfo.fileName + ":" + lineInfo.line + ":" + ranges.get(0).start + ": " + ANSI_COLOR + messageType + ": " + ANSI_RESET + message);
        System.out.println(String.format("%5d | ", lineInfo.line) + coloredLine);

        String marker = "      | " + repeatString(" ", ranges.get(0).start);
        for (i = 0; i < ranges.size(); ++i) {
            Range range = ranges.get(i);
            if (i > 0)
                marker += repeatString(" ", range.start - ranges.get(i-1).end);
            marker += color("^" + repeatString("~", range.end - range.start - 1), ANSI_COLOR);
        }

        System.out.println(marker);
    }

    public static void throwCompilerError(Token token, String errorMessage, Range range) {
        ArrayList<Range> ranges = new ArrayList<Range>();
        ranges.add(range);

        throwCompilerError(token, errorMessage, ranges);
    }

    public static void throwCompilerError(Token token, String errorMessage, ArrayList<Range> ranges) {
        reportMessage(token, errorMessage, ranges, "error", ANSI_RED);
        throw new CompilerThrowException();
    }

    public static void reportCompilerWarning(Token token, String warningMessage, Range range) {
        ArrayList<Range> ranges = new ArrayList<Range>();
        ranges.add(range);

        reportCompilerWarning(token, warningMessage, ranges);
    }

    public static void reportCompilerWarning(Token token, String warningMessage, ArrayList<Range> ranges) {
        reportMessage(token, warningMessage, ranges, "warning", ANSI_PURPLE);
    }

    public static void throwCompilerErrorWithNote(Token errorToken, String errorMessage, Range errorRange,
                                                  Token noteToken, String noteMessage, Range noteRange) {
        LineInfo errorLineInfo = getLineInfo(errorToken);
        LineInfo noteLineInfo = getLineInfo(noteToken);

        String coloredErrorLine = errorLineInfo.lineContent.substring(0, errorRange.start)
                                + red(errorLineInfo.lineContent.substring(errorRange.start, errorRange.end))
                                + errorLineInfo.lineContent.substring(errorRange.end);
        
        System.err.println(ANSI_BLUE + errorLineInfo.fileName + ":" + errorLineInfo.line + ":" + errorRange.start + ": " + ANSI_RED + "error: " + ANSI_RESET + errorMessage);
        System.err.println(String.format("%5d | ", errorLineInfo.line) + coloredErrorLine);
        String errorMarker = "      | " + repeatString(" ", errorRange.start);
        errorMarker += red("^" + repeatString("~", errorRange.end - errorRange.start - 1));
        System.err.println(errorMarker);

        String coloredNoteLine = noteLineInfo.lineContent.substring(0, noteRange.start)
                                + cyan(noteLineInfo.lineContent.substring(noteRange.start, noteRange.end))
                                + noteLineInfo.lineContent.substring(noteRange.end);

        System.err.println(ANSI_BLUE + noteLineInfo.fileName + ":" + noteLineInfo.line + ":" + noteRange.start + ": " + ANSI_CYAN + "note: " + ANSI_RESET + noteMessage);
        System.err.println(String.format("%5d | ", noteLineInfo.line) + coloredNoteLine);

        String noteMarker = "      | " + repeatString(" ", noteRange.start);
        noteMarker += cyan("^" + repeatString("~", noteRange.end - noteRange.start - 1));
        System.err.println(noteMarker);

        throw new CompilerThrowException();
    }

    public static void throwCompilerErrorWithNote(Token errorToken, String errorMessage,
                                                  Token noteToken, String noteMessage) {
        LineInfo errorLineInfo = getLineInfo(errorToken);
        LineInfo noteLineInfo = getLineInfo(noteToken);
        
        System.err.println(ANSI_BLUE + errorLineInfo.fileName + ":" + errorLineInfo.line + ": " + ANSI_RED + "error: " + ANSI_RESET + errorMessage);
        System.err.println(String.format("%5d | ", errorLineInfo.line) + errorLineInfo.lineContent);
        String errorMarker = "      | ";
        System.err.println(errorMarker);

        System.err.println(ANSI_BLUE + noteLineInfo.fileName + ":" + noteLineInfo.line + ": " + ANSI_CYAN + "note: " + ANSI_RESET + noteMessage);
        System.err.println(String.format("%5d | ", noteLineInfo.line) + noteLineInfo.lineContent);

        String noteMarker = "      | ";
        System.err.println(noteMarker);

        throw new CompilerThrowException();
    }

    public static String repeatString(String s, Integer count) {
        return new String(new char[count]).replace("\0", s);
    }

    public static LineInfo getLineInfo(Token token) {
        LineInfo lineInfo = new LineInfo();
        final String[] filePath = token.getInputStream().getSourceName().split("/");
        lineInfo.fileName = filePath[filePath.length - 1];
        
        lineInfo.line = token.getLine();
        
        final String[] lines = token.getInputStream().toString().split("\\R", -1);
        lineInfo.lineContent = lines[lineInfo.line - 1];

        return lineInfo;
    }
}

class LineInfo {
    String fileName;
    Integer line;
    String lineContent;
}

class CompilerThrowException extends RuntimeException{
    public CompilerThrowException() {
		super();
	}

	public CompilerThrowException(String errorMessage) {
		super(errorMessage);
	}
}