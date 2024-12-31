package com.example.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;

public class IdempotentConsumerExample {
    public static void main(String[] args) throws Exception {
        // Create Camel context
        CamelContext camelContext = new DefaultCamelContext();

        // Add routing rules
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {

                from("direct:start")
                        .idempotentConsumer(
                                header("messageId"), // The unique message ID
                                MemoryIdempotentRepository.memoryIdempotentRepository()
                        )
                        .log("Processing unique message: ${body}")
                        .to("mock:result");
            }
        });

        // Start the Camel context
        camelContext.start();

        // Send test messages
        camelContext.createProducerTemplate().sendBodyAndHeader("direct:start", "Message 1", "messageId", "1");
        camelContext.createProducerTemplate().sendBodyAndHeader("direct:start", "Message 1 Duplicate", "messageId", "1");
        camelContext.createProducerTemplate().sendBodyAndHeader("direct:start", "Message 2", "messageId", "2");

        // Keep application running for a while to process messages
        Thread.sleep(5000);

        // Stop the Camel context
        camelContext.stop();
    }

}


/// INFO  electronics - Order for electronics: TV
/// INFO  furniture - Order for furniture: Chair
/// INFO  other - Order for groceries: Apples