/* *********************************************************************
 * ECE351 
 * Department of Electrical and Computer Engineering 
 * University of Waterloo 
 * Term: Winter 2019 (1191)
 *
 * The base version of this file is the intellectual property of the
 * University of Waterloo. Redistribution is prohibited.
 *
 * By pushing changes to this file I affirm that I am the author of
 * all changes. I affirm that I have complied with the course
 * collaboration policy and have not plagiarized my work. 
 *
 * I understand that redistributing this file might expose me to
 * disciplinary action under UW Policy 71. I understand that Policy 71
 * allows for retroactive modification of my final grade in a course.
 * For example, if I post my solutions to these labs on GitHub after I
 * finish ECE351, and a future student plagiarizes them, then I too
 * could be found guilty of plagiarism. Consequently, my final grade
 * in ECE351 could be retroactively lowered. This might require that I
 * repeat ECE351, which in turn might delay my graduation.
 *
 * https://uwaterloo.ca/secretariat-general-counsel/policies-procedures-guidelines/policy-71
 * 
 * ********************************************************************/

package ece351.w.rdescent;

import org.parboiled.common.ImmutableList;

import ece351.util.Lexer;
import ece351.w.ast.WProgram;
import ece351.w.ast.Waveform;

public final class WRecursiveDescentParser {
    private final Lexer lexer;

    public WRecursiveDescentParser(final Lexer lexer) {
        this.lexer = lexer;
    }

    public static WProgram parse(final String input) {
    	final WRecursiveDescentParser p = new WRecursiveDescentParser(new Lexer(input));
        return p.parse();
    }

    public WProgram parse() {
    	// STUB: return null;
    	
    	WProgram a = new WProgram();
    	
    	while(!lexer.inspectEOF()) {
        	String name1 = new String();
        	ImmutableList<String> bits1= ImmutableList.of();
        	
	    	name1 = lexer.consumeID();
	    	lexer.consume(":");
	    	bits1 = bits1.append(lexer.consume("0","1"));
	    	while (!lexer.inspect(";")) {
	    		bits1 = bits1.append(lexer.consume("0","1"));
	    	}
	    	lexer.consume(";");
	    	
	    	Waveform w =new Waveform(bits1, name1);
	    	a = a.append(w);
    	}
    	
    	return a;
    	
    	
    	
// TODO: longer code snippet
//throw new ece351.util.Todo351Exception();
    }

	private void b(ImmutableList<String> bits1, String name1) {
		// TODO Auto-generated method stub
		
	}
}
