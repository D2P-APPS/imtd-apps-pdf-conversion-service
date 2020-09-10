package ims.imtd.apps.pdf.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"ims.imtd.apps.pdf"})
@SpringBootApplication
public class PdfConversionApplication {

	public static void main(String[] args) {

		SpringApplication.run(PdfConversionApplication.class, args);
	}

}
