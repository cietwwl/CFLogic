package com.kola.kmp.logic.npc.dialog;

import com.koala.game.player.KGamePlayerSession;

/**
 * <pre>
 * 此接口用于响应客户端的对话框按钮点击
 * 
 * 使用者可以使用默认处理器{@link KDefaultDialogProcesser}
 * 也可以实现本接口后，到 {@link KDialogService#onGameWorldInitComplete()}中进行硬代码注册
 * 
 * @author CamusHuang
 * @creation 2014-2-26 下午4:31:51
 * </pre>
 */
public abstract class IDialogProcesser {

	// 功能ID段
	private short minFunId;
	private short maxFunId;// 不包含

	public IDialogProcesser(short minFunId, short maxFunId) {
		this.minFunId = minFunId;
		this.maxFunId = maxFunId;
	}

	public boolean isMyFunId(short funId) {
		if (funId >= minFunId && funId < maxFunId) {
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 
	 * @param funId 功能ID，各处理器在注册时规定ID段
	 * @param script 脚本参数，各处理器内部自由定义管理，互不冲突
	 * @param session
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-26 下午4:30:36
	 * </pre>
	 */
	public abstract void processFun(short funId, String script, KGamePlayerSession session);
}
