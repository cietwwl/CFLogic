package com.kola.kmp.logic.other;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;

/**
 * KGame职业枚举，标识每个职业
 * 
 * @author PERRY CHAN
 */
public enum KJobTypeEnum {

	/**
	 * 职业：<strong>突击战士</strong>
	 */
	WARRIOR((byte)1, "突击战士", ""),
	/**
	 * 职业：<strong>暗影特工</strong>
	 */
	SHADOW((byte)2, "暗影特工", ""),
	/**
	 * 职业：<strong>枪械师</strong>
	 */
	GUNMAN((byte)3, "枪械师", ""),
	;

	private static final Map<Byte, KJobTypeEnum> _JOB_MAP = new HashMap<Byte, KJobTypeEnum>();
	private static List<KJobTypeEnum> _alljob = null;
	static{
		KJobTypeEnum[] alljobs = KJobTypeEnum.values();
		for (int i = alljobs.length; i-- > 0;) {
			KJobTypeEnum job = alljobs[i];
			_JOB_MAP.put(job.getJobType(), job);
		}
		_alljob = Arrays.asList(KJobTypeEnum.values()); // asList is
		// unmodifiedable
	}

	public static void init(KGameExcelRow[] rows) {

		KGameExcelRow row;
		int type;
		for (int i = 0; i < rows.length; i++) {
			row = rows[i];
			type = row.getInt("type");
			KJobTypeEnum job = _JOB_MAP.get(type);
			if (job == null) {
				throw new NullPointerException("加载职业的时候，发现配置文件有，但是枚举找不到！");
			} else {
				job._jobName = row.getData("name");
				job._descr = row.getData("descr");
			}
		}
	}

	private byte _jobType;
	private String _jobName;
	private String _descr;

	private KJobTypeEnum(byte pJob, String pJobName, String pDescr) {
		this._jobType = pJob;
		this._jobName = pJobName;
		this._descr = pDescr;
	}

	public byte getJobType() {
		return this._jobType;
	}

	public String getJobName() {
		return this._jobName;
	}

	public String getDescr() {
		return this._descr;
	}

	public static String getJobName(byte job) {
		KJobTypeEnum jobEnum = _JOB_MAP.get(job);
		if (jobEnum != null) {
			return jobEnum.getJobName();
		} else {
			return "";
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param job
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-5 上午11:36:33
	 * </pre>
	 */
	public static KJobTypeEnum getJob(byte job) {
		return _JOB_MAP.get(job);
	}
	
	public static KJobTypeEnum getJobByName(String name) {
		KJobTypeEnum job;
		KJobTypeEnum[] arrays = values();
		for (int i = 0; i < arrays.length; i++) {
			job = arrays[i];
			if (job.getJobName().equals(name)) {
				return job;
			}
		}
		return null;
	}

	/**
	 * 获取所有的职业枚举
	 * 
	 * @return
	 */
	public static List<KJobTypeEnum> getAllJobReadOnly() {
		return _alljob;
	}
}
