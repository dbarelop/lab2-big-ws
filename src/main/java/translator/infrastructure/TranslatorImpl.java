package translator.infrastructure;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;
import translator.domain.Language;
import translator.domain.LanguageSourceTarget;
import translator.domain.Translator;
import translator.exception.TranslatorException;

abstract class TranslatorImpl implements Translator {

    private static final String ENCODING_UTF_8 = "UTF-8";

    @Override
	@Async
    public AsyncResult<String> translate(LanguageSourceTarget languageSourceTarget, String text) {
        try {
            String encodedText = URLEncoder.encode(text, ENCODING_UTF_8);
            String from = languageSourceTarget.getSource().asStr();
            String to = languageSourceTarget.getTarget().asStr();
            return new AsyncResult<>(translateInternal(from, to, text, encodedText));
        } catch (IOException e) {
            log().error("Problems translating:" + e.getMessage(), e);
            throw new TranslatorException("Problems translating:" + e.getMessage(), e);
        }
    }

    private String translateInternal(String from, String to, String text, String encodedText) throws IOException {
        HttpRequestBase requestBase = getHttpRequest(from, to, text, encodedText);
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse response = httpclient.execute(requestBase);
        HttpEntity responseEntity = response.getEntity();
        String responseAsStr = transformToString(responseEntity);
        if (StringUtils.hasText(responseAsStr)) {
            return getTranslationFrom(responseAsStr);
        }
        return "";
    }

    protected abstract HttpRequestBase getHttpRequest(String from, String to, String text, String encodedText);

    /**
     * Get the translation text from the response
     *
     * @param responseAsStr: Response string that contains the translation
     * @return response
     */
    protected abstract String getTranslationFrom(String responseAsStr);

    @Override
    @Async
    public AsyncResult<Language> detectLanguage(String text, Collection<Language> hints) {
        try {
            String encodedText = URLEncoder.encode(text, ENCODING_UTF_8);
            String hintsStr = hints == null ? "" : hints.stream().map(lang -> lang.asStr()).collect(Collectors.joining(","));
            return new AsyncResult<>(detectLanguageInternal(text, encodedText, hintsStr));
        } catch (IOException e) {
            log().error("Problems detecting language:" + e.getMessage(), e);
            throw new TranslatorException("Problems detecting language:" + e.getMessage(), e);
        }
    }

    private Language detectLanguageInternal(String text, String encodedText, String hints) throws IOException {
        HttpRequestBase requestBase = getHttpRequestForLanguageDetection(text, encodedText, hints);
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse response = httpclient.execute(requestBase);
        HttpEntity responseEntity = response.getEntity();
        String responseAsStr = transformToString(responseEntity);
        if (StringUtils.hasText(responseAsStr)) {
            return getDetectedLanguageFrom(responseAsStr);
        }
        return null;
    }

    protected abstract HttpRequestBase getHttpRequestForLanguageDetection(String text, String encodedText, String hints);

    protected abstract Language getDetectedLanguageFrom(String responseAsStr);

    private static String transformToString(HttpEntity entity) throws IOException {
        if (entity == null) {
            return "";
        }
        try (
        	Scanner scanner = new Scanner(entity.getContent(), ENCODING_UTF_8)
        ) {
        	return scanner.useDelimiter("\\A").next();
        }
    }

}
