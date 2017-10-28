import ejb.EjbTest;
import hthurow.tomcatjndi.TomcatJndiTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 28.10.17
 */
@RunWith(Suite.class)
@SuiteClasses({
        TomcatJndiTest.class,
        EjbTest.class
})
public class TomcatJNDITestSuite {

}