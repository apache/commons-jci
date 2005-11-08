package org.apache.commons.jci.problems;

import junit.framework.TestCase;
import org.apache.commons.jci.compilers.eclipse.EclipseCompilationProblem;
import org.apache.commons.jci.compilers.groovy.GroovyCompilationProblem;
import org.apache.commons.jci.compilers.janino.JaninoCompilationProblem;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.janino.Location;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * TODO: It might be better to actually try to compile some faulty code and
 *       to handle the problems caused by those errors.
 */
public final class CompilationProblemTestCase extends TestCase {

    private static final String MESSAGE = "message";
    private static final short COLUMN = 2;
    private static final short LINE = 1;
    private static final String FILENAME = "filename";

    public void testJaninoCompilationProblem() {
        CompilationProblem problem = createJaninoCompilationProblem(true);
        assertEquals("wrong filename", FILENAME, problem.getFileName());
        assertEquals("wrong line number", LINE, problem.getStartLine());
        assertEquals("wrong column number", COLUMN, problem.getStartColumn());
        assertEquals("wrong message", MESSAGE, problem.getMessage());
        assertTrue("wrong error level", problem.isError());

        problem = createJaninoCompilationProblem(false);
        assertEquals("wrong filename", FILENAME, problem.getFileName());
        assertEquals("wrong line number", LINE, problem.getStartLine());
        assertEquals("wrong column number", COLUMN, problem.getStartColumn());
        assertEquals("wrong message", MESSAGE, problem.getMessage());
        assertFalse("wrong error level", problem.isError());
    }

    public void testEclipseCompilationProblem() {
        CompilationProblem problem = createEclipseCompilationProblem(true);
        assertEquals("wrong filename", FILENAME, problem.getFileName());
        assertEquals("wrong line number", LINE, problem.getStartLine());
        assertEquals("wrong column number", COLUMN, problem.getStartColumn());
        // FIXME: Don't know how to create eclipse's IProblem with message
        //assertEquals("wrong message", MESSAGE, problem.getMessage());
        assertTrue("wrong error level", problem.isError());

        problem = createEclipseCompilationProblem(false);
        assertEquals("wrong filename", FILENAME, problem.getFileName());
        assertEquals("wrong line number", LINE, problem.getStartLine());
        assertEquals("wrong column number", COLUMN, problem.getStartColumn());
        // FIXME: Don't know how to create eclipse's IProblem with message
        //assertEquals("wrong message", MESSAGE, problem.getMessage());
        assertFalse("wrong error level", problem.isError());

    }

    public void testGroovyCompilationProblem() {
        CompilationProblem problem = createGroovyCompilationProblem(true);
        // FIXME: Don't know how to access filename info.
        //assertEquals("wrong filename", FILENAME, problem.getFileName());
        assertEquals("wrong line number", LINE, problem.getStartLine());
        assertEquals("wrong column number", COLUMN, problem.getStartColumn());
        // FIXME: Don't know how to access the "real" message.
        assertEquals("wrong message", MESSAGE + " @ line " + LINE + ", column " + COLUMN + ".", problem.getMessage());
        assertTrue("wrong error level", problem.isError());

        problem = createGroovyCompilationProblem(false);
        // FIXME: Don't know how to create a LocatedMessage with filename info.
        //assertEquals("wrong filename", FILENAME, problem.getFileName());
        // FIXME: Don't know how to create a LocatedMessage with location info.
        //assertEquals("wrong line number", LINE, problem.getStartLine());
        //assertEquals("wrong column number", COLUMN, problem.getStartColumn());
        assertEquals("wrong message", MESSAGE, problem.getMessage());
        assertFalse("wrong error level", problem.isError());
    }

    private static JaninoCompilationProblem createJaninoCompilationProblem(final boolean pError) {
        Location location = new Location(FILENAME, LINE, COLUMN);
        return new JaninoCompilationProblem(location, MESSAGE, pError);
    }

    private static EclipseCompilationProblem createEclipseCompilationProblem(final boolean pError) {
        int severity;
        if (pError) {
            severity = 1;
        } else {
            severity = 0;
        }
        IProblemFactory problemFactory = new DefaultProblemFactory();
        IProblem problem =
            problemFactory.createProblem(FILENAME.toCharArray(), 1, new String[0],
                    new String[0], severity, COLUMN, COLUMN, LINE);
        return new EclipseCompilationProblem(problem);
    }

    private static GroovyCompilationProblem createGroovyCompilationProblem(final boolean pError) {
        Message message;
        if (pError) {
            message =
                new SyntaxErrorMessage(new SyntaxException(MESSAGE, LINE, COLUMN),
                                       new SourceUnit(FILENAME, "", new CompilerConfiguration() , null, null));
        } else {
            message =
                new WarningMessage(WarningMessage.NONE, MESSAGE, null, null);
        }
        return new GroovyCompilationProblem(message);
    }

}
