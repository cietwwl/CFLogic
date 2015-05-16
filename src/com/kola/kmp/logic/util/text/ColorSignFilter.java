package com.kola.kmp.logic.util.text;

import com.koala.game.util.KGameExcelTable.FrontStringFilter;

public class ColorSignFilter implements FrontStringFilter {

	
	@Override
	public String filter(String value) {
		String result= HyperTextTool.replaseCx(value);
		//
		return result.replace("{@r}", '\n'+"");
	}

}
