package ims.imtd.apps.pdf.services;

import ims.imtd.apps.pdf.exception.PdfConversionServiceException;
import ims.imtd.apps.pdf.types.PdfServiceDocumentType;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.File;

@Service
@Slf4j
public class DocumentConverterService {

	private DocumentConverter converter;

	@Autowired
	public DocumentConverterService(DocumentConverter converter) {
		this.converter = converter;
	}

	public void convertDocumentToPdf(File inputFile, File outputFile, String inputFileNameExt) {
		log.trace("About to convert from {} to pdf", inputFileNameExt); 
		try {
			converter.convert(inputFile)
				.as(DefaultDocumentFormatRegistry.getFormatByExtension(inputFileNameExt))
				.to(outputFile)
				.as(DefaultDocumentFormatRegistry.getFormatByExtension(
					PdfServiceDocumentType.PDF.toString().toLowerCase()))
				.execute();

		} catch (OfficeException e) {
			throw new PdfConversionServiceException(e.getCause());
		}
	}
}
