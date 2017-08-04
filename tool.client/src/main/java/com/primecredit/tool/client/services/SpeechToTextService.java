package com.primecredit.tool.client.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
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
import com.primecredit.tool.common.util.HostNameUtil;
import com.primecredit.tool.common.util.WavFileHandler;
import com.primecredit.tool.common.wsobject.request.RecongnitionRequest;
import com.primecredit.tool.common.wsobject.request.TestRequest;
import com.primecredit.tool.common.wsobject.response.RecognitionResponse;
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
	
	@Autowired
	private SpeechStatisticsService speechStatisticsService;

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

			// (99) Export to file Result
			int index = sourceFileName.indexOf(".");
			String filefirstName = sourceFileName.substring(0, index);
			// String ext = sourceFileName.substring(index);
			File textFile = new File(filefirstName + ".txt");

			try (FileWriter fw = new FileWriter(textFile); BufferedWriter bw = new BufferedWriter(fw)) {

				for (int version = 1; version <= 5; version++) {
					bw.write("Version (" + version + ")");
					bw.write("\n");
					for (DiarizationSpeech ds : dsList) {

						bw.write(ds.getName() + " : " + ds.getSpeechTextList().get(version - 1));
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

	public void speechStatistics() {
		List<String> txtFiles = FileUtil.listFiles(systemConfig.getWavPath(), "txt");
		
		Iterator<String> fileIter = txtFiles.iterator();
		while (fileIter.hasNext()) {
			String textFileName = fileIter.next();
			speechStatisticsService.statistics(textFileName);
		}
		
	}
	
	
	public void test() {
		String urlStr = ApplicationConfig.getSpeechRecognitionConvertServiceUrl();
		String sourceFileName = "/Users/maxwellyuen/Documents/samples/__2001214488_2001214476_d488f28966ec39d528f999c2.wav";
		Path path = Paths.get(sourceFileName);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RecongnitionRequest request = new RecongnitionRequest();
		request.setClientMachineId(HostNameUtil.getMachineHostName());
		request.setMillisecond(new Date().getTime());
		request.setFileData(data);

		RestTemplate restTemplate = new RestTemplate();
		RecognitionResponse response = restTemplate.postForObject(urlStr, request, RecognitionResponse.class);

	}
}
