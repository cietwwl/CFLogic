package com.kola.kmp.logic.combat.cmd;


/**
 * 
 * <pre>
 * 战斗指令的抽象，战场里面每一个与客户端交互的行为都会被
 * 封装为一个指令，然后会被丢到“指令池”里面去，等待循环执行
 * </pre>
 * 
 * @author PERRY CHAN
 */
public interface ICombatCommand {

	/**
	 * 
	 * 执行指令
	 * 
	 * @param combat
	 */
	public void execute();
}
