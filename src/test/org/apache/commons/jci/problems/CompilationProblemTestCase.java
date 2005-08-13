package org.apache.commons.jci.problems;

import junit.framework.TestCase;


public final class CompilationProblemTestCase extends TestCase {

    public void testToString() {
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

}
