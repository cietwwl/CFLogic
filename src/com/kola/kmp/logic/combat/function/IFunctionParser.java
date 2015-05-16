package com.kola.kmp.logic.combat.function;

import java.util.List;
/**
 * 
 * @author PERRY CHAN
 */
public interface IFunctionParser {

	/**
	 * 
	 * 
	 * @param args
	 * @return
	 */
	public IFunctionExecution parse(List<Integer> args);
}
