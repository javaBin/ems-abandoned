package no.java.ems.client.swing.binding;

import no.java.ems.domain.Language;
import org.jdesktop.beansbinding.Converter;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class LanguageConverter extends Converter<Language, Boolean> {

    public Boolean convertForward(final Language language) {
        return language == null || "en".equals(language.getIsoCode()) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Language convertReverse(final Boolean foreign) {
        return foreign == null || foreign ? new Language("en") : new Language("no");
    }

}
