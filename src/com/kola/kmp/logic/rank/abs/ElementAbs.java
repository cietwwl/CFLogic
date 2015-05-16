package com.kola.kmp.logic.rank.abs;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;

/**
 * <pre>
 * 排行榜元素
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午6:37:36
 * </pre>
 */
public abstract class ElementAbs implements Comparable<ElementAbs>, Cloneable {

	public final static Logger _LOGGER = KGameLogger.getLogger(ElementAbs.class);

	private final static String Str_elementId = "eId";
	private final static String Str_elementName = "eName";
	private final static String Str_elementLv = "eLv";
	private final static String Str_rank = "rank";

	protected int rank;
	public final long elementId;
	public final String elementName;
	protected int elementLv;
	

	public ElementAbs(long elementId, String elementName, int elementLv) {
		this.elementId = elementId;
		this.elementName = elementName;
		this.elementLv = elementLv;
	}

	public ElementAbs(DBRank db,JSONObject jsonCA) {
		this.rank = db.getRank();
		this.elementId = db.getElementId();
		try {
			this.elementName = jsonCA.getString(Str_elementName);
			this.elementLv = jsonCA.getInt(Str_elementLv);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	protected void saveToXML(Element element) {
		element.setAttribute(Str_rank, String.valueOf(rank));
		element.setAttribute(Str_elementId, String.valueOf(elementId));
		element.setAttribute(Str_elementName, elementName);
		element.setAttribute(Str_elementLv, String.valueOf(elementLv));
	}
	
	protected void saveToDB(DBRank db,JSONObject jsonCA) throws JSONException {
		db.setRank(rank);
		db.setElementId(elementId);
		jsonCA.put(Str_elementName, elementName);
		jsonCA.put(Str_elementLv, elementLv);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},elementId={},elementName={},elementLv={},", rank, elementId, elementName, elementLv);
	}

	public int getElementLv() {
		return elementLv;
	}

	public void setElementLv(int elementLv) {
		this.elementLv = elementLv;
	}

	public int getRank() {
		return rank;
	}

	/**
	 * <pre>
	 * 排名是否有变更
	 * 
	 * @param rank
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 下午12:52:41
	 * </pre>
	 */
	public boolean setRank(int rank) {
		if (this.rank != rank) {
			this.rank = rank;
			return true;
		}
		return false;
	}
	
	/**
	 * <pre>
	 * 根据双方是否新晋，当前排名决定双方的排序
	 * 
	 * @param element
	 * @return
	 * @author CamusHuang
	 * @creation 2014-7-31 下午6:40:00
	 * </pre>
	 */
	protected int compareFinal(ElementAbs element){
		{
			if(element.getRank()==0){
				// 对方是新晋
				if(rank == 0){
					// 均是新晋
					return 0;
				} else {
					// 本人不是新晋，本人优先
					return -1;
				}
			} else {
				// 对方不是新晋
				if(rank == 0){
					// 本人是新晋，对方优先
					return 1;
				} else {
					// 均不是新晋
					if(rank < element.getRank()){
						// 本人靠前，本人优先
						return -1;
					} else if(rank > element.getRank()){
						// 本人靠后，对方优先
						return 1;
					} else {
						return 0;
					}
				}
			}
		}
	}	

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
