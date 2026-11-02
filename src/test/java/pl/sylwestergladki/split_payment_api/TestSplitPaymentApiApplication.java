package pl.sylwestergladki.split_payment_api;

import org.springframework.boot.SpringApplication;

public class TestSplitPaymentApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(SplitPaymentApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
