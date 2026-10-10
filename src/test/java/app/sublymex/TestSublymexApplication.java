package app.sublymex;

import org.springframework.boot.SpringApplication;

public class TestSublymexApplication {

	public static void main(String[] args) {
		SpringApplication.from(SublymexApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
