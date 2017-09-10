package hthurow.tomcatjndi;

import org.apache.catalina.users.MemoryUserDatabase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import resources.SelfDefinedResource;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 24.07.17
 */
public class TomcatJndiTest {

    private TomcatJNDI tomcatJNDI;

    @Before
    public void setUp() throws Exception {
        tomcatJNDI = new TomcatJNDI();
    }

    @After
    public void tearDown() throws Exception {
        tomcatJNDI.tearDown();
        System.clearProperty("catalina.base");
    }

    @Test
    public void environment() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    @Test
    public void selfDefinedResource() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextSelfDefinedResource.xml"));
        InitialContext ic = new InitialContext();
        SelfDefinedResource selfDefinedResource = (SelfDefinedResource) ic.lookup("java:comp/env/my/resource");
        Assert.assertNotNull(selfDefinedResource);
    }

    @Test
    public void dataSource() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextDataSource.xml"));
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/Sybase");
        Assert.assertNotNull(ds);
    }

    /**
     * Throws OperationNotSupportedException as Tomcat server does.
     */
    @Test(expected = OperationNotSupportedException.class)
    public void onlyReadableENC() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
        ic.bind("java:comp/env/myObj", "my object");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void onlyReadableENCCreateContext() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
        Context myContext = ic.createSubcontext("myContext");
        myContext.bind("name", "Holger");
        String name = (String) ic.lookup("myContext/name");
        Assert.assertEquals("Holger", name);
        Context javaCtx = (Context) ic.lookup("java:");
        Assert.assertNotNull(javaCtx);
        javaCtx.bind("notPossible", "throws OperationNotSupportedException");
    }

    /**
     * Server throws OperationNotSupportedException when env object is not overridable and setExceptionOnFailedWrite(false) was not called.
     *
     */
    @Test //(expected = OperationNotSupportedException.class)
    public void close() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
        ic.close();
        ic = new InitialContext();
        myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    @Test
    public void severalJndiObjects() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextSeveralJndiObjects.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/Sybase");
        SelfDefinedResource selfDefinedResource = (SelfDefinedResource) ic.lookup("java:comp/env/my/resource");
        Assert.assertEquals(10, myInt);
        Assert.assertNotNull(ds);
        Assert.assertNotNull(selfDefinedResource);
    }

    @Test
    public void loadMultipleContextFiles() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextSelfDefinedResource.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextDataSource.xml"));

        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        SelfDefinedResource selfDefinedResource = (SelfDefinedResource) ic.lookup("java:comp/env/my/resource");
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/Sybase");
        Assert.assertEquals(10, myInt);
        Assert.assertNotNull(selfDefinedResource);
        Assert.assertNotNull(ds);
    }

    @Test
    public void loadSameContextXmlTwoTimes() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    /**
     * Is not overriden, because the Environment object is override=false. With override not set it defaults to true.
     */
    @Test
    public void overrideValue() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=5.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    @Test
    public void overrideValue2() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10;override=true.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=5.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(5, myInt);
    }

    @Test
    public void addToSameContext() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextSelfDefinedResource.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/my-myInt=5.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/my/myInt");
        SelfDefinedResource res = (SelfDefinedResource) ic.lookup("java:comp/env/my/resource");
        Assert.assertEquals(5, myInt);
        Assert.assertNotNull(res);
    }

    @Test
    public void webXmlEnvEntry() throws Exception {
        tomcatJNDI.processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(5, myInt);
    }

    /**
     * env-entry in WEB-INF/web.xml can override env-entry in web.xml.default. But env-entry in conf/web.xml can not be overridden:
     * <p>
     * 06-Aug-2017 11:18:45.718 SEVERE [RMI TCP Connection(2)-127.0.0.1] org.apache.tomcat.util.descriptor.web.WebXmlParser.parseWebXml Parse error in application web.xml file at file:/Users/hot/Library/Caches/IntelliJIdea2017.1/tomcat/Unnamed_TomcatEmbedded/conf/catalina/localhost/web.xml.default<br>
     org.xml.sax.SAXParseException; systemId: file:/Users/hot/Library/Caches/IntelliJIdea2017.1/tomcat/Unnamed_TomcatEmbedded/conf/catalina/localhost/web.xml.default; lineNumber: 32; columnNumber: 17; Error at (32, 17) : Duplicate env-entry name [envEntry]
     */
    @Test
    public void webXmlEnvEntryOverride() throws Exception {
        tomcatJNDI.processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"), true);
        tomcatJNDI.processWebXml(new File("src/test/resources/webXml/env-entry-myInt=10.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    /**
     * "Where the same resource name has been defined for a <env‐entry> element included in the web application deployment descriptor (/WEB‐INF/web.xml) and in an <Environment> element as part of the <Context> element for the web application, the values in the deployment descriptor will take precedence only if allowed by the corresponding <Environment> element (by setting the override attribute to "true")."
     * <p>
     * JNDI Resources HOW-TO - Apache Tomcat 8.0.pdf
     */
    @Test
    public void webXmlOverrideEnvironmentObject() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    /**
     * @see #webXmlOverrideEnvironmentObject()
     */
    @Test
    public void webXmlOverrideEnvironmentObject2() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10;override=true.xml"));
        tomcatJNDI.processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(5, myInt);
    }

    @Test
    public void webXmlResourceRef() throws Exception {
        tomcatJNDI.processWebXml(new File("src/test/resources/webXml/resource-ref.xml"));
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/datasource");
        Assert.assertNotNull(ds);
    }

    @Test
    public void resourceLink() throws Exception {
        System.setProperty("catalina.base", "src/test/resources/serverXml");
        tomcatJNDI.processServerXml(new File("src/test/resources/serverXml/conf/server.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/resourceLink-userDatabase.xml"));
        InitialContext ic = new InitialContext();
        MemoryUserDatabase userDatabase = (MemoryUserDatabase) ic.lookup("java:comp/env/userDatabase");
        //System.out.println(userDatabase);
        assertNotNull(userDatabase);

    }
}
