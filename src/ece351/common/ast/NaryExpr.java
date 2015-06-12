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

package ece351.common.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.parboiled.common.ImmutableList;

import ece351.util.Examinable;
import ece351.util.Examiner;

/**
 * An expression with multiple children. Must be commutative.
 */
public abstract class NaryExpr extends Expr {

	public final ImmutableList<Expr> children;

	public NaryExpr(final Expr... exprs) {
		Arrays.sort(exprs);
		ImmutableList<Expr> c = ImmutableList.of();
		for (final Expr e : exprs) {
			c = c.append(e);
		}
    	this.children = c;
	}
	
	public NaryExpr(final List<Expr> children) {
		final ArrayList<Expr> a = new ArrayList<Expr>(children);
		Collections.sort(a);
		this.children = ImmutableList.copyOf(a);
	}

	/**
	 * Each subclass must implement this factory method to return
	 * a new object of its own type. 
	 */
	public abstract NaryExpr newNaryExpr(final List<Expr> children);

	/**
	 * Construct a new NaryExpr (of the appropriate subtype) with 
	 * one extra child.
	 * @param e the child to append
	 * @return a new NaryExpr
	 */
	public NaryExpr append(final Expr e) {
		return newNaryExpr(children.append(e));
	}

	/**
	 * Construct a new NaryExpr (of the appropriate subtype) with 
	 * the extra children.
	 * @param list the children to append
	 * @return a new NaryExpr
	 */
	public NaryExpr appendAll(final List<Expr> list) {
		final List<Expr> a = new ArrayList<Expr>(children.size() + list.size());
		a.addAll(children);
		a.addAll(list);
		return newNaryExpr(a);
	}

	/**
	 * Check the representation invariants.
	 */
	public boolean repOk() {
		// programming sanity
		assert this.children != null;
		// should not have a single child: indicates a bug in simplification
		assert this.children.size() > 1 : "should have more than one child, probably a bug in simplification";
		// check that children is sorted
		int i = 0;
		for (int j = 1; j < this.children.size(); i++, j++) {
			final Expr x = this.children.get(i);
			assert x != null : "null children not allowed in NaryExpr";
			final Expr y = this.children.get(j);
			assert y != null : "null children not allowed in NaryExpr";
			assert x.compareTo(y) <= 0 : "NaryExpr.children must be sorted";
		}
		// no problems found
		return true;
	}

	/**
	 * The name of the operator represented by the subclass.
	 * To be implemented by each subclass.
	 */
	public abstract String operator();
	
	/**
	 * The complementary operation: NaryAnd returns NaryOr, and vice versa.
	 */
	abstract protected Class<? extends NaryExpr> getThatClass();
	

	/**
     * e op x = e for absorbing element e and operator op.
     * @return
     */
	public abstract ConstantExpr getAbsorbingElement();

    /**
     * e op x = x for identity element e and operator op.
     * @return
     */
	public abstract ConstantExpr getIdentityElement();


	@Override 
    public final String toString() {
    	final StringBuilder b = new StringBuilder();
    	b.append("(");
    	int count = 0;
    	for (final Expr c : children) {
    		b.append(c);
    		if (++count  < children.size()) {
    			b.append(" ");
    			b.append(operator());
    			b.append(" ");
    		}
    		
    	}
    	b.append(")");
    	return b.toString();
    }


	@Override
	public final int hashCode() {
		return 17 + children.hashCode();
	}

	@Override
	public final boolean equals(final Object obj) {
		if (!(obj instanceof Examinable)) return false;
		return examine(Examiner.Equals, (Examinable)obj);
	}
	
	@Override
	public final boolean isomorphic(final Examinable obj) {
		return examine(Examiner.Isomorphic, obj);
	}
	
	private boolean examine(final Examiner e, final Examinable obj) {
		// basics
		if (obj == null) return false;
		if (!this.getClass().equals(obj.getClass())) return false;
		final NaryExpr that = (NaryExpr) obj;
		
		// if the number of children are different, consider them not equivalent
		// since the n-ary expressions have the same number of children and they are sorted, just iterate and check
		// supposed to be sorted, but might not be (because repOk might not pass)
		// if they aren't the same elements in the same order return false
		// no significant differences
// TODO: 16 lines snipped
		if(this.children.size()!=that.children.size())return false;
		if(!e.orderedExamination(Examiner.Equals, this.children,that.children)) return false;
		return true;
	}

	
	@Override
	protected final Expr simplifyOnce() {
		assert repOk();
		final Expr result = 
				simplifyChildren().
				mergeGrandchildren().
				removeIdentityElements().
				removeDuplicates().
				simpleAbsorption().
				subsetAbsorption().
				singletonify();
		assert result.repOk();
		return result;
	}
	

	private NaryExpr simplifyChildren() {
		final ImmutableList<Expr> emptyList = ImmutableList.of();
		NaryExpr result = newNaryExpr(emptyList);
		for (final Expr e : children) {
			result = result.append(e.simplify());
		}
		// note: we do not assert repOk() here because the rep might not be ok
		// the result might contain duplicate children, and the children
		// might be out of order
		return result;
	}

	
	private NaryExpr mergeGrandchildren() {
		// extract children to merge using filter (because they are the same type as us)
		// remove children to merge from result by using filter
		// merge in the grandchildren
// TODO: 10 lines snipped
		NaryExpr filteredChildren = this.filter(this.getClass(),true);
		NaryExpr result = this.removeAll(filteredChildren.children, Examiner.Isomorphic);
		for(Expr child : filteredChildren.children){
			final NaryExpr nchild = (NaryExpr)child;
			for(Expr grandchild : nchild.children){
			result = result.append(grandchild);
			}
		}
		return result;
	}


    private NaryExpr removeDuplicates() {
		// remove duplicate children: x.x=x and x+x=x
		// since children are sorted this is fairly easy
    		// no changes
    		// removed some duplicates
// TODO: 20 lines snipped
    	ImmutableList<Expr> children = ImmutableList.of();
    	children = children.append(this.children.get(0));
    	for(int i = 1; i< this.children.size(); i++){
    		if(this.children.get(i).equals(this.children.get(i-1))){
    			continue; 
    		}
    		children = children.append(this.children.get(i));
    	}
    	return newNaryExpr(children);
    }

	private NaryExpr removeIdentityElements() {
    	// if we have only one child stop now and return self
    	// we have multiple children, remove the identity elements
    		// all children were identity elements, so now our working list is empty
    		// return a new list with a single identity element
    		// normal return
// TODO: 12 lines snipped
		if(this.children.size()==1)return this;
		NaryExpr identities = this.filter(getIdentityElement(),Examiner.Equals,true);
		NaryExpr result = this.removeAll(identities.children, Examiner.Equals);
		if(result.children.size()== 0){
			ImmutableList<Expr> single = ImmutableList.of();
			return newNaryExpr(single.append(getIdentityElement()));
		}
		return result;
}		

    private NaryExpr simpleAbsorption() {
		// (x.y) + x ... = x ...
		// check if there are any conjunctions that can be removed
// TODO: 21 lines snipped
    	if(this.children.size()==1) return this;
    	ImmutableList<Expr> conjs = ImmutableList.of();
    	for(Expr expr : this.children){
    		if(expr instanceof VarExpr){
    			for(Expr conj : this.children){
    				if(conj instanceof NaryExpr && ((NaryExpr)conj).contains(expr, Examiner.Equals))
    					conjs = conjs.append(conj);
    			}
    		}
    	}
    	return this.removeAll(conjs, Examiner.Equals);
	}

	private NaryExpr subsetAbsorption() {
		// check if there are any conjunctions that are supersets of others
		// e.g., ( a . b . c ) + ( a . b ) = a . b
// TODO: 30 lines snipped
		ImmutableList<Expr> conjs = ImmutableList.of();
    	for(Expr expr : this.children){
    		if(expr instanceof NaryExpr){
    			for(Expr conj : this.children){
    				if(conj instanceof NaryExpr && !conj.equals(expr)){
    					if(((NaryExpr)conj).children.containsAll(((NaryExpr) expr).children))
    						conjs = conjs.append(conj);
    				}
    			}
    		}
    	}
    	return this.removeAll(conjs, Examiner.Equals);
		/*for(Expr expr : this.children){
    		if(expr instanceof NaryExpr){
    			for(Expr conj : this.children){
    				if(conj instanceof NaryExpr && !((NaryExpr) conj).examine(Examiner.Equals,(NaryExpr)expr)){
    					Boolean match = true;
    					for(Expr var : ((NaryExpr)expr).children){
    						if(!((NaryExpr)conj).contains(var, Examiner.Equals)){
    							match = false;
    							break;
    						}
    					if(match) conjs = conjs.append(conj);
    					}
    				}
    			}
    		}
    	}
    	return this.removeAll(conjs, Examiner.Equals);*/
	}

	private Expr singletonify() {
		// if we have only one child, return it
		// absorbing element: 0.x=0 and 1+x=1
		// collapse complements
		// !x . x . ... = 0 and !x + x + ... = 1
		// x op !x = absorbing element
		// find all negations
		// for each negation, see if we find its complement
				// found matching negation and its complement)
				// return absorbing element
		// nothing to do, return self
// TODO: 24 lines snipped
		if(this.children.size()==1) return this.children.get(0);
		if(this.contains(getAbsorbingElement(), Examiner.Equals)) return getAbsorbingElement();
		for(Expr child : this.children){
			if(child instanceof NotExpr){
				if(this.contains(((NotExpr)child).expr,Examiner.Equals)) return getAbsorbingElement();
			}
		}
		return this;
	}
	/**
	 * Return a new NaryExpr with only the children of a certain type, 
	 * or excluding children of a certain type.
	 * @param filter
	 * @param shouldMatchFilter
	 * @return
	 */
	public final NaryExpr filter(final Class<? extends Expr> filter, final boolean shouldMatchFilter) {
		ImmutableList<Expr> l = ImmutableList.of();
		for (final Expr child : children) {
			if (child.getClass().equals(filter)) {
				if (shouldMatchFilter) {
					l = l.append(child);
				}
			} else {
				if (!shouldMatchFilter) {
					l = l.append(child);
				}
			}
		}
		return newNaryExpr(l);
	}

	public final NaryExpr filter(final Expr filter, final Examiner examiner, final boolean shouldMatchFilter) {
		ImmutableList<Expr> l = ImmutableList.of();
		for (final Expr child : children) {
			if (examiner.examine(child, filter)) {
				if (shouldMatchFilter) {
					l = l.append(child);
				}
			} else {
				if (!shouldMatchFilter) {
					l = l.append(child);
				}
			}
		}
		return newNaryExpr(l);
	}

	public final NaryExpr removeAll(final List<Expr> toRemove, final Examiner examiner) {
		NaryExpr result = this;
		for (final Expr e : toRemove) {
			result = result.filter(e, examiner, false);
		}
		return result;
	}

	public final boolean contains(final Expr expr, final Examiner examiner) {
		for (final Expr child : children) {
			if (examiner.examine(child, expr)) {
				return true;
			}
		}
		return false;
	}

}
