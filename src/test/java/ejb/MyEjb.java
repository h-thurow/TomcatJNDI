package ejb;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 14.10.17
 */
@Stateless
public class MyEjb implements MyEjbIF {

    private boolean isContructed;

    @Override
    public String sayHello() {
        String hello = "Hello";
        System.out.println(hello);
        return hello;
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
