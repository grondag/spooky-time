package com.fabriccommunity.spookytime.doomtree.heart;

import java.util.Comparator;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/** Maintains insertion order */
class PositionCollector {
	final LongOpenHashSet set = new LongOpenHashSet();
	final LongArrayList list = new LongArrayList();
	
	void add(long val) {
		if(set.add(val)) {
			list.add(val);
		}
	}

	void remove(long val) {
		set.remove(val);
		list.removeIf((long l) -> l == val);
	}

	long[] toLongArray() {
		return list.toLongArray();
	}

	boolean contains(long val) {
		return set.contains(val);
	}

	void sort(Comparator<? super Long> c) {
		list.sort(c);
	}
}