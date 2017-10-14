package ejb;

import javax.ejb.Stateless;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 14.10.17
 */
@Stateless
public class MyEjb implements MyEjbIF {

    @Override
    public String sayHello() {
        String hello = "Hello";
        System.out.println(hello);
        return hello;
    }
}
