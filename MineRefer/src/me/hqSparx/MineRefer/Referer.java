package me.hqSparx.MineRefer;

import java.util.ArrayList;
import java.util.List;

public class Referer {

	public String nickname = "";
	public int credits = 0;
	public List<String> players = new ArrayList<String>(256); 

	public Referer(String name, int credits) {
		this.nickname = name;
		this.credits = credits;
	}
	
	public void addPlayer(String name){
		this.players.add(name);
	}
	
	public String toString(){
		String result = "";
		String sep = ":";
		String sep2 = ".";
		
		result += nickname + sep + credits + sep;
		for (int i = 0; i < players.size(); i++)
		{
		    result += players.get(i) + sep2;
		}
		return result;	
	}
	
}
