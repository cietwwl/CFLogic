package com.kola.kmp.logic.chat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;

/**
 * <pre>
 * 世界播报的类型
 * 本类型主要用于统一约束世界播报的发送范围
 * 
 * 本类的枚举类型，发送内容等由于不是经常修改，所以暂由技术与策划同步维护。
 * 
 * 加载文件：res/gsconfig/broatcast.xls
 * 以表格为准，搜索本类实例进行映射赋值。
 * 
 * 变更：先修改res/gsconfig/broatcast.xls，再修改本类实例或字段，即可。
 * 
 * @author CamusHuang
 * @creation 2013-1-14 上午10:37:36
 * </pre>
 */
public final class KWordBroadcastType {

	public final static KWordBroadcastType 装备_恭喜x角色将x装备强化到x级 = new KWordBroadcastType("恭喜{}将{}强化到{}级！！！"); // 任意装备强化至60级
	public final static KWordBroadcastType 装备_恭喜x角色将x装备升至x星 = new KWordBroadcastType("恭喜{}将{}升至{}星！可喜可贺！"); // 任意装备升星至30星
	
	public final static KWordBroadcastType 庄园_x角色在庄园内收获金色果实获得了x物品x数量 = new KWordBroadcastType("{}在庄园内收获金色果实，获得了{}x{}"); // 使用钻石收获金色果实收获（非钻石收获不进行播报）

	public final static KWordBroadcastType 随从_x角色获得了一个红色随从x = new KWordBroadcastType("{}获得了一个红色随从{}！"); // 获得任意红色随从

	public final static KWordBroadcastType 竞技榜_x角色问鼎大元帅 = new KWordBroadcastType("恭喜{}以积分第一问鼎大元帅"); // 获得“大元帅”的玩家全服特殊滚条公告
	public final static KWordBroadcastType 竞技榜_恭喜x角色获得x职位 = new KWordBroadcastType("恭喜{}获得{}"); // 玩家获得竞技场职位（大元帅除外）时进行公告
	
	public final static KWordBroadcastType 军团_x角色创建了军团x = new KWordBroadcastType("{}创建了军团{}！");
	
	public final static KWordBroadcastType 军团战_军团战将于x时间开启快去报名吧 = new KWordBroadcastType("【军团战】将于{}开启，快去报名吧，丰厚的奖励等着你哦！");
	public final static KWordBroadcastType 军团战_由于第1场军团战不满足分组要求本周军团战取消 = new KWordBroadcastType("由于第1场【军团战】不满足分组要求，本周【军团战】取消！");
	public final static KWordBroadcastType 军团战_军团战入围军团已确认 = new KWordBroadcastType("【军团战】入围军团已确认，请进入军团战界面查看");
	public final static KWordBroadcastType 军团战_第x场军团战将于x分钟后开启 = new KWordBroadcastType("第{}场【军团战】将于{}分钟后开启"); // 每场军团战开始前30分钟时会进行世界播报
	public final static KWordBroadcastType 军团战_第x场军团战将于x分钟后开启请各参战军团做好准备 = new KWordBroadcastType("第{}场【军团战】将于{}分钟后开启，请各参战军团做好准备"); // 每场军团战开始前5分钟时会进行世界播报，共5场
	public final static KWordBroadcastType 军团战_第x场军团战将于x秒后开始 = new KWordBroadcastType("第{}场【军团战】将于{}秒后开始"); // 每场军团战开始前1分钟时会进行世界播报
	public final static KWordBroadcastType 军团战_第x场军团战正式开始请尽快入场 = new KWordBroadcastType("第{}场【军团战】正式开始，还未到场的参战人员请尽快入场"); // 每场军团战活动开始，共5场
	public final static KWordBroadcastType 军团战_第x场军团战开始入场 = new KWordBroadcastType("第{}场【军团战】开始入场");
	public final static KWordBroadcastType 军团战_第x场军团战现在开始 = new KWordBroadcastType("第{}场【军团战】现在开始");
	public final static KWordBroadcastType 军团战_x角色已经连杀5人 = new KWordBroadcastType("{}已经连杀5人，勇猛无双"); // 连续击杀敌方成员数量超过5人时会在本场景内进行播报，军团战场景播报
	public final static KWordBroadcastType 军团战_x角色已经连杀10人 = new KWordBroadcastType("{}已经连杀10人，妖孽般的杀戮"); // 连续击杀敌方成员数量超过10人时会在本场景内进行播报，军团战场景播报
	public final static KWordBroadcastType 军团战_x角色已经连杀20人 = new KWordBroadcastType("{}已经连杀20人，如神一般"); // 连续击杀敌方成员数量超过20人时会在本场景内进行播报，军团战场景播报
	public final static KWordBroadcastType 军团战_x角色的连杀被x角色终结了 = new KWordBroadcastType("{}的连杀被{}终结了"); // 连续杀敌数量超过5人后被人击杀时在本场景内进行播报，军团战场景播报
	public final static KWordBroadcastType 军团战_第x场军团战军团x战胜军团x恭喜军团x = new KWordBroadcastType("经过一场鏖战，第{}场【军团战】{}艰难的战胜{}。让我们恭喜军团{}！");
	public final static KWordBroadcastType 军团战_第x场军团战结束 = new KWordBroadcastType("第{}场【军团战】结束");
	public final static KWordBroadcastType 军团战_恭喜x军团在本周军团战中夺得冠军 = new KWordBroadcastType("恭喜军团{}在本届【军团战】中以绝对实力夺得冠军！"); // 军团战最后一场结束
	public final static KWordBroadcastType 军团战_本周军团战完满结束 = new KWordBroadcastType("本周【军团战】完满结束");
	
	public final static KWordBroadcastType 资源战_军团资源战将于x时间开启快去竞价吧 = new KWordBroadcastType("【资源争夺】将于{}开启，快去竞价吧，丰厚的奖励等着你哦！");
	public final static KWordBroadcastType 资源战_军团资源战竞价成功军团名单现已公布 = new KWordBroadcastType("【资源争夺】竞价成功军团名单现已公布，请到活动界面进行查看"); // 参赛名单公布后即周日0点开始，会每隔1小时进行世界播报，直至活动开始前1小时停止
	public final static KWordBroadcastType 资源战_军团资源战将于x分钟后开启 = new KWordBroadcastType("本次【资源争夺】将于{}分钟后开启");
	public final static KWordBroadcastType 资源战_军团资源战将于x分钟后开启请各参战军团做好准备 = new KWordBroadcastType("本次【资源争夺】将于{}分钟后开启，请各参战军团做好准备"); // 每次资源争夺战在开始前5分钟时会进行世界播报
	public final static KWordBroadcastType 资源战_军团资源战将于x秒后开启 = new KWordBroadcastType("本次【资源争夺】将于{}秒后开启");
	public final static KWordBroadcastType 资源战_军团资源战正式开始请尽快入场 = new KWordBroadcastType("本次【资源争夺】正式开始，还未到场的参战人员请尽快入场"); 
	public final static KWordBroadcastType 资源战_军团资源战开始入场 = new KWordBroadcastType("本次【资源争夺】开始入场");
	public final static KWordBroadcastType 资源战_军团资源战将于x分钟后结束 = new KWordBroadcastType("本次【资源争夺】将于{}分钟后结束");
	public final static KWordBroadcastType 资源战_军团资源战致胜关键 = new KWordBroadcastType("将对方成员杀死或挑战对方BOSS获得积分有助得胜！");
	public final static KWordBroadcastType 资源战_x军团在本次资源争夺活动中战胜x军团成功夺得x城市控制权 = new KWordBroadcastType("军团{}在本次【资源争夺】活动中战胜{}军团，成功夺得【{}】的控制权"); // 当一个城市的争夺战结束时，系统会进行世界播报
	public final static KWordBroadcastType 资源战_x军团在本次资源争夺活动中成功夺得x城市控制权 = new KWordBroadcastType("军团{}在本次【资源争夺】活动中成功夺得【{}】的控制权"); // 当一个城市的争夺战结束时，系统会进行世界播报
	public final static KWordBroadcastType 资源战_军团资源战完满结束 = new KWordBroadcastType("本周【资源争夺】完满结束");
	
	public final static KWordBroadcastType 丧尸攻城_当前距离丧尸攻城活动开启还有x分钟 = new KWordBroadcastType("当前距离丧尸攻城活动开启还有{}分钟"); // 距离活动开启剩余5分钟的情况下播报
	public final static KWordBroadcastType 丧尸攻城_恭喜x角色成功击杀x怪物 = new KWordBroadcastType("恭喜{}成功击杀{}"); // 丧尸攻城中击杀任意怪物时，丧尸攻城场景播报
	public final static KWordBroadcastType 丧尸攻城_恭喜x角色在本次活动中对丧尸攻城造成了x点伤害的恐怖输出排名第一 = new KWordBroadcastType("恭喜{}在本次活动中对丧尸攻城造成了{}点伤害的恐怖输出，排名第一！"); // 丧尸攻城活动结束，丧尸攻城场景播报
	public final static KWordBroadcastType 丧尸攻城_丧尸攻城活动结束时间剩余x分钟 = new KWordBroadcastType("【丧尸攻城】活动结束时间剩余{}分钟！"); // 丧尸攻城活动时间剩余5分钟时播报，丧尸攻城场景播报
	public final static KWordBroadcastType 丧尸攻城_BOSS还剩余百分之x生命值 = new KWordBroadcastType("BOSS还剩余{}%生命值，请再接再厉！"); // BOSS还剩余30%，20%，10%各播报一次，丧尸攻城场景播报


	public final static KWordBroadcastType 排行榜_我们的x榜冠军x角色上线了大家快去膜拜吧 = new KWordBroadcastType("我们的{}冠军{}上线了，大家快去膜拜吧！"); // 冠军上线公告
	
	
	public final static KWordBroadcastType 首冲礼包_XX领取了首冲礼包 = new KWordBroadcastType("{}通过充值领取了首冲礼包，获得大量机甲进阶石"); // 首冲公告
	public final static KWordBroadcastType 机甲进阶_XX将机甲进阶到XXXX = new KWordBroadcastType("{}将机甲进阶到{}，实力得到大幅提升"); // 进阶机甲公告，3阶以上公告
			
	public final static KWordBroadcastType 天赋_XX激活了XX天赋获得XX被动技能 = new KWordBroadcastType("{}激活了{}天赋，获得{}被动技能"); // 天赋页激活公告
	public final static KWordBroadcastType 天梯赛_XXX战队在天梯赛中段位进阶到XX = new KWordBroadcastType("{}战队在天梯赛中段位进阶到{}"); // 天梯赛进阶公告
	public final static KWordBroadcastType 天梯赛_XXX战队在天梯赛中连胜X场 = new KWordBroadcastType("{}战队在天梯赛中连胜{}场，势不可挡"); // 天梯赛连续胜利公告，5场以上进行公告
			
	public final static KWordBroadcastType 升星_XX激活了X阶升星套装 = new KWordBroadcastType("{}激活了{}阶升星套装，散发出耀眼的光芒"); // 升星激活2阶以上的套装进行公告，只限初次激活，反复激活不公告
	public final static KWordBroadcastType 宝石_XX激活X等宝石套装 = new KWordBroadcastType("{}激活{}等宝石套装，闪耀全身"); // 初次激活3等宝石套装以上进行公告，只限初次激活，反复激活不公告
			
	public final static KWordBroadcastType 随从_XX通过努力终于召唤出XXX随从 = new KWordBroadcastType("{}通过努力终于召唤出{}随从"); // 获得紫色以上品质随从时进行公告
	
	public final static KWordBroadcastType 竞技场_XXX已经连胜5场 = new KWordBroadcastType("{}已经连胜{}场，在竞技场中无人能敌，谁能阻止他"); // 竞技场中连续胜利5场以上，每次胜利进行播报
	public final static KWordBroadcastType 章节S奖励_XXX奋勇杀敌X章中达到所有S评级	 = new KWordBroadcastType("{}奋勇杀敌{}章中达到所有S评级，得到特别奖励"); // 领取章节中的全S奖励
	
	public final static KWordBroadcastType 挖矿_xx在挖矿中突然发现X个闪闪发光的XXXX = new KWordBroadcastType("{}在挖矿中突然发现{}个闪闪发光的{}"); // 在挖矿中获得宝石时
	public final static KWordBroadcastType 庄园_xx在庄园中暴打僵尸僵尸突然从口中掉出了X个X = new KWordBroadcastType("{}在庄园中暴打僵尸，僵尸突然从口中掉出了{}个{}"); // 打僵尸获得宝石时

	// ////////////////////////////////////////////////////////
	// ============= 以下内容不需要使用者关注=========================
	// ////////////////////////////////////////////////////////
	/**
	 * <pre>
	 * 使用EXCEL数据进行赋值
	 * 
	 * @param excelE
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2014-6-19 下午5:12:35
	 * </pre>
	 */
	public static void init(KGameExcelTable table) throws Exception {
		// 所有实例
		Map<String, KWordBroadcastType> instanceMap = new HashMap<String, KWordBroadcastType>();
		Field[] fields = KWordBroadcastType.class.getDeclaredFields();
		for (Field field : fields) {
			if(Modifier.isStatic(field.getModifiers()) || field.getType()==KWordBroadcastType.class){
				instanceMap.put(field.getName(), (KWordBroadcastType) field.get(null));
			}
		}

		// 加载数据
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		
		// 使用EXCEL的行数据对相应的类型进行赋值
		for (KGameExcelRow row : rows) {
			String name = row.getData("title");
			KWordBroadcastType type = instanceMap.get(name);
			if (type == null) {
				throw new KGameServerException("系统公告类型不存在 name = " + name);
			}
			ReflectPaser.parseExcelData(type, row, true);
			
			if(type.isShowInChannel==false && type.isShowTop==false){
				throw new KGameServerException("isShowInChannel 或  isShowTop 不能全为false name = " + name);
			}
			
			if(type.maxRoleLv<type.minRoleLv || type.maxRoleLv<1){
				throw new KGameServerException("minRoleLv 或  maxRoleLv 错误 name = " + name);
			}
		}
	}

	// -----------------------------------------------------------------
	public final String content;// 发送内容
	private boolean isShowTop = true;// 是否将本消息去除频道名称后显示于【世界播报】位置
	private boolean isShowInChannel = true;// 是否将本消息显示于频道界面
	private int minRoleLv = 1;
	private int maxRoleLv = Short.MAX_VALUE;

	private KWordBroadcastType(String template) {
		this.content = template;
	}

	public boolean isShowTop() {
		return isShowTop;
	}

	public boolean isShowInChannel() {
		return isShowInChannel;
	}

	public int getMinRoleLv() {
		return minRoleLv;
	}

	public int getMaxRoleLv() {
		return maxRoleLv;
	}
}
