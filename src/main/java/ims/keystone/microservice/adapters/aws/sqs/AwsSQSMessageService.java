package ims.keystone.microservice;

import com.google.gson.Gson;
import ims.imtd.core.adapters.message.ImtdCoreMessage;
import java.util.List;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class AwsSQSMessageService {
    private final String queueName;
    private final SqsClient sqsClient;

    public AwsSQSMessageService(String queueName) {
        this.queueName = queueName;
        this.sqsClient = SqsClient.create();
        System.out.println("AwsSQSMessageService::constructor");
    }

    public void send(ImtdCoreMessage message) {
        System.out.println("AwsSQSMessageService::send");
        try {
            CreateQueueRequest request = CreateQueueRequest.builder()
                    .queueName(queueName)
                    .build();
            CreateQueueResponse response = sqsClient.createQueue(request);

            GetQueueUrlRequest queueUrlRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();

            String queueUrl = sqsClient.getQueueUrl(queueUrlRequest).queueUrl();

            SendMessageRequest messageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(new Gson().toJson(message))
                    .delaySeconds(5)
                    .build();

            sqsClient.sendMessage(messageRequest);
        } catch (Exception e) {
            System.out.println("Error when sending message to queue" + e.getMessage());
        }
    }

    public ImtdCoreMessage receive() {
        System.out.println("AwsSQSMessageService::receive");
        //ImtdCoreMessage message = null;

        try {
            GetQueueUrlRequest queueUrlRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();

            String queueUrl = sqsClient.getQueueUrl(queueUrlRequest).queueUrl();
            System.out.println("queueUrl is " + queueUrl);

            ReceiveMessageRequest messageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(1)
                    .build();

            List<Message> messageRaw = sqsClient.receiveMessage(messageRequest).messages();
            System.out.println("messageRaw is " + messageRaw + " and its size is " + messageRaw.size());
            Message msg = messageRaw.get(0);
            msg.getClass();
            System.out.println("message class is " + msg.getClass());
            //Message msg1 = new Message();
            int i = msg.hashCode();
            //String msgstring = msg.getReceiptHandle();

            //message = new Gson().fromJson(msgstring, ImtdCoreMessage.class);    

        } catch (Exception e) {
            System.out.println("Error when receiving message from queue" + e.getMessage());
        }
        return null;
        //return message;
	}

}
