/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.samples;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.solacesystems.jms.SupportedProperty;

public class TopicPublisher {

    public void run(String... args) throws Exception {

        System.out.println("TopicPublisher initializing...");

        // The client needs to specify both of the following properties:
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");
        env.put(InitialContext.PROVIDER_URL, (String) args[0]);
        env.put(SupportedProperty.SOLACE_JMS_VPN, "default");
        env.put(Context.SECURITY_PRINCIPAL, "clientUsername");

        // InitialContext is used to lookup the JMS administered objects.
        InitialContext initialContext = new InitialContext(env);
        // Lookup ConnectionFactory.
        ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("/JNDI/CF/GettingStarted");
        // JMS Connection
        Connection connection = cf.createConnection();

        // Create a non-transacted, Auto Ack session.
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Lookup Topic in Solace JNDI.
        final Topic publishDestination = (Topic) initialContext.lookup("/JNDI/T/GettingStarted/pubsub");

        final MessageProducer producer = session.createProducer(publishDestination);

        TextMessage message = session.createTextMessage("Hello world!");

        System.out.printf("Connected. About to send request message '%s' to topic '%s'...%n", message.getText(),
                publishDestination.toString());

        // Leaving priority and Time to Live to their defaults.
        // NOTE: Priority is not supported by the Solace Message Bus
        producer.send(publishDestination, message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY,
                Message.DEFAULT_TIME_TO_LIVE);

        // Close consumer
        connection.close();
        initialContext.close();
        System.out.println("Message sent. Exiting.");
    }

    public static void main(String... args) throws Exception {

        // Check command line arguments
        if (args.length < 1) {
            System.out.println("Usage: TopicPublisher <msg_backbone_ip:port>");
            System.exit(-1);
        }

        TopicPublisher app = new TopicPublisher();
        app.run(args);
    }
}
