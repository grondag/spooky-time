package com.fabriccommunity.spookytime.doomtree.heart;

@FunctionalInterface interface Job {
	Job apply(DoomTreeHeartBlockEntity heart);
}