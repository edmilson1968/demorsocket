package br.com.edm.rsocket.control.controlservice;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@SpringBootApplication
public class ControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlServiceApplication.class, args);
	}

}

@Controller
class ControlServiceController {

//	private RSocketRequester rsocketRequester;
//	private RSocketRequester.Builder rsocketRequesterBuilder;
//	private static Disposable disposable;
//	private RSocketStrategies rsocketStrategies;
//
//	@Autowired
//	private Mono<RSocketRequester> inventoryRequester;
	private Mono<RSocketRequester> paymentRequester;

	public ControlServiceController(RSocketRequester.Builder builder) {
		try {
			paymentRequester = builder
					.connectTcp("localhost", 7100)
					.doOnError(error -> System.err.println("payment connection CLOSED"))
			;
//			paymentRequester = builder
//					.connectTcp("localhost", 7100)
//					.onErrorContinue((e, i) -> {
//						System.out.println("Error For Item +" + i);
//					})
//					.block();

//			paymentRequester.block().rsocket()
//					.onClose()
//					.doOnError(error -> System.err.println("payment connection CLOSED"))
//					.doFinally(consumer -> System.err.println("payment client DISCONNECTED"))
//					.subscribe();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		isPaymentRequesterConnected();
		//isInventoryRequesterConnected();
	}

	@MessageMapping("create-order")
	Flux<Order> requestResponse(Long id) {
//		System.out.println("criando id " + id);

		final Flux<Order> created = Flux
				.just(new Order(id, "created", Instant.now()))
//				.parallel()
//				.publishOn(Schedulers.elastic())
//				.doOnNext(o -> System.out.println(o))
				.flatMap(a -> payOrder(a.getOrderId()))
				//.concatMap(a -> inventoryOrder(a.getOrderId()))
				//.log()
//				.sequential()
				;
		return created;
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

//	private Mono<Order> inventoryOrder(Long id) {
//		System.out.println("inventario id " + id);
//		return inventoryRequester.flatMap(req -> req
//				.route("inventory-order")
//				.data(id)
//				.retrieveMono(Order.class)
////				.doOnNext(o -> System.out.println(o))
//		);
//	}

//	private boolean isPaymentRequesterConnected() {
//		if (null == this.paymentRequester || this.paymentRequester.rsocket().isDisposed()) {
//			return false;
//		}
//		return true;
//	}

//	private boolean isInventoryRequesterConnected() {
//		if (inventoryRequester == null) {
//			inventoryRequester =
//					builder.connectTcp("localhost", 7200)
//							.onErrorContinue((e, i) -> {
//								System.out.println("Error For Item +" + i);
//							})
//							.block();
//		}
//		return (inventoryRequester != null);
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
