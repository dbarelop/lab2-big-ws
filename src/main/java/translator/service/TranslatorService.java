package translator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import translator.domain.Language;
import translator.domain.TranslatedText;

import java.util.Collection;

public interface TranslatorService {

    TranslatedText translate(String langFrom, String langTo, String text);

    Language detectLanguage(String text, Collection<Language> hints);

    default Logger log() {
        return LoggerFactory.getLogger(getClass());
    }
}
