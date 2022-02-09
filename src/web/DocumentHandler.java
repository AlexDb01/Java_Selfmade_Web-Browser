package web;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class DocumentHandler {
	public String convert(Node node) {
	      StringWriter stringWriter = new StringWriter();
          
	      try {
	    	  Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    	  transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    	  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	  transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
	    	  return stringWriter.toString();
	      } 
	      catch (Exception e) {
	    	  e.printStackTrace();
	    	  return "";
	      }
	      
	}
	
}
