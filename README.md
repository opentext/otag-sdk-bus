# Open Text AppWorks service development kit bus

The service development kit (SDK) event bus provides access to the set of domain classes required to interact
 with an instance of an AppWorks Gateway from an AppWorks Service. These classes are used by the AppWorks 
 service development kit (https://github.com/opentext/otag-service-development-kit) to make use of the functionality
 the hosting Gateway provides. This module also provides a centralised non-blocking event bus, managed by the Gateway.
 
 Please include it in the dependencies of AppWorks Services that are to be deployed to the AppWorks 16.5.0 
 (or later) Gateway as follows. It should be marked as provided (i.e. not included in the final service zip) else 
 communication between the service and Gateway will fail.
 
 ```xml
<dependency>
    <groupId>com.opentext.otag.sdk.bus</groupId>
    <artifactId>otag-sdk-bus</artifactId>
    <version>16.5.0</version>
    <scope>provided</scope>
</dependency>
```

# Documentation

The SDK documentation is hosted over at the AppWorks developer portal, it can be found at the following location:

<https://developer.opentext.com/awd/resources/articles/15239948/developer+guide+opentext+appworks+16+service+development+kit>

# License

This software is available under the following licenses:

## Open Text End User License Agreement -

<https://developer.opentext.com/awd/resources/articles/15235159/end+user+software+license+agreement+for+open+text+corporation+software>

## Trial Use Agreement -

<https://developer.opentext.com/awd/resources/articles/15235173/trial+use+agreement>
