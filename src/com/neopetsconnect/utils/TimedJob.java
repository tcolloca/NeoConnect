package com.neopetsconnect.utils;

import java.util.concurrent.Callable;

public class TimedJob<T> implements Comparable<TimedJob<T>> {
	
	private static int ID = 0;

	private final Callable<T> callable;
	private final int id;
	
	private long remaining;
	
	public TimedJob(Callable<T> callable, long remaining) {
		this(ID++, callable, remaining);
	}
	
	private TimedJob(int id, Callable<T> callable, long remaining) {
		super();
		this.id = id;
		this.callable = callable;
		this.remaining = remaining;
	}
	
	Callable<T> getCallable() {
		return callable;
	}
	
	long getRemaining() {
		return remaining;
	}

	TimedJob<T> setRemaining(long remaining) {
		this.remaining = remaining;
		return this;
	}
	
	protected TimedJob<T> clone() {
		return new TimedJob<>(id, callable, remaining);
	}

	@Override
	public int compareTo(TimedJob<T> o) {
		if (o.id == this.id) {
			return 0;
		}
		int diff = (int) (this.remaining - o.getRemaining());
		if (diff == 0) {
			return this.id - o.id;
		}
		return diff;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimedJob<?> other = (TimedJob<?>) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TimedJob [id=" + id + ", callable=" + callable + ", remaining=" + remaining + "]";
	}
}
