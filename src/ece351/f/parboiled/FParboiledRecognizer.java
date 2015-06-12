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

package ece351.f.parboiled;

import org.parboiled.Rule;

import ece351.util.CommandLine;
import ece351.vhdl.VConstants;

//Parboiled requires that this class not be final
public /*final*/ class FParboiledRecognizer extends FBase implements VConstants {

	
	public static void main(final String... args) {
		final CommandLine c = new CommandLine(args);
    	process(FParboiledRecognizer.class, c.readInputSpec());
    }

	@Override
	public Rule Program() {
		// STUB: return NOTHING; // TODO: replace this stub
		// For the grammar production Id, ensure that the Id does not match any of the keywords specified
		// in the rule, 'Keyword'
// TODO: 39 lines snipped
		return OneOrMore(Formula());
			
	}
	//double check spaces could be result of error
	public Rule Formula(){
		return Sequence(Var(),"<=",W0(),Expr(),";",W0());
	}
	
	public Rule Expr(){
		return Sequence(Term(),ZeroOrMore(Sequence(OR(),W0(),Term())));
	}
	
	public Rule Term(){
		return Sequence(Factor(),ZeroOrMore(Sequence(AND(),W0(),Factor())));
	}
	
	public Rule Factor(){
		return FirstOf(Sequence(NOT(),W0(),Factor()),  Sequence("(",W0(),Expr(),")",W0()),   Var(),   Constant());
	}
	
	public Rule Constant(){
		return FirstOf(Sequence("'0'",W0()),Sequence("'1'",W0()));
	}
	
	public Rule Var(){
		return Sequence(
				TestNot(Keyword()),
				Char(),
				ZeroOrMore(FirstOf(Char(),Digit())),
				W0()
				);
	}
}
