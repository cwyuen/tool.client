package com.primecredit.tool.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.primecredit.tool.client.services.SpeechToTextService;

@SpringBootApplication
public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		logger.debug("SpeechWsApplication - Start");
		
		ApplicationContext context = SpringApplication.run(Main.class, args);
		
		SpeechToTextService speechToTextService = context.getBean(SpeechToTextService.class);
		try {
			//speechToTextService.run();
			speechToTextService.speechStatistics();
		} catch (Exception e) {
			logger.error("Exception - Main.main: " + e.getMessage());
		}
		
	}
}
