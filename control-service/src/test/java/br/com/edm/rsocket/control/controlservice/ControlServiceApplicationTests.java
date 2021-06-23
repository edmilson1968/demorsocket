package br.com.edm.rsocket.control.controlservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.stream.Stream;

class ControlServiceApplicationTests {

	@Test
	void contextLoads() {

		final Flux<Integer> pares = Flux.just(0, 2, 4, 6, 8, 10);
		final Flux<Integer> impar = Flux.just(1, 3, 5, 7, 9, 11);

		Flux.merge(
				impar.delayElements(Duration.ofSeconds(1)),
				pares.delayElements(Duration.ofSeconds(2))
		).subscribe(i -> System.out.print(i + " "));
	}

}
