package com.kola.kmp.logic.other;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.util.UtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KRandomModel<T> {

	private int _totalRate;
	private T _defaultKey;
	private List<KRandomEntry<T>> _rateList;
	
	public KRandomModel(Map<T, Integer> pRateMap, T pDefaultKey) {
		_rateList = new ArrayList<KRandomModel.KRandomEntry<T>>(pRateMap.size());
		for(Iterator<Map.Entry<T, Integer>> itr = pRateMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry<T, Integer> current = itr.next();
			_rateList.add(new KRandomEntry<T>(current.getKey(), current.getValue()));
		}
		this._totalRate = _rateList.get(_rateList.size() - 1).rate;
		this._defaultKey = pDefaultKey;
	}
	
	public T getRandomKey() {
		int actualRate = UtilTool.random(_totalRate);
		KRandomEntry<T> model;
		for (int i = 0; i < _rateList.size(); i++) {
			model = _rateList.get(i);
			if (actualRate < model.rate) {
				return model.key;
			}
		}
		return _defaultKey;
	}
	
	public static class KRandomEntry<T> implements Comparable<KRandomEntry<T>>{
		public final T key;
		public final int rate;

		public KRandomEntry(T pKey, int pRate) {
			this.key = pKey;
			this.rate = pRate;
		}

		@Override
		public int compareTo(KRandomEntry<T> o) {
			return this.rate < o.rate ? -1 : 1;
		}
	}
}
