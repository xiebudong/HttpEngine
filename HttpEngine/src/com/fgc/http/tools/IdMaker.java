package com.fgc.http.tools;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author FGC
 * @date 2014-08-18
 */

public class IdMaker {
	private static AtomicInteger sIDMaker = new AtomicInteger(0);
	public static int makeUniqueId(){
		sIDMaker.compareAndSet(Integer.MAX_VALUE, 1);
		return sIDMaker.incrementAndGet();
	}
}
