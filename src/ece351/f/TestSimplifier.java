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

package ece351.f;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ece351.f.ast.FProgram;
import ece351.f.rdescent.FRecursiveDescentParser;
import ece351.util.BaseTest351;
import ece351.util.CommandLine;
import ece351.util.ExaminableProperties;
import ece351.util.TestInputs351;

/**
 * These are the old simplifier tests that depend on your F parser.
 * We recommend that you use the new TestSimplifier2 instead, because
 * it does not depend on your F parser.
 */
@RunWith(Parameterized.class)
public final class TestSimplifier extends BaseTest351 {

	private final File f;

	public TestSimplifier(final File f) {
		this.f = f;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> files() {
		return TestInputs351.formulaFiles("opt.*");
	}

	@Test
	public void simplify() throws IOException {
		final String inputSpec = f.getAbsolutePath();
		final CommandLine c = new CommandLine("-h", "-o4", inputSpec);
		final String input = c.readInputSpec();
		System.out.println("processing " + inputSpec);
		System.out.println("input: ");
		System.out.println(input);
		
		// parse from the file to construct first AST
		final FProgram original = FRecursiveDescentParser.parse(input);
		System.out.println("input after parsing + pretty-printing:");
		System.out.println(original.toString());
		final FProgram simplified = original.simplify();
		System.out.println("ouput: ");
		System.out.println(simplified.toString());
		
		// check that the two ASTs are NOT isomorphic (the optimization should have done something)
		assertFalse("ASTs do not differ for " + inputSpec, original.isomorphic(simplified));

		// ok, something has happened
		// was it the right thing?
		final String s = File.separator;
		final String staffSpec = inputSpec.replace( s + "f" + s,
				s + "f" + s + "staff.out" + s + "simplified" + s );
		
		final CommandLine c3 = new CommandLine(staffSpec);
		final FProgram staff = FRecursiveDescentParser.parse(staffSpec).simplify();
		System.out.println("expected output:");
		System.out.println(staff.toString());
		assertTrue("ASTs differ for " + inputSpec, simplified.isomorphic(staff));

		// check equivalence
		assertTrue(original.equivalent(staff));
		assertTrue(original.equivalent(simplified));
		
		// check examinable sanity
		ExaminableProperties.checkAllUnary(original);
		ExaminableProperties.checkAllUnary(simplified);
		ExaminableProperties.checkAllUnary(staff);
		ExaminableProperties.checkAllBinary(original, simplified);
		ExaminableProperties.checkAllBinary(original, staff);
		ExaminableProperties.checkAllBinary(simplified, staff);
		ExaminableProperties.checkAllTernary(original, simplified, staff);
		
		// success!
		System.out.println("success!  " + inputSpec);
	}

}
