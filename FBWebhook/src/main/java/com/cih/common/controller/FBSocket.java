package com.cih.common.controller;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@ServerEndpoint("/chat")
public class FBSocket {

	private Session session;

	/**
	 * @OnOpen allows us to intercept the creation of a new session. The session
	 *         class allows us to send data to the user. In the method onOpen,
	 *         we'll let the user know that the handshake was successful.
	 */
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		FBWebhookController.instance.addSocket(this);
		System.out.println("[FBSocket] [onOpen]  " + session.getId() + " has opened a connection");
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		try {
			System.out.println("[FBSocket] [onMessage]   Sending Text:" + message);
			RestTemplate restTemplate = new RestTemplate();
			String url = "https://graph.facebook.com/v2.6/me/messages?access_token=CVG_Token";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<String>(message, headers);
			String resp = restTemplate.postForObject(url, entity, String.class);
			System.out.println("[FBSocket] [onMessage]   response :" + resp);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The user closes the connection.
	 * 
	 * Note: you can't send messages to the client from this method
	 */
	@OnClose
	public void onClose(Session session) {
		FBWebhookController.instance.removeSocket(this);
		System.out.println("[FBSocket] [onClose]  Session " + session.getId() + " has ended");
	}

	@OnError
	public void onError(Throwable e) {
		System.out.println("[FBSocket] [onError]  Session error");
		e.printStackTrace();
	}

	public void forwardMessage(String msg) {
		try {
			System.out.println("[FBSocket] [forwardMessage]  Message is "+msg);
			session.getBasicRemote().sendText(msg.toString());
		} catch (Exception ex) {
			System.out.println("[FBSocket] [forwardMessage]  Message is "+ex.getMessage());
			ex.printStackTrace();
		}
	}
}