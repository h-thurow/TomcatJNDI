package ejb;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.NamingException;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 14.10.17
 */
@Stateless
public class MyEjbClient {

    @EJB
    MyEjbIF myEjb;

    public MyEjbIF getInjectedEjb() throws NamingException {
        return myEjb;
    }
}
