package com.primecredit.tool.client.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.primecredit.tool.client.config.SystemConfig;
import com.primecredit.tool.common.domain.DiarizationSpeech;
import com.primecredit.tool.common.domain.NaturaLangEntry;
import com.primecredit.tool.common.util.FileUtils;
import com.primecredit.tool.common.util.WavFileHandler;

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
	private NaturalLanguageService naturalLanguageService;
	
	@Autowired
	private FrequencyWordService frequencyWordService;

	@Autowired
	private TextReportService textReportService;

	@PostConstruct
	public void onStartup() {
		//Remove all temp folder
		FileUtils.deleteFiles(systemConfig.getTempPath());
	}

	//@Scheduled(fixedRate = 60000)
	public void scheduleSpeech2Text() {
		logger.info("Start scheduleSpeech2Text every 60 secs");

		// (0) init var
		WavFileHandler wavFileHandler = WavFileHandler.getInstance();

		// (1) List all Wav file
		List<String> wavFiles = wavFileHandler.listWavFiles(systemConfig.getWavPath());

		// (2) Only handle 1 file
		if (wavFiles.size() > 0) {
			String sourceFileName = wavFiles.get(0);
			logger.info("File Name: {}", sourceFileName);

			File sourceFile = new File(sourceFileName);
			String shortFileName = sourceFile.getName();

			// (3) Speaker Diarization
			List<DiarizationSpeech> dsList = null;
			try {
				dsList = speakerIdentificationService.diarization(sourceFileName);
			} catch (Exception e) {
				logger.error("Exception 1 - SpeechToTextService.scheduleSpeech2Text: {}", e.getMessage());
			}

			// (4) Split Wav file by speaker
			if (dsList != null) {
				int count = 1;
				for (DiarizationSpeech ds : dsList) {
					int startLen = ds.getSegmentStart();
					int segmentLen = ds.getSegmentLen();
					String distFile = systemConfig.getTempPath() + String.valueOf(count) + "_" + shortFileName;
					ds.setSourceFileName(distFile);
					try {
						wavFileHandler.copyWavAudioBySecond(sourceFileName, distFile, (double) startLen,
								(double) segmentLen);
					} catch (Exception e) {
						logger.error("Exception 2 - SpeechToTextService.scheduleSpeech2Text: {}", e.getMessage());
					}
					count++;

				}
			}

			// (5) Call Speech Convert Service
			for (DiarizationSpeech ds : dsList) {
				List<String> speechTextList = speechRecongnitionService.convert(ds.getSourceFileName());
				ds.setSpeechTextList(speechTextList);
			}

			// (6) Export to file Result
			textReportService.exportSpeech2TextReport(sourceFileName, dsList);

			// (7) Move file to DONE folder
			FileUtils.moveFile(sourceFileName, systemConfig.getDonePath());

		}

		logger.info("End scheduleSpeech2Text every 60 secs");
	}

	

	
	public void run() throws Exception {
		// (0) init var
		WavFileHandler wavFileHandler = WavFileHandler.getInstance();

		// (1) Remove all temp folder
		FileUtils.deleteFiles(systemConfig.getTempPath());

		// (2) List all Wav file
		List<String> wavFiles = wavFileHandler.listWavFiles(systemConfig.getWavPath());

		Iterator<String> wavIter = wavFiles.iterator();

		int idx = 0;
		while (wavIter.hasNext()) {

			if (systemConfig.isDebugMode()) {

				idx++;
				if (idx > 5) {
					break;
				}
			}

			String sourceFileName = wavIter.next();

			logger.info("File Name: {}", sourceFileName);

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
			textReportService.exportSpeech2TextReport(sourceFileName, dsList);

		}

	}

	
	@Scheduled(fixedRate = 70000)
	public void scheduleSpeechStatistics() {
		// (1) List all  file
		List<String> txtFiles = FileUtils.listFiles(systemConfig.getWavPath(), "txt");

		if(txtFiles.size() > 0) {
			// (2) Only handle 1 file
			String textFileName = txtFiles.get(0);
			List<String> textLines =  extractFileContent(textFileName);
			int lineNo = 1;
			
			for(String text : textLines) {
				
				if(naturalLanguageService.isValidLine(text)) {
				
					String[] parts = text.split(" : ");
					StringBuilder sbTemp = new StringBuilder();
					
					//skip part 1
					for(int i=1; i<parts.length; i++) {
						sbTemp.append(parts[i]);
					}
					String content = sbTemp.toString();
				
					//(3) Check Natural Language
					List<NaturaLangEntry> entites = naturalLanguageService.analyzeEntities(content);
					
					//(4) Import Natural Language to Neo4j DB
					for(NaturaLangEntry entry : entites) {
						naturalLanguageService.importNaturaLangEntites(textFileName, lineNo, entry);
					}
					
					//(5) Frequency Word statistics
					frequencyWordService.frequencyWordStatistics(textFileName, lineNo, content);
				}
				
				lineNo++;
			}	
			
			
			// Move file to DONE folder
			FileUtils.moveFile(textFileName, systemConfig.getDonePath());
		}
	}
	
	private List<String> extractFileContent(String sourceFileName){
		List<String> results = new ArrayList<>();
		File sourceFile = new File(sourceFileName);
		try(FileReader fr = new FileReader(sourceFile);
				BufferedReader br = new BufferedReader(fr)){
			
			
			String lineStr = null;
			while ( (lineStr = br.readLine()) != null ) {
				
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
