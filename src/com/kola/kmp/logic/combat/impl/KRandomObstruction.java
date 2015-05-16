package com.kola.kmp.logic.combat.impl;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KRandomObstruction {
	
	public final int levelId;
	private int[] _obstructionTemplateIds;
	private int _rate;
	private KObstructionPosistion[] coords;
	
	
	KRandomObstruction(KGameExcelRow row) {
		this.levelId = row.getInt("levelId");
		this._obstructionTemplateIds = UtilTool.getStringToIntArray(row.getData("obstructionId"), ",");
		this._rate = row.getInt("rate");
		String[] strCoords = row.getData("coords").split(";");
		coords = new KObstructionPosistion[strCoords.length];
		String[] singleCoord;
		for (int i = 0; i < strCoords.length; i++) {
			singleCoord = strCoords[i].split(",");
			coords[i] = new KObstructionPosistion(Float.parseFloat(singleCoord[0]), Float.parseFloat(singleCoord[1]));
		}
	}
	
	KObstructionPosistion getRandomPosistion() {
		return coords[UtilTool.random(coords.length)];
	}
	
	boolean validateShow() {
		return UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT) <= _rate;
	}
	
	int getRandomObstructionTemplateId() {
		return _obstructionTemplateIds[UtilTool.random(_obstructionTemplateIds.length)];
	}
	
	static class KObstructionPosistion {
		
		public final float _x;
		public final float _y;

		public KObstructionPosistion(float pX, float pY) {
			this._x = pX;
			this._y = pY;
		}
	}
}
