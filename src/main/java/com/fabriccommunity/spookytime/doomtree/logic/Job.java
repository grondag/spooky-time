package com.fabriccommunity.spookytime.doomtree.logic;

@FunctionalInterface interface Job {
	Job apply(DoomTreeHeartBlockEntity heart);
}