package br.com.edm.rsocket.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.stream.Stream;

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
						})
						.block();
	}

	private Random random = new Random();

	@GetMapping(value = "/neworders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Order> create() {
		Flux<Order> ordersFlux = Flux
				.fromStream(Stream.generate(() -> random.nextLong()))
				.delayElements(Duration.ofSeconds(1))
				.flatMap(id -> sendOrder(id))
				;
		return ordersFlux;
	}

	@GetMapping(value = "/gera", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Order> geraOne() {
		Mono<Order> ordersFlux = Mono.just(random.nextLong())
//				.fromStream(Stream.generate(() -> random.nextLong()))
//				.delayElements(Duration.ofSeconds(1))
				.flatMap(id -> sendOneOrder(id)).log();
				;
		return ordersFlux;
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
	private Mono<Order> sendOneOrder(Long id) {
		Mono<Order> createdOrder =
				createOrderRequester
						.route("create-order-mono")
						.data(id)
						.retrieveMono(Order.class)
				;
		return createdOrder;
	}

//	private Mono<Order> payOrder(Order order) {
//		return payOrderRequester.route("payment-order").data(order).retrieveMono(Order.class);
//	}

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

	@Override
	public String toString() {
		return "Order{" +
				"orderId=" + orderId +
				", status='" + status + '\'' +
				", timestamp=" + timestamp +
				'}';
	}
}

