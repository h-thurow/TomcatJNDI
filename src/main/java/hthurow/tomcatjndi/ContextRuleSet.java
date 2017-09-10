package hthurow.tomcatjndi;

import org.apache.catalina.startup.LifecycleListenerRule;
import org.apache.catalina.startup.RealmRuleSet;
import org.apache.catalina.startup.SetContextPropertiesRule;
import org.apache.catalina.startup.SetNextNamingRule;
import org.apache.tomcat.util.digester.Digester;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 10.09.17
 */
public class ContextRuleSet extends org.apache.catalina.startup.ContextRuleSet {

    public ContextRuleSet(String prefix, boolean create) {
        super(prefix, create);
    }

    @Override
    public void addRuleInstances(Digester digester) {

        if (create) {
            digester.addObjectCreate(prefix + "Context",
                    "org.apache.catalina.core.StandardContext", "className");
            digester.addSetProperties(prefix + "Context");
        } else {
            digester.addRule(prefix + "Context", new SetContextPropertiesRule());
        }

        if (create) {
            digester.addRule(prefix + "Context",
                    new LifecycleListenerRule
                            ("org.apache.catalina.startup.ContextConfig",
                                    "configClass"));
            digester.addSetNext(prefix + "Context",
                    "addChild",
                    "org.apache.catalina.Container");
        }
        digester.addCallMethod(prefix + "Context/InstanceListener",
                "addInstanceListener", 0);

        digester.addObjectCreate(prefix + "Context/Listener",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties(prefix + "Context/Listener");
        digester.addSetNext(prefix + "Context/Listener",
                "addLifecycleListener",
                "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate(prefix + "Context/Loader",
                "org.apache.catalina.loader.WebappLoader",
                "className");
        digester.addSetProperties(prefix + "Context/Loader");
        digester.addSetNext(prefix + "Context/Loader",
                "setLoader",
                "org.apache.catalina.Loader");

        digester.addObjectCreate(prefix + "Context/Manager",
                "org.apache.catalina.session.StandardManager",
                "className");
        digester.addSetProperties(prefix + "Context/Manager");
        digester.addSetNext(prefix + "Context/Manager",
                "setManager",
                "org.apache.catalina.Manager");

        digester.addObjectCreate(prefix + "Context/Manager/Store",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties(prefix + "Context/Manager/Store");
        digester.addSetNext(prefix + "Context/Manager/Store",
                "setStore",
                "org.apache.catalina.Store");

        digester.addObjectCreate(prefix + "Context/Manager/SessionIdGenerator",
                "org.apache.catalina.util.StandardSessionIdGenerator",
                "className");
        digester.addSetProperties(prefix + "Context/Manager/SessionIdGenerator");
        digester.addSetNext(prefix + "Context/Manager/SessionIdGenerator",
                "setSessionIdGenerator",
                "org.apache.catalina.SessionIdGenerator");
            /*
             * <Parameter name="..." value="..."/> results in
             * SEVERE: End event threw exception
             java.lang.NoSuchMethodException: org.apache.catalina.deploy.NamingResources addApplicationParameter
             * </pre>
             */
//        digester.addObjectCreate(prefix + "Context/Parameter",
//                "org.apache.catalina.deploy.ApplicationParameter");
//        digester.addSetProperties(prefix + "Context/Parameter");
//        digester.addSetNext(prefix + "Context/Parameter",
//                "addApplicationParameter",
//                "org.apache.catalina.deploy.ApplicationParameter");

        digester.addRuleSet(new RealmRuleSet(prefix + "Context/"));

        digester.addObjectCreate(prefix + "Context/Resources",
                "org.apache.naming.resources.FileDirContext",
                "className");
        digester.addSetProperties(prefix + "Context/Resources");
        digester.addSetNext(prefix + "Context/Resources",
                "setResources",
                "javax.naming.directory.DirContext");

        digester.addObjectCreate(prefix + "Context/ResourceLink",
                "org.apache.catalina.deploy.ContextResourceLink");
        digester.addSetProperties(prefix + "Context/ResourceLink");
        digester.addRule(prefix + "Context/ResourceLink",
                new SetNextNamingRule("addResourceLink",
                        "org.apache.catalina.deploy.ContextResourceLink"));

        digester.addObjectCreate(prefix + "Context/Valve",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties(prefix + "Context/Valve");
        digester.addSetNext(prefix + "Context/Valve",
                "addValve",
                "org.apache.catalina.Valve");

        digester.addCallMethod(prefix + "Context/WatchedResource",
                "addWatchedResource", 0);

        digester.addCallMethod(prefix + "Context/WrapperLifecycle",
                "addWrapperLifecycle", 0);

        digester.addCallMethod(prefix + "Context/WrapperListener",
                "addWrapperListener", 0);

        digester.addObjectCreate(prefix + "Context/JarScanner",
                "org.apache.tomcat.util.scan.StandardJarScanner",
                "className");
        digester.addSetProperties(prefix + "Context/JarScanner");
        digester.addSetNext(prefix + "Context/JarScanner",
                "setJarScanner",
                "org.apache.tomcat.JarScanner");

    }
}
