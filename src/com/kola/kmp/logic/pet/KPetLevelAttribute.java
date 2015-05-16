package com.kola.kmp.logic.pet;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.other.KPetType;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetLevelAttribute {

	public final KPetType type;
	public final KPetQuality quality;
	public final int atk;
	public final int def;
	public final int maxHp;
	public final int hitRating;
	public final int dodgeRating;
	public final int critRating;
	public final int resilienceRating;
	
	public KPetLevelAttribute(KGameExcelRow row) {
		this.type = KPetType.getPetType(row.getInt("type"));
		this.quality = KPetQuality.getEnumQuality(row.getInt("quality"));
		this.atk = row.getInt("atk");
		this.def = row.getInt("def");
		this.maxHp = row.getInt("maxHp");
		this.hitRating = row.getInt("hitRating");
		this.dodgeRating = row.getInt("dodgeRating");
		this.critRating = row.getInt("critRating");
		this.resilienceRating = row.getInt("resilienceRating");
	}
}
