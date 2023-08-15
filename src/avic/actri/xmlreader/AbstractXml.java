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
 * xml文件操作抽象类。
 * 
 * @author tdan 2009-10-19
 * 
 */
public abstract class AbstractXml {

	private Element root = null;

	/**
	 * 根结点的节点名
	 */
	private String rootName;

	/**
	 * 加载xml文件
	 * 
	 * @param root
	 *            根结点的名字
	 * @param path
	 *            xml文件路径
	 * @throws Exception
	 */
	protected AbstractXml(String root, String path) throws Exception {
		rootName = root;
		loadFile(path);
	}

	/**
	 * 加载xml文件
	 * 
	 * @param rootType
	 *            根结点的名字
	 * @throws Exception
	 */
	protected AbstractXml(String rootType) throws Exception {
		rootName = rootType;
		loadFile(getFilePath());
	}

	/**
	 * 加载xml配置文件
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
	 * 保存xml文件
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
	 * 添加新的节点,新节点
	 * 
	 * @param parent
	 *            父节点
	 * @param nodeType
	 *            节点类型
	 * @return 添加的新节点
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
	 * 删除parent的child节点
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
	 * 查询父parent是否包含属性名为propertyName，值为propertyValue的节点
	 * 
	 * @param parent
	 *            父节点
	 * @param nodeType
	 *            子节点类型
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return 包含ture，不包含false
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
	 * 获得父parent是否包含属性名为propertyName，值为propertyValue的节点
	 * 
	 * @param parent
	 *            父节点
	 * @param nodeType
	 *            子节点类型
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return 找到的节点
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
	 * 给出的字符串是否只有数字
	 * 
	 * @param str
	 * @return
	 */
	public boolean isValidInteger(String str) {
		for (int i = 0; i < str.length(); i++) {
			int b = str.codePointAt(i);
			if (b < 48 || b > 57) {// 只能是数字
				return false;
			}
		}

		return true;
	}

	/**
	 * 获得配置文件的全路径
	 * 
	 * @return 配置文件的全路径
	 */
	protected abstract String getFilePath();

}
