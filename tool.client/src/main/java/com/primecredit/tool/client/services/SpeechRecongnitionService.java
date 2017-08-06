package com.primecredit.tool.client.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.primecredit.tool.common.parameter.ApplicationConfig;
import com.primecredit.tool.common.util.HostNameUtils;
import com.primecredit.tool.common.wsobject.request.RecongnitionRequest;
import com.primecredit.tool.common.wsobject.response.RecognitionResponse;

@Service
public class SpeechRecongnitionService {

	private static Logger logger = LoggerFactory.getLogger(SpeechRecongnitionService.class);
	
	public List<String> convert(String sourceFileName)  {

		String urlStr = ApplicationConfig.getSpeechRecognitionConvertServiceUrl();
		
		Path path = Paths.get(sourceFileName);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			logger.error("IOException - SpeechRecongnitionService.convert: " + e.getMessage());			
		}
		
		RecongnitionRequest request = new RecongnitionRequest();
		request.setClientMachineId(HostNameUtils.getMachineHostName());
		request.setMillisecond(new Date().getTime());
		request.setFileData(data);
		logger.info("Call SpeechRecognitionConvertService: " + sourceFileName);
		RestTemplate restTemplate = new RestTemplate();
		RecognitionResponse response = restTemplate.postForObject(urlStr, request, RecognitionResponse.class);


		return response.getSpeechTextList();
	}

	
}
