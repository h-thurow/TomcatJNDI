package ejb;

import hthurow.tomcatjndi.TomcatJNDI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 14.10.17
 */
public class EjbTest {
    private TomcatJNDI tomcatJNDI;

    @Before
    public void setUp() throws Exception {
        tomcatJNDI = new TomcatJNDI();
    }

    @After
    public void tearDown() throws Exception {
        tomcatJNDI.tearDown();
    }

    @Test
    public void openEjbStandaloneLocal() throws Exception {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.LocalInitialContextFactory");
        InitialContext ic = new InitialContext(env);
        Object ref = ic.lookup("MyEjbLocal");
//        MyEjbIF myEjb = (MyEjbIF) PortableRemoteObject.narrow(ref, MyEjbIF.class);
        MyEjbIF myEjb = (MyEjbIF) ref;
        assertEquals("Hello", myEjb.sayHello());
    }

    @Test
    public void ejbLocalInContextXml() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/java/ejb/contexts/ejbDefault.xml"));
        InitialContext context = new InitialContext();
        MyEjbIF myEjb = (MyEjbIF) context.lookup("java:comp/env/ejb/myEjb");
        assertEquals("Hello", myEjb.sayHello());
        assertTrue(myEjb.isConstructed());
        MyEjbClient client = (MyEjbClient) context.lookup("java:comp/env/ejb/myEjbClient");
        assertEquals("Hello", client.getInjectedEjb().sayHello());
    }

    @Test
    public void ejbLocalInServerXml() throws Exception {
        tomcatJNDI.processServerXml(new File("src/test/java/ejb/server.xml"), "/myWebApp");
        InitialContext context = new InitialContext();
        MyEjbIF myEjb = (MyEjbIF) context.lookup("java:comp/env/ejb/myEjb");
        assertEquals("Hello", myEjb.sayHello());
        assertTrue(myEjb.isConstructed());
        MyEjbClient client = (MyEjbClient) context.lookup("java:comp/env/ejb/myEjbClient");
        assertEquals("Hello", client.getInjectedEjb().sayHello());
    }

    @Test
    public void ejbLocalInServerXmlAndContextXml() throws Exception {
        tomcatJNDI.processServerXml(new File("src/test/java/ejb/ejbLocalInServerXmlAndContext/server.xml"), "myWebApp");
        tomcatJNDI.processContextXml(new File("src/test/java/ejb/ejbLocalInServerXmlAndContext/context.xml"));
        InitialContext context = new InitialContext();
        MyEjbIF myEjb = (MyEjbIF) context.lookup("java:comp/env/ejb/myEjb");
        assertEquals("Hello", myEjb.sayHello());
        assertTrue(myEjb.isConstructed());
        MyEjbClient client = (MyEjbClient) context.lookup("java:comp/env/ejb/myEjbClient");
        assertEquals("Hello", client.getInjectedEjb().sayHello());
    }

    @Test
    public void ejbRemoteInContextXml() throws Exception {
        Process process = Runtime.getRuntime().exec(new String[]{"java", "-Djava.util.logging.config.file=apache-openejb-7.0.4/conf/logging.properties", "-javaagent:apache-openejb-7.0.4/lib/openejb-javaagent-7.0.4.jar", "-cp", "apache-openejb-7.0.4/lib/openejb-core-7.0.4.jar:apache-openejb-7.0.4/lib/javaee-api-7.0-1.jar", "org.apache.openejb.cli.Bootstrap", "start"});
        try {
            Thread.sleep(5000);
            tomcatJNDI.processContextXml(new File("src/test/java/ejb/contexts/ejbDefaultRemote.xml"));
            InitialContext context = new InitialContext();
            MyRemoteEjbIF myEjb = (MyRemoteEjbIF) context.lookup("java:comp/env/ejb/myRemoteEjb");

            assertEquals("Hello from MyRemoteEjb", myEjb.sayHello());
            assertTrue(myEjb.isConstructed());
        }
        finally {
            if (process != null) {
                // Platform dependent code! Only working on *nix systems!
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                long pid = f.getLong(process);
                f.setAccessible(false);
                Runtime.getRuntime().exec(new String[]{"kill", "-6", String.valueOf(pid)});
            }
        }
    }
}