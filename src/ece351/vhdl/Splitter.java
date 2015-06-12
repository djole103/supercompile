/* *********************************************************************
 * ECE351 
 * Department of Electrical and Computer Engineering 
 * University of Waterloo 
 * Term: Winter 2015 (1151)
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

package ece351.vhdl;

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
import ece351.common.ast.VarExpr;
import ece351.common.ast.XNOrExpr;
import ece351.common.ast.XOrExpr;
import ece351.common.visitor.PostOrderExprVisitor;
import ece351.util.CommandLine;
import ece351.util.Examiner;
import ece351.vhdl.ast.Architecture;
import ece351.vhdl.ast.DesignUnit;
import ece351.vhdl.ast.IfElseStatement;
import ece351.vhdl.ast.Process;
import ece351.vhdl.ast.Statement;
import ece351.vhdl.ast.VProgram;

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
// TODO: 35 lines snipped
		VProgram splitted = new VProgram();
		ImmutableList<Statement> processes = ImmutableList.of();
		for(DesignUnit du : program.designUnits){
			for(Statement st : du.arch.statements){
				if(st instanceof Process){
					for(Statement sqst : ((Process)st).sequentialStatements){
						if(sqst instanceof IfElseStatement){
							for(Statement s : splitIfElseStatement((IfElseStatement)sqst)){
								processes = processes.append(s);
							}
						} else {
							processes = processes.append((Process)st);
							break;
						}
					}
				} else {
					processes = processes.append(st);
				}
			}
			du = du.setArchitecture(du.arch.varyStatements(processes));
			splitted = splitted.append(du);
			processes = ImmutableList.of();
		}
		return splitted;
	}
	
	// You do not have to use this helper method, but we found it useful
	
	private ImmutableList<Statement> splitIfElseStatement(final IfElseStatement ifStmt) {
		// loop over each statement in the ifBody
			// loop over each statement in the elseBody
				// check if outputVars are the same
					// initialize/clear this.usedVarsInExpr
					// call traverse a few times to build up this.usedVarsInExpr
					// build sensitivity list from this.usedVarsInExpr
					// build the resulting list of split statements
		// return result
// TODO: 34 lines snipped
		IfElseStatement ifelse; 
		ImmutableList<Statement> Sprocesses = ImmutableList.of();
		ImmutableList<String> sensitivityL;
		for(AssignmentStatement asIF : ifStmt.ifBody){
			for(AssignmentStatement asELSE : ifStmt.elseBody){
				if(asIF.outputVar.equivalent(asELSE.outputVar)){
					this.usedVarsInExpr.clear();
					this.traverseAssignmentStatement(asIF);
					this.traverseAssignmentStatement(asELSE);
					this.traverseExpr(ifStmt.condition);
					sensitivityL = ImmutableList.of();
					for(String s : usedVarsInExpr){
						sensitivityL = sensitivityL.append(s);
					}
					ifelse = new IfElseStatement(ifStmt.condition);
					ifelse = ifelse.appendToTrueBlock(asIF);
					ifelse = ifelse.appendToElseBlock(asELSE);
					Process proc = new Process(ImmutableList.of((Statement)ifelse),sensitivityL);
					Sprocesses = Sprocesses.append(proc);
				}
			}
		}
		return Sprocesses;
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
