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

package ece351.f.rdescent;

import ece351.common.ast.AndExpr;
import ece351.common.ast.ConstantExpr;
import ece351.common.ast.Constants;
import ece351.common.ast.Expr;
import ece351.common.ast.NotExpr;
import ece351.common.ast.OrExpr;
import ece351.common.ast.VarExpr;
import ece351.util.CommandLine;
import ece351.util.Lexer;

public final class FRecursiveDescentRecognizer implements Constants {
   
    private final Lexer lexer;

    public static void main(final String arg) {
    	main(new String[]{arg});
    }
    
    public static void main(final String[] args) {
    	final CommandLine c = new CommandLine(args);
        final Lexer lexer = new Lexer(c.readInputSpec());
        final FRecursiveDescentRecognizer r = new FRecursiveDescentRecognizer(lexer);
        r.recognize();
    }

    public FRecursiveDescentRecognizer(final Lexer lexer) {
        this.lexer = lexer;
    }

    public void recognize() {
        program();
    }

    void program() {
    	do {
    		formula();
    	} while (!lexer.inspectEOF());
        lexer.consumeEOF();
    }

    void formula() {
        var();
        lexer.consume("<=");
        expr();
        lexer.consume(";");
    }
    
    void expr() {
    	term();
    	
    } // TODO // TODO: replace this stub
    void term() {
    	while(!lexer.inspect(";") && !lexer.inspect(")") ) {
	    	if (lexer.inspect("and")) {
	    		lexer.consume("and");
	    	}
	    	else if (lexer.inspect("or")) {
	    		lexer.consume("or");
	    	}
	    	else {
	    		factor();
	    	}
    	}
    } // TODO // TODO: replace this stub
    void factor() { 
		if (peekConstant()) {
			constant();
		}
		else if (lexer.inspect("false")) {
			lexer.consume("false");
		}
		else if (lexer.inspect("true")) {
			lexer.consume("true");
		}
    	else if(lexer.inspect("(")) {
    		lexer.consume("(");
    		term();
    		lexer.consume(")");
    	}
    	else if (lexer.inspect("not")) {
    		lexer.consume("not");
    	}
		else {
			var();
		}
    }
    void var() {
		if (lexer.inspectID()) {
			lexer.consumeID();
		}
    } // TODO // TODO: replace this stub
    void constant() {
		if(peekConstant()) {
			lexer.consume("'");
		}
		if (lexer.inspect("1")) {
			lexer.consume("1");
		}
		else if (lexer.inspect("0")) {
			lexer.consume("0");
		}
		if(peekConstant()) {
			lexer.consume("'");
		}
		//return ConstantExpr.make(cons);
    } // TODO // TODO: replace this stub
    // helper functions
    private boolean peekConstant() {
        final boolean result = lexer.inspect("'"); //constants start (and end) with single quote
    	return result;
    }

}

