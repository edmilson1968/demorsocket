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

@SpringBootApplication
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

}

@RestController
class OrderController {

	private Mono<RSocketRequester> paymentRequester;
	private Mono<RSocketRequester> inventoryRequester;

	public OrderController(@Autowired RSocketRequester.Builder builder) {
		paymentRequester = builder
				.connectTcp("localhost", 7100)
				.doOnError(error -> System.err.println("payment connection CLOSED"))
		;

		inventoryRequester = builder
				.connectTcp("localhost", 7200)
				.doOnError(error -> System.err.println("inventory connection CLOSED"))
		;
	}

	@GetMapping(value = "/neworders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Order> create() {
		Flux<Order> ordercreate = Flux
				.range(1, 5)
				.delayElements(Duration.ofSeconds(1))
				.map(i -> Integer.toUnsignedLong(i))
				.map(o -> new Order(o, "created", Instant.now()))
				.doOnNext(o -> System.out.println(o))
				;

		Flux<Order> payment = Flux.merge(ordercreate)
				.flatMap(o -> payOrder(o.getOrderId()))
				.flatMap(o -> check(o))
				.doOnNext(o -> System.out.println(o))
				;

		Flux<Order> inventory = Flux.merge(payment)
				.filter(o -> "payment approved".equals(o.getStatus()))
				.concatMap(o -> inventoryOrder(o.getOrderId()))
				.doOnNext(o -> System.out.println(o))
				;

		Flux<Order> merged = inventory.mergeWith(payment).mergeWith(ordercreate);
		return merged;
	}

	private Mono<Order> check(Order o) {
		return Mono.fromSupplier(() -> {
			if ("payment approved".equals(o.getStatus())) {
				return o;
			} else {
				Order ret = new Order(o.getOrderId(), "cancelled", Instant.now());
				System.out.println(ret);
				return ret;
			}
		});
	}


	private Mono<Order> payOrder(Long id) {

		Mono<Order> pay =
				paymentRequester
					.flatMap(req -> req
						.route("payment-order")
						.data(id)
						.retrieveMono(Order.class)
						.doOnNext(o -> System.out.println(o))
					)
				;
		return pay;
	}

	private Mono<Order> inventoryOrder(Long id) {

		Mono<Order> inventory =
				inventoryRequester
					.flatMap(req -> req
						.route("inventory-order")
						.data(id)
						.retrieveMono(Order.class)
						.doOnNext(o -> System.out.println(o))
					)
				;
		return inventory;
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

	@Override
	public String toString() {
		return "Order{" +
				"orderId=" + orderId +
				", status='" + status + '\'' +
				", timestamp=" + timestamp +
				'}';
	}
}

