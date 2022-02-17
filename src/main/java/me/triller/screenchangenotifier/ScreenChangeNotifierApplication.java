package me.triller.screenchangenotifier;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ScreenChangeNotifierApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(ScreenChangeNotifierApplication.class);

		builder.headless(false); // Application can't be headless, if screen needs to be captured

		ConfigurableApplicationContext context = builder.run(args);
	}

}
