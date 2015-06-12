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

package ece351.f.rdescent;

import java.util.Arrays;

import ece351.util.CommandLine;
import ece351.util.Lexer;
import ece351.vhdl.VConstants;

public final class FRecursiveDescentRecognizer implements VConstants {
   
    private final Lexer lexer;

    public static void main(final String arg) {
    	main(new String[]{arg});
    }
    
    public static void main(final String[] args) {
    	final CommandLine c = new CommandLine(args);
        final Lexer lexer = new Lexer(c.readInputSpec());
        //System.out.println(Arrays.asList(args));
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
    
    void expr(){
    	term();
    	while(lexer.inspect(VConstants.OR)){
    		lexer.consume(VConstants.OR);
    		term();
   		}
    } 
  
    void term(){
    	factor();
    	while(lexer.inspect(VConstants.AND)){
    		lexer.consume(VConstants.AND);
    		factor();
    	}
    }
	
	void factor() {
		if(lexer.inspect(VConstants.NOT)){
			lexer.consume(VConstants.NOT);
			factor();
		} else if(lexer.inspect("(")){
			lexer.consume("(");
			expr();
			lexer.consume(")");
		} else if (peekConstant()){
			constant();
		} else if(lexer.inspectID()) var();
	}
	
	void var(){ 
			lexer.consumeID();
	}
	void constant() {
		lexer.consume("'");
		lexer.consume("0","1");
		lexer.consume("'");
	}

    // helper functions
    private boolean peekConstant() {
        final boolean result = lexer.inspect("'"); //constants start (and end) with single quote
    	return result;
    }

}

