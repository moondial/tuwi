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

//import java.util.Hashtable;

/**
 * A regular expression compiler class.  This class compiles a pattern string into a
 * regular expression program interpretable by the RE evaluator class.  The 'recompile'
 * command line tool uses this compiler to pre-compile regular expressions for use
 * with RE.  For a description of the syntax accepted by RECompiler and what you can
 * do with regular expressions, see the documentation for the RE matcher class.
 *
 * @see RE
 * @see recompile
 *
 * @author <a href="mailto:jonl@muppetlabs.com">Jonathan Locke</a>
 * @author <a href="mailto:gholam@xtra.co.nz">Michael McCallum</a>
 * @version $Id: RECompiler.java 518156 2007-03-14 14:31:26Z vgritsenko $
 */
public class RECompiler
{
    // The compiled program
    char[] instruction;                                 // The compiled RE 'program' instruction buffer
    int lenInstruction;                                 // The amount of the program buffer currently in use

    // Input state for compiling regular expression
    String pattern;                                     // Input string
    int len;                                            // Length of the pattern string
    int idx;                                            // Current input i into ac
    int parens;                                         // Total number of paren pairs

    int bracketMin;                                     // Minimum number of matches
    int bracketOpt;                                     // Additional optional matches
/* サポートしない
    // Lookup table for POSIX character class names
    static final Hashtable hashPOSIX = new Hashtable();
    static
    {
        hashPOSIX.put("alnum",     new Character(RE.POSIX_CLASS_ALNUM));
        hashPOSIX.put("alpha",     new Character(RE.POSIX_CLASS_ALPHA));
        hashPOSIX.put("blank",     new Character(RE.POSIX_CLASS_BLANK));
        hashPOSIX.put("cntrl",     new Character(RE.POSIX_CLASS_CNTRL));
        hashPOSIX.put("digit",     new Character(RE.POSIX_CLASS_DIGIT));
        hashPOSIX.put("graph",     new Character(RE.POSIX_CLASS_GRAPH));
        hashPOSIX.put("lower",     new Character(RE.POSIX_CLASS_LOWER));
        hashPOSIX.put("print",     new Character(RE.POSIX_CLASS_PRINT));
        hashPOSIX.put("punct",     new Character(RE.POSIX_CLASS_PUNCT));
        hashPOSIX.put("space",     new Character(RE.POSIX_CLASS_SPACE));
        hashPOSIX.put("upper",     new Character(RE.POSIX_CLASS_UPPER));
        hashPOSIX.put("xdigit",    new Character(RE.POSIX_CLASS_XDIGIT));
        hashPOSIX.put("javastart", new Character(RE.POSIX_CLASS_JSTART));
        hashPOSIX.put("javapart",  new Character(RE.POSIX_CLASS_JPART));
    }
*/

    /**
     * Constructor.  Creates (initially empty) storage for a regular expression program.
     */
    public RECompiler()
    {
        // Start off with a generous, yet reasonable, initial size
        instruction = new char[128];
        lenInstruction = 0;
    }

    /**
     * Ensures that n more characters can fit in the program buffer.
     * If n more can't fit, then the size is doubled until it can.
     * @param n Number of additional characters to ensure will fit.
     */
    void ensure(int n)
    {
        // Get current program length
        int curlen = instruction.length;

        // If the current length + n more is too much
        if (lenInstruction + n >= curlen)
        {
            // Double the size of the program array until n more will fit
            while (lenInstruction + n >= curlen)
            {
                curlen *= 2;
            }

            // Allocate new program array and move data into it
            char[] newInstruction = new char[curlen];
            System.arraycopy(instruction, 0, newInstruction, 0, lenInstruction);
            instruction = newInstruction;
        }
    }

    /**
     * Inserts a node with a given opcode and opdata at insertAt.  The node relative next
     * pointer is initialized to 0.
     * @param opcode Opcode for new node
     * @param opdata Opdata for new node (only the low 16 bits are currently used)
     * @param insertAt Index at which to insert the new node in the program
     */
    void nodeInsert(char opcode, int opdata, int insertAt)
    {
        // Make room for a new node
        ensure(3);

        // Move everything from insertAt to the end down nodeSize elements
        System.arraycopy(instruction, insertAt, instruction, insertAt + 3, lenInstruction - insertAt);
        instruction[insertAt /* + RE.offsetOpcode */] = opcode;
        instruction[insertAt    + 1   ] = (char) opdata;
        instruction[insertAt    + 2     ] = 0;
        lenInstruction += 3;
    }

    /**
     * Appends a node to the end of a node chain
     * @param node Start of node chain to traverse
     * @param pointTo Node to have the tail of the chain point to
     */
    void setNextOfEnd(int node, int pointTo)
    {
        // Traverse the chain until the next offset is 0
        int next = instruction[node + 2];
        // while the 'node' is not the last in the chain
        // and the 'node' is not the last in the program.
        while ( next != 0 && node < lenInstruction )
        {
            // if the node we are supposed to point to is in the chain then
            // point to the end of the program instead.
            // Michael McCallum <gholam@xtra.co.nz>
            // FIXME: This is a _hack_ to stop infinite programs.
            // I believe that the implementation of the reluctant matches is wrong but
            // have not worked out a better way yet.
            if (node == pointTo) {
                pointTo = lenInstruction;
            }
            node += next;
            next = instruction[node + 2];
        }

        // if we have reached the end of the program then dont set the pointTo.
        // im not sure if this will break any thing but passes all the tests.
        if ( node < lenInstruction ) {
            // Some patterns result in very large programs which exceed
            // capacity of the short used for specifying signed offset of the
            // next instruction. Example: a{1638}
            int offset = pointTo - node;
            if (offset != (short) offset) {
                throw new IllegalArgumentException/*RESyntaxException*/("正規表現: Exceeded short jump range.");
            }

            // Point the last node in the chain to pointTo.
            instruction[node + 2] = (char) (short) offset;
        }
    }

    /**
     * Adds a new node
     * @param opcode Opcode for node
     * @param opdata Opdata for node (only the low 16 bits are currently used)
     * @return Index of new node in program
     */
    int node(char opcode, int opdata)
    {
        // Make room for a new node
        ensure(3);

        // Add new node at end
        instruction[lenInstruction /* + RE.offsetOpcode */] = opcode;
        instruction[lenInstruction    + 1   ] = (char) opdata;
        instruction[lenInstruction    + 2     ] = 0;
        lenInstruction += 3;

        // Return i of new node
        return lenInstruction - 3;
    }


    /**
     * Throws a new internal error exception
     * @exception Error Thrown in the event of an internal error.
     */
    void internalError() throws Error
    {
        throw new Error("Internal error!");
    }

    /**
     * Throws a new syntax error exception
     * @exception IllegalArgumentException/*RESyntaxException* / Thrown if the regular expression has invalid syntax.
     */
    void syntaxError(String s) throws IllegalArgumentException/*RESyntaxException*/
    {
        throw new IllegalArgumentException/*RESyntaxException*/("正規表現ｴﾗｰ: " + s);
    }

    /**
     * Match an escape sequence.  Handles quoted chars and octal escapes as well
     * as normal escape characters.  Always advances the input stream by the
     * right amount. This code "understands" the subtle difference between an
     * octal escape and a backref.  You can access the type of ESC_CLASS or
     * ESC_COMPLEX or ESC_BACKREF by looking at pattern[idx - 1].
     * @return ESC_* code or character if simple escape
     * @exception IllegalArgumentException/*RESyntaxException* / Thrown if the regular expression has invalid syntax.
     */
    int escape() throws IllegalArgumentException/*RESyntaxException*/
    {
        // "Shouldn't" happen
        if (pattern.charAt(idx) != '\\')
        {
            internalError();
        }

        // Escape shouldn't occur as last character in string!
        if (idx + 1 == len)
        {
            syntaxError("Escape terminates string");
        }

        // Switch on character after backslash
        idx += 2;
        char escapeChar = pattern.charAt(idx - 1);
        switch (escapeChar)
        {
            case 'b':
            case 'B':
                return 0xffffe;

            case 'w':
            case 'W':
            case 's':
            case 'S':
            case 'd':
            case 'D':
                return 0xffffd;

            case 'u':
            case 'x':
                {
                    // Exact required hex digits for escape type
                    int hexDigits = (escapeChar == 'u' ? 4 : 2);

                    // Parse up to hexDigits characters from input
                    int val = 0;
                    for ( ; idx < len && hexDigits-- > 0; idx++)
                    {
                        // Get char
                        char c = pattern.charAt(idx);

                        // If it's a hexadecimal digit (0-9)
                        if (c >= '0' && c <= '9')
                        {
                            // Compute new value
                            val = (val << 4) + c - '0';
                        }
                        else
                        {
                            // If it's a hexadecimal letter (a-f)
                            c = Character.toLowerCase(c);
                            if (c >= 'a' && c <= 'f')
                            {
                                // Compute new value
                                val = (val << 4) + (c - 'a') + 10;
                            }
                            else
                            {
                                // If it's not a valid digit or hex letter, the escape must be invalid
                                // because hexDigits of input have not been absorbed yet.
                                syntaxError("Expected " + hexDigits + " hexadecimal digits after \\" + escapeChar);
                            }
                        }
                    }
                    return val;
                }

            case 't':
                return '\t';

            case 'n':
                return '\n';

            case 'r':
                return '\r';

            case 'f':
                return '\f';

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':

                // An octal escape starts with a 0 or has two digits in a row
                if ((idx < len && Character.isDigit(pattern.charAt(idx))) || escapeChar == '0')
                {
                    // Handle \nnn octal escapes
                    int val = escapeChar - '0';
                    if (idx < len && Character.isDigit(pattern.charAt(idx)))
                    {
                        val = ((val << 3) + (pattern.charAt(idx++) - '0'));
                        if (idx < len && Character.isDigit(pattern.charAt(idx)))
                        {
                            val = ((val << 3) + (pattern.charAt(idx++) - '0'));
                        }
                    }
                    return val;
                }

                // It's actually a backreference (\[1-9]), not an escape
                return 0xfffff;

            default:

                // Simple quoting of a character
                return escapeChar;
        }
    }

    /**
     * Match a terminal node.
     * @param flags Flags
     * @return Index of terminal node (closeable)
     * @exception IllegalArgumentException/*RESyntaxException* / Thrown if the regular expression has invalid syntax.
     */
    int terminal(int[] flags) throws IllegalArgumentException/*RESyntaxException*/
    {
        switch (pattern.charAt(idx))
        {
        case '$':
        case '^':
        case '.':
            return node(pattern.charAt(idx++), 0);

        case '[':
            // Check for bad calling or empty class
				        if (pattern.charAt(idx) != '[')
				        {
				            internalError();
				        }
				
				        // Check for unterminated or empty class
				        if ((idx + 1) >= len || pattern.charAt(++idx) == ']')
				        {
				            syntaxError("Empty or unterminated class");
				        }
				
				        // Check for POSIX character class
				/*iアプリにはi18nなんて意味無し。
				        if (idx < len && pattern.charAt(idx) == ':')
				        {
				            // Skip colon
				            idx++;
				
				            // POSIX character classes are denoted with lowercase ASCII strings
				            int idxStart = idx;
				            while (idx < len && pattern.charAt(idx) >= 'a' && pattern.charAt(idx) <= 'z')
				            {
				                idx++;
				            }
				
				            // Should be a ":]" to terminate the POSIX character class
				            if ((idx + 1) < len && pattern.charAt(idx) == ':' && pattern.charAt(idx + 1) == ']')
				            {
				                // Get character class
				                String charClass = pattern.substring(idxStart, idx);
				
				                // Select the POSIX class id
				                Character i = (Character)hashPOSIX.get(charClass);
				                if (i != null)
				                {
				                    // Move past colon and right bracket
				                    idx += 2;
				
				                    // Return new POSIX character class node
				                    return node(RE.OP_POSIXCLASS, i.charValue());
				                }
				                syntaxError("Invalid POSIX character class '" + charClass + "'");
				            }
				            syntaxError("Invalid POSIX character class syntax");
				        }
				*/
				
				        // Try to build a class.  Create OP_ANYOF node
				        int ret1 = node('[', 0);
				
				        // Parse class declaration
				        char CHAR_INVALID = Character.MAX_VALUE;
				        char last = CHAR_INVALID;
				        char simpleChar;
				        boolean include = true;
				        boolean definingRange = false;
				        int idxFirst = idx;
				        char rangeStart = Character.MIN_VALUE;
				        char rangeEnd;
				        RERange range = new RERange();
				        while (idx < len && pattern.charAt(idx) != ']')
				        {
				
				            switchOnCharacter:
				
				            // Switch on character
				            switch (pattern.charAt(idx))
				            {
				                case '^':
				                    include = !include;
				                    if (idx == idxFirst)
				                    {
				                        range.include(Character.MIN_VALUE, Character.MAX_VALUE, true);
				                    }
				                    idx++;
				                    continue;
				
				                case '\\':
				                {
				                    // Escape always advances the stream
				                    int c1;
				                    switch (c1 = escape ())
				                    {
				                        case 0xffffe:
				                        case 0xfffff:
				
				                            // Word boundaries and backrefs not allowed in a character class!
				                            syntaxError("Bad character class");
				
				                        case 0xffffd:
				
				                            // Classes can't be an endpoint of a range
				                            if (definingRange)
				                            {
				                                syntaxError("Bad character class");
				                            }
				
				                            // Handle specific type of class (some are ok)
				                            switch (pattern.charAt(idx - 1))
				                            {
				                                case 'S':
				                                    range.include(Character.MIN_VALUE, 7, include);   // [Min - \b )
				                                    range.include((char) 11, include);                // ( \n - \f )
				                                    range.include(14, 31, include);                   // ( \r - ' ')
				                                    range.include(33, Character.MAX_VALUE, include);  // (' ' - Max]
				                                    break;
				
				                                case 'W':
				                                    range.include(Character.MIN_VALUE, '/', include); // [Min - '0')
				                                    range.include(':', '@', include);                 // ('9' - 'A')
				                                    range.include('[', '^', include);                 // ('Z' - '_')
				                                    range.include('`', include);                      // ('_' - 'a')
				                                    range.include('{', Character.MAX_VALUE, include); // ('z' - Max]
				                                    break;
				
				                                case 'D':
				                                    range.include(Character.MIN_VALUE, '/', include); // [Min - '0')
				                                    range.include(':', Character.MAX_VALUE, include); // ('9' - Max]
				                                    break;
				
				                                case 's':
				                                    range.include('\t', include);
				                                    range.include('\r', include);
				                                    range.include('\f', include);
				                                    range.include('\n', include);
				                                    range.include('\b', include);
				                                    range.include(' ', include);
				                                    break;
				
				                                case 'w':
				                                    range.include('a', 'z', include);
				                                    range.include('A', 'Z', include);
				                                    range.include('_', include);
				
				                                    // Fall through!
				
				                                case 'd':
				                                    range.include('0', '9', include);
				                                    break;
				                            }
				
				                            // Make last char invalid (can't be a range start)
				                            last = CHAR_INVALID;
				                            break;
				
				                        default:
				
				                            // Escape is simple so treat as a simple char
				                            simpleChar = (char) c1;
				                            break switchOnCharacter;
				                    }
				                }
				                continue;
				
				                case '-':
				
				                    // Start a range if one isn't already started
				                    if (definingRange)
				                    {
				                        syntaxError("Bad class range");
				                    }
				                    definingRange = true;
				
				                    // If no last character, start of range is 0
				                    rangeStart = (last == CHAR_INVALID ? 0 : last);
				
				                    // Premature end of range. define up to Character.MAX_VALUE
				                    if ((idx + 1) < len && pattern.charAt(++idx) == ']')
				                    {
				                        simpleChar = Character.MAX_VALUE;
				                        break;
				                    }
				                    continue;
				
				                default:
				                    simpleChar = pattern.charAt(idx++);
				                    break;
				            }
				
				            // Handle simple character simpleChar
				            if (definingRange)
				            {
				                // if we are defining a range make it now
				                rangeEnd = simpleChar;
				
				                // Actually create a range if the range is ok
				                if (rangeStart >= rangeEnd)
				                {
				                    syntaxError("Bad character class");
				                }
				                range.include(rangeStart, rangeEnd, include);
				
				                // We are done defining the range
				                last = CHAR_INVALID;
				                definingRange = false;
				            }
				            else
				            {
				                // If simple character and not start of range, include it
				                if (idx >= len || pattern.charAt(idx) != '-')
				                {
				                    range.include(simpleChar, include);
				                }
				                last = simpleChar;
				            }
				        }
				
				        // Shouldn't be out of input
				        if (idx == len)
				        {
				            syntaxError("Unterminated character class");
				        }
				
				        // Absorb the ']' end of class marker
				        idx++;
				
				        // Emit character class definition
				        instruction[ret1 + 1] = (char)range.num;
				        for (int i = 0; i < range.num; i++)
				        {
				            // Make room for character
							ensure(1);
							
							// Add character
							instruction[lenInstruction++] = ((char)range.minRange[i]);
				            // Make room for character
							ensure(1);
							
							// Add character
							instruction[lenInstruction++] = ((char)range.maxRange[i]);
				        }
				        return ret1;

        case '(':
            return expr(flags);

        case ')':
            syntaxError("Unexpected close paren");

        case '|':
            internalError();

        case ']':
            syntaxError("Mismatched class");

        case 0:
            syntaxError("Unexpected end of input");

        case '?':
        case '+':
        case '{':
        case '*':
            syntaxError("Missing operand to closure");

        case '\\':
            {
                // Don't forget, escape() advances the input stream!
                int idxBeforeEscape = idx;

                // Switch on escaped character
                switch (escape())
                {
                    case 0xffffd:
                    case 0xffffe:
                        flags[0] &= ~1;
                        return node('\\', pattern.charAt(idx - 1));

                    case 0xfffff:
                        {
                            char backreference = (char)(pattern.charAt(idx - 1) - '0');
                            if (parens <= backreference)
                            {
                                syntaxError("Bad backreference");
                            }
                            flags[0] |= 1;
                            return node('#', backreference);
                        }

                    default:

                        // We had a simple escape and we want to have it end up in
                        // an atom, so we back up and fall though to the default handling
                        idx = idxBeforeEscape;
                        flags[0] &= ~1;
                        break;
                }
            }
        }

        // Everything above either fails or returns.
        // If it wasn't one of the above, it must be the start of an atom.
        flags[0] &= ~1;
        // Create a string node
		int ret = node('A', 0);
		
		// Length of atom
		int lenAtom = 0;
		
		// Loop while we've got input
		
		atomLoop:
		
		while (idx < len)
		{
		    // Is there a next char?
		    if ((idx + 1) < len)
		    {
		        char c = pattern.charAt(idx + 1);
		
		        // If the next 'char' is an escape, look past the whole escape
		        if (pattern.charAt(idx) == '\\')
		        {
		            int idxEscape = idx;
		            escape();
		            if (idx < len)
		            {
		                c = pattern.charAt(idx);
		            }
		            idx = idxEscape;
		        }
		
		        // Switch on next char
		        switch (c)
		        {
		            case '{':
		            case '?':
		            case '*':
		            case '+':
		
		                // If the next character is a closure operator and our atom is non-empty, the
		                // current character should bind to the closure operator rather than the atom
		                if (lenAtom != 0)
		                {
		                    break atomLoop;
		                }
		        }
		    }
		
		    // Switch on current char
		    switch (pattern.charAt(idx))
		    {
		        case ']':
		        case '^':
		        case '$':
		        case '.':
		        case '[':
		        case '(':
		        case ')':
		        case '|':
		            break atomLoop;
		
		        case '{':
		        case '?':
		        case '*':
		        case '+':
		
		            // We should have an atom by now
		            if (lenAtom == 0)
		            {
		                // No atom before closure
		                syntaxError("Missing operand to closure");
		            }
		            break atomLoop;
		
		        case '\\':
		
		            {
		                // Get the escaped character (advances input automatically)
		                int idxBeforeEscape = idx;
		                int c = escape();
		
		                // Check if it's a simple escape (as opposed to, say, a backreference)
		                if ((c & 0xffff0) == 0xffff0)
		                {
		                    // Not a simple escape, so backup to where we were before the escape.
		                    idx = idxBeforeEscape;
		                    break atomLoop;
		                }
		
		                // Add escaped char to atom
		                // Make room for character
						ensure(1);
						
						// Add character
						instruction[lenInstruction++] = ((char) c);
		                lenAtom++;
		            }
		            break;
		
		        default:
		
		            // Add normal character to atom
		            // Make room for character
					ensure(1);
					
					// Add character
					instruction[lenInstruction++] = pattern.charAt(idx++);
		            lenAtom++;
		            break;
		    }
		}
		
		// This "shouldn't" happen
		if (lenAtom == 0)
		{
		    internalError();
		}
		
		// Emit the atom length into the program
		instruction[ret + 1] = (char)lenAtom;
		return ret;
    }

    /**
     * Compile body of one branch of an or operator (implements concatenation)
     *
     * @param flags Flags passed by reference
     * @return Pointer to first node in the branch
     * @exception IllegalArgumentException/*RESyntaxException* / Thrown if the regular expression has invalid syntax.
     */
    int branch(int[] flags) throws IllegalArgumentException/*RESyntaxException*/
    {
        // Get each possibly closured piece and concat
        int node;
        int ret = -1;
        int chain = -1;
        int[] closureFlags = new int[1];
        boolean nullable = true;
        while (idx < len && pattern.charAt(idx) != '|' && pattern.charAt(idx) != ')')
        {
            // Get new node
            closureFlags[0] = 0;
			// Before terminal
			int idxBeforeTerminal = idx;
			
			// Values to pass by reference to terminal()
			int[] terminalFlags = { 0 };
			
			// Get terminal symbol
			int ret1 = terminal(terminalFlags);
			
			// Or in flags from terminal symbol
			closureFlags[0] |= terminalFlags[0];
			while(true){
			// Advance input, set NODE_NULLABLE flag and do sanity checks
			if (idx >= len)
			{
			    break;
			}
			
			boolean greedy = true;
			char closureType = pattern.charAt(idx);
			switch (closureType)
			{
			    case '?':
			    case '*':
			
			        // The current node can be null
			        closureFlags[0] |= 1;
			
			        // Drop through
			
			    case '+':
			
			        // Eat closure character
			        idx++;
			
			        // Drop through
			
			    case '{':
			
			        // Don't allow blantant stupidity
			        int opcode = instruction[ret1];
			        if (opcode == '^' || opcode == '$')
			        {
			            syntaxError("Bad closure operand");
			        }
			        if ((terminalFlags[0] & 1) != 0)
			        {
			            syntaxError("Closure operand can't be nullable");
			        }
			}
			
			// If the next character is a '?', make the closure non-greedy (reluctant)
			if (idx < len && pattern.charAt(idx) == '?')
			{
			    idx++;
			    greedy = false;
			}
			
			if (greedy)
			{
			    // Actually do the closure now
			    switch (closureType)
			    {
			        case '{':
			        {
			            while(true){
						    // Current character must be a '{'
						    if (idx >= len || pattern.charAt(idx++) != '{')
						    {
						        internalError();
						    }
						
						    // Next char must be a digit
						    if (idx >= len || !Character.isDigit(pattern.charAt(idx)))
						    {
						        syntaxError("Expected digit");
						    }
						
						    // Get min ('m' of {m,n}) number
						    StringBuffer number = new StringBuffer();
						    while (idx < len && Character.isDigit(pattern.charAt(idx)))
						    {
						        number.append(pattern.charAt(idx++));
						    }
						    try
						    {
						        bracketMin = Integer.parseInt(number.toString());
						    }
						    catch (NumberFormatException e)
						    {
						        syntaxError("Expected valid number");
						    }
						
						    // If out of input, fail
						    if (idx >= len)
						    {
						        syntaxError("Expected comma or right bracket");
						    }
						
						    // If end of expr, optional limit is 0
						    if (pattern.charAt(idx) == '}')
						    {
						        idx++;
						        bracketOpt = 0;
						        break;
						    }
						
						    // Must have at least {m,} and maybe {m,n}.
						    if (idx >= len || pattern.charAt(idx++) != ',')
						    {
						        syntaxError("Expected comma");
						    }
						
						    // If out of input, fail
						    if (idx >= len)
						    {
						        syntaxError("Expected comma or right bracket");
						    }
						
						    // If {m,} max is unlimited
						    if (pattern.charAt(idx) == '}')
						    {
						        idx++;
						        bracketOpt = (-1);
						        break;
						    }
						
						    // Next char must be a digit
						    if (idx >= len || !Character.isDigit(pattern.charAt(idx)))
						    {
						        syntaxError("Expected digit");
						    }
						
						    // Get max number
						    number.setLength(0);
						    while (idx < len && Character.isDigit(pattern.charAt(idx)))
						    {
						        number.append(pattern.charAt(idx++));
						    }
						    try
						    {
						        bracketOpt = Integer.parseInt(number.toString()) - bracketMin;
						    }
						    catch (NumberFormatException e)
						    {
						        syntaxError("Expected valid number");
						    }
						
						    // Optional repetitions must be >= 0
						    if (bracketOpt < 0)
						    {
						        syntaxError("Bad range");
						    }
						
						    // Must have close brace
						    if (idx >= len || pattern.charAt(idx++) != '}')
						    {
						        syntaxError("Missing close brace");
						    }
						    break;
						}
			            int bracketEnd = idx;
			            int bracketMin = this.bracketMin;
			            int bracketOpt = this.bracketOpt;
			
			            // Pointer to the last terminal
			            int pos = ret1;
			
			            // Process min first
			            for (int c = 0; c < bracketMin; c++)
			            {
			                // Rewind stream and run it through again - more matchers coming
			                idx = idxBeforeTerminal;
			                setNextOfEnd(pos, pos = terminal(terminalFlags));
			            }
			
			            // Do the right thing for maximum ({m,})
			            if (bracketOpt == (-1))
			            {
			                // Drop through now and closure expression.
			                // We are done with the {m,} expr, so skip rest
			                idx = bracketEnd;
			                nodeInsert('*', 0, pos);
			                setNextOfEnd(pos + 3, pos);
			                break;
			            }
			            else if (bracketOpt > 0)
			            {
			                int opt[] = new int[bracketOpt + 1];
			                // Surround first optional terminal with MAYBE
			                nodeInsert('?', 0, pos);
			                opt[0] = pos;
			
			                // Add all the rest optional terminals with preceeding MAYBEs
			                for (int c = 1; c < bracketOpt; c++)
			                {
			                    opt[c] = node('?', 0);
			                    // Rewind stream and run it through again - more matchers coming
			                    idx = idxBeforeTerminal;
			                    terminal(terminalFlags);
			                }
			
			                // Tie ends together
			                int end = opt[bracketOpt] = node('N', 0);
			                for (int c = 0; c < bracketOpt; c++)
			                {
			                    setNextOfEnd(opt[c], end);
			                    setNextOfEnd(opt[c] + 3, opt[c + 1]);
			                }
			            }
			            else
			            {
			                // Rollback terminal - no opt matchers present
			                lenInstruction = pos;
			                node('N', 0);
			            }
			
			            // We are done. skip the reminder of {m,n} expr
			            idx = bracketEnd;
			            break;
			        }
			
			        case '?':
			        {
			            nodeInsert('?', 0, ret1);
			            int n = node('N', 0);
			            setNextOfEnd(ret1, n);
			            setNextOfEnd(ret1 + 3, n);
			            break;
			        }
			
			        case '*':
			        {
			            nodeInsert('*', 0, ret1);
			            setNextOfEnd(ret1 + 3, ret1);
			            break;
			        }
			
			        case '+':
			        {
			            nodeInsert('C', 0, ret1);
			            int n = node('+', 0);
			            setNextOfEnd(ret1 + 3, n);
			            setNextOfEnd(n, ret1);
			            break;
			        }
			    }
			}
			else
			{
			    // Actually do the closure now
			    switch (closureType)
			    {
			        case '?':
			        {
			            nodeInsert('/', 0, ret1);
			            int n = node('N', 0);
			            setNextOfEnd(ret1, n);
			            setNextOfEnd(ret1 + 3, n);
			            break;
			        }
			
			        case '*':
			        {
			            nodeInsert('8', 0, ret1);
			            setNextOfEnd(ret1 + 3, ret1);
			            break;
			        }
			
			        case '+':
			        {
			            nodeInsert('C', 0, ret1);
			            int n = node('=', 0);
			            setNextOfEnd(n, ret1);
			            setNextOfEnd(ret1 + 3, n);
			            break;
			        }
			    }
			}
			break;
			}
            node = ret1;
            if (closureFlags[0] == 0)
            {
                nullable = false;
            }

            // If there's a chain, append to the end
            if (chain != -1)
            {
                setNextOfEnd(chain, node);
            }

            // Chain starts at current
            chain = node;
            if (ret == -1) {
                ret = node;
            }
        }

        // If we don't run loop, make a nothing node
        if (ret == -1)
        {
            ret = node('N', 0);
        }

        // Set nullable flag for this branch
        if (nullable)
        {
            flags[0] |= 1;
        }

        return ret;
    }

    /**
     * Compile an expression with possible parens around it.  Paren matching
     * is done at this level so we can tie the branch tails together.
     *
     * @param flags Flag value passed by reference
     * @return Node i of expression in instruction array
     * @exception IllegalArgumentException/*RESyntaxException* / Thrown if the regular expression has invalid syntax.
     */
    int expr(int[] flags) throws IllegalArgumentException/*RESyntaxException*/
    {
        // Create open paren node unless we were called from the top level (which has no parens)
        int paren = -1;
        int ret = -1;
        int closeParens = parens;
        if ((flags[0] & 2) == 0 && pattern.charAt(idx) == '(')
        {
            // if its a cluster ( rather than a proper subexpression ie with backrefs )
            if (idx + 2 < len && pattern.charAt(idx + 1) == '?' && pattern.charAt(idx + 2) == ':')
            {
                paren = 2;
                idx += 3;
                ret = node('<', 0);
            }
            else
            {
                paren = 1;
                idx++;
                ret = node('(', parens++);
            }
        }
        flags[0] &= ~2;

        // Process contents of first branch node
        boolean open = false;
        int branch = branch(flags);
        if (ret == -1)
        {
            ret = branch;
        }
        else
        {
            setNextOfEnd(ret, branch);
        }

        // Loop through branches
        while (idx < len && pattern.charAt(idx) == '|')
        {
            // Now open the first branch since there are more than one
            if (!open) {
                nodeInsert('|', 0, branch);
                open = true;
            }

            idx++;
            setNextOfEnd(branch, branch = node('|', 0));
            branch(flags);
        }

        // Create an ending node (either a close paren or an OP_END)
        int end;
        if (paren > 0)
        {
            if (idx < len && pattern.charAt(idx) == ')')
            {
                idx++;
            }
            else
            {
                syntaxError("Missing close paren");
            }
            if (paren == 1)
            {
                end = node(')', closeParens);
            }
            else
            {
                end = node('>', 0);
            }
        }
        else
        {
            end = node('E', 0);
        }

        // Append the ending node to the ret nodelist
        setNextOfEnd(ret, end);

        // Hook the ends of each branch to the end node
        int currentNode = ret;
        int nextNodeOffset = instruction[currentNode + 2];
        // while the next node o
        while (nextNodeOffset != 0 && currentNode < lenInstruction)
        {
            // If branch, make the end of the branch's operand chain point to the end node.
            if (instruction[currentNode /* + RE.offsetOpcode */] == '|')
            {
                setNextOfEnd(currentNode + 3, end);
            }
            nextNodeOffset = instruction[currentNode + 2];
            currentNode += nextNodeOffset;
        }

        // Return the node list
        return ret;
    }

    /**
     * Compiles a regular expression pattern into a program runnable by the pattern
     * matcher class 'RE'.
     * @param pattern Regular expression pattern to compile (see RECompiler class
     * for details).
     * @return A compiled regular expression program.
     * @exception IllegalArgumentException/*RESyntaxException* / Thrown if the regular expression has invalid syntax.
     * @see RECompiler
     * @see RE
     */
    public REProgram compile(String pattern) throws IllegalArgumentException/*RESyntaxException*/
    {
        // Initialize variables for compilation
        this.pattern = pattern;                         // Save pattern in instance variable
        len = pattern.length();                         // Precompute pattern length for speed
        idx = 0;                                        // Set parsing i to the first character
        lenInstruction = 0;                             // Set emitted instruction count to zero
        parens = 1;                                     // Set paren level to 1 (the implicit outer parens)

        // Initialize pass by reference flags value
        int[] flags = { 2 };

        // Parse expression
        expr(flags);

        // Should be at end of input
        if (idx != len)
        {
            if (pattern.charAt(idx) == ')')
            {
                syntaxError("Unmatched close paren");
            }
            syntaxError("Unexpected input remains");
        }

        // Return the result
        char[] ins = new char[lenInstruction];
        System.arraycopy(instruction, 0, ins, 0, lenInstruction);
        return new REProgram(parens, ins);
    }

    /**
     * Local, nested class for maintaining character ranges for character classes.
     */
    class RERange
    {
        int size = 16;                      // Capacity of current range arrays
        int[] minRange = new int[size];     // Range minima
        int[] maxRange = new int[size];     // Range maxima
        int num = 0;                        // Number of range array elements in use

        /**
         * Deletes the range at a given i from the range lists
         * @param focus Index of range to delete from minRange and maxRange arrays.
         */
        void delete(int index)
        {
            // Return if no elements left or i is out of range
            if (num == 0 || index >= num)
            {
                return;
            }

            // Move elements down
            while (++index < num)
            {
                if (index - 1 >= 0)
                {
                    minRange[index-1] = minRange[index];
                    maxRange[index-1] = maxRange[index];
                }
            }

            // One less element now
            num--;
        }

        /**
         * Merges a range into the range list, coalescing ranges if possible.
         * @param min Minimum end of range
         * @param max Maximum end of range
         */
        void merge(int min, int max)
        {
            // Loop through ranges
            for (int i = 0; i < num; i++)
            {
                // Min-max is subsumed by minRange[i]-maxRange[i]
                if (min >= minRange[i] && max <= maxRange[i])
                {
                    return;
                }

                // Min-max subsumes minRange[i]-maxRange[i]
                else if (min <= minRange[i] && max >= maxRange[i])
                {
                    delete(i);
                    merge(min, max);
                    return;
                }

                // Min is in the range, but max is outside
                else if (min >= minRange[i] && min <= maxRange[i])
                {
                    min = minRange[i];
                    delete(i);
                    merge(min, max);
                    return;
                }

                // Max is in the range, but min is outside
                else if (max >= minRange[i] && max <= maxRange[i])
                {
                    max = maxRange[i];
                    delete(i);
                    merge(min, max);
                    return;
                }
            }

            // Must not overlap any other ranges
            if (num >= size)
            {
                size *= 2;
                int[] newMin = new int[size];
                int[] newMax = new int[size];
                System.arraycopy(minRange, 0, newMin, 0, num);
                System.arraycopy(maxRange, 0, newMax, 0, num);
                minRange = newMin;
                maxRange = newMax;
            }
            minRange[num] = min;
            maxRange[num] = max;
            num++;
        }

        /**
         * Removes a range by deleting or shrinking all other ranges
         * @param min Minimum end of range
         * @param max Maximum end of range
         */
        void remove(int min, int max)
        {
            // Loop through ranges
            for (int i = 0; i < num; i++)
            {
                // minRange[i]-maxRange[i] is subsumed by min-max
                if (minRange[i] >= min && maxRange[i] <= max)
                {
                    delete(i);
                    return;
                }

                // min-max is subsumed by minRange[i]-maxRange[i]
                else if (min >= minRange[i] && max <= maxRange[i])
                {
                    int minr = minRange[i];
                    int maxr = maxRange[i];
                    delete(i);
                    if (minr < min)
                    {
                        merge(minr, min - 1);
                    }
                    if (max < maxr)
                    {
                        merge(max + 1, maxr);
                    }
                    return;
                }

                // minRange is in the range, but maxRange is outside
                else if (minRange[i] >= min && minRange[i] <= max)
                {
                    minRange[i] = max + 1;
                    return;
                }

                // maxRange is in the range, but minRange is outside
                else if (maxRange[i] >= min && maxRange[i] <= max)
                {
                    maxRange[i] = min - 1;
                    return;
                }
            }
        }

        /**
         * Includes (or excludes) the range from min to max, inclusive.
         * @param min Minimum end of range
         * @param max Maximum end of range
         * @param include True if range should be included.  False otherwise.
         */
        void include(int min, int max, boolean include)
        {
            if (include)
            {
                merge(min, max);
            }
            else
            {
                remove(min, max);
            }
        }

        /**
         * Includes a range with the same min and max
         * @param minmax Minimum and maximum end of range (inclusive)
         * @param include True if range should be included.  False otherwise.
         */
        void include(char minmax, boolean include)
        {
            include(minmax, minmax, include);
        }
    }
}
