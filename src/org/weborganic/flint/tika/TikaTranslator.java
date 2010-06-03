package org.weborganic.flint.tika;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.content.Content;
import org.weborganic.flint.content.ContentTranslator;
import org.xml.sax.ContentHandler;
/**
 * This translator uses Tika.
 * 
 * @author Jean-Baptiste Reure
 * @version 25 March 2010
 */
public class TikaTranslator implements ContentTranslator {
  private static final Logger logger = Logger.getLogger(TikaTranslator.class);
  /**
   * The factory used to produce an output handler
   */
  private final static SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
  /**
   * The object used to parse the 
   */
  private final AutoDetectParser tikaParser;
  /**
   * Create a new translator using the provided parser
   * @param parser the parser to use
   */
  public TikaTranslator(AutoDetectParser parser) {
    this.tikaParser = parser;
  }
  @Override
  public Reader translate(Content content) throws IndexException {
    try {
      logger.debug("Attempting to translate content "+content.toString());
      // TODO add metadata?
      Metadata metadata = new Metadata();
      // create output stream
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      this.tikaParser.parse(content.getSource(), getHandler(out), metadata);
      // create reader on results
      return new StringReader(new String(out.toByteArray(), "UTF-8"));
    } catch (Exception e) {
      logger.error("Failed to translate content "+content.toString(), e);
      return null;
    }
  }
  /**
   * Create a new handler with an XML output (will be XHTML).
   * 
   * @param out the stream to use as output
   * @return the handler used by the Tika parser
   * @throws TransformerConfigurationException if the factory cannot create a new handler.
   */
  private ContentHandler getHandler(OutputStream out) throws TransformerConfigurationException {
    TransformerHandler handler = factory.newTransformerHandler();
    handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
    handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
    handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    handler.setResult(new StreamResult(out));
    return handler;
  }
}