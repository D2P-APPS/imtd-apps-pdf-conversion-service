package ims.keystone.microservice;

import ims.imtd.apps.pdf.commands.CreatePdfCommandMessage;
import ims.imtd.core.adapters.message.ImtdCoreMessage;
import java.io.File;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfConversionService {

	@Autowired
	private PdfConversionConfiguration config;

	@Autowired
	private DocumentConverter converter;

	//@Autowired
	private AwsSQSMessageService sqsService;

	private boolean firstTime = true;

	public void process() {

		// register this worker with the pdf registry, only do it once
		if (firstTime) {

			try {
	        	log.debug("First Time");
				firstTime = false;
				sqsService = new AwsSQSMessageService(config.getSqsProcessQueue());

			} catch (Exception e) {
				log.debug("Something went wrong");
			}

		}

 		log.trace("step one: retrieve message from the SQS queue and delete message");
		//ImtdCoreMessage message = sqsService.receive();
		//String message = sqsService.receive();
            // Autowire eventually ??? 
		//sqsService = new AwsSQSMessageService(config.getSqsProcessQueue());
		log.trace(sqsService.toString());
		ImtdCoreMessage message = sqsService.receive();
		CreatePdfCommandMessage message1 = (CreatePdfCommandMessage)message; 
		log.trace("i got here");
		log.trace(message1.getDocumentDownloadUrl());
        		
		if (message != null) {
			log.trace("step two: retrieve S3Object from bucket");
			//S3Object originalS3Object = s3Service.retrieveFromS3BucketLanding(filename);

			log.trace("step three: step convert file to a pdf");
			final DocumentFormat targetFormat =
				DefaultDocumentFormatRegistry.getFormatByExtension("pdf");

			try {

				// get the original filename and change it to the resultant filename with the .pdf extension
				String inputFileName = "00000_TestDoc.docx";
				int dot = inputFileName.lastIndexOf(".");		
				String inputFileNameExt = inputFileName.substring(dot+1); 
				String outputFileName = String.format("%s.pdf", inputFileName.substring(0, dot)); 

				String inputFileNamePath = String.format ("/data/1/uploaddir/%s", inputFileName);
				File inputFile = new File(inputFileNamePath);
				String outputFileNamePath = String.format ("/data/1/downloaddir/%s", outputFileName);
				File outputFile = new File(outputFileNamePath);

				converter.convert(inputFile)
					.as(DefaultDocumentFormatRegistry.getFormatByExtension(inputFileNameExt))
					.to(outputFile)
					.as(targetFormat)
					.execute();

			} catch (OfficeException e) {
				log.debug("Something went wrong with jodconverter/soffice conversion");
			}

			log.trace("step four: send file/message to finished s3 bucket");
		}	
	}
}
