package hthurow.tomcatjndi;

import org.apache.catalina.*;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.NamingResources;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.naming.NamingContext;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NamingException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 26.07.17
 */
public class NamingContextListener extends org.apache.catalina.core.NamingContextListener {

//    private final NamingResources finalNamingResources;

//    public NamingContextListener(NamingResources finalNamingResources) {
////        this.finalNamingResources = finalNamingResources;
//    }

//    public NamingContextListener() {
//
//    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        container = event.getLifecycle();
        if (container instanceof Context) {
            ((Context)container).setLoader(new Loader() {
                @Override
                public void backgroundProcess() {

                }

                @Override
                public ClassLoader getClassLoader() {
                    return this.getClass().getClassLoader();
                }

                @Override
                public Container getContainer() {
                    return null;
                }

                @Override
                public void setContainer(Container container) {

                }

                @Override
                public boolean getDelegate() {
                    return false;
                }

                @Override
                public void setDelegate(boolean delegate) {

                }

                @Override
                public String getInfo() {
                    return null;
                }

                @Override
                public boolean getReloadable() {
                    return false;
                }

                @Override
                public void setReloadable(boolean reloadable) {

                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener listener) {

                }

                @Override
                public void addRepository(String repository) {

                }

                @Override
                public String[] findRepositories() {
                    return new String[0];
                }

                @Override
                public boolean modified() {
                    return false;
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener listener) {

                }
            });
        }

        super.lifecycleEvent(event);

//        if (Lifecycle.CONFIGURE_START_EVENT.equals(event.getType())) {
//            container = event.getLifecycle();
//            initialized = true;
//            namingResources = finalNamingResources;
//            try {
//                // "/": Bei einem anderen name werden zwar die Subkontexte erzeugt, aber beim tiefsten fehlen die gebundenen Objekte oder es wird eine OperationNotSupportedException geworfen beim Initialisieren. NamingContextListener.name ist default "/". Zum Binden eines Objekts wird in NamingContextListener#propertyChange() der Kontext mit diesem Namen mit ContextAccessController.setWritable(getName(), container) vorübergehend writable gemacht. Unterscheiden sich NamingContextListener.name und NamingContext.name, wird ein anderer Kontext writable gemacht als der, in dem das Objekt gebunden werden soll.
//                //String name = "/";
//                namingContext = new NamingContext(null, name);
//
//                // false: Schluckt OperationNotSupportedException bei bind(), wenn read-only context.
//                namingContext.setExceptionOnFailedWrite(true);
//
//                compCtx = namingContext.createSubcontext("comp");
//                envCtx = compCtx.createSubcontext("env");
//                // Speichert namingContext in ContextBindings#contextNameBindings unter dem Namen NamingContextListener@803
//                ContextBindings.bindContext(name, namingContext);
//                // Speichert in clNameBindings
//                ContextBindings.bindClassLoader(name, this, this.getClass().getClassLoader());
//
//                ContextAccessController.setReadOnly(name);
//                if (container instanceof Server) {
//                    org.apache.naming.factory.ResourceLinkFactory.setGlobalContext(namingContext);
//                    ((StandardServer)container).setGlobalNamingContext(namingContext);
//                }
//            }
//            catch (NamingException e) {
//                e.printStackTrace();
//            }
//        }
//        else if (Lifecycle.CONFIGURE_STOP_EVENT.equals(event.getType())) {
//            try {
//                ContextBindings.unbindContext(name, namingContext);
//                ContextBindings.unbindClassLoader(name, this, Thread.currentThread().getContextClassLoader());
//                ContextAccessController.setWritable(name, null);
//            }
//            finally {
//                objectNames.clear();
//                namingContext = null;
//                envCtx = null;
//                compCtx = null;
//                initialized = false;
//            }
//        }
    }

    /**
     *
     * Nur bei einer DataSource aufgerufen?
     * <p>
     * Sonst "Failed to register in JMX: javax.management.RuntimeOperationsException: Object name cannot be null"
     * <pre>
     Jul 24, 2017 7:28:36 AM org.apache.tomcat.util.modeler.Registry registerComponent
     SEVERE: Error registering null
     javax.management.RuntimeOperationsException: Object name cannot be null
     ...
     Jul 24, 2017 7:28:36 AM org.apache.catalina.core.NamingContextListener addResource
     WARNING: Failed to register in JMX: javax.management.RuntimeOperationsException: Object name cannot be null
     * </pre>
     */
    @Override
    protected ObjectName createObjectName(ContextResource resource) throws MalformedObjectNameException {
        String domain = "Catalina";
        ObjectName name;
        String quotedResourceName = ObjectName.quote(resource.getName());
        name = new ObjectName(domain + ":type=DataSource" +
                ",class=" + resource.getType() +
                ",name=" + quotedResourceName);
        return (name);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // TODO Aufruf mit ResourceLink behandeln.
        try {
            super.propertyChange(event);
        }
        catch (SecurityException e) {
            // Thrown with ResourceLinks
            System.out.println("Exception caught:");
            e.printStackTrace();
        }
    }
}
