# TomcatJNDI - Test DataSource and classes dependent on Tomcat's JNDI environment outside of Tomcat

A common problem when testing classes normally running within Tomcat is that they do some JNDI lookups to access objects stored as environment resources by Tomcat. But when testing those classes outside of Tomcat the JNDI environment does not exist. This is where TomcatJNDI comes into play. It will provide you with any JNDI based resource you configured within Tomcat. Just point it at the configuration files to process.

Admittedly it does less more than a little bit of configuration. All the magic comes from Tomcat's JNDI system itself. TomcatJNDI is based on embedded Tomcat but initializes only Tomcat's JNDI environment without starting a server. So you can access all your resources as configured in Tomcat's configuration files in tests or from within any Java SE application.

## Download

Up to Tomcat 7:

    <dependency>
        <groupId>com.github.h-thurow</groupId>
        <artifactId>TomcatJNDI</artifactId>
        <version>1.0.0</version>
    </dependency>
    
or <a href=http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.h-thurow%22%20AND%20a%3A%22TomcatJNDI%22>download from here</a>.

Tomcat 8:

    <dependency>
        <groupId>com.github.h-thurow</groupId>
        <artifactId>tomcat8jndi</artifactId>
        <version>1.0.0</version>
    </dependency>
    
or <a href=http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.h-thurow%22%20AND%20a%3A%22tomcat8jndi%22>download from here</a>.

## How to use

TomcatJNDI's API is simple. There are some process* Methods for the different configuration files:

    processServerXml(File serverXml)            // conf/server.xml
    processServerXml(File serverXml, String contextName)
    processServerXml(File serverXml, String engineName, String hostName, String contextName)
    
    processContextXml(File contextXml)          // conf/context.xml, context.xml.default, META-INF/context.xml or conf/Catalina/localhost/[context_name].xml
    
    processDefaultWebXml(File defaultWebXml)    // conf/web.xml
    processHostWebXml(File hostWebXml)          // web.xml.default
    processWebXml(File webXml)                  // WEB-INF/web.xml


All these methods are well documented on the class. 

You don't have to call all of these methods, just that one, appropriate for the type of file containing your configuration. From the various web.xml files only those files are required to be loaded in which you have declared a \<env-entry> element. 

There is one really important thing: When loading more than one configuration file comply with Tomcat's load sequence:


    conf/server.xml > conf/tomcat-users.xml > conf/context.xml > conf/Catalina/localhost/context.xml.default > conf/Catalina/localhost/[context_name].xml or META-INF/context.xml > conf/web.xml > conf/Catalina/localhost/web.xml.default   > WEB-INF/web.xml


Say you have configured some env-entry elements in WEB-INF/web.xml and a DataSource in META-INF/context.xml load context.xml before web.xml:
    
    TomcatJNDI tomcatJNDI = new TomcatJNDI();
    tomcatJNDI.processContextXml(contextXmlFile);
    tomcatJNDI.processWebXml(webXmlFile);
    tomcatJNDI.start();

When using TomcatJNDI in unit tests call

    tomcatJNDI.tearDown()

after every test to clear the environment before executing the following test.

## Only interested in a DataSource?

Store a file named context.xml containing

    <Context>
        <Resource name="path/to/datasource"
                  auth="Container"
                  type="javax.sql.DataSource"
                  username="userName"
                  password="password"
                  driverClassName="driverClassName"
                  url="url" />
    </Context>

Adjust the values as appropriate for your database.
    
Load this additional dependency

    <dependency>
        <groupId>org.apache.tomcat</groupId>
        <artifactId>tomcat-dbcp</artifactId>
        <!-- 7.0.79 | 8.0.47 -->
        <version>7.0.79</version>
    </dependency> 

Let TomcatJNDI initialize the JNDI environment

    TomcatJNDI tomcatJNDI = new TomcatJNDI();
    tomcatJNDI.processContextXml(contextXmlFile);
    tomcatJNDI.start();
    
Access the DataSource

    DataSource ds = (DataSource) InitialContext.doLookup("java:comp/env/path/to/datasource")
    
Note that you have to lookup the DataSource under "java:comp/env/...".

## Known to be working so far

|  | Element | Tested with |
| :---| :---| :---|
| server.xml | GlobalNamingResources/Resource | DataSource, JavaBean, UserDatabase
| | Host/Context/Environment | Boxed primitives
| | Host/Context/Ejb | EJB
| context.xml | Context/Resource | DataSource, JavaBean, JavaMail Session
| | Context/ResourceLink | DataSource, JavaBean, UserDatabase
| | Context/Environment | Boxed primitives
| | Context/Ejb | EJB
| | Context/Transaction | UserTransaction
| web.xml | \<env-entry> | Boxed primitives








