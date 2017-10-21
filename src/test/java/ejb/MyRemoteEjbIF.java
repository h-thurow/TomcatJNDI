package ejb;

import javax.ejb.Remote;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 21.10.17
 */
@Remote
public interface MyRemoteEjbIF {
    String sayHello();

    boolean isConstructed();
}
