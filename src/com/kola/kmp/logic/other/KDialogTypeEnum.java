package com.kola.kmp.logic.other;


/**
 * <pre>
 * 对话框类型定义
 * 
 * @author CamusHuang
 * @creation 2014-3-11 下午3:01:00
 * </pre>
 */
public enum KDialogTypeEnum {
	取消等待(0), //
	简单对话框(1), //
	带功能键的对话框(2), //
	浮动文字提示(4), //
	浮动数据提示(5), //
	单选框(6), //
	;

	public final int sign;

	private KDialogTypeEnum(int sign) {
		this.sign = sign;
	}
}
