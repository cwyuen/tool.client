package com.primecredit.tool.client;

public class TestSpeakerIdentification {

	public static void main(String args[]) {
		
		/*
		String url = "http://localhost:18761/SpeakerIdentification/diarization";

		String filePath = "/Users/maxwellyuen/Documents/samples/__2001242917_2001242915_a1feac6a2744f1ca7b8f6d57.wav";
		File file = new File(filePath);

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
		HttpEntity multipart = builder.build();
		httpPost.setEntity(multipart);

	
		try {
			CloseableHttpResponse response = client.execute(httpPost);
			String json = EntityUtils.toString(response.getEntity());
			System.out.println(json);
			
			ObjectMapper mapper = new ObjectMapper();
			
			List<DiarizationSpeech> dsList = mapper.readValue(json, new TypeReference<List<DiarizationSpeech>>(){});
			
			for(DiarizationSpeech ds : dsList) {
				System.out.println(ds.getSegmentStart() + " - " + (ds.getSegmentStart()+ ds.getSegmentLen()) + " : " + ds.getName());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

}
