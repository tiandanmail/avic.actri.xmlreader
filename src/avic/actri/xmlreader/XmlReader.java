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
 * xml文件操作类。
 * 
 * @author tdan 2010-12-25
 * 
 */
public class XmlReader {

	/**
	 * 根据指定路径创建XmlReader对象。
	 * <p>
	 * 如果filepath指向的文件不存在，会创建该文件，并创建类型rootnode的根节点。
	 * </p>
	 * 
	 * @param filepath
	 *            XML文件路径
	 * @param rootType
	 *            根节点类型，当创建新的XML文件时作为其根节点
	 * @return XmlReader对象
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
				throw new IllegalArgumentException("参数错误" + filepath);
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

	/*** XML 文件路径 */
	private String fPath = null;

	/*** 根节点 */
	private Element fRoot = null;

	/**
	 * 构造xml文件阅读器
	 * 
	 * @param filePath
	 *            xml文件路径
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 *             xml文件访问异常
	 */
	public XmlReader(String filePath) throws ParserConfigurationException,
			SAXException, IOException {
		assert filePath != null;
		fPath = filePath;
		loadFile();
	}

	/**
	 * 复制子节点到父节点。注意：该方法不保存该xml文件，要调用{@link #doStore()}方法。
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childElement
	 *            子节点
	 * @return 复制完成的子节点
	 */
	public Element appendElement(Element parentElement, Element childElement) {
		assert parentElement != null;
		assert childElement != null;
		return appendElement(parentElement, childElement, true);
	}

	/**
	 * 复制源节点到父节点。 注意：该方法不保存该xml文件，要调用{@link #doStore()}方法。
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childElement
	 *            子节点
	 * @param appendChildren
	 *            复制源节点的子节点
	 * @return 复制完成的子节点
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
	 * 修改XML文件根节点为新节点
	 * 
	 * @param newRoot
	 *            新的根节点
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
	 * 复制源节点属性到目的节点
	 * 
	 * @param dstElement
	 *            源节点
	 * @param srcElement
	 *            目的节点
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
	 * 复制源节点的子Element到目标节点
	 * 
	 * @param dstElement
	 *            目标节点
	 * @param srcElement
	 *            源节点
	 * @param includeAttr
	 *            是否复制根源节点的属性
	 * @return 经过处理后的目标节点
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
	 * 复制节点到指定父节点
	 * 
	 * @param parent
	 *            父节点
	 * @param srcElement
	 *            源节点
	 * @return 新节点
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
	 * 添加新的节点,新节点
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            节点类型
	 * @return 添加的新节点
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
	 * 为父节点创建唯一的全新的节点
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
	 * 保存xml文件
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
	 * 获得父节点指定类型直属子节点。
	 * <p>
	 * 如该类型子节点有多个，则只返回第一个。 只获得父节点的直接孩子节点，不包括孙子节点
	 * </p>
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            子节点类型
	 * @return 子节点，或null
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
	 * 获得父节点指定类型首个子节点值。
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            子节点类型
	 * @return 子节点值，或null
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
	 * 获得父parent节点属性名为propertyName，值为propertyValue的节点
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            子节点类型
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return 找到的节点，或null
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
	 * 获得根节点指定类型子节点表
	 * 
	 * @param childType
	 *            子节点类型
	 * @return 子节点表，根节点无该类型子节点时，表长度为0
	 */
	public List<Element> getElement(String childType) {
		assert childType != null;
		return getElementList(getRoot(), childType);
	}

	/**
	 * 获得节点指定类型直属子节点的指定属性值列表
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            子节点类型
	 * @param property
	 *            子节点属性
	 * @return 子节点指定属性的值列表，可能包括值为""的字符串
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
	 * 获得父节点指定类型、指定节点值的首个子节点
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            节点类型
	 * @param value
	 *            节点值
	 * @return 子节点值，或null
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
	 * 获得父节点指定类型首个子节点值。
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            子节点类型
	 * @return 子节点值，或null
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
	 * 获得父节点指定类型子节点表
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            子节点类型
	 * @return 子节点表，指定父节点无该类型子节点时，表长度为0
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
	 * 获得父parent包含属性名为propertyName，值为propertyValue的直属子节点列表
	 * 
	 * @param parentElement
	 *            父节点
	 * @param childType
	 *            子节点类型
	 * @param propertyName
	 *            属性名
	 * @param propertyName
	 *            属性值
	 * @return 符合条件的节点列表
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
	 * 获得xml文件全路径
	 * 
	 * @return xml文件全路径
	 */
	public String getPath() {
		return fPath;
	}

	/**
	 * 获得XML文件根节点
	 * 
	 * @return XML文件根节点
	 */
	public Element getRoot() {
		return fRoot;
	}

	/**
	 * 加载xml配置文件
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
	 * 删除parentElement下类型为childType的子节点
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
