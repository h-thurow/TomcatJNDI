# TomcatJNDI

Use classes dependent on Tomcat's JNDI environment outside of Tomcat.

The most common use case is the DataSouce, you configured within e. g. context.xml to access it via JNDI lookup. But it is not limited to DataSources. Instead TomcatJNDI will provide you with any JNDI based resource you configured within Tomcat. Just point TomcatJNDI at the configuration files to process.

Admittedly TomcatJNDI does less more than a little bit of configuration. All the magic comes from Tomcat's JNDI system itself.

### How to use

TomcatJNDI's API is simple. There are some process* Methods for the different configuration files:

    processServerXml(File serverXml)
    processServerXml(File serverXml, String contextName)
    processServerXml(File serverXml, String engineName, String hostName, String contextName)
    
    processContextXml(File contextXml)
    
    processDefaultWebXml(File defaultWebXml)
    processHostWebXml(File hostWebXml)
    processWebXml(File webXml)


All these methods are well documented on the class. 

You don't have to call all of these methods, just that one, appropriate for the type of file containing your configuration. From the various web.xml files only those files are required to be loaded in which you have declared a \<env-entry> element. 

There is one really important thing: When loading more than one configuration file comply with Tomcat's load sequence:


    conf/server.xml > conf/tomcat-users.xml > conf/context.xml > conf/Catalina/localhost/context.xml.default > conf/Catalina/localhost/[context_name].xml or META-INF/context.xml > conf/web.xml > conf/Catalina/localhost/web.xml.default   > WEB-INF/web.xml


Say you have configured some env-entry elements in WEB-INF/web.xml and a DataSource in META-INF/context.xml load context.xml before web.xml.  

After you have submitted all of your configuration files you have to call

    start()

When using TomcatJNDI in unit tests call

    tearDown()

after every test to clear the environment before executing the following test.

### Known to be working so far

|  | Element | Tested with |
| :---| :---| :---|
| server.xml | GlobalNamingResources/Resource | DataSource, JavaBean, UserDatabase
| | Host/Context/Environment | Boxed primitives
| | Host/Context/Ejb | EJB
| context.xml | Context/Resource | DataSource, JavaBean, javax.mail.Session
| | Context/ResourceLink | DataSource, JavaBean, UserDatabase
| | Context/Environment | Boxed primitives
| | Context/Ejb | EJB
| | Context/Transaction | UserTransaction
| web.xml | \<env-entry> | Boxed primitives








