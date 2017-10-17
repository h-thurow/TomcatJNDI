package hthurow.tomcatjndi;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.Server;
import org.apache.catalina.core.*;
import org.apache.catalina.deploy.*;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.ContextRuleSet;
import org.apache.catalina.startup.NamingRuleSet;
import org.apache.catalina.startup.WebRuleSet;
import org.apache.tomcat.util.digester.Digester;
import org.xml.sax.SAXException;

import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * TODO Ensure correct files are provided to {@link #processDefaultWebXml(File)}, {@link #processHostWebXml(File)}, {@link #processServerXml(File)}, {@link #processWebXml(File)} and {@link #processContextXml(File)}.<br>
 * TODO Ensure correct order: server.xml > context xml files > web xml files.<br>
 * TODO server.xml parsing requires context name argument in case there are multiple contexts declared.<br>
 * TODO Remote EJBs<br>
 * TODO EJB declaration in server.xml<br>
 * TODO Transaction<br>
 * TODO Web fragment support<br>
 *
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 29.07.17
 */
public class TomcatJNDI {

    private NamingResources namingResources;
    private org.apache.catalina.core.NamingContextListener namingContextListener;
    private Server server;
    private StandardContext standardContext;
    private NamingContextListener globalNamingContextListener;
    private String hostName;
    private String engineName;
    private String contextName;

    public TomcatJNDI() {
        /* See Tomcat.enableNaming()
javax.naming.Context
public static final String URL_PKG_PREFIXES = "java.naming.factory.url.pkgs"
Constant that holds the name of the environment property for specifying the list of package prefixes to use when loading in URL context factories. The value of the property should be a colon-separated list of package prefixes for the class name of the factory class that will create a URL context factory. This property may be specified in the environment, an applet parameter, a system property, or one or more resource files. The prefix com.sun.jndi.url is always appended to the possibly empty list of package prefixes.
The value of this constant is "java.naming.factory.url.pkgs".

See also javax.naming.spi.NamingManager.getURLContext()
 */
        System.setProperty
                (Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.naming.java.javaURLContextFactory");
        // TODO Property nicht einfach überschreiben.
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");


    }

    private void initializeContext() {
        // TODO null ersetzen durch Optional?
        if (standardContext == null) {
            standardContext = new StandardContext();
            standardContext.setName("TomcatJNDI");
            standardContext.setParent(new StandardHost());
            StandardEngine standardEngine = new StandardEngine();
            standardContext.getParent().setParent(standardEngine);
            StandardService service = new StandardService();
            if (server == null) {
                server = new StandardServer();
            }
            service.setServer(server);
            standardEngine.setService(service);
            namingResources = new NamingResources();
            standardContext.setNamingResources(namingResources);
            namingContextListener = new NamingContextListener();
            namingContextListener.setName("TomcatJNDI");
            namingContextListener.lifecycleEvent(new LifecycleEvent(standardContext, Lifecycle.CONFIGURE_START_EVENT, null));
        }
    }

    /**
     * Subsequent calls with different context.xml files are possible. All objects are merged in one context. Comply with the correct order: conf/context.xml > context.xml.default > META-INF/context.xml respectively conf/Catalina/[host_name]/[context_name].xml).
     * @see #processServerXml(File)
     */
    public void processContextXml(File contextXml) {
        initializeContext();
        Digester digester = new Digester();
        digester.push(standardContext);
        // Siehe org.apache.catalina.startup.ContextConfig.createContextDigester()
        NamingRuleSet namingRuleSet = new NamingRuleSet("Context/");
        ContextRuleSet contextRuleSet = new ContextRuleSet("", false);
//        namingRuleSet.addRuleInstances(digester);
        digester.addRuleSet(namingRuleSet);
        digester.addRuleSet(contextRuleSet);
        try {
            digester.parse(contextXml);
        }
        catch (IOException | SAXException e) {
            // TODO Logging
            e.printStackTrace();
        }
    }

    /**
     * Initialization sequence is: {@link #processServerXml(File)} > {@link #processContextXml(File)} > {@link #processDefaultWebXml(File)} > {@link #processHostWebXml(File)} > {@link #processWebXml(File)}. Not every method must be called, but you have to comply with the given order.
     *
     * @param serverXml conf/server.xml
     */
    public void processServerXml(File serverXml) {
        if (server == null) {
            if (serverXml.getName().equals("server.xml")) {
                TomcatJNDICatalina catalina = new TomcatJNDICatalina();
                Digester digester = catalina.getDigester();
                digester.push(catalina);
                try {
                    digester.parse(serverXml);
                    server = catalina.getServer();
                    initializeGlobalNamingContext();
                    initializeContextFromServerXml();
                }
                catch (IOException | SAXException e) {
                    // TODO Logging
                    e.printStackTrace();
                }
            }
            else {
                throw new RuntimeException("Not a server.xml file");
            }
        }
        else {
            throw new RuntimeException("There can only be one server.xml");
        }
    }

    private void initializeGlobalNamingContext() {
        NamingResources globalNamingResources = server.getGlobalNamingResources();
        globalNamingContextListener = new NamingContextListener();
        globalNamingResources.addPropertyChangeListener(globalNamingContextListener);
        globalNamingContextListener.setName("TomcatJNDIServer");
        //ContextAccessController.setWritable("TomcatJNDIServer", server);
        globalNamingContextListener.lifecycleEvent(new LifecycleEvent(server, Lifecycle.CONFIGURE_START_EVENT, null));
    }

    /**
     * @see #processServerXml(File, String)
     */
    public void processServerXml(File serverXml, String engineName, String hostName, String contextName) {
        Objects.requireNonNull(serverXml);
        this.engineName = Objects.requireNonNull(engineName);
        this.hostName = Objects.requireNonNull(hostName);
        this.contextName = Objects.requireNonNull(contextName);
        processServerXml(serverXml);
    }

    /**
     * If you have declared some JNDI resources in server.xml within a Context element.
     * Engine name defaults to "Catalina",  Host name to "localhost".
     *
     * @param contextName name of the context as in Context's path attribute.
     */
    public void processServerXml(File serverXml, String contextName) {
        processServerXml(serverXml, "Catalina", "localhost", contextName);
    }

    private void initializeContextFromServerXml() {
        if (contextName != null) {
            standardContext = (StandardContext) server.findService(engineName).getContainer().findChild(hostName).findChild("/" + contextName);
            namingContextListener = standardContext.getNamingContextListener();
            if (namingContextListener == null) {
                namingContextListener = new NamingContextListener();
                namingContextListener.setName(standardContext.getName());
            }
            namingResources = standardContext.getNamingResources();
            namingResources.addPropertyChangeListener(namingContextListener);
            namingContextListener.lifecycleEvent(new LifecycleEvent(standardContext, Lifecycle.CONFIGURE_START_EVENT, null));
        }
    }

    /**
     *
     * @param hostWebXml web.xml.default
     * @see #processServerXml(File)
     */
    public void processHostWebXml(File hostWebXml) {
        if (hostWebXml.getName().equals("web.xml.default")) {
            processWebXml(hostWebXml, true);
        }
        else {
            throw new RuntimeException("Not a web.xml.default file");
        }
    }

    /**
     * @param setOverrideable web.xml.default: true. conf/web.xml and WEB-INF/web.xml: false.
     */
    @SuppressWarnings("WeakerAccess")
    private void processWebXml(File anyWebXml, boolean setOverrideable) {
        initializeContext();
        Digester digester = new Digester();
        WebXml webXml = new WebXml();
        digester.push(webXml);
        WebRuleSet webRuleSet = new WebRuleSet();
        webRuleSet.addRuleInstances(digester);
        try {
            digester.parse(anyWebXml);
            addEnvironment(webXml, setOverrideable);
            Collection<ContextResource> resources = webXml.getResourceRefs().values();
            for (ContextResource resource : resources) {
                namingResources.addResource(resource);
            }
            Map<String, ContextEjb> ejbRefs = webXml.getEjbRefs();
            for (ContextEjb contextEjb : ejbRefs.values()) {
                namingResources.addEjb(contextEjb);
            }
        }
        catch (IOException | SAXException e) {
            // TODO Logging
            e.printStackTrace();
        }
    }

    private void addEnvironment(WebXml webXml, boolean setOverrideable) {
        Collection<ContextEnvironment> envEntries = webXml.getEnvEntries().values();
        for (ContextEnvironment envEntry : envEntries) {
            envEntry.setOverride(setOverrideable);
            namingResources.addEnvironment(envEntry);
        }
    }

    /**
     *
     * @param webXmlFile WEB-INF/web.xml.
     * @see #processServerXml(File)
     */
    public void processWebXml(File webXmlFile) {
        processWebXml(webXmlFile, true);
    }

    void _processWebXml(File webXmlFile) {
        processWebXml(webXmlFile, true);
    }

    /**
     *
     * @param file conf/web.xml
     * @see #processServerXml(File)
     */
    public void processDefaultWebXml(File file) {
        processWebXml(file, false);
    }

    public void tearDown() {
        if (namingContextListener != null) {
            namingContextListener.lifecycleEvent(new LifecycleEvent(standardContext, Lifecycle.CONFIGURE_STOP_EVENT, null));
//            namingResources.removePropertyChangeListener(namingContextListener);
            standardContext = null;
        }
        if (globalNamingContextListener != null) {
            globalNamingContextListener.lifecycleEvent(new LifecycleEvent(server, Lifecycle.CONFIGURE_STOP_EVENT, null));
        }
    }

    private static class TomcatJNDICatalina extends Catalina {
        private Digester getDigester() {
            return super.createStartDigester();
        }
    }
}
