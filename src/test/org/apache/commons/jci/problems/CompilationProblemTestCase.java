package org.apache.commons.jci.problems;

import junit.framework.TestCase;


public final class CompilationProblemTestCase extends TestCase {

    public void testLocation() {
        CompilationProblem problem;
        
        problem = new CompilationProblem(
                0,
                "filename",
                "message",
                1,
                2,
                true
                );
        assertTrue("filename (1-2) : message".equals(problem.toString()));

        problem = new CompilationProblem(
                0,
                "filename",
                "message",
                1,
                1,
                true
                );
        assertTrue("filename (1) : message".equals(problem.toString()));
    }

    public void testErrorHandling() {
        final CompilationProblemHandler handler = new ConsoleCompilationProblemHandler();
        final CompilationProblem error = new CompilationProblem(
                0,
                "filename",
                "message",
                1,
                1,
                true
                );
        handler.handle(error);

        final CompilationProblem warning = new CompilationProblem(
                0,
                "filename",
                "message",
                1,
                1,
                false
                );
        handler.handle(warning);
    }
}
