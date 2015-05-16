package com.kola.kmp.logic.competition.teampvp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.kola.kgame.cache.util.UtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPRandomName {

	private static List<String> _maleNames;
	private static List<String> _femaleNames;
	private static List<String> _teamNames;
	
	public static void init(String maleNamePath, String femaleNamePath, String teamNamePath) throws Exception {
		_maleNames = readToList(maleNamePath);
		_femaleNames = readToList(femaleNamePath);
		_teamNames = readToList(teamNamePath);
	}
	
	private static List<String> readToList(String path) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		List<String> list = new LinkedList<String>();
		String name;
		while ((name = br.readLine()) != null) {
			list.add(name);
		}
		br.close();
		return new ArrayList<String>(list);
	}
	
	public static String randomTeamName() {
		return _teamNames.get(UtilTool.random(_teamNames.size()));
	}
	
	public static String randomRobotName(boolean male) {
		if (male) {
			return _maleNames.get(UtilTool.random(_maleNames.size()));
		} else {
			return _femaleNames.get(UtilTool.random(_maleNames.size()));
		}
	}
}
