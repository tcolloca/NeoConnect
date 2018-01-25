package com.neopetsconnect.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import com.neopetsconnect.main.Status;

public class TimedJobsRunner<T> {

	private final BiFunction<Callable<T>, T, TimedJob<T>> transformer;
	private final Set<Callable<T>> alwaysJobSet = new HashSet<>();
	private final TreeSet<TimedJob<T>> timedJobSet = new TreeSet<TimedJob<T>>();

	public TimedJobsRunner(BiFunction<Callable<T>, T, TimedJob<T>> transformer) {
		this.transformer = transformer;
	}

	/**
	 * Blocking function.
	 * @throws Exception 
	 */
	public void run() throws Exception {
		while (!ConfigProperties.getStatus().equals(Status.OFF)) {
			if (ConfigProperties.getStatus().equals(Status.PAUSED)) {
				Utils.sleep(0.01);
			}
			TimedJob<T> first = timedJobSet.first();
			Utils.sleep(first.getRemaining());
			runAlwaysJobs();

			runTimedJobs(updateRemainings(first));
		}
	}

	public void addAlwaysJob(Callable<T> runnable) {
		alwaysJobSet.add(runnable);
	}

	public void addJob(Callable<T> runnable, long timeInSecs) {
		timedJobSet.add(new TimedJob<>(runnable, timeInSecs));
	}

	private TreeSet<TimedJob<T>> updateRemainings(TimedJob<T> first) {
		TreeSet<TimedJob<T>> aux = new TreeSet<>();
		for (TimedJob<T> job : timedJobSet) {
			aux.add(job.clone().setRemaining(job.getRemaining() - first.getRemaining()));
		}
		return aux;
	}

	private void runAlwaysJobs() throws Exception {
		Set<Callable<T>> aux = new HashSet<>(alwaysJobSet);
		for (Callable<T> job : aux) {
			alwaysJobSet.remove(job);
			T result = job.call();
			TimedJob<T> newJob = transformer.apply(job, result);
			if (newJob != null) {
				alwaysJobSet.add(newJob.getCallable());
			}
		}
	}

	private void runTimedJobs(Set<TimedJob<T>> updatedTimedJobs) throws Exception {
		for (TimedJob<T> job : updatedTimedJobs) {
			timedJobSet.remove(job);
			if (job.getRemaining() <= 0) {
				timedJobSet.remove(job);
				T result = job.getCallable().call();
				TimedJob<T> newJob = transformer.apply(job.getCallable(), result);
				if (newJob != null) {
					timedJobSet.add(newJob);
				}
			} else {
				timedJobSet.add(job);
			}
		}
	}

}
