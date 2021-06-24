package br.com.edm.rsocket.control.controlservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@SpringBootApplication
public class ControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlServiceApplication.class, args);
	}

}

@Controller
class RSocketController {

	private final RSocketRequester inventoryRequester;
	private final RSocketRequester paymentRequester;

	public RSocketController(@Autowired RSocketRequester.Builder builder) {
		this.paymentRequester = null;
//				builder.connectTcp("localhost", 7100)
//						.onErrorContinue((e, i) -> {
//							System.out.println("Error For Item +" + i );
//						})
//						.block();

		this.inventoryRequester = null;
//				builder.connectTcp("localhost", 7200)
//						.onErrorContinue((e, i) -> {
//							System.out.println("Error For Item +" + i );
//						})
//						.block();
	}

	@MessageMapping("create-order")
	Flux<Order> requestResponse(Long id) {
		System.out.printf("criando pedido: %s\n", id);

		Flux<Order> orderCreated =
			Flux.generate((SynchronousSink<Order> synchronousSink) -> {
				synchronousSink.next(new Order(id, "created", Instant.now()));
			});

//		Disposable created = Flux.fromStream(Stream.generate(() -> new Order(id, "created", Instant.now()))).subscribe();

//		Flux<Order> pay = Flux.merge(orderCreated)
//			.doOnNext(
//					o -> new Order(o.getOrderId(), "payment"  , Instant.now())
//			)
		;
//
//		Flux<Order> invent = Flux.merge(pay)
//			.doOnNext(o -> new Order(o.getOrderId(), "inventory", Instant.now())).delayElements(Duration.ofSeconds(10));
		;
		return orderCreated;
	}

	@MessageMapping("create-order-mono")
	Mono<Order> requestMonoResponse(Long id) {
		System.out.printf("criando um pedido: %s\n", id);
		Mono<Order> orderMono = Mono.just(new Order(id, "created", Instant.now()));

		return orderMono.log();
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
