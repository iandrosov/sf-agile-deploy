/*
 * sf-agile-deploy - Salesforce Agile Deployment tool
 *
 * Copyright © 2017 Igor Androsov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.
 */

package com.force.deploy.jgit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class ConsoleOutputCapturer {
    private ByteArrayOutputStream baos;
    private PrintStream previous;
    private boolean capturing;

    public void start() {
        if (capturing) {
            return;
        }

        capturing = true;
        previous = System.out;      
        baos = new ByteArrayOutputStream();

        OutputStream outputStreamCombiner = 
                new OutputStreamCombiner(Arrays.asList(previous, baos)); 
        PrintStream custom = new PrintStream(outputStreamCombiner);

        System.setOut(custom);
    }

    public String stop() {
        if (!capturing) {
            return "";
        }

        System.setOut(previous);

        String capturedValue = baos.toString();             

        baos = null;
        previous = null;
        capturing = false;

        return capturedValue;
    }

    private static class OutputStreamCombiner extends OutputStream {
        private List<OutputStream> outputStreams;

        public OutputStreamCombiner(List<OutputStream> outputStreams) {
            this.outputStreams = outputStreams;
        }

        public void write(int b) throws IOException {
            for (OutputStream os : outputStreams) {
                os.write(b);
            }
        }

        public void flush() throws IOException {
            for (OutputStream os : outputStreams) {
                os.flush();
            }
        }

        public void close() throws IOException {
            for (OutputStream os : outputStreams) {
                os.close();
            }
        }
    }

}
