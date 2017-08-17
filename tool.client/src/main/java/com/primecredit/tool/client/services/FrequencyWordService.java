package com.primecredit.tool.client.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.primecredit.tool.common.parameter.ApplicationConfig;
import com.primecredit.tool.common.util.HostNameUtils;
import com.primecredit.tool.common.wsobject.request.FrequencyWordRequest;
import com.primecredit.tool.common.wsobject.response.FrequencyWordResponse;

@Service
public class FrequencyWordService {
	private static Logger logger = LoggerFactory.getLogger(FrequencyWordService.class);
	
	public boolean frequencyWordStatistics(String sourceFile, int line, String text) {
		
		FrequencyWordRequest request = new FrequencyWordRequest();
		String urlStr = ApplicationConfig.getSpeechFreqWordStatisticsServiceUrl();

		request.setClientMachineId(HostNameUtils.getMachineHostName());
		request.setMillisecond(new Date().getTime());
		request.setSourceFile(sourceFile);
		request.setLine(line);
		request.setText(text);
		
		logger.info("Send Request to {}, {}, {}, {}",urlStr, text, sourceFile, line);
		
		RestTemplate restTemplate = new RestTemplate();
		FrequencyWordResponse response = restTemplate.postForObject(urlStr, request, FrequencyWordResponse.class);
		
	
		return response.isSuccess();
	}
	
}
