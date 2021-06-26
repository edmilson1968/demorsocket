package br.com.edm.rsocket.control.controlservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@SpringBootApplication
public class ControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlServiceApplication.class, args);
	}

}

@Controller
class ControlServiceController {

	private RSocketRequester inventoryRequester;
	private RSocketRequester paymentRequester;

	private final RSocketRequester.Builder builder;

	public ControlServiceController(@Autowired RSocketRequester.Builder builder) {
		this.builder = builder;
		isInventoryRequesterConnected();
		isPaymentRequesterConnected();
	}

	@MessageMapping("create-order")
	Flux<Order> requestResponse(Long id) {
		final Flux<Order> created = Flux
				.just(new Order(id, "created", Instant.now()))
				.doOnNext(o -> System.out.println(o))
				.concatMap(a -> payOrder(a.getOrderId()))
				.concatMap(a -> inventoryOrder(a.getOrderId()))
				;
		return created;
	}

	private Mono<Order> payOrder(Long id) {
		return paymentRequester
			.route("payment-order")
			.data(id)
			.retrieveMono(Order.class)
			.doOnNext(o -> System.out.println(o))
		;
	}

	private boolean isPaymentRequesterConnected() {
		if (paymentRequester == null) {
			paymentRequester =
				builder.connectTcp("localhost", 7100)
						.onErrorContinue((e, i) -> {
							System.out.println("Error For Item +" + i );
						})
						.block();
		}
		return (paymentRequester != null);
	}

	private boolean isInventoryRequesterConnected() {
		if (inventoryRequester == null) {
			inventoryRequester =
					builder.connectTcp("localhost", 7200)
							.onErrorContinue((e, i) -> {
								System.out.println("Error For Item +" + i);
							})
							.block();
		}
		return (inventoryRequester != null);
	}

	private Mono<Order> inventoryOrder(Long id) {
		return inventoryRequester
			.route("inventory-order")
			.data(id)
			.retrieveMono(Order.class)
			.doOnNext(o -> System.out.println(o))
		;
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
