package ejb;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 21.10.17
 */
@Stateless
public class MyRemoteEjb implements MyRemoteEjbIF {

    private boolean isContructed;

    @Override
    public String sayHello() {
        return "Hello from MyRemoteEjb";
    }

    @PostConstruct
    private void postConstruct() {
        isContructed = true;
    }

    @Override
    public boolean isConstructed() {
        return isContructed;
    }
}
