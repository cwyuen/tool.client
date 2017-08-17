package com.primecredit.tool.client.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.primecredit.tool.common.parameter.ApplicationConfig;
import com.primecredit.tool.common.util.HostNameUtils;
import com.primecredit.tool.common.wsobject.request.SpeechStatisticsRequest;
import com.primecredit.tool.common.wsobject.response.SpeechStatisticsResponse;

@Service
public class SpeechStatisticsService {

	private static Logger logger = LoggerFactory.getLogger(SpeechStatisticsService.class);
	
	public boolean statistics(String sourceFileName) {
		
		List<String> speechTexts = extractFileContent(sourceFileName);
		SpeechStatisticsRequest request = new SpeechStatisticsRequest();
		
		
		request.setClientMachineId(HostNameUtils.getMachineHostName());
		request.setMillisecond(new Date().getTime());
		request.setSpeechTexts(speechTexts);
		request.setSourceFileName(sourceFileName);
		
		String urlStr = "";

		RestTemplate restTemplate = new RestTemplate();
		SpeechStatisticsResponse response = restTemplate.postForObject(urlStr, request, SpeechStatisticsResponse.class);
		
		return response.isSuccess();
	}
	
	private List<String> extractFileContent(String sourceFileName){
		List<String> results = new ArrayList<>();
		File sourceFile = new File(sourceFileName);
		try(FileReader fr = new FileReader(sourceFile);
				BufferedReader br = new BufferedReader(fr)){
			
			
			String lineStr = null;
			while ( (lineStr = br.readLine()) != null ) {
				
				String[] parts = lineStr.split(" : ");
				
				//Skip first part (speaker ID)
				StringBuilder sbTmp = new StringBuilder();
				if(parts.length > 1) {
					for(int i=1; i<parts.length ; i++) {
						sbTmp.append(parts[i]);
					}
				}
				
				String text = sbTmp.toString();
				if(!text.trim().equals("")) {
					results.add(sbTmp.toString());
				}
			}

			
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		
		return results;
	}
}
