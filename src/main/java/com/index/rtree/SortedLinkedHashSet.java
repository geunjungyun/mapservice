package com.index.rtree;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class SortedLinkedHashSet<E> extends LinkedHashSet<E> implements Comparable<Set<Integer>>{

	public int compareTo(Set<Integer> o) {
		if(this.size() > o.size())
			return 1;
		else
			return 0;
	}
	
	
	public static Set<Integer> toSortedLinkedHashSet(Object[] object) {
		long st = System.currentTimeMillis();
		Set<Integer> rs = new SortedLinkedHashSet<Integer>();
		
		for(int i=0, size=object.length; i<size; i++) {
			rs.add((Integer) object[i]);
		}
		
		long et = System.currentTimeMillis();
		
		return rs;		
	}

}
