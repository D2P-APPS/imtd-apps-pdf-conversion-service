package ims.imtd.apps.pdf.exception;

public class PdfConversionServiceException extends RuntimeException {
	public PdfConversionServiceException() {
	}

	public PdfConversionServiceException(String message) {
		super(message);
	}

	public PdfConversionServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PdfConversionServiceException(Throwable cause) {
		super(cause);
	}

	public PdfConversionServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
