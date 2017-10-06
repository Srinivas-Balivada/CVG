package com.cih.common.controller;

import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/rest")
public class FBWebhookController {

	public static FBWebhookController instance;

	private CopyOnWriteArrayList<FBSocket> fbSockets = new CopyOnWriteArrayList<FBSocket>();

	public FBWebhookController() {
		instance = this;
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.GET)
	public String getMsg(@PathVariable String name, ModelMap model) {

		model.addAttribute("msg", name);
		return "list";
	}

	@RequestMapping(value = "/fbpage", method = RequestMethod.GET)
	public ResponseEntity<String> getfacebookChallengePage(@RequestParam(value = "hub.mode") String mode,
			@RequestParam(value = "hub.verify_token") String verify_token,
			@RequestParam(value = "hub.challenge") String challenge) {
            System.out.println("[FBWebhookController] [getfacebookChallengePage] Start of method fbtoken");
		if (mode.equalsIgnoreCase("subscribe") && verify_token.equalsIgnoreCase("CVG_Token")) {
			return new ResponseEntity<String>(challenge, HttpStatus.OK);
		}
		return new ResponseEntity<String>("Challenge Failed", HttpStatus.FORBIDDEN);
	}

	@RequestMapping(value = "/fbpage", method = RequestMethod.POST)
	public ResponseEntity<String> facebookData(@RequestBody String jsonData) throws Exception {
		   System.out.println("[FBWebhookController] [facebookData] [fbreceive] Start of method jsonData "+jsonData);
		try {
			if (fbSockets.size() > 0) {
				System.out.println("[FBWebhookController] [facebookData] [fbreceive] FBSocket count=" + fbSockets.size());
				for (FBSocket socket : fbSockets) {
					socket.forwardMessage(jsonData);
				}
			} else {
				System.out.println("No connections.");
			}
			return new ResponseEntity<String>("Message Received ", HttpStatus.OK);
		} catch (Exception e) {

		}
		return new ResponseEntity<String>("Message Failed", HttpStatus.FORBIDDEN);
	}

	public void addSocket(FBSocket fbSocket) {
		fbSockets.add(fbSocket);
	}

	public void removeSocket(FBSocket fbSocket) {
		fbSockets.remove(fbSocket);
	}

}