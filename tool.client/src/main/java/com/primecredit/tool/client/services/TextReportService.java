package com.primecredit.tool.client.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.primecredit.tool.common.domain.DiarizationSpeech;

@Service
public class TextReportService {
	private static Logger logger = LoggerFactory.getLogger(TextReportService.class);
	
	public boolean exportSpeech2TextReport(String sourceFileName, List<DiarizationSpeech>  dsList) {
		boolean result = true;
		int index = sourceFileName.indexOf(".");
		String filefirstName = sourceFileName.substring(0, index);
		File textFile = new File(filefirstName + ".txt");

		try (FileWriter fw = new FileWriter(textFile); BufferedWriter bw = new BufferedWriter(fw)) {

			for (int version = 1; version <= 20; version++) {
				bw.write("Version (" + version + ")");
				bw.write("\n");
				for (DiarizationSpeech ds : dsList) {

					bw.write(ds.getSegmentStart() + " - "  + ds.getName() + " : " + ds.getSpeechTextList().get(version - 1));
					bw.write("\n");

				}

				bw.write("\n");
			}
		} catch (IOException e) {
			result = false;
			logger.error("IO Exceptiion - TextReportService.exportSpeech2TextReport: " +e.getMessage());
		}
		
		return result;
	}
}
