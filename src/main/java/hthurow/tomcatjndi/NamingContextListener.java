package hthurow.tomcatjndi;

import org.apache.catalina.*;
import org.apache.catalina.deploy.ContextResource;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.beans.PropertyChangeEvent;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 26.07.17
 */
public class NamingContextListener extends org.apache.catalina.core.NamingContextListener {

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        container = event.getLifecycle();
        if (container instanceof Context) {
            ((Context)container).setLoader(new Loader());
        }

        super.lifecycleEvent(event);

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
