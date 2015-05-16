package com.kola.kmp.logic.combat.state;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KCombatStateBaseImpl implements ICombatState {

	private static final int SECOND_TO_MILLIS = (int)TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);
//	private static final int CYC_ALLOW_MISTAKE = 300; // 结算周期buff的时候，允许与结束时间的误差
	
	protected static final int INDEX_LAST_TIME_MILLIS = 0;
	
	protected int stateTemplateId;
	protected String stateName;
	protected String stateDescr;
	protected int stateType;
	protected int stateIcon;
	protected int resId;
	protected int groupId;
	protected int level;
	
	protected int lastMillis; // 持续时间
	
	private long _startTimeMillis; // 开始时间
	protected long endTimeMillis;
	protected long preEndTimeMillis;
	protected boolean effective = true;
	
//	private List<Long> _cycTimeOrders;
	
	private void setEndTimes(long happenTime) {
		this._startTimeMillis = happenTime;
		this.preEndTimeMillis = this.endTimeMillis;
		this.endTimeMillis = this._startTimeMillis + lastMillis;
	}
	
	/**
	 * 
	 * 解析参数
	 * 
	 * @param paras
	 */
	protected abstract void parsePara(int[] paras);
	
	/**
	 * 
	 * 通知buff被添加到单位身上
	 * 
	 * @param combat
	 * @param actor
	 * @param happenTime
	 */
	protected abstract void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime);
	
	/**
	 * 
	 * 通知buff从单位身上移除
	 * 
	 * @param combat
	 * @param actor
	 * @param happenTime
	 */
	protected abstract void onRemoved(ICombat combat, ICombatSkillActor actor);
	
	/**
	 * 生成周期buff的时间序列
	 * 
	 * @param happenTime buff开始的时间
	 */
	protected void generateCycTimeOrders(ICombat combat, ICombatMember target, long happenTime) {
		if (this.isCycState()) {
			long tempHappenTime;
			if (preEndTimeMillis > 0) {
				tempHappenTime = preEndTimeMillis;
			} else {
				tempHappenTime = happenTime;
			}
			long time = tempHappenTime + SECOND_TO_MILLIS;
			long tempEndTime = endTimeMillis + 1; // +1，while里面就不用<=
			do {
//				this._cycTimeOrders.add(time);
				combat.addExtractOperation(new KStateDurationEffectOperation(time, target, this));
//				LOGGER.info("状态id：{}，添加执行时间：{}", this.getStateTemplateId(), time);
			} while ((time += SECOND_TO_MILLIS) < tempEndTime);
		}
	}
	
	protected void init(ICombatStateTemplate template) {
		this.stateTemplateId = template.getStateTemplateId();
		this.stateName = template.getStateName();
		this.stateDescr = template.getStateDesc();
		this.stateType = template.getStateType();
		this.stateIcon = template.getStateIcon();
		this.resId = template.getResId();
		this.groupId = template.getGroupId();
		this.level = template.getLevel();
		this.parsePara(template.getParas());
//		if(this.isCycState()) {
//			_cycTimeOrders = new LinkedList<Long>();
//		} else {
//			_cycTimeOrders = Collections.emptyList();
//		}
	}
	
	protected void setLastTimeMillis(int para) {
		if (para == -1) {
			this.lastMillis = Integer.MAX_VALUE;
		} else {
			this.lastMillis = (int) TimeUnit.MILLISECONDS.convert(para, TimeUnit.SECONDS);
		}
	}
	
	@Override
	public boolean isCycState() {
		return false;
	}
	
	@Override
	public long getStartTimeMillis() {
		return _startTimeMillis;
	}
	
	@Override
	public int getStateTemplateId() {
		return stateTemplateId;
	}
	
	@Override
	public int getIconResId() {
		return stateIcon;
	}
	
	@Override
	public int getGroupId() {
		return groupId;
	}
	
	@Override
	public int getLevel() {
		return level;
	}
	
	@Override
	public long getEndTime() {
		return endTimeMillis;
	}
	
	public final void notifyAdded(ICombat combat, ICombatMember target, long happenTime) {
		this.setEndTimes(happenTime);
		this.generateCycTimeOrders(combat, target,happenTime);
		this.onAdded(combat, target.getSkillActor(), happenTime);
	}
	
	@Override
	public final void notifyRemoved(ICombat combat, ICombatSkillActor actor) {
		this.effective = false;
		this.onRemoved(combat, actor);
	}
	
	@Override
	public void notifyExtend(ICombat combat, ICombatMember target, long happenTime) {
		this.setEndTimes(happenTime);
		if(this.isCycState()) {
//			this._cycTimeOrders.clear(); // 把这个buff剩余的执行时间清空，然后重置
			this.generateCycTimeOrders(combat, target, happenTime); // 重新生成执行时间序列
		}
	}
	
	@Override
	public boolean isTimeOut(long time) {
		return time > endTimeMillis;
	}
	
	@Override
	public boolean isEffective() {
		return effective;
	}
	
	@Override
	public final List<IOperation> getCycStateOperation(ICombat combat, ICombatMember target, long start, long end) {
//		end = end + CYC_ALLOW_MISTAKE; // 允许偏差300毫秒？
//		if (this.isCycState()) {
//			List<IOperation> list = new ArrayList<IOperation>();
//			Long time;
//			for (Iterator<Long> itr = this._cycTimeOrders.iterator(); itr.hasNext();) {
//				time = itr.next();
//				if (time < end) {
//					list.add(new KStateDurationEffectOperation(time, target, this));
//					itr.remove();
//				} else {
//					break;
//				}
//			}
//			return list;
//		}
		return null;
	}
}
