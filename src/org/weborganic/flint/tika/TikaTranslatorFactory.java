package org.weborganic.flint.tika;

import java.util.ArrayList;
import java.util.List;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.parser.AutoDetectParser;
import org.weborganic.flint.content.ContentTranslator;
import org.weborganic.flint.content.ContentTranslatorFactory;
/**
 * Tika translator factory.
 * 
 * @author Jean-Baptiste Reure
 * @version 25 March 2010
 */
public class TikaTranslatorFactory implements ContentTranslatorFactory {
  /**
   * The config object, the default for now. see file tika-config.xml in tika core project for more info
   */
  private final TikaConfig tikaconfig;
  /**
   * the parser used by the translator
   */
  private final AutoDetectParser parser;
  /**
   * create the factory
   */
  public TikaTranslatorFactory() {
    this.tikaconfig = TikaConfig.getDefaultConfig();
    this.parser = new AutoDetectParser(tikaconfig);
  }
  @Override
  public ContentTranslator createTranslator(String mimeType) {
    return new TikaTranslator(this.parser);
  }
  @Override
  public List<String> getMimeTypesSupported() {
    return new ArrayList<String>(this.tikaconfig.getParsers().keySet());
  }
}