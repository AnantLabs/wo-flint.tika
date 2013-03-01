package org.weborganic.flint.tika;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.api.Content;
import org.weborganic.flint.api.ContentTranslator;
import org.xml.sax.ContentHandler;

import com.topologi.diffx.xml.XMLWriter;
import com.topologi.diffx.xml.XMLWriterImpl;

/**
 * This translator uses Tika.
 *
 * @author Jean-Baptiste Reure
 * @version 25 March 2010
 */
public class TikaTranslator implements ContentTranslator {

  /**
   * Logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TikaTranslator.class);

  /**
   * The factory used to produce an output handler
   */
  private final static SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

  /**
   * The object used to parse the
   */
  private final static AutoDetectParser TIKA_PARSER = new AutoDetectParser(TikaConfig.getDefaultConfig());

  @Override
  public Reader translate(Content content) throws IndexException {
    // check for deleted content
    if (content.isDeleted()) return null;
    try {
      LOGGER.debug("Attempting to translate content "+content.toString());
      // include metadata
      Metadata metadata = new Metadata();
      // create output stream
      String xmlContent;
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TIKA_PARSER.parse(content.getSource(), getHandler(out), metadata);
        xmlContent = new String(out.toByteArray(), "UTF-8");
      } catch (TikaException te) {
        // should be HTML??
        xmlContent = "<error>"+(te.getMessage() == null ? "Unknown error while reading content in TIKA" : te.getMessage())+"</error>";
      }
      StringWriter sw = new StringWriter();
      XMLWriter xml = new XMLWriterImpl(sw);
      xml.openElement("root");
      // metadata
      String[] mnames = metadata.names();
      if (mnames != null && mnames.length > 0) {
        xml.openElement("metadata");
        for (String mname : mnames) {
          xml.openElement("property");
          xml.attribute("name", mname);
          if (metadata.isMultiValued(mname)) {
            String[] values = metadata.getValues(mname);
            for (String value : values)
              xml.element("string", value);
          } else
            xml.writeText(metadata.get(mname));
          xml.closeElement();
        }
        // end metadata
        xml.closeElement();
      }
      // content
      xml.openElement("content");
      xml.writeXML(xmlContent);
      xml.closeElement();
      // end root
      xml.closeElement();
      // create reader on results
      return new StringReader(sw.toString());
    } catch (Exception e) {
      LOGGER.error("Failed to translate content "+content.toString(), e);
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
    handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    handler.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    handler.setResult(new StreamResult(out));
    return handler;
  }
}
