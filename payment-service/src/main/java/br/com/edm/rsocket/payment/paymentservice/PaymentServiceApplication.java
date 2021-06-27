package br.com.edm.rsocket.payment.paymentservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}

@RestController
class PaymentController {

	@Value("${sleep.min}")
	private Long sleepMin;

	@Value("${sleep.max}")
	private Long sleepMax;

	@PostMapping(value = "/payment-order/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	Mono<Order> requestResponse(@PathVariable Long id) throws InterruptedException {
		delay();
		return Mono
				.just(
					new Order(
						id,
						((id % 2 == 0 || id % 3 == 0) ? "payment approved": "payment refused"),
						Instant.now()
					)
				)
				.doOnNext(o -> System.out.println(o))
		;
	}

	private void delay() {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextLong(sleepMin, sleepMax));
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

	@Override
	public String toString() {
		return "Order{" +
				"orderId=" + orderId +
				", status='" + status + '\'' +
				", timestamp=" + timestamp +
				'}';
	}
}
