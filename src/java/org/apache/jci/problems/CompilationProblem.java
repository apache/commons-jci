/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jci.problems;

/**
 * @author tcurdt
 *
 */
public class CompilationProblem {

    private final int id;
    private final String filename;
    private final String message;
    private final int lineStart;
    private final int lineStop;
    private final boolean fatal;
    
    public CompilationProblem(
            final int pId,
            final String pFilename,
            final String pMessage,
            final int pLineStart,
            final int pLineStop,
            final boolean pFatal
            ) {
        id = pId;
        filename = pFilename;
        message = pMessage;
        lineStart = pLineStart;
        lineStop = pLineStop;
        fatal = pFatal;
    }
    
    public boolean isFatal() {
        return fatal;
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(filename).append(" (");
        if (lineStart == lineStop) {
            sb.append(lineStart);            
        } else {
            sb.append(lineStart).append('-').append(lineStop);
        }
        sb.append(") : ");
        sb.append(message);
        
        return sb.toString();
    }
}
