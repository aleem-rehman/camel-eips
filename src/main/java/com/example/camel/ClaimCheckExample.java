package com.example.camel;

import com.example.dao.InMemoryRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ClaimCheckExample {
    public static void main(String[] args) throws Exception {
        // Create Camel Context
        CamelContext camelContext = new DefaultCamelContext();

        // Create In-Memory ClaimCheck Repository
        InMemoryRepository claimCheckRepository = new InMemoryRepository();


        camelContext.getRegistry().bind("claimCheckRepo", claimCheckRepository);

        // Add Routes
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Route to extract and store the "blob" field
                from("direct:start")
                        .log("Original JSON message received: ${body}")
                        .process(exchange -> {
                            // Parse JSON and extract the blob field
                            String json = exchange.getIn().getBody(String.class);
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);

                            // Extract the "blob" field
                            String blob = (String) jsonMap.remove("blob");

                            // Store the blob in the external repository
                            String claimCheckKey = "blob-" + jsonMap.get("id");
                            exchange.getIn().setHeader("claimCheckKey", claimCheckKey);

                            InMemoryRepository repo = exchange.getContext().getRegistry().lookupByNameAndType("claimCheckRepo", InMemoryRepository.class);
                            repo.add(claimCheckKey, blob);

                            // Update the message body with the remaining JSON
                            exchange.getIn().setBody(objectMapper.writeValueAsString(jsonMap));
                        })
                        .log("Lightweight JSON message after blob storage: ${body}")
                        .to("direct:processMessage");

                // Route to retrieve and reconstruct the JSON message
                from("direct:processMessage")
                        .log("Processing lightweight JSON message: ${body}")
                        .process(exchange -> {
                            // Parse the lightweight JSON
                            String lightweightJson = exchange.getIn().getBody(String.class);
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMap = objectMapper.readValue(lightweightJson, Map.class);

                            // Retrieve the blob from the repository
                            String claimCheckKey = exchange.getIn().getHeader("claimCheckKey", String.class);
                            InMemoryRepository repo = exchange.getContext().getRegistry().lookupByNameAndType("claimCheckRepo", InMemoryRepository.class);
                            String blob = repo.get(claimCheckKey);

                            // Add the blob back to the JSON
                            jsonMap.put("blob", blob);

                            // Update the message body with the reconstructed JSON
                            exchange.getIn().setBody(objectMapper.writeValueAsString(jsonMap));
                        })
                        .log("Reconstructed JSON message: ${body}")
                        .to("direct:end");

                // Final route to log the output
                from("direct:end")
                        .log("Final JSON message: ${body}");
            }
        });

        // Start Camel Context
        camelContext.start();

        // Send test JSON message
        String testJson = "{\"id\":\"12345\", \"name\":\"example\", \"blob\":\"very_large_binary_data_here\"}";
        camelContext.createProducerTemplate().sendBody("direct:start", testJson);

        // Allow routes to process for a few seconds
        Thread.sleep(5000);

        // Stop Camel Context
        camelContext.stop();
    }
}
