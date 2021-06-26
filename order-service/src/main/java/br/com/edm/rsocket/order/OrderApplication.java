package br.com.edm.rsocket.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

}

@RestController
class OrderController {

	private final RSocketRequester createOrderRequester;

	public OrderController(@Autowired RSocketRequester.Builder builder) {
		this.createOrderRequester =
				builder.connectTcp("localhost", 7000)
						.onErrorContinue((e, i) -> {
							System.out.println("Error For Item +" + i);
						}).block();
	}

	private Random random = new Random();

	@GetMapping(value = "/neworders")
	public void create() {
		ExecutorService myPool = Executors.newFixedThreadPool(10);
		Flux
			.range(1, 10)
			.delayElements(Duration.ofSeconds(1))
			.parallel(10)
			.runOn(Schedulers.fromExecutorService(myPool))
			.map(i -> Integer.toUnsignedLong(i))
			.flatMap(id -> sendOrder(id))
			.sequential()
			.subscribe();
	}

	private Flux<Order> sendOrder(Long id) {
		Flux<Order> createdOrder =
				createOrderRequester
						.route("create-order")
						.data(id)
						.retrieveFlux(Order.class)
				;
		return createdOrder;
	}

}

class Order {

	private Long orderId;
	private String status;
	private Instant timestamp;

	public Order(Long orderId, String status, Instant timestamp) {
		this.orderId = orderId;
		this.status = status;
		this.timestamp = timestamp;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getStatus() {
		return status;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

}

