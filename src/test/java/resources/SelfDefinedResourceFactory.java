package resources;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 18.07.17
 */
public class SelfDefinedResourceFactory implements ObjectFactory {

    /**
     * Wird erst gerufen bei einem lookup auf die resource und nur beim ersten lookup.
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        return new SelfDefinedResource();
    }
}
