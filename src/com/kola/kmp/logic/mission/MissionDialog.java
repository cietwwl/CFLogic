package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.mission.MissionReward.MissionItemRewardTemplate;

/**
 * 任务对白数据类
 * 
 * @author zhaizl
 * 
 */
public class MissionDialog {
	// //任务对话编号
	// private int missionDialogId;
	// //任务对话类型
	// private MissionDialogType dialogtype;
	// 任务对话内容的分段对话列表
	private List<Dialogue> dialogueList = new ArrayList<MissionDialog.Dialogue>();

	public Map<Byte, String> collectItemDialogContentMap = new HashMap<Byte, String>();

	public static final byte default_job = 0;

	// public int getMissionDialogId() {
	// return missionDialogId;
	// }

	// public MissionDialogType getDialogtype() {
	// return dialogtype;
	// }

	public List<Dialogue> getDialogueList() {
		return dialogueList;
	}

	public void setDialogueList(List<Dialogue> dialogueList) {
		this.dialogueList = dialogueList;
	}

	public MissionDialog constructCollectItemAcceptDialog(byte job) {
		MissionDialog dialog = new MissionDialog();
		for (int i = 0; i < dialogueList.size(); i++) {
			Dialogue tempD = dialogueList.get(i);
			Dialogue dl = new Dialogue(tempD.content, tempD.question,
					tempD.hasQuestion, tempD.type);
			if (tempD.itemRewardTemplate != null) {
				dl.itemRewardTemplate.addAll(tempD.itemRewardTemplate);
			}
			if (i == (dialogueList.size() - 1)) {
				dl.content += this.collectItemDialogContentMap.get(job);
			}
			dialog.dialogueList.add(dl);
		}
		return dialog;
	}

	/**
	 * 表示某一段对话的内容
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class Dialogue {

		public final static byte DIALOGUE_TYPE_NORMAL = 1;

		public final static byte DIALOGUE_TYPE_MISSION_TIPS = 2;

		private String question;
		private String content;
		private boolean hasQuestion;
		private byte type;
		private List<MissionItemRewardTemplate> itemRewardTemplate;

		public Dialogue(String content, String question, boolean hasQuestion,
				byte type) {
			this.question = question;
			this.content = content;
			this.hasQuestion = hasQuestion;
		}

		/**
		 * 一段对话中的问题
		 * 
		 * @return
		 */
		public String getQuestion() {
			return question;
		}

		/**
		 * 一段对话的内容
		 * 
		 * @return
		 */
		public String getContent(byte occupationType) {
			if (type == DIALOGUE_TYPE_MISSION_TIPS) {
				for (MissionItemRewardTemplate mrTemplate : itemRewardTemplate) {
					content += "  "
							+ mrTemplate.getRewardItemTemplate(occupationType).name
							+ "x" + mrTemplate.rewardCount + "  ";
				}
			}
			return content;
		}

		/**
		 * 该段对话是否有询问的问题
		 * 
		 * @return
		 */
		public boolean isHasQuestion() {
			return hasQuestion;
		}

		public List<MissionItemRewardTemplate> getItemRewardTemplate() {
			return itemRewardTemplate;
		}

		public void setItemRewardTemplate(
				List<MissionItemRewardTemplate> itemRewardTemplate) {
			this.itemRewardTemplate = itemRewardTemplate;
		}
	}

	// public static enum MissionDialogType{
	//
	// MISSION_PROLOGUE_DIALOG(1,"任务开场对白"),MISSION_ACCEPT_DIALOG(2,"接受任务对白"),
	// MISSION_COMPLETED_DIALOG(3,"完成任务对白"),MISSION_UNCOMPLETED_DIALOG(4,"未完成任务对白");
	//
	//
	// private final byte dialogType;
	// private final String typeName;
	// private MissionDialogType(int dialogType, String typeName) {
	// this.dialogType = (byte)dialogType;
	// this.typeName = typeName;
	// }
	//
	// // 所有枚举
	// private static final Map<Byte, MissionDialogType> enumMap = new
	// HashMap<Byte, MissionDialogType>();
	// static {
	// MissionDialogType[] enums = MissionDialogType.values();
	// MissionDialogType type;
	// for (int i = 0; i < enums.length; i++) {
	// type = enums[i];
	// enumMap.put(type.dialogType, type);
	// }
	// }
	//
	// /**
	// * <pre>
	// * 通过标识数值获取枚举对象
	// *
	// * @param type
	// * @return
	// * @creation 2012-12-3 下午3:53:28
	// * </pre>
	// */
	// public static MissionDialogType getEnum(byte type) {
	// return enumMap.get(type);
	// }
	//
	//
	// }

}
