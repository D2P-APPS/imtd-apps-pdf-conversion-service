package ims.imtd.apps.pdf.adapters.aws.sqs;

import com.google.gson.Gson;
import ims.imtd.apps.pdf.commands.CreatePdfCommandMessage;
import ims.imtd.core.adapters.message.ImtdCoreMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
public class AwsSQSMessageService {

	private final String accountId;
	private final String queueName;
	private final SqsClient sqsClient;

	public AwsSQSMessageService(String accountId, String queueName) {
		this.accountId = accountId;
		this.queueName = queueName;
		this.sqsClient = SqsClient.create();

		log.debug("Account Id is: {}", this.accountId);
	}

	/**
	 * Send message to SQS
	 */
    public void send(ImtdCoreMessage message) {

		log.debug("AwsSQSMessageService::send");
		try {
			GetQueueUrlRequest queueUrlRequest = GetQueueUrlRequest.builder()
				.queueName(queueName)
				.queueOwnerAWSAccountId(accountId)
				.build();

			String queueUrl = sqsClient.getQueueUrl(queueUrlRequest).queueUrl();

			SendMessageRequest messageRequest = SendMessageRequest.builder()
				.queueUrl(queueUrl)
				.messageBody(new Gson().toJson(message))
				.delaySeconds(5)
				.build();

			sqsClient.sendMessage(messageRequest);
		} catch (Exception e) {
			log.debug("Error when sending message to queue: {}", e.getMessage());
		}
	}

	/**
	 * Receive message from SQS
	 */
	public ImtdCoreMessage receive() {

		log.debug("AwsSQSMessageService::receive");
		CreatePdfCommandMessage message = null;

		try {
			GetQueueUrlRequest queueUrlRequest = GetQueueUrlRequest.builder()
				.queueName(queueName)
				.queueOwnerAWSAccountId(accountId)
				.build();

			String queueUrl = sqsClient.getQueueUrl(queueUrlRequest).queueUrl();
			log.debug("queueUrl is {}", queueUrl);

			ReceiveMessageRequest messageRequest = ReceiveMessageRequest.builder()
				.queueUrl(queueUrl)
				.maxNumberOfMessages(1)
				.build();

			List<Message> messageRaw = sqsClient.receiveMessage(messageRequest).messages();
			log.debug("messageRaw is {} and it contains {} messages(s)", messageRaw, messageRaw.size());

			if (messageRaw.size() > 0) {

				Message msg = messageRaw.get(0);
				String msgBody = msg.body();
				log.debug("msgBody is {}", msgBody);
				message = new Gson().fromJson(msgBody, CreatePdfCommandMessage.class);

				// delete the message from the queue
				delete(msg, queueUrl);
			}
								
		} catch (Exception e) {
			log.debug("Error when receiving message from queue: {}", e.getMessage());
		}

		return message;
	}

	/**
	 * Delete message from SQS
	 */
	private void delete(Message msg, String queueUrl) {

		log.debug("AwsSQSMessageService::delete");
		try {

			DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
				.queueUrl(queueUrl)
				.receiptHandle(msg.receiptHandle())
				.build();

			sqsClient.deleteMessage(deleteMessageRequest);
								
		} catch (Exception e) {
			log.debug("Error when deleting message from queue: {}", e.getMessage());
		}
	}
}
