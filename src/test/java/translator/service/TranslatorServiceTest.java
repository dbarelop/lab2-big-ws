package translator.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import translator.Application;
import translator.domain.Language;
import translator.domain.TranslatedText;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TranslatorServiceTest {

    @Autowired
    TranslatorService translatorService;

    @Test
    public void translateTest() throws Exception {
        TranslatedText translatedText = translatorService.translate("en", "es", "This is a test of translation service");
        assertEquals("Esto es una prueba de servicio de traducción",translatedText.getTranslation());
    }

    @Test
    public void detectTest() {
        final String TEXT = "This is a test of the language detection service";
        Language detectedLanguage = translatorService.detectLanguage(TEXT, null);
        assertEquals(detectedLanguage, Language.ENGLISH);
    }

    @Test
    public void translateAndDetectTest() {
        final String TEXT = "Esto es una prueba del servicio de traducción y detección de idioma";
        final Language FROM = Language.SPANISH;
        final Language TO = Language.RUSSIAN;
        Language detectedFromLanguage = translatorService.detectLanguage(TEXT, null);
        assertEquals(detectedFromLanguage, FROM);
        TranslatedText translatedText = translatorService.translate(FROM.asStr(), TO.asStr(), TEXT);
        Language detectedToLanguage = translatorService.detectLanguage(translatedText.getTranslation(), null);
        assertEquals(detectedToLanguage, TO);
    }

}
