/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.regexp;

//import java.io.Serializable;

/**
 * A class that holds compiled regular expressions.  This is exposed mainly
 * for use by the recompile utility (which helps you produce precompiled
 * REProgram objects). You should not otherwise need to work directly with
 * this class.
 *
 * @see RE
 * @see RECompiler
 *
 * @author <a href="mailto:jonl@muppetlabs.com">Jonathan Locke</a>
 * @version $Id: REProgram.java 518156 2007-03-14 14:31:26Z vgritsenko $
 */
public class REProgram //implements Serializable
{
    char[] instruction;         // The compiled regular expression 'program'
    int lenInstruction;         // The amount of the instruction buffer in use
    char[] prefix;              // Prefix string optimization
    int flags;                  // Optimization flags (REProgram.OPT_*)
    int maxParens = -1;

    /**
     * Constructs a program object from a character array
     * @param instruction Character array with RE opcode instructions in it
     */
/*    public REProgram(char[] instruction)
    {
        this(instruction, instruction.length);
    }
*/
    /**
     * Constructs a program object from a character array
     * @param parens Count of parens in the program
     * @param instruction Character array with RE opcode instructions in it
     */
    public REProgram(int parens, char[] instruction)
    {
        this(instruction, instruction.length);
        this.maxParens = parens;
    }

    /**
     * Constructs a program object from a character array
     * @param instruction Character array with RE opcode instructions in it
     * @param lenInstruction Amount of instruction array in use
     */
    public REProgram(char[] instruction, int lenInstruction)
    {
        setInstructions(instruction, lenInstruction);
    }

    /**
     * Returns a copy of the current regular expression program in a character
     * array that is exactly the right length to hold the program.  If there is
     * no program compiled yet, getInstructions() will return null.
     * @return A copy of the current compiled RE program
     */
/*    public char[] getInstructions()
    {
        // Ensure program has been compiled!
        if (lenInstruction != 0)
        {
            // Return copy of program
            char[] ret = new char[lenInstruction];
            System.arraycopy(instruction, 0, ret, 0, lenInstruction);
            return ret;
        }
        return null;
    }
*/
    /**
     * Sets a new regular expression program to run.  It is this method which
     * performs any special compile-time search optimizations.  Currently only
     * two optimizations are in place - one which checks for backreferences
     * (so that they can be lazily allocated) and another which attempts to
     * find an prefix anchor string so that substantial amounts of input can
     * potentially be skipped without running the actual program.
     * @param instruction Program instruction buffer
     * @param lenInstruction Length of instruction buffer in use
     */
    public void setInstructions(char[] instruction, int lenInstruction) {
		// Save reference to instruction array
		this.instruction = instruction;
		this.lenInstruction = lenInstruction;

		// Initialize other program-related variables
		this.flags = 0;
		this.prefix = null;

		// Try various compile-time optimizations if there's a program
		if (instruction != null && lenInstruction != 0) {
			// If the first node is a branch
			if (lenInstruction >= 3 && instruction[0 + 0] == '|') {
				// to the end node
				int next = (short) instruction[0 + 2];
				if (instruction[next + 0] == 'E' && lenInstruction >= (3 * 2)) {
					final char nextOp = instruction[3 + 0];
					// the branch starts with an atom
					if (nextOp == 'A') {
						// then get that atom as an prefix because there's no other choice
						int lenAtom = instruction[3 + 1];
						this.prefix = new char[lenAtom];
						System
								.arraycopy(instruction, 3 * 2, prefix, 0,
										lenAtom);
					}
					// the branch starts with a BOL
					else if (nextOp == '^') {
						// then set the flag indicating that BOL is present
						this.flags |= 2;
					}
				}
			}

			BackrefScanLoop:

			// Check for backreferences
			for (int i = 0; i < lenInstruction; i += 3) {
				switch (instruction[i + 0]) {
				case '[':
					i += (instruction[i + 1] * 2);
					break;

				case 'A':
					i += instruction[i + 1];
					break;

				case '#':
					flags |= 1;
					break BackrefScanLoop;
				}
			}
		}
	}
    /**
     * Returns a copy of the prefix of current regular expression program
     * in a character array.  If there is no prefix, or there is no program
     * compiled yet, <code>getPrefix</code> will return null.
     * @return A copy of the prefix of current compiled RE program
     */
/*    public char[] getPrefix()
    {
        if (prefix != null)
        {
            // Return copy of prefix
            char[] ret = new char[prefix.length];
            System.arraycopy(prefix, 0, ret, 0, prefix.length);
            return ret;
        }
        return null;
    }
*/
}
