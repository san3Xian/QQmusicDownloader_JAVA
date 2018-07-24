package Qmusic;

import java.io.*;
import javax.sound.sampled.*;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

/**
 * 调用mp3 spi实现对mp3文件解码播放<br />
 * jar包地址 http://www.javazoom.net/mp3spi/sources.html<br />
 * document http://www.javazoom.net/mp3spi/documents.html<br />
 * !本类在本项目中并没有用到！因为不能解析m4a文件
 */
public class AudioPlay {
	
	public static void main(String arg0[])
			throws IOException, FileNotFoundException, UnsupportedAudioFileException, LineUnavailableException {
		System.out.println("meida player test");
		/* 文件播放debug */
		String debugUrl2 = "D:\\user\\Documents\\eclipse-workspace\\QQmusicDownloader\\tmp\\BINGBIAN病变 (女声版).mp3";
		File file = new File(debugUrl2);
		AudioInputStream in = AudioSystem.getAudioInputStream(file);
		AudioInputStream din = null;
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
				baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);
		rawplay(decodedFormat, din);
		in.close();

	}

	public static void urlPlay(String url, String title)
			throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		File file = urlToFile(url, title);
		AudioInputStream in = AudioSystem.getAudioInputStream(file);
		AudioInputStream din = null;
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
				baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);
		rawplay(decodedFormat, din);
		in.close();
	}

	private static void rawplay(AudioFormat targetFormat, AudioInputStream din)
			throws IOException, LineUnavailableException {
		byte[] data = new byte[4096];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1) {
				nBytesRead = din.read(data, 0, data.length);
				if (nBytesRead != -1)
					nBytesWritten = line.write(data, 0, nBytesRead);
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}

	private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}

	private static File urlToFile(String url, String title) throws IOException {
		System.out.println("Temp directory: " + System.getProperty("user.dir") + "\\tmp\\" + title);
		File returnFile = new File(System.getProperty("user.dir") + "\\tmp\\" + title);
		if (!returnFile.exists()) {
			returnFile.createNewFile();
		}
		OutputStream output = null;
		BufferedOutputStream bufferedOutput = null;
		output = new FileOutputStream(returnFile);
		bufferedOutput = new BufferedOutputStream(output);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpUriRequest httpUrlRe = new HttpGet(url);
		httpUrlRe.addHeader(new BasicHeader("Cookie", "qqmusic_fromtag=66"));
		CloseableHttpResponse response = httpClient.execute(httpUrlRe);
		if (response != null) {
			HttpEntity entity = response.getEntity();
			bufferedOutput.write(EntityUtils.toByteArray(entity));
			bufferedOutput.close();
		}
		if (response != null) {
			response.close();
		}
		if (httpClient != null) {
			httpClient.close();
		}
		return returnFile;
	}
}