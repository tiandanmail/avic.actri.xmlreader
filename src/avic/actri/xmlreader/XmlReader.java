/**
 * @copyright actri.avic
 */
package avic.actri.xmlreader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * xml�ļ������ࡣ
 * 
 * @author tdan 2010-12-25
 * 
 */
public class XmlReader {

	/**
	 * ����ָ��·������XmlReader����
	 * <p>
	 * ���filepathָ����ļ������ڣ��ᴴ�����ļ�������������rootnode�ĸ��ڵ㡣
	 * </p>
	 * 
	 * @param filepath
	 *            XML�ļ�·��
	 * @param rootType
	 *            ���ڵ����ͣ��������µ�XML�ļ�ʱ��Ϊ����ڵ�
	 * @return XmlReader����
	 * @throws Exception
	 */
	public static XmlReader createNewXml(String filepath, String rootType)
			throws Exception {
		assert filepath != null;
		assert rootType != null;
		File file = new File(filepath);
		if (!file.exists()) {
			File parentdir = file.getParentFile();
			if (parentdir == null) {
				throw new IllegalArgumentException("��������" + filepath);
			} else if (!parentdir.exists()) {
				parentdir.mkdirs();
			}

			file.createNewFile();

			StringBuffer buffer = new StringBuffer();
			buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"); //$NON-NLS-1$
			buffer.append("\n<"); //$NON-NLS-1$
			buffer.append(rootType);
			buffer.append(">"); //$NON-NLS-1$
			buffer.append("</"); //$NON-NLS-1$
			buffer.append(rootType);
			buffer.append(">"); //$NON-NLS-1$

			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(buffer.toString());
			bw.close();
		}

		return new XmlReader(file.getAbsolutePath());
	}

	public static void main(String args[]) {
		try {
			File file = new File("D:\\p.xml"); //$NON-NLS-1$
			XmlReader reader = new XmlReader(file.getAbsolutePath());

			File pos = new File("D:\\pos.xml"); //$NON-NLS-1$
			XmlReader readerpos = new XmlReader(pos.getAbsolutePath());
			Element memorySizeElement = readerpos.getElement(
					readerpos.getRoot(), "MemorySize"); //$NON-NLS-1$

			Element root = reader.getRoot();
			Element testElement = reader.getElement(root, "Test"); //$NON-NLS-1$
			reader.appendElement(testElement, memorySizeElement);
			reader.doStore();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/*** XML �ļ�·�� */
	private String fPath = null;

	/*** ���ڵ� */
	private Element fRoot = null;

	/**
	 * ����xml�ļ��Ķ���
	 * 
	 * @param filePath
	 *            xml�ļ�·��
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 *             xml�ļ������쳣
	 */
	public XmlReader(String filePath) throws ParserConfigurationException,
			SAXException, IOException {
		assert filePath != null;
		fPath = filePath;
		loadFile();
	}

	/**
	 * �����ӽڵ㵽���ڵ㡣ע�⣺�÷����������xml�ļ���Ҫ����{@link #doStore()}������
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childElement
	 *            �ӽڵ�
	 * @return ������ɵ��ӽڵ�
	 */
	public Element appendElement(Element parentElement, Element childElement) {
		assert parentElement != null;
		assert childElement != null;
		return appendElement(parentElement, childElement, true);
	}

	/**
	 * ����Դ�ڵ㵽���ڵ㡣 ע�⣺�÷����������xml�ļ���Ҫ����{@link #doStore()}������
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childElement
	 *            �ӽڵ�
	 * @param appendChildren
	 *            ����Դ�ڵ���ӽڵ�
	 * @return ������ɵ��ӽڵ�
	 */
	public Element appendElement(Element parentElement, Element childElement,
			boolean appendChildren) {
		assert parentElement != null;
		assert childElement != null;

		Node textNode = parentElement.getPreviousSibling();
		if (textNode != null && textNode.getNodeType() == Node.TEXT_NODE) {
			String text = textNode.getNodeValue();
			if (text.length() > 1) {
				text = text.substring(1);
				text = text + "\t"; //$NON-NLS-1$
			} else {
				text = "\t"; //$NON-NLS-1$
			}
			Node newNode = parentElement.getOwnerDocument()
					.createTextNode(text);
			parentElement.appendChild(newNode);
		} else {
			Node newNode = parentElement.getOwnerDocument().createTextNode(
					"\n\t"); //$NON-NLS-1$
			parentElement.appendChild(newNode);
		}

		Element created = parentElement.getOwnerDocument().createElement(
				childElement.getNodeName());
		copyAttribute(created, childElement);
		if (!appendChildren) {
			return created;
		}

		boolean hasTextContent = true;
		NodeList list = childElement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i) instanceof Element) {
				hasTextContent = false;
			}
		}

		if (hasTextContent) {
			String textContent = childElement.getTextContent().trim();
			if (textContent != null && !"".equals(textContent)) { //$NON-NLS-1$
				created.setTextContent(textContent);
			}
		}

		parentElement.appendChild(created);

		NodeList childNodes = childElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				appendElement(created, (Element) node);
			} else if (node.getNodeType() == Node.TEXT_NODE) {
				Node newNode = parentElement.getOwnerDocument().createTextNode(
						node.getNodeValue());
				created.appendChild(newNode);
			}
		}

		return created;
	}

	/**
	 * �޸�XML�ļ����ڵ�Ϊ�½ڵ�
	 * 
	 * @param newRoot
	 *            �µĸ��ڵ�
	 */
	public void changeRoot(Element newRoot) {
		assert newRoot != null;
		Document doc = fRoot.getOwnerDocument();
		doc.removeChild(fRoot);
		Element created = doc.createElement(newRoot.getNodeName());
		copyAttribute(created, newRoot);
		doc.appendChild(created);
		NodeList children = newRoot.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				appendElement(created, (Element) node);
			}
		}
	}

	/**
	 * ����Դ�ڵ����Ե�Ŀ�Ľڵ�
	 * 
	 * @param dstElement
	 *            Դ�ڵ�
	 * @param srcElement
	 *            Ŀ�Ľڵ�
	 */
	public void copyAttribute(Element dstElement, Node srcElement) {
		assert dstElement != null;
		assert srcElement != null;
		NamedNodeMap attrs = srcElement.getAttributes();
		for (int ii = 0; ii < attrs.getLength(); ii++) {
			Attr attr = (Attr) attrs.item(ii);
			dstElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
		}
	}

	/**
	 * ����Դ�ڵ����Element��Ŀ��ڵ�
	 * 
	 * @param dstElement
	 *            Ŀ��ڵ�
	 * @param srcElement
	 *            Դ�ڵ�
	 * @param includeAttr
	 *            �Ƿ��Ƹ�Դ�ڵ������
	 * @return ����������Ŀ��ڵ�
	 */
	public Element copyChildrenElement(Element dstElement, Element srcElement,
			boolean includeAttr) {
		assert dstElement != null;
		assert srcElement != null;
		if (includeAttr) {
			copyAttribute(dstElement, srcElement);
		}
		NodeList nodelist = srcElement.getChildNodes();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node instanceof Element) {
				copyElement(dstElement, (Element) node);
			}
		}

		return dstElement;
	}

	/**
	 * ���ƽڵ㵽ָ�����ڵ�
	 * 
	 * @param parent
	 *            ���ڵ�
	 * @param srcElement
	 *            Դ�ڵ�
	 * @return �½ڵ�
	 */
	public Element copyElement(Element dstElement, Element srcElement) {
		assert dstElement != null;
		assert srcElement != null;

		Element newElement = createElement(dstElement, srcElement.getNodeName());
		copyAttribute(newElement, srcElement);

		NodeList nodelist = srcElement.getChildNodes();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node instanceof Element) {
				copyElement(newElement, (Element) node);
			}
		}

		return newElement;
	}

	/**
	 * ����µĽڵ�,�½ڵ�
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ڵ�����
	 * @return ��ӵ��½ڵ�
	 */
	public Element createElement(Element parentElement, String childType) {
		assert parentElement != null;
		assert childType != null;

		Node textNode = parentElement.getPreviousSibling();
		if (textNode != null && textNode instanceof Text) {
			Node newNode = textNode.cloneNode(true);
			newNode.setNodeValue(newNode.getNodeValue() + "\t"); //$NON-NLS-1$
			parentElement.appendChild(newNode);
		} else {
			Node newNode = parentElement.getOwnerDocument().createTextNode(
					"\n\t"); //$NON-NLS-1$
			parentElement.appendChild(newNode);
		}
		Element created = parentElement.getOwnerDocument().createElement(
				childType);
		parentElement.appendChild(created);
		return created;
	}

	/**
	 * Ϊ���ڵ㴴��Ψһ��ȫ�µĽڵ�
	 * 
	 * @param parentElt
	 * @param childType
	 * @return
	 */
	public Element createTheElement(Element parentElt, String childType) {
		Element Elt = getElement(parentElt, childType);
		if (Elt != null) {
			removeChild(parentElt, childType);
		}
		return createElement(parentElt, childType);
	}

	public Element createElement(Element parentElement, String elementType,
			Map<String, String> map) {
		Element newElement = createElement(parentElement, elementType);
		for (Entry<String, String> e : map.entrySet()) {
			String key = e.getKey();
			String value = e.getValue();
			if (key == null) {
				continue;
			}
			if (value == null) {
				value = "";
			}
			newElement.setAttribute(e.getKey(), e.getValue());
		}
		return newElement;
	}

	/**
	 * ����xml�ļ�
	 * 
	 * @throws TransformerException
	 */
	public void doStore() throws TransformerException {
		Document doc = fRoot.getOwnerDocument();
		doc.normalize();

		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer transformer = tfactory.newTransformer();
		Properties prot = new Properties();
		prot.put(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		prot.put(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		prot.put(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		prot.put(OutputKeys.VERSION, "1.0"); //$NON-NLS-1$
		transformer.setOutputProperties(prot);

		DOMSource doms = new DOMSource(doc);
		StreamResult sr = new StreamResult(new File(fPath));
		transformer.transform(doms, sr);
	}

	/**
	 * ��ø��ڵ�ָ������ֱ���ӽڵ㡣
	 * <p>
	 * ��������ӽڵ��ж������ֻ���ص�һ���� ֻ��ø��ڵ��ֱ�Ӻ��ӽڵ㣬���������ӽڵ�
	 * </p>
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ӽڵ�����
	 * @return �ӽڵ㣬��null
	 */
	public Element getElement(Element parentElement, String childType) {
		assert parentElement != null;
		assert childType != null;

		NodeList list = parentElement.getElementsByTagName(childType);
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			if (parentElement.equals(element.getParentNode())) {
				return element;
			}
		}
		return null;
	}

	/**
	 * ��ø��ڵ�ָ�������׸��ӽڵ�ֵ��
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ӽڵ�����
	 * @return �ӽڵ�ֵ����null
	 */
	public String getChildContent(Element parentElement, String childType) {
		assert parentElement != null;
		assert childType != null;
		Element childElement = getElement(parentElement, childType);
		if (childElement != null) {
			return childElement.getTextContent().trim();
		} else {
			return null;
		}
	}

	/**
	 * ��ø�parent�ڵ�������ΪpropertyName��ֵΪpropertyValue�Ľڵ�
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ӽڵ�����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return �ҵ��Ľڵ㣬��null
	 */
	public Element getElement(Element parentElement, String childType,
			String propertyName, String propertyValue) {
		assert parentElement != null;
		assert childType != null;
		assert propertyName != null;
		assert propertyValue != null;

		NodeList children = parentElement.getElementsByTagName(childType);
		for (int i = 0; i < children.getLength(); i++) {
			Element node = (Element) children.item(i);
			if (!node.getParentNode().equals(parentElement)) {
				continue;
			}
			if (node.getAttribute(propertyName).equals(propertyValue)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * ��ø��ڵ�ָ�������ӽڵ��
	 * 
	 * @param childType
	 *            �ӽڵ�����
	 * @return �ӽڵ�����ڵ��޸������ӽڵ�ʱ������Ϊ0
	 */
	public List<Element> getElement(String childType) {
		assert childType != null;
		return getElementList(getRoot(), childType);
	}

	/**
	 * ��ýڵ�ָ������ֱ���ӽڵ��ָ������ֵ�б�
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ӽڵ�����
	 * @param property
	 *            �ӽڵ�����
	 * @return �ӽڵ�ָ�����Ե�ֵ�б����ܰ���ֵΪ""���ַ���
	 */
	public List<String> getElementAttributes(Element parentElement,
			String childType, String property) {
		assert parentElement != null;
		assert childType != null;
		assert property != null;

		List<String> list = new LinkedList<String>();

		NodeList nodelist = parentElement.getElementsByTagName(childType);
		for (int i = 0; i < nodelist.getLength(); i++) {
			Element node = (Element) nodelist.item(i);
			if (node.getParentNode().equals(parentElement)) {
				list.add(node.getAttribute(property));
			}
		}

		return list;
	}

	/**
	 * ��ø��ڵ�ָ�����͡�ָ���ڵ�ֵ���׸��ӽڵ�
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ڵ�����
	 * @param value
	 *            �ڵ�ֵ
	 * @return �ӽڵ�ֵ����null
	 */
	public Element getElementByContent(Element parentElement, String childType,
			String value) {
		assert parentElement != null;
		assert childType != null;
		assert value != null;

		NodeList list = parentElement.getElementsByTagName(childType);
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			if (element.getTextContent().trim().equals(value)) {
				return element;
			}
		}

		return null;
	}

	/**
	 * ��ø��ڵ�ָ�������׸��ӽڵ�ֵ��
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ӽڵ�����
	 * @return �ӽڵ�ֵ����null
	 */
	public String getElementContent(Element parentElement, String childType) {
		assert parentElement != null;
		assert childType != null;

		Element childElement = getElement(parentElement, childType);
		if (childElement != null) {
			return childElement.getTextContent().trim();
		} else {
			return null;
		}
	}

	/**
	 * ��ø��ڵ�ָ�������ӽڵ��
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ӽڵ�����
	 * @return �ӽڵ��ָ�����ڵ��޸������ӽڵ�ʱ������Ϊ0
	 */
	public List<Element> getElementList(Element parentElement, String childType) {
		assert parentElement != null;
		assert childType != null;

		NodeList nodelist = parentElement.getElementsByTagName(childType);
		List<Element> list = new ArrayList<Element>(nodelist.getLength());
		for (int i = 0; i < nodelist.getLength(); i++) {
			Element node = (Element) nodelist.item(i);
			if (node.getParentNode().equals(parentElement)) {
				list.add(node);
			}
		}

		return list;
	}

	/**
	 * ��ø�parent����������ΪpropertyName��ֵΪpropertyValue��ֱ���ӽڵ��б�
	 * 
	 * @param parentElement
	 *            ���ڵ�
	 * @param childType
	 *            �ӽڵ�����
	 * @param propertyName
	 *            ������
	 * @param propertyName
	 *            ����ֵ
	 * @return ���������Ľڵ��б�
	 */
	public List<Element> getElementList(Element parentElement,
			String childType, String propertyName, String propertyValue) {
		assert parentElement != null;
		assert childType != null;
		assert propertyName != null;
		assert propertyValue != null;

		List<Element> list = new LinkedList<Element>();
		NodeList children = parentElement.getElementsByTagName(childType);
		for (int i = 0; i < children.getLength(); i++) {
			Element node = (Element) children.item(i);
			if (!node.getParentNode().equals(parentElement)) {
				continue;
			}
			if (node.getAttribute(propertyName).equals(propertyValue)) {
				list.add(node);
			}
		}
		return list;
	}

	/**
	 * ���xml�ļ�ȫ·��
	 * 
	 * @return xml�ļ�ȫ·��
	 */
	public String getPath() {
		return fPath;
	}

	/**
	 * ���XML�ļ����ڵ�
	 * 
	 * @return XML�ļ����ڵ�
	 */
	public Element getRoot() {
		return fRoot;
	}

	/**
	 * ����xml�����ļ�
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws Exception
	 */
	private void loadFile() throws ParserConfigurationException, SAXException,
			IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File(fPath));
		doc.normalize();
		fRoot = doc.getDocumentElement();
	}

	/**
	 * ɾ��parentElement������ΪchildType���ӽڵ�
	 * 
	 * @param parentElement
	 * @param childType
	 */
	public void removeChild(Element parentElement, String childType) {
		assert parentElement != null;
		assert childType != null;

		NodeList list = parentElement.getElementsByTagName(childType);
		int length = list.getLength();
		while (length-- > 0) {
			Node node = list.item(0);
			Node previousNode = node.getPreviousSibling();
			if (previousNode != null
					&& previousNode.getNodeType() == Node.TEXT_NODE) {
				parentElement.removeChild(previousNode);
			}
			parentElement.removeChild(node);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XmlReader) {
			return new File(getPath()).equals(new File(((XmlReader) obj)
					.getPath()));
		} else {
			return false;
		}
	}
}
