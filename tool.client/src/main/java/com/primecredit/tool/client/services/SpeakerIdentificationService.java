package com.primecredit.tool.client.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.primecredit.tool.common.domain.DiarizationSpeech;
import com.primecredit.tool.common.parameter.ApplicationConfig;
import com.primecredit.tool.common.util.HostNameUtils;
import com.primecredit.tool.common.wsobject.request.DiarizationRequest;
import com.primecredit.tool.common.wsobject.response.DiarizationResponse;


@Service
public class SpeakerIdentificationService {
	
	private static Logger logger = LoggerFactory.getLogger(SpeakerIdentificationService.class);
	
	public List<DiarizationSpeech> diarization(String sourceFileName) throws Exception {

		String urlStr = ApplicationConfig.getSpeakerIdentificationDiarizationServiceUrl();
		//URI uri = new URL(urlStr).toURI();
		logger.info("diarization source: " + sourceFileName);
		Path path = Paths.get(sourceFileName);
		
		byte[] data = Files.readAllBytes(path);
		
		DiarizationRequest request = new DiarizationRequest();
		request.setClientMachineId(HostNameUtils.getMachineHostName());
		request.setMillisecond(new Date().getTime());
		request.setFileData(data);
		
		RestTemplate restTemplate = new RestTemplate();
		DiarizationResponse response = restTemplate.postForObject(urlStr, request, DiarizationResponse.class);


		List<DiarizationSpeech> dsList = response.getDsList();
		for(DiarizationSpeech ds : dsList) {
			ds.setSourceFileName(sourceFileName);
		}
        
		return dsList;
	}
}
