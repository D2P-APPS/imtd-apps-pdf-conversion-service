package ims.imtd.apps.pdf.adapters.aws.sqs.poller;

import ims.imtd.apps.pdf.services.PdfConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@EnableScheduling
@Slf4j
public class PdfConversionSQSPollerAdapter {
	@Autowired
	private PdfConversionService service;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	//@Override
	// schedule every 20 seconds
	@Scheduled(fixedRate = 20000)
	public void poll() {
		log.debug("current time {}", dateFormat.format(new Date()));

		service.process();
	}
}
