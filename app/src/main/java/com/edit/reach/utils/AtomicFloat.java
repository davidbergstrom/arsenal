package com.edit.reach.utils;

/**
 * This class is used to model an AtomicFloat
 * Created by: Tim Kerschbaumer
 * Project: arsenal
 * Date: 14-10-20
 * Time: 15:13
 */
import java.util.concurrent.atomic.AtomicInteger;
import static java.lang.Float.*;

public class AtomicFloat extends Number {

	private final AtomicInteger intBits;

	public AtomicFloat(float initialValue) {
		intBits = new AtomicInteger(floatToIntBits(initialValue));
	}

	public final void set(float newValue) {
		intBits.set(floatToIntBits(newValue));
	}

	public final float get() {
		return intBitsToFloat(intBits.get());
	}

	public float floatValue() {
		return get();
	}

	public double doubleValue() {
		return (double) get();
	}

	public int intValue() {
		return (int) get();
	}

	public long longValue()     {
		return (long) get();
	}

}
