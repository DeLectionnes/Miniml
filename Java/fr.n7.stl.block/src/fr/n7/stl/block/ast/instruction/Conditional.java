/**
 * 
 */
package fr.n7.stl.block.ast.instruction;

import java.util.Optional;

import fr.n7.stl.block.ast.Block;
import fr.n7.stl.block.ast.SemanticsUndefinedException;
import fr.n7.stl.block.ast.expression.Expression;
import fr.n7.stl.block.ast.scope.Declaration;
import fr.n7.stl.block.ast.scope.HierarchicalScope;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;
import fr.n7.stl.block.ast.type.*;

/**
 * Implementation of the Abstract Syntax Tree node for a conditional instruction.
 * @author Marc Pantel
 *
 */
public class Conditional implements Instruction {

	protected Expression condition;
	protected Block thenBranch;
	protected Block elseBranch;

	public Conditional(Expression _condition, Block _then, Block _else) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = _else;
	}

	public Conditional(Expression _condition, Block _then) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "if (" + this.condition + " )" + this.thenBranch + ((this.elseBranch != null)?(" else " + this.elseBranch):"");
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndBackwardResolve(HierarchicalScope<Declaration> _scope) {
		boolean result_condition = this.condition.collectAndBackwardResolve(_scope);
		boolean result_then = this.thenBranch.collectAndBackwardResolve(_scope);
		if (this.elseBranch != null) {
			boolean result_else = this.elseBranch.collectAndBackwardResolve(_scope);
			return result_condition && result_else && result_then;
		}
		return result_condition && result_then;
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#resolve(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean fullResolve(HierarchicalScope<Declaration> _scope) {
		boolean result_condition = this.condition.fullResolve(_scope);
		boolean result_then = this.thenBranch.fullResolve(_scope);
		if (this.elseBranch != null) {
			boolean result_else = this.elseBranch.fullResolve(_scope);
			return result_condition && result_else && result_then;
		}
		return result_condition && result_then;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#checkType()
	 */
	@Override
	public boolean checkType() {
		boolean condition_result = this.condition.getType().compatibleWith(AtomicType.BooleanType);
		boolean then_result = this.thenBranch.checkType();
		if (this.elseBranch != null) {
			boolean else_result = this.elseBranch.checkType();
			if (!(condition_result && then_result && else_result)) {
				Logger.error("Error : Type");
				return false;
			}
			return true;
		}
		if (!(condition_result && then_result)) {
			Logger.error("Error : Type");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#allocateMemory(fr.n7.stl.tam.ast.Register, int)
	 */
	@Override
	public int allocateMemory(Register _register, int _offset) {
		this.thenBranch.allocateMemory(_register, _offset);
		if (elseBranch != null) {
			this.elseBranch.allocateMemory(_register, _offset);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = _factory.createFragment();
		_result.append(this.condition.getCode(_factory));
		_result.append(this.thenBranch.getCode(_factory));
		if (elseBranch != null) {
			_result.append(this.elseBranch.getCode(_factory));
		}
		return _result;
	}

}
