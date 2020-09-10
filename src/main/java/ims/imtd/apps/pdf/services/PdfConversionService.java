package ims.imtd.apps.pdf.services;

import ims.imtd.apps.pdf.adapters.aws.s3.AwsS3Service;
import ims.imtd.apps.pdf.adapters.aws.sqs.AwsSQSMessageService;
import ims.imtd.apps.pdf.application.config.PdfConversionConfiguration;
import ims.imtd.apps.pdf.commands.CreatePdfCommandMessage;
import ims.imtd.apps.pdf.constants.MessageMetadataConstants;
import ims.imtd.apps.pdf.events.PdfCreatedEventMessage;
import ims.imtd.apps.pdf.exception.PdfConversionServiceException;
import ims.imtd.apps.pdf.types.PdfServiceStatusType;
import ims.imtd.core.adapters.message.ImtdCoreMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.TikaMimeKeys;
import org.apache.tika.mime.MimeTypeException;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class PdfConversionService {
	private PdfConversionConfiguration config;
	private DocumentConverterService converterService;

	private AwsSQSMessageService sqsService;
	private AwsSQSMessageService sqsStatusService;
	private AwsS3Service s3Service;

	@Autowired
    public PdfConversionService (PdfConversionConfiguration config, 
								 DocumentConverterService converterService) {
		this.config = config;
		this.converterService = converterService;
		sqsService = new AwsSQSMessageService(config.getSqsAccountId(), config.getSqsProcessQueue());
		sqsStatusService = new AwsSQSMessageService(config.getSqsAccountId(), config.getSqsStatusQueue());
		s3Service = new AwsS3Service();
	}

	public void process() {
		log.trace("step one: retrieve message from the SQS queue and delete message");
		CreatePdfCommandMessage message = (CreatePdfCommandMessage)sqsService.receive();

		while (message != null) {
			boolean errorStatus = false;
			String statusMessage = "PDF Conversion successful";

			log.trace("DocumentDownloadUrl is {}", message.getDocumentDownloadUrl());
			// TODO - which one should we use?
			log.trace("NonSearchablePdfUploadUrl is {}",
				message.getMetadata().get(MessageMetadataConstants.SIGNED_URL_NON_SEARCHABLE_PDF_UPLOAD_KEY));
			log.trace("PdfUploadUrl is {}",
				message.getMetadata().get(MessageMetadataConstants.SIGNED_URL_SOURCE_FILE_KEY));

			log.trace("step two: retrieve file from S3 bucket using presigned URL");

			String inputFileNamePath = String.format ("/data/1/tempdir/TempFile");
			String outputFileNamePath = String.format ("/data/1/tempdir/TempFile.pdf");
			File inputFile = new File(inputFileNamePath);
			File outputFile = new File(outputFileNamePath);

			try {

				// get the file from S3 using presigned URL
				s3Service.downloadFile(inputFile, message.getDocumentDownloadUrl());
				//verify the content-type of the file
				InputStream stream = this.getClass().getClassLoader().getResourceAsStream(inputFile.getAbsolutePath());
				TikaConfig config = TikaConfig.getDefaultConfig();
				Tika tika = new Tika();
				try {
					String mediaType = tika.detect(inputFile);
					log.trace("step three: convert file to a pdf");
					converterService.convertDocumentToPdf(inputFile, outputFile,
						config.getMimeRepository().forName(mediaType).getExtension().substring(1));

				} catch (IOException | MimeTypeException e) {
					throw new PdfConversionServiceException("There was an error getting the content-type from the file.",
						e.getCause());
				}

				log.trace("step four: send resultant pdf file to S3 bucket using presigned URL");
				s3Service.uploadFile(outputFile, 
					// TODO - which one should we use?
					message.getMetadata().get(MessageMetadataConstants.SIGNED_URL_NON_SEARCHABLE_PDF_UPLOAD_KEY));
					//message.getMetadata().get(MessageMetadataConstants.SIGNED_URL_SOURCE_FILE_KEY));

			} catch (PdfConversionServiceException e) {
				errorStatus = true;
				statusMessage = e.getMessage();
				log.debug(statusMessage);
			}

			log.trace("step five: send the file's conversion status to the status queue");

			PdfCreatedEventMessage statusMsg = new PdfCreatedEventMessage();
			statusMsg.setMessageId(UUID.randomUUID());
			statusMsg.setMessageDate(new Date());
			statusMsg.setDocId(message.getDocId());
			statusMsg.setStatus(PdfServiceStatusType.COMPLETE);
			if (errorStatus) {
				statusMsg.setStatus(PdfServiceStatusType.FAILED);
			}	
			statusMsg.setMessage(statusMessage);

			sqsStatusService.send(statusMsg);

			// retrieve the next message from the SQS queue
			log.trace("step one: retrieve message from the SQS queue and delete message");
			message = (CreatePdfCommandMessage)sqsService.receive();
		}	
	}
}
