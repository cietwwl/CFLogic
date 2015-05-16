package com.kola.kmp.logic.other;

/**
 * 
 * @author PERRY CHAN
 */
public class KActionResult<T> {

	public boolean success;
	public String tips;
	public T attachment;
	
	public KActionResult(){
		
	}

	public KActionResult(boolean success, String tips) {
		super();
		this.success = success;
		this.tips = tips;
	}

	public KActionResult(boolean success, String tips, T attachment) {
		super();
		this.success = success;
		this.tips = tips;
		this.attachment = attachment;
	}

}
