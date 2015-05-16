package com.kola.kmp.logic.npc.dialog;

import com.kola.kmp.logic.util.tips.GlobalTips;

/**
 * 对话框的按钮对象
 * 
 * @author PERRY
 * 
 */
public class KDialogButton {

	// 默认功能ID
	public static final short FUN_ID_CLOSE_DIALOG = -100;// 关闭对话框
	// 默认按钮名称：不能用final
	public static String CONFIRM_DISPLAY_TEXT = GlobalTips.确定;
	public static String CANCEL_DISPLAY_TEXT = GlobalTips.取消;
	// 默认按钮：不能用final
	public static KDialogButton CANCEL_BUTTON = new KDialogButton(FUN_ID_CLOSE_DIALOG, "", CANCEL_DISPLAY_TEXT);
	public static KDialogButton CONFIRM_BUTTON = new KDialogButton(FUN_ID_CLOSE_DIALOG, "", CONFIRM_DISPLAY_TEXT);

	// --------------------------------------------
	/**
	 * 功能id
	 */
	public final short funId;
	/**
	 * 脚本参数
	 */
	public final String script;
	/**
	 * 按钮显示的文本
	 */
	public final String displayText;

	public KDialogButton(short pFunId, String pScript, String pDisplayText) {
		this.funId = pFunId;
		this.script = pScript;
		this.displayText = pDisplayText;
	}
}
