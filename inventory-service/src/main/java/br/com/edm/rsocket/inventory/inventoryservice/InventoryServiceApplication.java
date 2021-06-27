package br.com.edm.rsocket.inventory.inventoryservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

}

@Controller
class InventoryController {

	@Value("${sleep.min}")
	private Long sleepMin;

	@Value("${sleep.max}")
	private Long sleepMax;

	@MessageMapping("inventory-order")
	Mono<Order> requestResponse(Long id) throws InterruptedException {
		delay();
		return Mono.just(new Order(id, "inventory", Instant.now()))
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
