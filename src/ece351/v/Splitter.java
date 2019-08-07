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

package ece351.v;

import java.util.LinkedHashSet;
import java.util.Set;

import org.parboiled.common.ImmutableList;

import ece351.common.ast.AndExpr;
import ece351.common.ast.AssignmentStatement;
import ece351.common.ast.ConstantExpr;
import ece351.common.ast.EqualExpr;
import ece351.common.ast.Expr;
import ece351.common.ast.NAndExpr;
import ece351.common.ast.NOrExpr;
import ece351.common.ast.NaryAndExpr;
import ece351.common.ast.NaryOrExpr;
import ece351.common.ast.NotExpr;
import ece351.common.ast.OrExpr;
import ece351.common.ast.Statement;
import ece351.common.ast.VarExpr;
import ece351.common.ast.XNOrExpr;
import ece351.common.ast.XOrExpr;
import ece351.common.visitor.PostOrderExprVisitor;
import ece351.util.CommandLine;
import ece351.v.ast.Architecture;
import ece351.v.ast.DesignUnit;
import ece351.v.ast.IfElseStatement;
import ece351.v.ast.Process;
import ece351.v.ast.VProgram;

/**
 * Process splitter.
 */
public final class Splitter extends PostOrderExprVisitor {
	private final Set<String> usedVarsInExpr = new LinkedHashSet<String>();

	public static void main(String[] args) {
		System.out.println(split(args));
	}
	
	public static VProgram split(final String[] args) {
		return split(new CommandLine(args));
	}
	
	public static VProgram split(final CommandLine c) {
		final VProgram program = DeSugarer.desugar(c);
        return split(program);
	}
	
	public static VProgram split(final VProgram program) {
		VProgram p = Elaborator.elaborate(program);
		final Splitter s = new Splitter();
		return s.splitit(p);
	}

	private VProgram splitit(final VProgram program) {
					// Determine if the process needs to be split into multiple processes
						// Split the process if there are if/else statements so that the if/else statements only assign values to one pin
// TODO: longer code snippet
		VProgram result = new VProgram();
		for(DesignUnit units : program.designUnits){
			ImmutableList<Statement> a = ImmutableList.<Statement>of();
			Architecture b = units.arch;
			for(Statement s : b.statements){
				if (s instanceof AssignmentStatement) {
					a = a.append(s);
				}
				else{
					for(Statement s1 : ((Process)s).sequentialStatements){
						if(s1 instanceof AssignmentStatement){
							Process p = new Process();
							p = p.setSensitivityList(((Process)s).sensitivityList);
							p = p.appendStatement(s1);
							a = a.append(p);
						}
						else{
							ImmutableList<Statement> r1 = splitIfElseStatement((IfElseStatement)s1);
							for(Statement p1 : r1){
								a = a.append(p1);
							}
						}
					}
				}
			}
			result = result.append(units.setArchitecture(b.varyStatements(a)));
		}
		return result;
//throw new ece351.util.Todo351Exception();
	}
	
	// You do not have to use this helper method, but we found it useful
	
	private ImmutableList<Statement> splitIfElseStatement(final IfElseStatement ifStmt) {
		ImmutableList<Statement> result = ImmutableList.<Statement>of();
		for(Statement s_if : ifStmt.ifBody){
			// loop over each statement in the ifBody
			for(Statement s_else :ifStmt.elseBody){
				// loop over each statement in the elseBody
				if(((AssignmentStatement)s_if).outputVar.equals(((AssignmentStatement)s_else).outputVar)){
					// check if outputVars are the same
					usedVarsInExpr.clear();
					// initialize/clear this.usedVarsInExpr
					traverseExpr(((AssignmentStatement)s_if).expr);
					traverseExpr(((AssignmentStatement)s_else).expr);
					traverseExpr(ifStmt.condition);
					// call traverse a few times to build up this.usedVarsInExpr
					Process p = new Process();
					for(String s : usedVarsInExpr){
						p = p.appendSensitivity(s);
						// build sensitivity list from this.usedVarsInExpr
					}
					ImmutableList<AssignmentStatement> f1 = ImmutableList.<AssignmentStatement>of();
					ImmutableList<AssignmentStatement> f2 = ImmutableList.<AssignmentStatement>of();
					f1 = f1.append((AssignmentStatement)s_else);
					f2 = f2.append((AssignmentStatement)s_if);
					p = p.appendStatement(new IfElseStatement(f1, f2, ifStmt.condition));
					result = result.append(p);
					// build the resulting list of split statements
				}
			}
		}
		return result;
		// return result
// TODO: longer code snippet
	}

	@Override
	public Expr visitVar(final VarExpr e) {
		this.usedVarsInExpr.add(e.identifier);
		return e;
	}

	// no-ops
	@Override public Expr visitConstant(ConstantExpr e) { return e; }
	@Override public Expr visitNot(NotExpr e) { return e; }
	@Override public Expr visitAnd(AndExpr e) { return e; }
	@Override public Expr visitOr(OrExpr e) { return e; }
	@Override public Expr visitXOr(XOrExpr e) { return e; }
	@Override public Expr visitNAnd(NAndExpr e) { return e; }
	@Override public Expr visitNOr(NOrExpr e) { return e; }
	@Override public Expr visitXNOr(XNOrExpr e) { return e; }
	@Override public Expr visitEqual(EqualExpr e) { return e; }
	@Override public Expr visitNaryAnd(NaryAndExpr e) { return e; }
	@Override public Expr visitNaryOr(NaryOrExpr e) { return e; }

}
