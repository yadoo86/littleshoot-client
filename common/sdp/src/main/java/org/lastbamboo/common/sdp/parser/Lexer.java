package org.lastbamboo.common.sdp.parser;
import org.lastbamboo.common.sdp.LexerCore;

public class Lexer extends LexerCore {
	public Lexer(String lexerName, String buffer) {
		super(lexerName, buffer);

	}

	public void selectLexer(String lexerName) {
	}

	public static String getFieldName(String line) {
		int i = line.indexOf("=");
		if (i == -1)
			return null;
		else
			return line.substring(0, i);
	}
}
/*
 * $Log: Lexer.java,v $
 * Revision 1.2  2004/01/22 13:26:28  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */