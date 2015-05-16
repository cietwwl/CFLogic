package com.kola.kmp.logic.rank.abs;


/**
 * <pre>
 * DB中排行榜类型的ID分配
 * 1~10用于角色排行榜
 * 11~20用于军团排行榜
 * 21~N用于军团资源争夺不同城市的竞价榜
 * 
 * @author CamusHuang
 * @creation 2014-2-20 下午4:57:51
 * </pre>
 */
public interface RankTypeInterface {

	public String name();
	/**
	 * <pre>
	 * 标识数值
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-13 上午10:05:10
	 * </pre>
	 */
	public int getSign();

	public int getMaxLen();

	public int getJoinMinLv();

	/**
	 * <pre>
	 * 保存路径
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-13 上午10:05:05
	 * </pre>
	 */
	public String getSaveDirPath();

	/**
	 * <pre>
	 * 保存文件名称
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-13 上午10:05:19
	 * </pre>
	 */
	public String getSaveFileName();

	/**
	 * <pre>
	 * 保存文件后缀
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-13 上午10:05:32
	 * </pre>
	 */
	public String getSaveFileNameSuffix();

}