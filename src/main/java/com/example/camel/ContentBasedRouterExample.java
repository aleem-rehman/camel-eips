package com.example.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class ContentBasedRouterExample {
    public static void main(String[] args) throws Exception {
        // Create Camel context
        CamelContext camelContext = new DefaultCamelContext();

        // Add routing rules
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:orders")
                    .choice()
                        .when(simple("${body} contains 'electronics'"))
                            .to("log:electronics")
                        .when(simple("${body} contains 'furniture'"))
                            .to("log:furniture")
                        .otherwise()
                            .to("log:other");
            }
        });

        // Start the Camel context
        camelContext.start();

        // Send test messages
        camelContext.createProducerTemplate().sendBody("direct:orders", "Order for electronics: TV");
        camelContext.createProducerTemplate().sendBody("direct:orders", "Order for furniture: Chair");
        camelContext.createProducerTemplate().sendBody("direct:orders", "Order for groceries: Apples");

        // Keep application running for a while to process messages
        Thread.sleep(5000);

        // Stop the Camel context
        camelContext.stop();
    }

}


/// INFO  electronics - Order for electronics: TV
/// INFO  furniture - Order for furniture: Chair
/// INFO  other - Order for groceries: Apples