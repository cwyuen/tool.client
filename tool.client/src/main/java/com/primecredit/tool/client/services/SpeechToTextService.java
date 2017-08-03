package com.primecredit.tool.client.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.primecredit.tool.client.config.SystemConfig;
import com.primecredit.tool.common.domain.DiarizationSpeech;
import com.primecredit.tool.common.parameter.ApplicationConfig;
import com.primecredit.tool.common.util.FileUtil;
import com.primecredit.tool.common.util.WavFileHandler;
import com.primecredit.tool.common.wsobject.request.TestRequest;
import com.primecredit.tool.common.wsobject.response.TestResponse;

@Service
public class SpeechToTextService {

	private static Logger logger = LoggerFactory.getLogger(SpeechToTextService.class);

	@Autowired
	private SystemConfig systemConfig;

	@Autowired
	private SpeakerIdentificationService speakerIdentificationService;

	@Autowired
	private SpeechRecongnitionService speechRecongnitionService;

	public void run() throws Exception {
		// (0) init var
		WavFileHandler wavFileHandler = WavFileHandler.getInstance();

		// (1) Remove all temp folder
		FileUtil.deleteFiles(systemConfig.getTempPath());

		// (2) List all Wav file
		List<String> wavFiles = wavFileHandler.listWavFiles(systemConfig.getWavPath());

		Iterator<String> wavIter = wavFiles.iterator();
		while (wavIter.hasNext()) {
			String sourceFileName = wavIter.next();
			logger.info("File Name: {}", sourceFileName);
			;

			File sourceFile = new File(sourceFileName);
			String shortFileName = sourceFile.getName();

			// (3) Speaker Diarization
			List<DiarizationSpeech> dsList = speakerIdentificationService.diarization(sourceFileName);

			// (4) Split Wav file by speaker
			if (dsList != null) {
				int count = 1;
				for (DiarizationSpeech ds : dsList) {
					int startLen = ds.getSegmentStart();
					int segmentLen = ds.getSegmentLen();
					String distFile = systemConfig.getTempPath() + String.valueOf(count) + "_" + shortFileName;
					ds.setSourceFileName(distFile);
					wavFileHandler.copyWavAudioBySecond(sourceFileName, distFile, (double) startLen,
							(double) segmentLen);
					count++;

				}
			}

			// (5) Call Speech Convert Service
			for (DiarizationSpeech ds : dsList) {
				List<String> speechTextList = speechRecongnitionService.convert(ds.getSourceFileName());
				ds.setSpeechTextList(speechTextList);
			}
			

			//(99) Export to file Result
			int index = sourceFileName.indexOf(".");
			String filefirstName = sourceFileName.substring(0, index);
			//String ext = sourceFileName.substring(index);
			File textFile = new File(filefirstName + ".txt");
			
			try (FileWriter fw = new FileWriter(textFile); BufferedWriter bw = new BufferedWriter(fw)) {
				
				for(int version = 1; version<=5; version++) {
					bw.write("Version (" + version + ")");
					bw.write("\n");
					for(DiarizationSpeech ds: dsList) {
						
						bw.write(ds.getName() + " : " + ds.getSpeechTextList().get(version-1));
						bw.write("\n");

					}
					
					bw.write("==============================================");
					bw.write("\n");
					bw.write("\n");
				}
				bw.close();
				fw.close();
			}
			
			
		}
		
	}

	public void test() {
		String urlStr = ApplicationConfig.getSpeakerIdentificationDiarizationServiceUrl();
		System.out.println(urlStr);

		try {
			URI uri = new URL(urlStr).toURI();
			TestRequest tr = new TestRequest();
			tr.setName("Test 001");
			tr.getValues().add("001");
			tr.getValues().add("002");
			tr.getValues().add("003");
			tr.getValues().add("004");

			RestTemplate restTemplate = new RestTemplate();
			// restTemplate.getMessageConverters().add(new
			// MappingJacksonHttpMessageConverter());
			// restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
			TestResponse testResponse = restTemplate.postForObject(uri, tr, TestResponse.class);

			System.out.println(testResponse.getName());
			for (String val : testResponse.getValues()) {
				System.out.println(val);
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
