/**
 * @copyright actri.avic
 */
package avic.actri.xmlreader;

import java.io.File;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * xml�ļ����������ࡣ
 * 
 * @author tdan 2009-10-19
 * 
 */
public abstract class AbstractXml {

	private Element root = null;

	/**
	 * �����Ľڵ���
	 */
	private String rootName;

	/**
	 * ����xml�ļ�
	 * 
	 * @param root
	 *            ����������
	 * @param path
	 *            xml�ļ�·��
	 * @throws Exception
	 */
	protected AbstractXml(String root, String path) throws Exception {
		rootName = root;
		loadFile(path);
	}

	/**
	 * ����xml�ļ�
	 * 
	 * @param rootType
	 *            ����������
	 * @throws Exception
	 */
	protected AbstractXml(String rootType) throws Exception {
		rootName = rootType;
		loadFile(getFilePath());
	}

	/**
	 * ����xml�����ļ�
	 * 
	 * @throws Exception
	 * 
	 */
	private void loadFile(String path) throws Exception {
		DocumentBuilder db = null;
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();

		File file = new File(path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			if (file.createNewFile()) {
				doc = db.newDocument();
				root = doc.createElement(rootName);
				doc.appendChild(root);
			} else {
				return;
			}
		} else {
			doc = db.parse(path);
			doc.normalize();
			root = doc.getDocumentElement();
		}
	}

	/**
	 * ����xml�ļ�
	 * 
	 * @throws Exception
	 * 
	 */
	public void doStore() throws Exception {
		Document doc = root.getOwnerDocument();
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer serializer = tfactory.newTransformer();
		Properties prot = new Properties();
		prot.put(OutputKeys.METHOD, "xml");
		prot.put(OutputKeys.ENCODING, "UTF-8");
		prot.put(OutputKeys.VERSION, "1.0");
		prot.put(OutputKeys.INDENT, "yes");
		serializer.setOutputProperties(prot);
		doc.getDocumentElement().normalize();
		serializer.transform(new DOMSource(doc), new StreamResult(new File(
				getFilePath())));
	}

	public Element getRoot() {
		return root;
	}

	/**
	 * ����µĽڵ�,�½ڵ�
	 * 
	 * @param parent
	 *            ���ڵ�
	 * @param nodeType
	 *            �ڵ�����
	 * @return ��ӵ��½ڵ�
	 * @throws Exception
	 */
	public Element createElement(Element parent, String nodeType)
			throws Exception {
		Element created = parent.getOwnerDocument().createElement(nodeType);
		parent.appendChild(created);
		doStore();
		return created;
	}

	/**
	 * ɾ��parent��child�ڵ�
	 * 
	 * @param parent
	 * @param child
	 * @throws Exception
	 */
	public void removeChild(Element parent, Element child) throws Exception {
		parent.removeChild(child);
		doStore();
	}

	/**
	 * ��ѯ��parent�Ƿ����������ΪpropertyName��ֵΪpropertyValue�Ľڵ�
	 * 
	 * @param parent
	 *            ���ڵ�
	 * @param nodeType
	 *            �ӽڵ�����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return ����ture��������false
	 */
	public boolean has(Element parent, String nodeType, String propertyName,
			String propertyValue) {
		NodeList children = parent.getElementsByTagName(nodeType);
		for (int i = 0; i < children.getLength(); i++) {
			Element node = (Element) children.item(i);
			if (node.getAttribute(propertyName).equals(propertyValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ��ø�parent�Ƿ����������ΪpropertyName��ֵΪpropertyValue�Ľڵ�
	 * 
	 * @param parent
	 *            ���ڵ�
	 * @param nodeType
	 *            �ӽڵ�����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return �ҵ��Ľڵ�
	 */
	public Element getElement(Element parent, String nodeType,
			String propertyName, String propertyValue) {
		NodeList children = parent.getElementsByTagName(nodeType);
		for (int i = 0; i < children.getLength(); i++) {
			Element node = (Element) children.item(i);
			if (node.getAttribute(propertyName).equals(propertyValue)) {
				return node;
			}
		}
		return null;
	}

	public long parseLong(String str) {
		if (isValidInteger(str)) {
			return Long.parseLong(str);
		}

		return -1;
	}

	public int parseInt(String str) {
		if (isValidInteger(str)) {
			return Integer.parseInt(str);
		}

		return -1;
	}

	/**
	 * �������ַ����Ƿ�ֻ������
	 * 
	 * @param str
	 * @return
	 */
	public boolean isValidInteger(String str) {
		for (int i = 0; i < str.length(); i++) {
			int b = str.codePointAt(i);
			if (b < 48 || b > 57) {// ֻ��������
				return false;
			}
		}

		return true;
	}

	/**
	 * ��������ļ���ȫ·��
	 * 
	 * @return �����ļ���ȫ·��
	 */
	protected abstract String getFilePath();

}
