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

package ece351.f.parboiled;

import org.parboiled.Rule;

import ece351.common.ast.AndExpr;
import ece351.common.ast.AssignmentStatement;
import ece351.common.ast.ConstantExpr;
import ece351.common.ast.Constants;
import ece351.common.ast.Expr;
import ece351.common.ast.NotExpr;
import ece351.common.ast.OrExpr;
import ece351.common.ast.VarExpr;
import ece351.f.ast.FProgram;
import ece351.util.CommandLine;

// Parboiled requires that this class not be final
public /*final*/ class FParboiledParser extends FBase implements Constants {

	
	public static void main(final String[] args) {
    	final CommandLine c = new CommandLine(args);
    	final String input = c.readInputSpec();
    	final FProgram fprogram = parse(input);
    	assert fprogram.repOk();
    	final String output = fprogram.toString();
    	
    	// if we strip spaces and parens input and output should be the same
    	if (strip(input).equals(strip(output))) {
    		// success: return quietly
    		return;
    	} else {
    		// failure: make a noise
    		System.err.println("parsed value not equal to input:");
    		System.err.println("    " + strip(input));
    		System.err.println("    " + strip(output));
    		System.exit(1);
    	}
    }
	
	private static String strip(final String s) {
		return s.replaceAll("\\s", "").replaceAll("\\(", "").replaceAll("\\)", "");
	}
	
	public static FProgram parse(final String inputText) {
		final FProgram result = (FProgram) process(FParboiledParser.class, inputText).resultValue;
		assert result.repOk();
		return result;
	}

	@Override
	public Rule Program() {
		return Sequence(
				push(new FProgram()),
				Sequence(
						OneOrMore(Sequence(Formula(), push(((FProgram)pop()).append(pop())))), 
						W0(), 
						EOI
						)
		);
	}

	
	public Rule Formula() {
		return Sequence(
				Var(),W0(), "<=", W0(),Expr(),swap(),
				push(new AssignmentStatement((VarExpr)pop(),(Expr)pop())),swap(),
				W0(), ";", W0()
		);
	}

	public Rule Expr() {
		return Sequence(
				Term(),
				ZeroOrMore(
						Sequence(W0(), "or", W0(),Term(),swap(),push(new OrExpr((Expr)pop(),(Expr)pop())))
				)
		);
	}

	public Rule Term() {
		return Sequence(
				Factor(),
				ZeroOrMore(
						Sequence(W0(), "and", W0(),Factor(),swap(),push(new AndExpr((Expr)pop(),(Expr)pop())))
					)
				);
	}
	//------------------------------------------------------------------------------------------------------------
	public Rule Factor() {
		return FirstOf(notFactor(), Sequence("(", W0(), Expr(), W0(), ")"), Trueconst(), Falseconst(), Var(), Constant());
	}

	public Rule Var() {
		return Sequence(
				Sequence(
						FirstOf(CharRange('A', 'Z'), CharRange('a', 'z')), 
						ZeroOrMore(FirstOf(FirstOf(CharRange('A', 'Z'), CharRange('a', 'z')), CharRange('0', '9'), '_'))
						),
				push(new VarExpr(match()))
		);
	}
	public Rule notFactor() {return Sequence(W0(),"not", W0(), Factor(), push(new NotExpr((Expr)pop())));}
	public Rule Trueconst() {return Sequence(W0(),"true", push(ConstantExpr.make(true))) ;}
	public Rule Falseconst() {return Sequence(W0(),"false", push(ConstantExpr.make(false))) ;}
	public Rule Constant() {return Sequence(W0(), "'", FirstOf("0","1"), push(ConstantExpr.make(match())), "'");}
	//----------------------------------------------------------------------------------------------------------
	// sub method for the Factor
}
