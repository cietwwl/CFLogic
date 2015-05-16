package com.kola.kmp.logic.mission.guide;

public class NoviceGuideRecord {
	
	public static int max_step = 19;

	public boolean isCompleteGuide;
	public int guideStep;
	public boolean isCompleteFirstGuideBattle;

	public NoviceGuideRecord() {
	}

	public NoviceGuideRecord(boolean isCompleteGuide, int guideStep) {
		this.isCompleteGuide = isCompleteGuide;
		this.guideStep = guideStep;
	}

	public void decode(String data) {
		String[] datas;
		if (data != null && (datas = data.split(",")).length == 3) {
			guideStep = Integer.parseInt(datas[2]);
			isCompleteGuide = datas[0].equals("true")||datas[0].equals("1");
			isCompleteFirstGuideBattle = datas[1].equals("true")||datas[1].equals("1");
		}
	}

	public String encode() {
		return (isCompleteGuide?1:0)+","+(isCompleteFirstGuideBattle?1:0)+ "," +guideStep;
	}

}
