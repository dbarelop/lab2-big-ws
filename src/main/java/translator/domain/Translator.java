package translator.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.Collection;

public interface Translator {

    AsyncResult<String> translate(LanguageSourceTarget languageSourceTarget, String text);

    AsyncResult<Language> detectLanguage(String text, Collection<Language> hints);

    default Logger log() {
        return LoggerFactory.getLogger(getClass());
    }

}
