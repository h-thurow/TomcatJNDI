package ejb;

import hthurow.tomcatjndi.TomcatJNDI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
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
    public void ejbLocalContextXml() throws Exception {
        tomcatJNDI.processContextXml(new File("src/test/java/ejb/contexts/ejbDefault.xml"));
        InitialContext context = new InitialContext();
        MyEjbIF myEjb = (MyEjbIF) context.lookup("java:comp/env/ejb/myEjb");
        assertEquals("Hello", myEjb.sayHello());
        assertTrue(myEjb.isConstructed());
        MyEjbClient client = (MyEjbClient) context.lookup("java:comp/env/ejb/myEjbClient");
        assertEquals("Hello", client.getInjectedEjb().sayHello());
    }

    @Test
    public void ejbLocalServerXml() throws Exception {
        tomcatJNDI.processServerXml(new File("src/test/java/ejb/server.xml"), "myWebApp");
        InitialContext context = new InitialContext();
        MyEjbIF myEjb = (MyEjbIF) context.lookup("java:comp/env/ejb/myEjb");
        assertEquals("Hello", myEjb.sayHello());
        assertTrue(myEjb.isConstructed());
        MyEjbClient client = (MyEjbClient) context.lookup("java:comp/env/ejb/myEjbClient");
        assertEquals("Hello", client.getInjectedEjb().sayHello());
    }
}