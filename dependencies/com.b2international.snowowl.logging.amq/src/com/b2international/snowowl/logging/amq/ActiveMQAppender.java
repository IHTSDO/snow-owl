/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package com.b2international.snowowl.logging.amq;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import ch.qos.logback.classic.net.JMSQueueAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.net.JMSAppenderBase;

/**
 * A tweaked version of Logback's JMS queue appender that publishes events to an ActiveMQ Queue. The events are
 * serialized and transmitted as JMS message type {@link javax.jms.TextMessage}.
 * 
 * @see JMSQueueAppender
 */
public class ActiveMQAppender extends JMSAppenderBase<ILoggingEvent> {

	private static final String QUEUE_SUFFIX = "traceability";

	static int SUCCESSIVE_FAILURE_LIMIT = 3;

	protected String queuePrefix = "default";
	
	public void setQueuePrefix(String queuePrefix) {
		this.queuePrefix = queuePrefix;
	}
	
	public String getQueuePrefix() {
		return queuePrefix;
	}
	
	QueueConnection queueConnection;
	QueueSession queueSession;
	QueueSender queueSender;

	int successiveFailureCount = 0;

	/**
	 * Options are activated and become effective only after calling this method.
	 */
	public void start() {
		addInfo("Setting up ActiveMQAppender");
		try {
			String providerURL = getProviderURL();
			QueueConnectionFactory connectionFactory = new ActiveMQConnectionFactory(providerURL);

			if (userName != null) {
				queueConnection = connectionFactory.createQueueConnection(userName, password);
			} else {
				queueConnection = connectionFactory.createQueueConnection();
			}

			queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			String queueName = String.format("%s.%s", queuePrefix, QUEUE_SUFFIX);
			Queue queue = queueSession.createQueue(queueName);
			queueSender = queueSession.createSender(queue);

			addInfo("ProviderURL: " + providerURL + ", userName: " + userName + ", queueName: " + queueName);

			queueConnection.start();
		} catch (Exception e) {
			addError("Error while activating options for appender named [" + name + "].", e);
		}

		if (queueConnection != null && queueSession != null && queueSender != null) {
			super.start();
		}
	}

	/**
	 * Close this JMSAppender. Closing releases all resources used by the
	 * appender. A closed appender cannot be re-opened.
	 */
	public synchronized void stop() {
		addInfo("Stopping ActiveMQAppender");

		// The synchronized modifier avoids concurrent append and close operations
		if (!this.started) {
			return;
		}

		this.started = false;

		try {

			if (queueSession != null) {
				queueSession.close();
			}

			if (queueConnection != null) {
				queueConnection.close();
			}

		} catch (Exception e) {
			addError("Error while closing JMSAppender [" + name + "].", e);
		}

		// Help garbage collection
		queueSender = null;
		queueSession = null;
		queueConnection = null;
	}

	/**
	 * This method called by {@link AppenderBase#doAppend} method to do most
	 * of the real appending work.
	 */
	public void append(ILoggingEvent event) {
		addInfo("Logging message. Started:" + isStarted() + " message length:" + event.getMessage().length());

		if (!isStarted()) {
			return;
		}

		try {
			TextMessage msg = queueSession.createTextMessage(event.getMessage());
			queueSender.send(msg);
			successiveFailureCount = 0;
		} catch (Exception e) {
			successiveFailureCount++;
			if (successiveFailureCount > SUCCESSIVE_FAILURE_LIMIT) {
				stop();
			}

			addError("Could not send message in JMSQueueAppender [" + name + "].", e);
		}
	}

	/**
	 * Returns the QueueConnection used for this appender. Only valid after
	 * start() method has been invoked.
	 */
	protected QueueConnection getQueueConnection() {
		return queueConnection;
	}

	/**
	 * Returns the QueueSession used for this appender. Only valid after start()
	 * method has been invoked.
	 */
	protected QueueSession getQueueSession() {
		return queueSession;
	}

	/**
	 * Returns the QueueSender used for this appender. Only valid after start()
	 * method has been invoked.
	 */
	protected QueueSender getQueueSender() {
		return queueSender;
	}
}
