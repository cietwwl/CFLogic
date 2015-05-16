package com.kola.kmp.logic.npc.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.other.KDialogTypeEnum;
import com.kola.kmp.protocol.npc.KNpcProtocol;

/**
 * <pre>
 * 客户端点击功能NPC的功能项
 * </pre>
 */
public class KShowDialogMsg implements KNpcProtocol {

	public static KGameMessage createSimpleDialogMsg(String title, String content, boolean center, byte time) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.简单对话框.sign);
		msg.writeUtf8String(UtilTool.getNotNullString(title));
		msg.writeUtf8String(UtilTool.getNotNullString(content));
		msg.writeBoolean(center);
		msg.writeByte(time);
		return msg;
	}

	public static KGameMessage createFunMsg(String title, String content, boolean center, byte time, List<KDialogButton> buttons) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.带功能键的对话框.sign);
		msg.writeUtf8String(UtilTool.getNotNullString(title));
		msg.writeUtf8String(UtilTool.getNotNullString(content));
		msg.writeBoolean(center);
		msg.writeByte(time);
		msg.writeByte(buttons.size());
		KDialogButton button;
		for (int i = 0; i < buttons.size(); i++) {
			button = buttons.get(i);
			msg.writeShort(button.funId);
			msg.writeUtf8String(UtilTool.getNotNullString(button.displayText));
			msg.writeUtf8String(UtilTool.getNotNullString(button.script));
		}
		return msg;
	}

	/**
	 * <pre>
	 * 创建一条UI风格（区别于NPC风格）的单选对话框消息
	 * 
	 * @param title
	 * @param content
	 * @param center
	 * @param time
	 * @param buttons
	 * @return
	 * @author CamusHuang
	 * @creation 2013-8-26 上午11:22:09
	 * </pre>
	 */
	public static KGameMessage createUIRatioDialogMsg(String title, String content, boolean center, byte time, List<KDialogButton> buttons) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.单选框.sign);
		msg.writeUtf8String(UtilTool.getNotNullString(title));
		msg.writeUtf8String(UtilTool.getNotNullString(content));
		msg.writeBoolean(center);
		msg.writeByte(time);
		msg.writeByte(buttons.size());
		KDialogButton button;
		for (int i = 0; i < buttons.size(); i++) {
			button = buttons.get(i);
			msg.writeShort(button.funId);
			msg.writeUtf8String(UtilTool.getNotNullString(button.displayText));
			msg.writeUtf8String(UtilTool.getNotNullString(button.script));
		}
		return msg;
	}

	public static KGameMessage createNullDialogMsg() {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.取消等待.sign);
		return msg;
	}

	public static KGameMessage createUprisingDialogMsg(String... content) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.浮动文字提示.sign);
		msg.writeByte(content.length);
		for (int i = 0; i < content.length; i++) {
			msg.writeUtf8String(UtilTool.getNotNullString(content[i]));
		}
		return msg;
	}

	public static KGameMessage createUprisingDialogMsg(List<String> content) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.浮动文字提示.sign);
		msg.writeByte(content.size());
		for (String temp : content) {
			msg.writeUtf8String(UtilTool.getNotNullString(temp));
		}
		return msg;
	}

	public static KGameMessage createDataUprisingDialogMsg(String... content) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.浮动数据提示.sign);
		msg.writeByte(content.length);
		for (int i = 0; i < content.length; i++) {
			msg.writeUtf8String(UtilTool.getNotNullString(content[i]));
		}
		return msg;
	}

	public static KGameMessage createDataUprisingDialogMsg(List<String> content) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_SHOW_DIALOG);
		msg.writeByte(KDialogTypeEnum.浮动数据提示.sign);
		msg.writeByte(content.size());
		for (String tips : content) {
			msg.writeUtf8String(UtilTool.getNotNullString(tips));
		}
		return msg;
	}	
}