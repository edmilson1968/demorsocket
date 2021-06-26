package br.com.edm.rsocket.payment.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}

@Controller
class PaymentController {

	@MessageMapping("payment-order")
	Mono<Order> requestResponse(Long id) throws InterruptedException {
		delay();
		return Mono.just(new Order(id, "payment", Instant.now()));
	}

	private void delay() {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 4000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
