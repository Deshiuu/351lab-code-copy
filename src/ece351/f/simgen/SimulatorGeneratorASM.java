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

package ece351.f.simgen;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import ece351.common.ast.AndExpr;
import ece351.common.ast.AssignmentStatement;
import ece351.common.ast.BinaryExpr;
import ece351.common.ast.ConstantExpr;
import ece351.common.ast.EqualExpr;
import ece351.common.ast.Expr;
import ece351.common.ast.NAndExpr;
import ece351.common.ast.NOrExpr;
import ece351.common.ast.NaryAndExpr;
import ece351.common.ast.NaryOrExpr;
import ece351.common.ast.NotExpr;
import ece351.common.ast.OrExpr;
import ece351.common.ast.VarExpr;
import ece351.common.ast.XNOrExpr;
import ece351.common.ast.XOrExpr;
import ece351.common.visitor.PostOrderExprVisitor;
import ece351.f.analysis.DepthCounter;
import ece351.f.analysis.DetermineInputVars;
import ece351.f.ast.FProgram;

/**
 * Generate Jasmin assembler code to simulate an F program.
 * Only generate the methods that compute the signal values.
 * The test harness will copy in the bytecodes for the main method
 * from the code generated in the previous lab.
 */
public final class SimulatorGeneratorASM extends PostOrderExprVisitor {

	/**
	 * Maps input variable names for the current AssignmentStatement to their
	 * Java argument index position.
	 * 
	 * @see DetermineInputVars
	 */
	private Map<String,Integer> vars;
	
	/** 
	 * Where to write the output. Use the provided utility methods to get indenting.
	 * 
	 * @see #indent()
	 * @see #outdent()
	 * @see #println(String, String)
	 * @see #println(String)
	 */
	private PrintWriter out = new PrintWriter(System.out);
	
	/** 
	 * The current level of indenting.
	 * 
	 * @see #indent()
	 * @see #outdent()
	 */
	private String indent = "";

	public SimulatorGeneratorASM() {
		super();
	}

	public void generate(final String fName, final FProgram program, final PrintWriter out) {
		this.out = out;
		final String cleanFName = fName.replace('-', '_');
		final String baseName = "Simulator_" + cleanFName;
		final String derivedName = baseName + "_asm";

		// class declaration
		println(".class public " + derivedName);
		println(".super java/lang/Object");
		indent();
		
		// default constructor
		// each method
// TODO: longer code snippet
throw new ece351.util.Todo351Exception();
		// do not print ".end class" --- Jasmin will reject the file
		out.flush();
	}
	
	public void generate(final AssignmentStatement stmt) {
		// determine the input variables for this stmt
		// build up vars map
		// local vars start indexing at 0 for static methods (1 for instance methods)
		// save our vars map
		// method signature
		// limits on locals and operand stack
		// @see DepthCounter
		// evaluate expression as integers
		// normalize final int to boolean
		// end method
// TODO: longer code snippet
throw new ece351.util.Todo351Exception();
	}

	@Override
	public Expr visitConstant(final ConstantExpr e) {
			// true is one
			// false is zero
// TODO: short code snippet
throw new ece351.util.Todo351Exception();
		return e;
	}

	@Override
	public Expr visitVar(final VarExpr e) {
		// there are special instructions iload_0 to iload_3
		// for the first four local vars
		// after that use the general iload instruction
// TODO: short code snippet
throw new ece351.util.Todo351Exception();
		return e;
	}

	/**
	 * Two possible approaches:
	 * 1. Use conditionals (ifs) to check if the value is zero or non-zero.
	 * 2. Arithmetic. First normalize the value on the stack, 
	 *    then do a bitwise exclusive or (ixor) to toggle between zero and one.
	 */
	@Override public Expr visitNot(final NotExpr e) {
// TODO: short code snippet
throw new ece351.util.Todo351Exception();
		return e; 
	}


	private final NormalizationStrategy normStrategy = new ConditionalNormalizationStrategy();
//	private final NormalizationStrategy normStrategy = new ArithmeticNormalizationStrategy();
	
	private abstract class NormalizationStrategy {
		
		private int lastNormalizedDepth = 0;
		private static final int THRESHOLD = 5;
		private final DepthCounter depthCounter = new DepthCounter();
		
		final void normalizeIfNecessary(BinaryExpr e) {
// TODO: short code snippet
throw new ece351.util.Todo351Exception();
		}

		
		/**
		 * Normalize is a function that converts non-zero positive
		 * integers to 1, and leaves zero as zero.
		 * You could implement it with conditionals (ifs) or by
		 * arithmetic: 2x/(x+1). Whatever your strategy,
		 * implement it in Java assembly.
		 * Normalize might requires some stack space, so you will need to
		 * take that into account in the DepthCounter.
		 *
		 * @see DepthCounter#measureUnaryExpr(ece351.common.ast.UnaryExpr, int)
		 * @see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html
		 * @see https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings
		 */
		abstract void normalize();
	}
	
	private final class ConditionalNormalizationStrategy extends NormalizationStrategy {

		private final AtomicInteger labelID = new AtomicInteger();
		
		@Override
		void normalize() {
// TODO: longer code snippet
throw new ece351.util.Todo351Exception();
		}
		
	}

	private final class ArithmeticNormalizationStrategy extends NormalizationStrategy {

		@Override
		void normalize() {
// TODO: short code snippet
throw new ece351.util.Todo351Exception();
		}
		
	}
	
	@Override public Expr visitAnd(final AndExpr e) {
// TODO: short code snippet
throw new ece351.util.Todo351Exception();
		return e;
	}

	@Override public Expr visitOr(final OrExpr e) {
// TODO: short code snippet
throw new ece351.util.Todo351Exception();
		return e;
	}

	@Override public Expr visitNaryAnd(final NaryAndExpr e) { throw new UnsupportedOperationException(); }
	@Override public Expr visitNaryOr(final NaryOrExpr e) { throw new UnsupportedOperationException(); }
	@Override public Expr visitNOr(final NOrExpr e) { throw new UnsupportedOperationException(); }
	@Override public Expr visitXOr(final XOrExpr e) { throw new UnsupportedOperationException(); }
	@Override public Expr visitXNOr(final XNOrExpr e) { throw new UnsupportedOperationException(); }
	@Override public Expr visitNAnd(final NAndExpr e) { throw new UnsupportedOperationException(); }
	@Override public Expr visitEqual(final EqualExpr e) { throw new UnsupportedOperationException(); }

	private void println(final String s, final String comment) {
		out.print(indent);
		out.print(s);
		for (int i = s.length(); i < 20; i++) {
			out.print(" ");
		}
		out.print("; ");
		out.println(comment);
	}
	
	private void println(final String s) {
		out.print(indent);
		out.println(s);
	}

	private void indent() {
		indent = indent + "    ";
	}
	
	private void outdent() {
		indent = indent.substring(0, indent.length() - 4);
	}

}
