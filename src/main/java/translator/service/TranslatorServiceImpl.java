package translator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import translator.domain.Language;
import translator.domain.LanguageSourceTarget;
import translator.domain.TranslatedText;
import translator.domain.Translator;
import translator.exception.TranslatorException;

@Service
public class TranslatorServiceImpl implements TranslatorService {
    @Autowired
    Translator translator;
 
    @Override
	public TranslatedText translate(String langFrom, String langTo, String text) {
        LanguageSourceTarget languageSourceTarget = new LanguageSourceTarget(Language.fromString(langFrom), Language.fromString(langTo));
        if (languageSourceTarget.sourceAndTargeAreEquals()) {
            throw new TranslatorException("The languages from and to must be different.");
        }
        Future<String> translatorResult = translator.translate(languageSourceTarget, text);
        TranslatedText response = new TranslatedText();
        response.setFrom(languageSourceTarget.getSourceAsStr());
        response.setTo(languageSourceTarget.getTargetAsStr());
        response.setTranslation(getTranslation(translatorResult));
        return response;
    }

    @Override
    public Language detectLanguage(String text, Collection<Language> hints) {
        Future<Language> detectionResult = translator.detectLanguage(text, hints);
        return getDetection(detectionResult);
    }

    private String getTranslation(Future<String> futureResult) {
        try {
            return futureResult.get();
        } catch (Throwable e) {
            log().error("Problems getting the translation", e);
            return "Error:" + e.getMessage();
        }
    }

    private Language getDetection(Future<Language> futureResult) {
        try {
            return futureResult.get();
        } catch (InterruptedException | ExecutionException e) {
            log().error("Problems detecting the language", e);
            return null;
        }
    }
}
