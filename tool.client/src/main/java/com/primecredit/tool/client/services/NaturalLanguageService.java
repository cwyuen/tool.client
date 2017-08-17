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

import com.primecredit.tool.common.domain.NaturaLangEntry;
import com.primecredit.tool.common.parameter.ApplicationConfig;
import com.primecredit.tool.common.util.HostNameUtils;
import com.primecredit.tool.common.wsobject.request.NaturalLangRequest;
import com.primecredit.tool.common.wsobject.response.NaturalLangResponse;

@Service
public class NaturalLanguageService {
	private static Logger logger = LoggerFactory.getLogger(NaturalLanguageService.class);
	
	public List<NaturaLangEntry> analyzeEntities(String sourceFileName) {
		
		List<NaturaLangEntry> results = new ArrayList<>();
		
		List<String> speechTexts = extractFileContent(sourceFileName);
		
		int line = 1;
		for(String text : speechTexts) {
			
			if(isValidLine(text)) {
				
				String[] parts = text.split(" : ");
				StringBuilder sbTmp = new StringBuilder();
				for(int p=1; p<parts.length; p++) {
					sbTmp.append(parts[p]);
				}
				
				NaturalLangResponse response = analyzeNaturalLangRequest(sbTmp.toString());
				
				if(response.isSuccess()) {
					for(NaturaLangEntry entry : response.getEntites()) {
						System.out.println(entry.getName());
						importNaturaLangEntites(sourceFileName, line, entry);
					}	
				}
			}
			
		
			line++;
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
	
	private boolean isValidLine(String lineStr) {
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
	
	private List<String> extractFileContent(String sourceFileName){
		List<String> results = new ArrayList<>();
		File sourceFile = new File(sourceFileName);
		try(FileReader fr = new FileReader(sourceFile);
				BufferedReader br = new BufferedReader(fr)){
			
			
			String lineStr = null;
			while ( (lineStr = br.readLine()) != null ) {
				
				
				/*
				String[] parts = lineStr.split(" : ");
				
				//Skip first part (speaker ID)
				
				if(parts.length > 1) {
					for(int i=1; i<parts.length ; i++) {
						sbTmp.append(parts[i]);
					}
				}
				
				String text = sbTmp.toString();
				if(!text.trim().equals("")) {
					results.add(sbTmp.toString());
				}
				*/
				results.add(lineStr);
			}

			
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		return results;
	}
		
}
