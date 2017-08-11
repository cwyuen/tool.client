package com.primecredit.tool.client.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
//@PropertySource("classpath:config_windows.properties")
@PropertySource("classpath:config_macos.properties")
public class SystemConfig {
	@Value("${system.debug}")
	private String systemDebug;

	@Value("${dictionary.folder}")
	private String dictionaryFolder;

	@Value("${dictionary.single.word}")
	private String dictionarySingleWord;

	@Value("${dictionary.mix.word}")
	private String dictionaryMixWord;

	@Value("${temp.path}")
	private String tempPath;

	@Value("${wav.path}")
	private String wavPath;

	@Value("${speech.service")
	private String speechService;

	public String getDictionaryFolder() {
		return dictionaryFolder;
	}

	public void setDictionaryFolder(String dictionaryFolder) {
		this.dictionaryFolder = dictionaryFolder;
	}

	public String getDictionarySingleWord() {
		return dictionarySingleWord;
	}

	public void setDictionarySingleWord(String dictionarySingleWord) {
		this.dictionarySingleWord = dictionarySingleWord;
	}

	public String getDictionaryMixWord() {
		return dictionaryMixWord;
	}

	public void setDictionaryMixWord(String dictionaryMixWord) {
		this.dictionaryMixWord = dictionaryMixWord;
	}

	public String getDictionarySingleWordPath() {
		return dictionaryFolder + File.separatorChar + dictionarySingleWord;
	}

	public String getDictionaryMixWordPath() {
		return dictionaryFolder + File.separatorChar + dictionaryMixWord;
	}

	public String getSystemDebug() {
		return systemDebug;
	}

	public void setSystemDebug(String systemDebug) {
		this.systemDebug = systemDebug;
	}

	public boolean isDebugMode() {
		if ("Y".equalsIgnoreCase(systemDebug)) {
			return true;
		}

		return false;
	}

	public String getTempPath() {
		return tempPath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public String getWavPath() {
		return wavPath;
	}

	public void setWavPath(String wavPath) {
		this.wavPath = wavPath;
	}

	public String getWorkingPath() {
		return tempPath;
	}

	public String getWavSourcePath() {
		return wavPath;
	}

	public boolean isGoogleSpeechApiEnable() {
		if ("Google".equalsIgnoreCase(speechService)) {
			return true;
		}
		return false;
	}

}
