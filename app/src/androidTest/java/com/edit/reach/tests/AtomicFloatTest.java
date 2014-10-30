package com.edit.reach.tests;

import com.edit.reach.utils.AtomicFloat;
import junit.framework.TestCase;

public class AtomicFloatTest extends TestCase {
	AtomicFloat f1;
	AtomicFloat f2;
	AtomicFloat f3;
	AtomicFloat f4;

	public void setUp() throws Exception {
		super.setUp();
		f1 = new AtomicFloat(2.3f);
		f2 = new AtomicFloat(5f);
		f3 = new AtomicFloat(0.4f);
		f4 = new AtomicFloat(0);
	}

	public void testSet() throws Exception {
		f4 = new AtomicFloat(23f);
		assertTrue(f4.get() == 23f);
	}

	public void testGet() throws Exception {
		assertTrue(f1.get() == 2.3f);
		assertTrue(f2.get() == 5f);
		assertTrue(f3.get() == 0.4f);
	}

	public void testFloatValue() throws Exception {
		assertTrue(f1.floatValue() == f1.get());
		assertTrue(f2.floatValue() == f2.get());
		assertTrue(f3.floatValue() == f3.get());
	}

	public void testDoubleValue() throws Exception {
		assertTrue(f1.doubleValue() == ((double)2.3f));
		assertTrue(f2.doubleValue() == ((double)5f));
		assertTrue(f3.doubleValue() == ((double)0.4f));
	}

	public void testIntValue() throws Exception {
		assertTrue(f1.intValue() == ((int)2.3f));
		assertTrue(f2.intValue() == ((int)5f));
		assertTrue(f3.intValue() == ((int)0.4f));
	}

	public void testLongValue() throws Exception {
		assertTrue(f1.longValue() == ((long)2.3f));
		assertTrue(f2.longValue() == ((long)5f));
		assertTrue(f3.longValue() == ((long)0.4f));
	}
}