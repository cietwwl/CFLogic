package com.kola.kmp.logic.mission.assistant;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.other.KNPCOrderEnum;

public class KAssistantDataStruct {

	
	public static class MajorAssistantData{
		public int id;
		public String title;
		public int iconId;
		public List<Integer> idList;
		public List<MinorAssistantData> minDataList;
		
		public MajorAssistantData(int id, String title, int iconId,String idData) {
			super();
			this.id = id;
			this.title = title;
			this.iconId = iconId;
			this.idList = new ArrayList<Integer>();
			this.minDataList = new ArrayList<MinorAssistantData>();
			String[] ids = idData.split(",");
			for (int i = 0; i < ids.length; i++) {
				idList.add(Integer.parseInt(ids[i]));
			}
		}
	}
	
	public static class MinorAssistantData{
		public int id;
		public String title;
		public String produceTips;
		public int iconId;
		public KNPCOrderEnum npcOrder;
		public int starIconId;
		
		public MinorAssistantData(int id, String title, String produceTips,
				int iconId, KNPCOrderEnum npcOrder,int starIconId) {
			super();
			this.id = id;
			this.title = title;
			this.produceTips = produceTips;
			this.iconId = iconId;
			this.npcOrder = npcOrder;
			this.starIconId = starIconId;
		}
	}
}
