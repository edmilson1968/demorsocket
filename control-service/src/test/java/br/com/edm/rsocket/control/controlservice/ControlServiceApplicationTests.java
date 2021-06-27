package br.com.edm.rsocket.control.controlservice;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ControlServiceApplicationTests {

	@Test
	public void threadNonBlockingBySchedulersExecutorFixed() {
		ExecutorService myPool = Executors.newFixedThreadPool(10);
		Flux.range(1,6)
				.parallel(10)
				.runOn(Schedulers.fromExecutorService(myPool))
				.flatMap(a -> Mono.just(blockingGetInfo(a)))
				.sequential()
				.subscribe(System.out::println);
	}

	public void delay() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the info about the current code and time
	 * This is a generic Example of a Blocking call
	 * In reality it can be any Blocking call
	 */
	public String blockingGetInfo(Integer input) {
		delay();
		return String.format("[%d] on thread [%s] at time [%s]",
				input,
				Thread.currentThread().getName(),
				new Date());
	}
}
