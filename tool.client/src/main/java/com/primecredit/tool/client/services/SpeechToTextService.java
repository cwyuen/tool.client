package com.primecredit.tool.client.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	private SpeechStatisticsService speechStatisticsService;
	
	@Autowired
	private NaturalLanguageService naturalLanguageService;
	
	@Autowired
	private TextReportService textReportService;

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
			
			
			if(systemConfig.isDebugMode()) {
				
				idx++;
				if(idx > 5) {
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

	public void speechStatistics() {
		List<String> txtFiles = FileUtils.listFiles(systemConfig.getWavPath(), "txt");
		
		Iterator<String> fileIter = txtFiles.iterator();
		while (fileIter.hasNext()) {
			String textFileName = fileIter.next();
			//speechStatisticsService.statistics(textFileName);
			List<NaturaLangEntry> entites = naturalLanguageService.analyzeEntities(textFileName);
			
			for(NaturaLangEntry ne : entites) {
				
			}
		}
		
	}
	
}
