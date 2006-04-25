package org.apache.commons.jci.compilers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.jci.problems.CompilationProblem;


public final class CompilationResult {
    
    final Collection errors = new ArrayList();
    final Collection warnings = new ArrayList();

    public CompilationResult( final Collection pProblems ) {
        for (final Iterator it = pProblems.iterator(); it.hasNext();) {
            final CompilationProblem problem = (CompilationProblem) it.next();
            if (problem.isError()) {
                errors.add(problem);
            } else {
                warnings.add(problem);
            }
        }
    }
    
    public CompilationProblem[] getErrors() {
        final CompilationProblem[] result = new CompilationProblem[errors.size()];
        errors.toArray(result);
        return result;
    }

    public CompilationProblem[] getWarnings() {
        final CompilationProblem[] result = new CompilationProblem[warnings.size()];
        warnings.toArray(result);
        return result;
    }
}
