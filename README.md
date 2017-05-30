## Spring `DataSourceInitializer` bug reproduce

This repo contains spring boot configuration and two junit test cases that demonstrate a potential bug with the Spring [`DataSourceInitializer`][DataSourceInitializer] when used with two `DataSource` beans.

### The problem

The problem occurs when there is a `DataSource` bean that depends on another `DataSource` bean. This repo has an
example using a `@Primary` bean that needs another `DataSource` bean in construction. In this case, when the
`DataSourceInitializer` is enabled, the Spring context fails to start because of a reported circular bean reference:

    2017-05-30 15:53:47.023 ERROR 56382 --- [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener@24313fcc] to prepare test instance [org.example.TestFailsWhenInitializerRuns@7748410a]

    java.lang.IllegalStateException: Failed to load ApplicationContext
        at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:124) ~[spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:83) ~[spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener.prepareTestInstance(SpringBootDependencyInjectionTestExecutionListener.java:47) ~[spring-boot-test-autoconfigure-1.5.3.RELEASE.jar:1.5.3.RELEASE]
        at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:230) ~[spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.createTest(SpringJUnit4ClassRunner.java:228) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.test.context.junit4.SpringJUnit4ClassRunner$1.runReflectiveCall(SpringJUnit4ClassRunner.java:287) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12) [junit-4.12.jar:4.12]
        at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.methodBlock(SpringJUnit4ClassRunner.java:289) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:247) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:94) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290) [junit-4.12.jar:4.12]
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71) [junit-4.12.jar:4.12]
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288) [junit-4.12.jar:4.12]
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58) [junit-4.12.jar:4.12]
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268) [junit-4.12.jar:4.12]
        at org.springframework.test.context.junit4.statements.RunBeforeTestClassCallbacks.evaluate(RunBeforeTestClassCallbacks.java:61) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.test.context.junit4.statements.RunAfterTestClassCallbacks.evaluate(RunAfterTestClassCallbacks.java:70) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363) [junit-4.12.jar:4.12]
        at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.run(SpringJUnit4ClassRunner.java:191) [spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86) [.cp/:na]
        at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38) [.cp/:na]
        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:459) [.cp/:na]
        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:678) [.cp/:na]
        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:382) [.cp/:na]
        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192) [.cp/:na]
    Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'primaryDataSource' defined in org.example.ExampleConfig: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [javax.sql.DataSource]: Factory method 'primaryDataSource' threw exception; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'secondaryDataSource' defined in org.example.ExampleConfig: Initialization of bean failed; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSourceInitializer': Invocation of init method failed; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'primaryDataSource': Requested bean is currently in creation: Is there an unresolvable circular reference?
        at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:599) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1173) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1067) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:513) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:483) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:306) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:302) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:761) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:866) ~[spring-context-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:542) ~[spring-context-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:737) ~[spring-boot-1.5.3.RELEASE.jar:1.5.3.RELEASE]
        at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:370) ~[spring-boot-1.5.3.RELEASE.jar:1.5.3.RELEASE]
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:314) ~[spring-boot-1.5.3.RELEASE.jar:1.5.3.RELEASE]
        at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:120) ~[spring-boot-test-1.5.3.RELEASE.jar:1.5.3.RELEASE]
        at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:98) ~[spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:116) ~[spring-test-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        ... 24 common frames omitted
    Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [javax.sql.DataSource]: Factory method 'primaryDataSource' threw exception; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'secondaryDataSource' defined in org.example.ExampleConfig: Initialization of bean failed; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSourceInitializer': Invocation of init method failed; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'primaryDataSource': Requested bean is currently in creation: Is there an unresolvable circular reference?
        at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:189) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:588) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        ... 41 common frames omitted
    Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'secondaryDataSource' defined in org.example.ExampleConfig: Initialization of bean failed; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSourceInitializer': Invocation of init method failed; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'primaryDataSource': Requested bean is currently in creation: Is there an unresolvable circular reference?
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:483) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:306) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:302) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.context.annotation.ConfigurationClassEnhancer$BeanMethodInterceptor.obtainBeanInstanceFromFactory(ConfigurationClassEnhancer.java:389) ~[spring-context-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.context.annotation.ConfigurationClassEnhancer$BeanMethodInterceptor.intercept(ConfigurationClassEnhancer.java:361) ~[spring-context-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.example.ExampleConfig$$EnhancerBySpringCGLIB$$19db3d47.secondaryDataSource(<generated>) ~[bin/:na]
        at org.example.ExampleConfig.primaryDataSource(ExampleConfig.java:28) ~[bin/:na]
        at org.example.ExampleConfig$$EnhancerBySpringCGLIB$$19db3d47.CGLIB$primaryDataSource$0(<generated>) ~[bin/:na]
        at org.example.ExampleConfig$$EnhancerBySpringCGLIB$$19db3d47$$FastClassBySpringCGLIB$$6d526655.invoke(<generated>) ~[bin/:na]
        at org.springframework.cglib.proxy.MethodProxy.invokeSuper(MethodProxy.java:228) ~[spring-core-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.context.annotation.ConfigurationClassEnhancer$BeanMethodInterceptor.intercept(ConfigurationClassEnhancer.java:358) ~[spring-context-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.example.ExampleConfig$$EnhancerBySpringCGLIB$$19db3d47.primaryDataSource(<generated>) ~[bin/:na]
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0]
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0]
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0]
        at java.lang.reflect.Method.invoke(Method.java:483) ~[na:1.8.0]
        at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:162) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        ... 42 common frames omitted
    Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSourceInitializer': Invocation of init method failed; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'primaryDataSource': Requested bean is currently in creation: Is there an unresolvable circular reference?
        at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization(InitDestroyAnnotationBeanPostProcessor.java:137) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyBeanPostProcessorsBeforeInitialization(AbstractAutowireCapableBeanFactory.java:409) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1620) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:555) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:483) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:306) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:302) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:220) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveNamedBean(DefaultListableBeanFactory.java:1018) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:345) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:340) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerPostProcessor.postProcessAfterInitialization(DataSourceInitializerPostProcessor.java:62) ~[spring-boot-autoconfigure-1.5.3.RELEASE.jar:1.5.3.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyBeanPostProcessorsAfterInitialization(AbstractAutowireCapableBeanFactory.java:423) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1633) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:555) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        ... 61 common frames omitted
    Caused by: org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'primaryDataSource': Requested bean is currently in creation: Is there an unresolvable circular reference?
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.beforeSingletonCreation(DefaultSingletonBeanRegistry.java:347) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:223) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:302) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:220) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveNamedBean(DefaultListableBeanFactory.java:1037) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:345) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:340) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.context.support.AbstractApplicationContext.getBean(AbstractApplicationContext.java:1093) ~[spring-context-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.boot.autoconfigure.jdbc.DataSourceInitializer.init(DataSourceInitializer.java:77) ~[spring-boot-autoconfigure-1.5.3.RELEASE.jar:1.5.3.RELEASE]
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0]
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0]
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0]
        at java.lang.reflect.Method.invoke(Method.java:483) ~[na:1.8.0]
        at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleElement.invoke(InitDestroyAnnotationBeanPostProcessor.java:366) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleMetadata.invokeInitMethods(InitDestroyAnnotationBeanPostProcessor.java:311) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization(InitDestroyAnnotationBeanPostProcessor.java:134) ~[spring-beans-4.3.8.RELEASE.jar:4.3.8.RELEASE]
        ... 76 common frames omitted

### The Setup

This repo contains one Spring-Boot 1.5 based `@SpringBootConfiguration` class, `org.example.ExampleConfig`, that
defines the two `DataSource` beans. There are two test classes, `org.example.TestFailsWhenInitializerRuns` and
`org.example.TestPassesWithInitializerDisabled` that simply test that the spring context starts. The only difference
between the two test classes is that the passing class disables the `DataSourceInitializer` by setting
`spring.datasource.initialize=false` in the `@SpringBootTest` annotation.

The tests can be run using gradle. For example, `gradlew test`. `TestFailsWhenInitializerRuns` is expected to fail,
demonstrating the potential bug, while `TestPassesWithInitializerDisabled` is expected to pass.

### The Cause

It appears this is caused by an interaction between [`DataSourceInitializer`][DataSourceInitializer] and
[`DataSourceInitializerPostProcessor`][DataSourceInitializerPostProcessor]. The rough sequence of events seems to be:

1. `@Primary DataSource` bean starts creation.
2. This triggers creation of secondary `DataSource` bean (before the `@Primary` is finished being created, so the
   `@Primary` bean is still in creation for all subsequent steps).
3. Secondary `DataSource` bean is instantiated then post processors start running.
4. `DataSourceInitializerPostProcessor` runs and sees that the bean that has been created is a `DataSource`, so it asks
   the `beanFactory` for the `DataSourceInitializer` bean in order to trigger the initializer to run. (The javadoc
   comment states this is used to fire [`DataSourceInitializedEvent`][DataSourceInitializedEvent]s, but it's not clear
   to me why these events are necessary at this point in the lifecycle).
5. The `DataSourceInitializer` bean is instantiated and its `@PostConstruct init` method is run. This method calls into
   the `applicationContext` to get a bean of type `DataSource`.
6. The `applicationContext` finds both `primaryDataSource` and `secondaryDataSource` bean definitions as candidates.
7. The `applicationContext` selects `primaryDataSource` as the bean to return since it is marked `@Primary`. However,
   this triggers the circular reference exception since `primaryDataSource` is already in creation.

### Expected Result

Ideally, the `DataSourceInitializer` would only run on the `@Primary DataSource` bean definition. In doing so, it would
not run as supporting `DataSource` beans are created. I could see this working in at least two possible ways:

* The `DataSourceInitializerPostProcessor` could be smarter about when to ask the `beanFactory` for the
   `DataSourceInitializer` bean (e.g., it detects that the bean it is post processing is not primary?).
* The `DataSourceInitializer` could be smarter about asking for a `DataSource` bean.

[DataSourceInitializer]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/datasource/init/DataSourceInitializer.html
[DataSourceInitializerPostProcessor]: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jdbc/DataSourceInitializerPostProcessor.java
[DataSourceInitializedEvent]: http://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/jdbc/DataSourceInitializedEvent.html