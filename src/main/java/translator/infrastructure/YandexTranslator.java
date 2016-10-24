package translator.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import translator.domain.Language;
import translator.exception.TranslatorException;

import java.io.IOException;

@Component("yandexTranslator")
public class YandexTranslator extends TranslatorImpl {

	private ObjectMapper om = new ObjectMapper(); 
	
	@Value("${yandex.api_key}")
	private String API_KEY;
	
    @Override
    protected HttpRequestBase getHttpRequest(String from, String to, String text, String encodedText) {
		return new HttpGet("https://translate.yandex.net/api/v1.5/tr.json/translate?key="+API_KEY+"&lang="+from+"-"+to+"&text="+encodedText);
    }

    @Override
	protected String getTranslationFrom(String responseAsStr) {
    	try {
			return (String) om.readValue(responseAsStr, YandexResponse.class).text[0];
		} catch (Exception e) {
			throw new TranslatorException("Failed processing "+responseAsStr, e);
		}
    }

	@Override
	protected HttpRequestBase getHttpRequestForLanguageDetection(String text, String encodedText, String hints) {
		return new HttpGet("https://translate.yandex.net/api/v1.5/tr.json/detect?key=" + API_KEY + "&text=" + encodedText + (hints.isEmpty() ? "" : "&hint=" + hints));
	}

	@Override
	protected Language getDetectedLanguageFrom(String responseAsStr) {
		try {
			String langStr = (String) om.readValue(responseAsStr, YandexLanguageDetectionResponse.class).lang;
			return Language.fromString(langStr);
		} catch (IOException e) {
			throw new TranslatorException("Failed processing "+responseAsStr, e);
		}
	}

}

class YandexResponse {
    public String code;
    public String lang;
    public Object[] text;
}

class YandexLanguageDetectionResponse {
	public String code;
	public String lang;
}
