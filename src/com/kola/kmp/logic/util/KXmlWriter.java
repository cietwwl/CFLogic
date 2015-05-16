package com.kola.kmp.logic.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author PERRY CHAN
 */
public class KXmlWriter {

	private XMLOutputter _out;
	private Document _doc;
	private FileOutputStream _fos;
	
	public KXmlWriter(String path, boolean good2Look) throws IOException {
		if (good2Look) {
			_out = new XMLOutputter(Format.getPrettyFormat());
		} else {
			_out = new XMLOutputter();
		}
		_doc = new Document(new Element("root"));
		File file=new File(path);
		_fos = new FileOutputStream(file);
	}
	
	public void addElement(Element element) {
		_doc.getRootElement().addContent(element);
	}
	
	public void setElement(int index, Element element) {
		_doc.getRootElement().setContent(index, element);
	}
	
	public Element removeElement(int index) {
		return (Element)_doc.getRootElement().removeContent(index);
	}
	
	public Element getRoot(){
		return _doc.getRootElement();
	}
	
	public boolean removeElement(Element element) {
		return _doc.getRootElement().removeContent(element);
	}
	
	public void output() throws IOException {
		_out.output(this._doc, _fos);
		_fos.close();
	}
}
