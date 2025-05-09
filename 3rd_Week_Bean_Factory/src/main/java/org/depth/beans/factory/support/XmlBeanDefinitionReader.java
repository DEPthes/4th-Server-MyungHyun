package org.depth.beans.factory.support;

import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.BeanDefinitionRegistry;
import org.depth.beans.factory.exception.BeanCreationException;
import org.depth.beans.factory.exception.BeansException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public int loadBeanDefinitions(String location) throws BeansException {
        int count = 0;
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(location)) {
            if (inputStream == null) {
                throw new BeanCreationException("", "Cannot find configuration file: " + location);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList beanNodes = doc.getElementsByTagName("bean");

            for (int i = 0; i < beanNodes.getLength(); i++) {
                Element beanElement = (Element) beanNodes.item(i);
                String beanName = beanElement.getAttribute("id");
                String className = beanElement.getAttribute("class");

                if (beanName.isEmpty() || className.isEmpty()) {
                    throw new BeanCreationException("", "Bean id or class not specified in XML for one of the beans.");
                }

                Class<?> beanClass = Class.forName(className);
                BeanDefinition bd = parseToBeanDefinition(beanName, beanClass, beanElement);
                getRegistry().registerBeanDefinition(beanName, bd);
                count++;
            }
        } catch (Exception e) {
            throw new BeansException("Error loading bean definitions from XML: " + location, e);
        }
        return count;
    }

    private static BeanDefinition parseToBeanDefinition(String beanName, Class<?> beanClass, Element beanElement) {
        BeanDefinition bd = new BeanDefinition(beanName, beanClass);

        // 생성자 인자 파싱
        NodeList constructorArgNodes = beanElement.getElementsByTagName("constructor-arg");
        for (int j = 0; j < constructorArgNodes.getLength(); j++) {
            Element argElement = (Element) constructorArgNodes.item(j);
            String refBeanName = argElement.getAttribute("ref");
            if (!refBeanName.isEmpty()) {
                bd.addConstructorArgBeanName(refBeanName);
            }
            // TODO: value 타입 인자 처리
        }

        // 프로퍼티(setter) 주입 파싱
        NodeList propertyNodes = beanElement.getElementsByTagName("property");
        for (int j = 0; j < propertyNodes.getLength(); j++) {
            Element propElement = (Element) propertyNodes.item(j);
            String propName = propElement.getAttribute("name");
            String refBeanName = propElement.getAttribute("ref");

            if (!propName.isEmpty() && !refBeanName.isEmpty()) {
                bd.addPropertyBeanName(propName, refBeanName);
            }
            // TODO: value 타입 프로퍼티 처리
        }
        return bd;
    }

    @Override
    public int loadBeanDefinitions(String... locations) throws BeansException {
        int count = 0;
        for (String location : locations) {
            count += loadBeanDefinitions(location);
        }
        return count;
    }
}