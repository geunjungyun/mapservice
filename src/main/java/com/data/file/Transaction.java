package com.data.file;

import java.io.IOException;

public abstract class Transaction {
	
	
	int changeSize = 0;
	int commitSize = 1000;
	
	long st = System.currentTimeMillis();
	
	int commit() {
		long et = System.currentTimeMillis();
		//System.out.println("Transaction size = " + this.changeSize+", time="+(et-st)/1000.0 + " sec");
		
		st = et;
		int size = this.changeSize;
		this.changeSize = 0;
		return size;
	}
	
	void setAutoCommitSize(int size) {
		this.commitSize = size;
	}
	
	//abstract int checkCommit() throws Exception ;
}
