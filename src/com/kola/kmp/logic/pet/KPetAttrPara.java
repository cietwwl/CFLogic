package com.kola.kmp.logic.pet;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetAttrPara {

	public final int maxHpPara;
	public final int atkPara;
	public final int defPara;
	public final int hitRatingPara;
	public final int critRatingPara;
	public final int dodgeRatingPara;
	public final int resilienceRatingPara;
	public final int defIgnoreParaPara;
	public final int faintResistRatingPara;
	
	public KPetAttrPara(KGameExcelRow row) {
		this.maxHpPara = row.getInt("maxHp");
		this.atkPara = row.getInt("atk");
		this.defPara = row.getInt("def");
		this.hitRatingPara = row.getInt("hitRating");
		this.critRatingPara = row.getInt("critRating");
		this.dodgeRatingPara = row.getInt("dodgeRating");
		this.resilienceRatingPara = row.getInt("resilienceRating");
		this.defIgnoreParaPara = row.getInt("defIgnore");
		this.faintResistRatingPara = row.getInt("faintResistRating");
	}
}
