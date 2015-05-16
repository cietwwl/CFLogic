package com.kola.kmp.logic.map;

import com.koala.game.exception.KGameServerException;

public class KGameMapModuleException extends KGameServerException{
	
	public KGameMapModuleException(String message, Throwable cause) {
		super(message, cause);
	}



	public KGameMapModuleException(String message) {
		super(message);
	}

}
