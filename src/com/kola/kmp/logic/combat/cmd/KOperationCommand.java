package com.kola.kmp.logic.combat.cmd;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public class KOperationCommand implements ICombatCommand {

	private ICombat _combat;
	private final List<IOperation> _operations;

	KOperationCommand(ICombat pCombat, List<IOperation> opList) {
		this._combat = pCombat;
		this._operations = new ArrayList<IOperation>(opList);
	}

	@Override
	public void execute() {
		IOperationResult result;
		IOperation operation;
		for (int i = 0; i < _operations.size(); i++) {
			operation = _operations.get(i);
			try {
				result = operation.executeOperation(_combat);
				if (result != null) {
					_combat.addOperationResult(result);
				}
				_combat.afterOneOperationExecuted(operation.getOperationTime());
			} catch (Exception e) {
				ICombat.LOGGER.error("执行op出现异常！op类型：{}", operation.getClass().getName(), e);
				continue;
			}
		}
	}

}
