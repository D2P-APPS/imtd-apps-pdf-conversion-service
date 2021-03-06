package ims.imtd.apps.pdf.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@ConfigurationProperties
@Getter
@PropertySource("classpath:pdfconversion.yaml")
public class PdfConversionConfiguration {

	@Value("${sqsProcessQueue}")
	private String sqsProcessQueue;
	@Value("${sqsStatusQueue}")
	private String sqsStatusQueue;
	@Value("${sqsAccountId:#{null}}")
	private String sqsAccountId;

}
