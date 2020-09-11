package ims.imtd.apps.pdf.adapters.aws.s3;

import ims.imtd.apps.pdf.exception.PdfConversionServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


@Slf4j
public class AwsS3Service {

	/**
	 * Upload the resultant PDF file to S3 using a presigned URL
	 */
	public String uploadFile(File file, String uploadFileURL) {
		log.debug("uploadFileURL is : {}", uploadFileURL);

		String response = null;
		HttpURLConnection con = null;

		try {

			URL url = new URL(uploadFileURL);
			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod(HttpMethod.PUT.toString());
			// TODO: really application/pdf, but it is broke in this version of the test driver
			//con.setRequestProperty("Content-Type", MediaType.APPLICATION_PDF.toString());
			con.setRequestProperty("Content-Type", "application/docx");
			FileUtils.copyFile(file, con.getOutputStream());

			response = con.getResponseMessage();

			log.debug("uploadFile HTTP Response Code : {}", con.getResponseCode());
			log.debug("uploadFile HTTP Response Message : {}", response);

			if (HttpURLConnection.HTTP_OK != con.getResponseCode()) {
				throw new PdfConversionServiceException("File upload failed");
			}
		} catch (IOException e) {
			throw new PdfConversionServiceException(e.getCause());
		} finally {
			con.disconnect();
		}
		return response;
    }

	/**
	 *  Download the file from S3 using a presigned URL
	 */
	public void downloadFile (File file, String downloadFileURL) {
		log.debug("downloadFileURL is : {}", downloadFileURL);

		HttpURLConnection con = null;
		try {
			URL obj = new URL(downloadFileURL);
			con = (HttpURLConnection) obj.openConnection();
			int responseCode = con.getResponseCode();
			log.debug("Download Response Code : {}", responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {

				log.debug("Success so far on download");
				FileUtils.copyInputStreamToFile(con.getInputStream(), file);
				log.debug("File downloaded");
			} else {
				log.debug("No file to download. Server replied HTTP code: {} " + responseCode);
				throw new PdfConversionServiceException("File download failed");
			}

		} catch (IOException e) {
			throw new PdfConversionServiceException(e.getCause());
		} finally {
			con.disconnect();
		}
	}
}
