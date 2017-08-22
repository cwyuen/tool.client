package com.primecredit.tool.client.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.primecredit.tool.common.domain.NaturaLangEntry;
import com.primecredit.tool.common.parameter.ApplicationConfig;
import com.primecredit.tool.common.util.HostNameUtils;
import com.primecredit.tool.common.wsobject.request.NaturalLangRequest;
import com.primecredit.tool.common.wsobject.response.NaturalLangResponse;

@Service
public class NaturalLanguageService {
	private static Logger logger = LoggerFactory.getLogger(NaturalLanguageService.class);
	
	public List<NaturaLangEntry> analyzeEntities(String textLine) {
		
		List<NaturaLangEntry> results = new ArrayList<>();
		NaturalLangResponse response = analyzeNaturalLangRequest(textLine);

		if(response.isSuccess()) {
			for(NaturaLangEntry entry : response.getEntites()) {
				logger.info(entry.getName());
				results.add(entry);
			}	
		}
	
		return results;
		
		
	}
	
	private NaturalLangResponse analyzeNaturalLangRequest(String text) {
		NaturalLangRequest request = new NaturalLangRequest();
		
		String urlStr = ApplicationConfig.getNaturalLanguageAnalyzeEntitiesUrl();
		
		request.setClientMachineId(HostNameUtils.getMachineHostName());
		request.setMillisecond(new Date().getTime());
		request.setInput(text);

		RestTemplate restTemplate = new RestTemplate();
		NaturalLangResponse response = restTemplate.postForObject(urlStr, request, NaturalLangResponse.class);
		
		return response;
	}
	
	public boolean importNaturaLangEntites(String sourceFile, int line, NaturaLangEntry entry) {
		NaturalLangRequest request = new NaturalLangRequest();
		String urlStr = ApplicationConfig.getSpeechNatualLangStatisticsServiceUrl();

		request.setClientMachineId(HostNameUtils.getMachineHostName());
		request.setMillisecond(new Date().getTime());
		request.setSourceFile(sourceFile);
		request.setLine(line);
		request.setEntry(entry);
		
		RestTemplate restTemplate = new RestTemplate();
		NaturalLangResponse response = restTemplate.postForObject(urlStr, request, NaturalLangResponse.class);
		
		return response.isSuccess();
	}
	
	public boolean isValidLine(String lineStr) {
		String[] parts = lineStr.split(" : ");
		
		if(parts.length < 1) {
			return false;
		}
		if(lineStr.startsWith("Version (")) {
			return false;
		}
		
		if(lineStr.trim().equals("")) {
			return false;
		}
		
		return true;
	}
	
	
		
}
