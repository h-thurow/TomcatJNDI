package hthurow.tomcatjndi;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.ServerOptions;
import com.dumbster.smtp.SmtpServer;
import com.dumbster.smtp.SmtpServerFactory;
import junit.framework.AssertionFailedError;
import org.apache.catalina.users.MemoryUserDatabase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import resources.JavaBean;
import resources.SelfDefinedResource;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * TODO Test bean provided as GlobalNamingResource.
 * TODO Test all factories in org.apache.naming.factory, e. g. SendMailFactory etc.
 *
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
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    @Test
    public void selfDefinedResource() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextSelfDefinedResource.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        SelfDefinedResource selfDefinedResource = (SelfDefinedResource) ic.lookup("java:comp/env/my/resource");
        Assert.assertNotNull(selfDefinedResource);
    }

    @Test
    public void dataSourceInContext() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextDataSource.xml"));
        tomcatJNDI.start();
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/hsqldb");
        Assert.assertNotNull(ds);
        Connection connection = ds.getConnection();
        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate("CREATE TABLE MY_TABLE" +
                    " (NAME VARCHAR(254))");
            statement.executeUpdate("INSERT INTO MY_TABLE (NAME) VALUES ('test')");
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM MY_TABLE");
            resultSet.next();
            int rowCount = resultSet.getInt(1);
            assertEquals(1, rowCount);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if (statement != null) {
                statement.executeQuery("DROP TABLE MY_TABLE");
                statement.close();
                connection.close();
            }
        }
    }

    @Test
    public void resourceEnvRef() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/resource-env-ref/context.xml"));
        tomcatJNDI.processWebXml(new File("src/test/resources/resource-env-ref/web.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        JavaBean javaBean = (JavaBean) ic.lookup("java:comp/env/bean/JavaBean");
        assertNotNull(javaBean);
        assertEquals("TomcatJNDI", javaBean.getSomeString());
    }

    /**
     * &lt;Parameter name="googleMapsKey" value="..." override="false"/> in real_context.xml resulted in
     * <pre>
     * SEVERE: End event threw exception
     java.lang.NoSuchMethodException: org.apache.catalina.deploy.NamingResources addApplicationParameter
     * </pre>
     */
    @Test
    public void resourceEnvRefRealWebRealContextXml() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/resource-env-ref/real_context.xml"));
        tomcatJNDI.processWebXml(new File("src/test/resources/resource-env-ref/real_web.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        JavaBean javaBean = (JavaBean) ic.lookup("java:comp/env/bean/JavaBean");
        assertNotNull(javaBean);
        assertEquals("TomcatJNDI", javaBean.getSomeString());
        String name = (String) ic.lookup("java:comp/env/logback/context-name");
        assertEquals("opixweb", name);
    }

    /**
     * Throws OperationNotSupportedException as Tomcat server does.
     */
    @Test(expected = OperationNotSupportedException.class)
    public void onlyReadableENC() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
        ic.bind("java:comp/env/myObj", "my object");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void onlyReadableENCCreateContext() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.start();
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
        tomcatJNDI.start();
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
        tomcatJNDI.start();
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
        tomcatJNDI.start();

        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        SelfDefinedResource selfDefinedResource = (SelfDefinedResource) ic.lookup("java:comp/env/my/resource");
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/hsqldb");
        Assert.assertEquals(10, myInt);
        Assert.assertNotNull(selfDefinedResource);
        Assert.assertNotNull(ds);
    }

    @Test
    public void loadSameContextXmlTwoTimes() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI.start();
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
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    @Test
    public void overrideValue2() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10;override=true.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=5.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(5, myInt);
    }

    @Test
    public void addToSameContext() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/contextSelfDefinedResource.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/my-myInt=5.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/my/myInt");
        SelfDefinedResource res = (SelfDefinedResource) ic.lookup("java:comp/env/my/resource");
        Assert.assertEquals(5, myInt);
        Assert.assertNotNull(res);
    }

    @Test
    public void webXmlEnvEntry() throws Exception {
        tomcatJNDI._processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        tomcatJNDI.start();
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
        tomcatJNDI._processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        tomcatJNDI._processWebXml(new File("src/test/resources/webXml/env-entry-myInt=10.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(10, myInt);
    }

    /**
     * Siehe {@link #webXmlEnvEntryOverride()}.
     */
    @Test
    public void webXmlEnvEntryDoNotOverrideDefaultWebXml() throws Exception {
        tomcatJNDI.processDefaultWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        tomcatJNDI._processWebXml(new File("src/test/resources/webXml/env-entry-myInt=10.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(5, myInt);
    }

    /**
     * "Where the same resource name has been defined for a <env‐entry> element included in the web application deployment descriptor (/WEB‐INF/web.xml) and in an <Environment> element as part of the <Context> element for the web application, the values in the deployment descriptor will take precedence only if allowed by the corresponding <Environment> element (by setting the override attribute to "true")."
     * <p>
     * JNDI Resources HOW-TO - Apache Tomcat 8.0.pdf
     */
    @Test
    public void webXmlOverrideEnvironmentObject() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/myInt=10.xml"));
        tomcatJNDI._processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        tomcatJNDI.start();
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
        tomcatJNDI._processWebXml(new File("src/test/resources/webXml/env-entry-myInt=5.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        int myInt = (int) ic.lookup("java:comp/env/myInt");
        Assert.assertEquals(5, myInt);
    }

    @Test
    public void globalNamingResourceUserDatabase() throws Exception {
        // So tomcat-users.xml is to be found.
        System.setProperty("catalina.base", "src/test/resources/GlobalNamingResources/UserDatabase");
        tomcatJNDI.processServerXml(new File("src/test/resources/GlobalNamingResources/UserDatabase/conf/server.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/GlobalNamingResources/UserDatabase/context.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        MemoryUserDatabase userDatabase = (MemoryUserDatabase) ic.lookup("java:comp/env/userDatabase");
        //System.out.println(userDatabase);
        assertNotNull(userDatabase);

    }

    @Test
    public void globalDataSource() throws Exception {
        tomcatJNDI.processServerXml(new File("src/test/resources/GlobalNamingResources/DataSource/server.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/GlobalNamingResources/DataSource/context.xml"));
        tomcatJNDI.start();
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/linkToGlobalDataSource");
        assertNotNull(ds);
        Connection connection = ds.getConnection();
        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate("CREATE TABLE MY_TABLE" +
                    " (NAME VARCHAR(254))");
            statement.executeUpdate("INSERT INTO MY_TABLE (NAME) VALUES ('test')");
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM MY_TABLE");
            resultSet.next();
            int rowCount = resultSet.getInt(1);
            assertEquals(1, rowCount);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if (statement != null) {
                statement.executeQuery("DROP TABLE MY_TABLE");
                statement.close();
                connection.close();
            }
        }
    }

    @Test
    public void javaMailSession() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/resources/contexts/javaMailSession.xml"));
        tomcatJNDI.start();
        InitialContext context = new InitialContext();
        Session mailSession = (Session) context.lookup("java:comp/env/mail/Session");
        assertNotNull(mailSession);

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom();
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("thurow.h@gmail.com"));
        message.setSubject("TomcatJndiTest#javaMailSession");
//        message.setContent("body text", "text/plain");
        message.setText("Hello World");

        SmtpServer server = SmtpServerFactory.startServer(new ServerOptions(new String[]{mailSession.getProperty("mail.smtp.port")}));
        try {
            Transport.send(message);
        }
        finally {
            server.stop();
        }

        assertTrue(server.getEmailCount() == 1);
        MailMessage email = server.getMessage(0);
        assertEquals("TomcatJndiTest#javaMailSession", email.getFirstHeaderValue("Subject"));
    }

    @Test
    public void globalJavaBean() throws Exception {
        tomcatJNDI.processServerXml(new File("src/test/resources/GlobalNamingResources/JavaBean/server.xml"));
        tomcatJNDI.processContextXml(new File("src/test/resources/GlobalNamingResources/JavaBean/context.xml"));
        tomcatJNDI.start();
        InitialContext context = new InitialContext();
        JavaBean bean = (JavaBean) context.lookup("java:comp/env/bean/JavaBean");
        assertNotNull(bean);
        assertEquals("TomcatJNDI", bean.getSomeString());
    }

    @Test
    public void host_context_environment() throws Exception {
        tomcatJNDI.processServerXml(new File("src/test/resources/Host/Context/Environment/server.xml"), "/myContext");
        tomcatJNDI.start();
        int myInt = InitialContext.doLookup("java:comp/env/Host/Context/myInt");
        assertEquals(5, myInt);
        try {
            InitialContext.doLookup("java:comp/env/Host/Context2/myInt");
            throw new AssertionFailedError("java:comp/env/Host/Context2/myInt must not exist.");
        }
        catch (NamingException ignored) { }
    }
}
