// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.AdvisorDef;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.IOCConstants;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.ServiceLifecycle2;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.ContributionDef2;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.def.ServiceDef2;
import org.apache.tapestry5.ioc.def.ServiceDef3;
import org.apache.tapestry5.ioc.internal.services.PerthreadManagerImpl;
import org.apache.tapestry5.ioc.internal.services.RegistryShutdownHubImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.internal.util.Orderer;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry5.ioc.services.ServiceLifecycleSource;
import org.apache.tapestry5.ioc.services.Status;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TapestryIOCModule;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.slf4j.Logger;

@SuppressWarnings("all")
public class RegistryImpl implements Registry, InternalRegistry, ServiceProxyProvider
{
    private static final String SYMBOL_SOURCE_SERVICE_ID = "SymbolSource";

    private static final String REGISTRY_SHUTDOWN_HUB_SERVICE_ID = "RegistryShutdownHub";

    static final String PERTHREAD_MANAGER_SERVICE_ID = "PerthreadManager";

    private static final String SERVICE_ACTIVITY_SCOREBOARD_SERVICE_ID = "ServiceActivityScoreboard";

    /**
     * The set of marker annotations for a builtin service.
     */
    private final static Set<Class> BUILTIN = CollectionFactory.newSet();

    // Split create/assign to appease generics gods
    static
    {
        BUILTIN.add(Builtin.class);
    }

    /**
     * Used to obtain the {@link org.apache.tapestry5.ioc.services.ClassFactory} service, which is
     * crucial when creating
     * runtime classes for proxies and the like.
     */
    static final String CLASS_FACTORY_SERVICE_ID = "ClassFactory";

    static final String PLASTIC_PROXY_FACTORY_SERVICE_ID = "PlasticProxyFactory";

    static final String LOGGER_SOURCE_SERVICE_ID = "LoggerSource";

    private final OneShotLock lock = new OneShotLock();

    private final OneShotLock eagerLoadLock = new OneShotLock();

    private final Map<String, Object> builtinServices = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, Class> builtinTypes = CollectionFactory.newCaseInsensitiveMap();

    private final RegistryShutdownHubImpl registryShutdownHub;

    private final LoggerSource loggerSource;

    /**
     * Map from service id to the Module that contains the service.
     */
    private final Map<String, Module> serviceIdToModule = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, ServiceLifecycle2> lifecycles = CollectionFactory.newCaseInsensitiveMap();

    private final PerthreadManager perthreadManager;

    private final ClassFactory classFactory;

    private final PlasticProxyFactory proxyFactory;

    private final ServiceActivityTracker tracker;

    private SymbolSource symbolSource;

    private final Map<Module, Set<ServiceDef2>> moduleToServiceDefs = CollectionFactory.newMap();

    /**
     * From marker type to a list of marked service instances.
     */
    private final Map<Class, List<ServiceDef2>> markerToServiceDef = CollectionFactory.newMap();

    private final Set<ServiceDef2> allServiceDefs = CollectionFactory.newSet();

    private final OperationTracker operationTracker;

    private final TypeCoercerProxy typeCoercerProxy = new TypeCoercerProxyImpl(this);

    /**
     * Constructs the registry from a set of module definitions and other resources.
     * 
     * @param moduleDefs
     *            defines the modules (and builders, decorators, etc., within)
     * @param classFactory
     *            TODO
     * @param proxyFactory
     *            TODO
     * @param loggerSource
     *            used to obtain Logger instances
     */
    public RegistryImpl(Collection<ModuleDef> moduleDefs, ClassFactory classFactory, PlasticProxyFactory proxyFactory,
            LoggerSource loggerSource)
    {
        assert moduleDefs != null;
        assert classFactory != null;
        assert proxyFactory != null;
        assert loggerSource != null;

        this.loggerSource = loggerSource;

        operationTracker = new PerThreadOperationTracker(loggerSource.getLogger(Registry.class));

        this.classFactory = classFactory;
        this.proxyFactory = proxyFactory;

        Logger logger = loggerForBuiltinService(PERTHREAD_MANAGER_SERVICE_ID);

        perthreadManager = new PerthreadManagerImpl(logger);

        final ServiceActivityTrackerImpl scoreboardAndTracker = new ServiceActivityTrackerImpl(perthreadManager);

        tracker = scoreboardAndTracker;

        logger = loggerForBuiltinService(REGISTRY_SHUTDOWN_HUB_SERVICE_ID);

        registryShutdownHub = new RegistryShutdownHubImpl(logger);

        lifecycles.put("singleton", new SingletonServiceLifecycle());

        registryShutdownHub.addRegistryShutdownListener(new RegistryShutdownListener()
        {
            public void registryDidShutdown()
            {
                scoreboardAndTracker.shutdown();
            }
        });

        for (ModuleDef def : moduleDefs)
        {
            logger = this.loggerSource.getLogger(def.getLoggerName());

            Module module = new ModuleImpl(this, tracker, def, proxyFactory, logger);

            Set<ServiceDef2> moduleServiceDefs = CollectionFactory.newSet();

            for (String serviceId : def.getServiceIds())
            {
                ServiceDef2 serviceDef = module.getServiceDef(serviceId);

                moduleServiceDefs.add(serviceDef);
                allServiceDefs.add(serviceDef);

                Module existing = serviceIdToModule.get(serviceId);

                if (existing != null)
                    throw new RuntimeException(IOCMessages.serviceIdConflict(serviceId,
                            existing.getServiceDef(serviceId), serviceDef));

                serviceIdToModule.put(serviceId, module);

                // The service is defined but will not have gone further than that.
                tracker.define(serviceDef, Status.DEFINED);

                for (Class marker : serviceDef.getMarkers())
                    InternalUtils.addToMapList(markerToServiceDef, marker, serviceDef);
            }

            moduleToServiceDefs.put(module, moduleServiceDefs);
        }

        addBuiltin(SERVICE_ACTIVITY_SCOREBOARD_SERVICE_ID, ServiceActivityScoreboard.class, scoreboardAndTracker);
        addBuiltin(LOGGER_SOURCE_SERVICE_ID, LoggerSource.class, this.loggerSource);
        addBuiltin(CLASS_FACTORY_SERVICE_ID, ClassFactory.class, this.classFactory);
        addBuiltin(PERTHREAD_MANAGER_SERVICE_ID, PerthreadManager.class, perthreadManager);
        addBuiltin(REGISTRY_SHUTDOWN_HUB_SERVICE_ID, RegistryShutdownHub.class, registryShutdownHub);
        addBuiltin(PLASTIC_PROXY_FACTORY_SERVICE_ID, PlasticProxyFactory.class, proxyFactory);

        validateContributeDefs(moduleDefs);

        scoreboardAndTracker.startup();

        SerializationSupport.setProvider(this);
    }

    /**
     * Validate that each module's ContributeDefs correspond to an actual service.
     */
    private void validateContributeDefs(Collection<ModuleDef> moduleDefs)
    {
        for (ModuleDef module : moduleDefs)
        {
            Set<ContributionDef> contributionDefs = module.getContributionDefs();

            for (ContributionDef cd : contributionDefs)
            {
                String serviceId = cd.getServiceId();

                ContributionDef2 cd2 = InternalUtils.toContributionDef2(cd);

                if (cd2.getServiceId() != null)
                {
                    if (!serviceIdToModule.containsKey(serviceId)) { throw new IllegalArgumentException(
                            IOCMessages.contributionForNonexistentService(cd)); }
                }
                else if (!isContributionForExistentService(module, cd2)) { throw new IllegalArgumentException(
                        IOCMessages.contributionForUnqualifiedService(cd2)); }
            }
        }

    }

    /**
     * Invoked when the contribution method didn't follow the naming convention and so doesn't identify
     * a service by id; instead there was an @Contribute to identify the service interface.
     */
    @SuppressWarnings("all")
    private boolean isContributionForExistentService(ModuleDef moduleDef, final ContributionDef2 cd)
    {
        final Set<Class> contributionMarkers = new HashSet(cd.getMarkers());

        boolean localOnly = contributionMarkers.contains(Local.class);

        Flow<ServiceDef2> serviceDefs = localOnly ? getLocalServiceDefs(moduleDef) : F.flow(allServiceDefs);

        contributionMarkers.retainAll(getMarkerAnnotations());
        contributionMarkers.remove(Local.class);

        // Match services with the correct interface AND having as markers *all* the marker annotations

        Flow<ServiceDef2> filtered = serviceDefs.filter(F.and(new Predicate<ServiceDef2>()
        {
            public boolean accept(ServiceDef2 object)
            {
                return object.getServiceInterface().equals(cd.getServiceInterface());
            }
        }, new Predicate<ServiceDef2>()
        {
            public boolean accept(ServiceDef2 serviceDef)
            {
                return serviceDef.getMarkers().containsAll(contributionMarkers);
            }
        }));

        // That's a lot of logic; the good news is it will short-circuit as soon as it finds a single match,
        // thanks to the laziness inside Flow.

        return !filtered.isEmpty();
    }

    private Flow<ServiceDef2> getLocalServiceDefs(final ModuleDef moduleDef)
    {
        return F.flow(moduleDef.getServiceIds()).map(new Mapper<String, ServiceDef2>()
        {
            public ServiceDef2 map(String value)
            {
                return InternalUtils.toServiceDef2(moduleDef.getServiceDef(value));
            }
        });
    }

    /**
     * It's not unreasonable for an eagerly-loaded service to decide to start a thread, at which
     * point we raise issues
     * about improper publishing of the Registry instance from the RegistryImpl constructor. Moving
     * eager loading of
     * services out to its own method should ensure thread safety.
     */
    public void performRegistryStartup()
    {
        eagerLoadLock.lock();

        List<EagerLoadServiceProxy> proxies = CollectionFactory.newList();

        for (Module m : moduleToServiceDefs.keySet())
            m.collectEagerLoadServices(proxies);

        // TAPESTRY-2267: Gather up all the proxies before instantiating any of them.

        for (EagerLoadServiceProxy proxy : proxies)
            proxy.eagerLoadService();

        getService("RegistryStartup", Runnable.class).run();

        cleanupThread();
    }

    public Logger getServiceLogger(String serviceId)
    {
        Module module = serviceIdToModule.get(serviceId);

        assert module != null;

        return loggerSource.getLogger(module.getLoggerName() + "." + serviceId);
    }

    private Logger loggerForBuiltinService(String serviceId)
    {
        return loggerSource.getLogger(TapestryIOCModule.class + "." + serviceId);
    }

    private <T> void addBuiltin(final String serviceId, final Class<T> serviceInterface, T service)
    {
        builtinTypes.put(serviceId, serviceInterface);
        builtinServices.put(serviceId, service);

        // Make sure each of the builtin services is also available via the Builtin annotation
        // marker.

        ServiceDef2 serviceDef = new ServiceDef2()
        {
            public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
            {
                return null;
            }

            public Set<Class> getMarkers()
            {
                return BUILTIN;
            }

            public String getServiceId()
            {
                return serviceId;
            }

            public Class getServiceInterface()
            {
                return serviceInterface;
            }

            public String getServiceScope()
            {
                return ScopeConstants.DEFAULT;
            }

            public boolean isEagerLoad()
            {
                return false;
            }

            public boolean isPreventDecoration()
            {
                return true;
            }
        };

        for (Class marker : serviceDef.getMarkers())
        {
            InternalUtils.addToMapList(markerToServiceDef, marker, serviceDef);
            allServiceDefs.add(serviceDef);
        }

        tracker.define(serviceDef, Status.BUILTIN);
    }

    public synchronized void shutdown()
    {
        lock.lock();

        registryShutdownHub.fireRegistryDidShutdown();

        SerializationSupport.clearProvider(this);
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        lock.check();

        T result = checkForBuiltinService(serviceId, serviceInterface);
        if (result != null)
            return result;

        // Checking serviceId and serviceInterface is overkill; they have been checked and rechecked
        // all the way to here.

        Module containingModule = locateModuleForService(serviceId);

        return containingModule.getService(serviceId, serviceInterface);
    }

    private <T> T checkForBuiltinService(String serviceId, Class<T> serviceInterface)
    {
        Object service = builtinServices.get(serviceId);

        if (service == null)
            return null;

        try
        {
            return serviceInterface.cast(service);
        }
        catch (ClassCastException ex)
        {
            throw new RuntimeException(IOCMessages.serviceWrongInterface(serviceId, builtinTypes.get(serviceId),
                    serviceInterface));
        }
    }

    public void cleanupThread()
    {
        lock.check();

        perthreadManager.cleanup();
    }

    private Module locateModuleForService(String serviceId)
    {
        Module module = serviceIdToModule.get(serviceId);

        if (module == null)
            throw new UnknownValueException(String.format("Service id '%s' is not defined by any module.", serviceId),
                    new AvailableValues("Defined service ids", serviceIdToModule));

        return module;
    }

    public <T> Collection<T> getUnorderedConfiguration(ServiceDef3 serviceDef, Class<T> objectType)
    {
        lock.check();

        final Collection<T> result = CollectionFactory.newList();

        for (Module m : moduleToServiceDefs.keySet())
            addToUnorderedConfiguration(result, objectType, serviceDef, m);

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getOrderedConfiguration(ServiceDef3 serviceDef, Class<T> objectType)
    {
        lock.check();

        String serviceId = serviceDef.getServiceId();
        Logger logger = getServiceLogger(serviceId);

        Orderer<T> orderer = new Orderer<T>(logger);
        Map<String, OrderedConfigurationOverride<T>> overrides = CollectionFactory.newCaseInsensitiveMap();

        for (Module m : moduleToServiceDefs.keySet())
            addToOrderedConfiguration(orderer, overrides, objectType, serviceDef, m);

        // An ugly hack ... perhaps we should introduce a new builtin service so that this can be
        // accomplished in the normal way?

        if (serviceId.equals("MasterObjectProvider"))
        {
            ObjectProvider contribution = new ObjectProvider()
            {
                public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
                {
                    return findServiceByMarkerAndType(objectType, annotationProvider, null);
                }
            };

            orderer.add("ServiceByMarker", (T) contribution);
        }

        for (OrderedConfigurationOverride<T> override : overrides.values())
            override.apply();

        return orderer.getOrdered();
    }

    public <K, V> Map<K, V> getMappedConfiguration(ServiceDef3 serviceDef, Class<K> keyType, Class<V> objectType)
    {
        lock.check();

        // When the key type is String, then a case insensitive map is used.

        Map<K, V> result = newConfigurationMap(keyType);
        Map<K, ContributionDef> keyToContribution = newConfigurationMap(keyType);
        Map<K, MappedConfigurationOverride<K, V>> overrides = newConfigurationMap(keyType);

        for (Module m : moduleToServiceDefs.keySet())
            addToMappedConfiguration(result, overrides, keyToContribution, keyType, objectType, serviceDef, m);

        for (MappedConfigurationOverride<K, V> override : overrides.values())
        {
            override.apply();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> newConfigurationMap(Class<K> keyType)
    {
        if (keyType.equals(String.class))
        {
            Map<String, K> result = CollectionFactory.newCaseInsensitiveMap();

            return (Map<K, V>) result;
        }

        return CollectionFactory.newMap();
    }

    private <K, V> void addToMappedConfiguration(Map<K, V> map, Map<K, MappedConfigurationOverride<K, V>> overrides,
            Map<K, ContributionDef> keyToContribution, Class<K> keyClass, Class<V> valueType, ServiceDef3 serviceDef,
            final Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef2> contributions = module.getContributorDefsForService(serviceDef);

        if (contributions.isEmpty())
            return;

        Logger logger = getServiceLogger(serviceId);

        boolean debug = logger.isDebugEnabled();

        final ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

        for (final ContributionDef def : contributions)
        {
            final MappedConfiguration<K, V> validating = new ValidatingMappedConfigurationWrapper<K, V>(valueType,
                    resources, typeCoercerProxy, map, overrides, serviceId, def, keyClass, keyToContribution);

            String description = IOCMessages.invokingMethod(def);

            if (debug)
                logger.debug(description);

            operationTracker.run(description, new Runnable()
            {
                public void run()
                {
                    def.contribute(module, resources, validating);
                }
            });
        }
    }

    private <T> void addToUnorderedConfiguration(Collection<T> collection, Class<T> valueType, ServiceDef3 serviceDef,
            final Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef2> contributions = module.getContributorDefsForService(serviceDef);

        if (contributions.isEmpty())
            return;

        Logger logger = getServiceLogger(serviceId);

        boolean debug = logger.isDebugEnabled();

        final ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

        for (final ContributionDef def : contributions)
        {
            final Configuration<T> validating = new ValidatingConfigurationWrapper<T>(valueType, resources,
                    typeCoercerProxy, collection, serviceId);

            String description = IOCMessages.invokingMethod(def);

            if (debug)
                logger.debug(description);

            operationTracker.run(description, new Runnable()
            {
                public void run()
                {
                    def.contribute(module, resources, validating);
                }
            });
        }
    }

    private <T> void addToOrderedConfiguration(Orderer<T> orderer,
            Map<String, OrderedConfigurationOverride<T>> overrides, Class<T> valueType, ServiceDef3 serviceDef,
            final Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef2> contributions = module.getContributorDefsForService(serviceDef);

        if (contributions.isEmpty())
            return;

        Logger logger = getServiceLogger(serviceId);
        boolean debug = logger.isDebugEnabled();

        final ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

        for (final ContributionDef def : contributions)
        {
            final OrderedConfiguration<T> validating = new ValidatingOrderedConfigurationWrapper<T>(valueType,
                    resources, typeCoercerProxy, orderer, overrides, def);

            String description = IOCMessages.invokingMethod(def);

            if (debug)
                logger.debug(description);

            operationTracker.run(description, new Runnable()
            {
                public void run()
                {

                    def.contribute(module, resources, validating);
                }
            });
        }
    }

    public <T> T getService(Class<T> serviceInterface)
    {
        lock.check();

        List<String> serviceIds = findServiceIdsForInterface(serviceInterface);

        if (serviceIds == null)
            serviceIds = Collections.emptyList();

        switch (serviceIds.size())
        {
            case 0:

                throw new RuntimeException(IOCMessages.noServiceMatchesType(serviceInterface));

            case 1:

                String serviceId = serviceIds.get(0);

                return getService(serviceId, serviceInterface);

            default:

                Collections.sort(serviceIds);

                throw new RuntimeException(IOCMessages.manyServiceMatches(serviceInterface, serviceIds));
        }
    }

    private List<String> findServiceIdsForInterface(Class serviceInterface)
    {
        List<String> result = CollectionFactory.newList();

        for (Module module : moduleToServiceDefs.keySet())
            result.addAll(module.findServiceIdsForInterface(serviceInterface));

        for (Map.Entry<String, Object> entry : builtinServices.entrySet())
        {
            if (serviceInterface.isInstance(entry.getValue()))
                result.add(entry.getKey());
        }

        Collections.sort(result);

        return result;
    }

    public ServiceLifecycle2 getServiceLifecycle(String scope)
    {
        lock.check();

        ServiceLifecycle result = lifecycles.get(scope);

        if (result == null)
        {
            ServiceLifecycleSource source = getService("ServiceLifecycleSource", ServiceLifecycleSource.class);

            result = source.get(scope);
        }

        if (result == null)
            throw new RuntimeException(IOCMessages.unknownScope(scope));

        return InternalUtils.toServiceLifecycle2(result);
    }

    public List<ServiceDecorator> findDecoratorsForService(ServiceDef3 serviceDef)
    {
        lock.check();

        assert serviceDef != null;

        Logger logger = getServiceLogger(serviceDef.getServiceId());

        Orderer<ServiceDecorator> orderer = new Orderer<ServiceDecorator>(logger);

        for (Module module : moduleToServiceDefs.keySet())
        {
            Set<DecoratorDef> decoratorDefs = module.findMatchingDecoratorDefs(serviceDef);

            if (decoratorDefs.isEmpty())
                continue;

            ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

            for (DecoratorDef decoratorDef : decoratorDefs)
            {
                ServiceDecorator decorator = decoratorDef.createDecorator(module, resources);

                orderer.add(decoratorDef.getDecoratorId(), decorator, decoratorDef.getConstraints());
            }
        }

        return orderer.getOrdered();
    }

    public List<ServiceAdvisor> findAdvisorsForService(ServiceDef3 serviceDef)
    {
        lock.check();

        assert serviceDef != null;

        Logger logger = getServiceLogger(serviceDef.getServiceId());

        Orderer<ServiceAdvisor> orderer = new Orderer<ServiceAdvisor>(logger);

        for (Module module : moduleToServiceDefs.keySet())
        {
            Set<AdvisorDef> advisorDefs = module.findMatchingServiceAdvisors(serviceDef);

            if (advisorDefs.isEmpty())
                continue;

            ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

            for (AdvisorDef advisorDef : advisorDefs)
            {
                ServiceAdvisor advisor = advisorDef.createAdvisor(module, resources);

                orderer.add(advisorDef.getAdvisorId(), advisor, advisorDef.getConstraints());
            }
        }

        return orderer.getOrdered();
    }

    public ClassFab newClass(Class serviceInterface)
    {
        lock.check();

        return classFactory.newClass(serviceInterface);
    }

    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator,
            Module localModule)
    {
        lock.check();

        AnnotationProvider effectiveProvider = annotationProvider != null ? annotationProvider
                : new NullAnnotationProvider();

        // We do a check here for known marker/type combinations, so that you can use a marker
        // annotation
        // to inject into a contribution method that contributes to MasterObjectProvider.
        // We also force a contribution into MasterObjectProvider to accomplish the same thing.

        T result = findServiceByMarkerAndType(objectType, annotationProvider, localModule);

        if (result != null)
            return result;

        MasterObjectProvider masterProvider = getService(IOCConstants.MASTER_OBJECT_PROVIDER_SERVICE_ID,
                MasterObjectProvider.class);

        return masterProvider.provide(objectType, effectiveProvider, locator, true);
    }

    private Collection<ServiceDef2> filterByType(Class<?> objectType, Collection<ServiceDef2> serviceDefs)
    {
        Collection<ServiceDef2> result = CollectionFactory.newSet();

        for (ServiceDef2 sd : serviceDefs)
        {
            if (objectType.isAssignableFrom(sd.getServiceInterface()))
            {
                result.add(sd);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T findServiceByMarkerAndType(Class<T> objectType, AnnotationProvider provider, Module localModule)
    {
        if (provider == null)
            return null;

        boolean localOnly = localModule != null && provider.getAnnotation(Local.class) != null;

        Set<ServiceDef2> matches = CollectionFactory.newSet();

        matches.addAll(filterByType(objectType, localOnly ? moduleToServiceDefs.get(localModule) : allServiceDefs));

        List<Class> markers = CollectionFactory.newList();

        if (localOnly)
            markers.add(Local.class);

        for (Class marker : markerToServiceDef.keySet())
        {
            if (provider.getAnnotation(marker) == null)
                continue;

            markers.add(marker);

            matches = intersection(matches, markerToServiceDef.get(marker));
        }

        // If didn't see @Local or any recognized marker annotation, then don't try to filter that
        // way.
        // Continue on, eventually to the MasterObjectProvider service.

        if (markers.isEmpty())
            return null;

        switch (matches.size())
        {

            case 1:

                ServiceDef def = matches.iterator().next();

                return getService(def.getServiceId(), objectType);

            case 0:

                // It's no accident that the user put the marker annotation at the injection
                // point, since it matches a known marker annotation, it better be there for
                // a reason. So if we don't get a match, we have to assume the user expected
                // one, and that is an error.

                // This doesn't help when the user places an annotation they *think* is a marker
                // but isn't really a marker (because no service is marked by the annotation).

                throw new RuntimeException(IOCMessages.noServicesMatchMarker(objectType, markers));

            default:
                throw new RuntimeException(IOCMessages.manyServicesMatchMarker(objectType, markers, matches));
        }
    }

    /**
     * Filters the set into a new set, containing only elements shared between the set and the
     * filter collection.
     * 
     * @param set
     *            to be filtered
     * @param filter
     *            values to keep from the set
     * @return a new set containing only the shared values
     */
    private static <T> Set<T> intersection(Set<T> set, Collection<T> filter)
    {
        if (set.isEmpty())
            return Collections.emptySet();

        Set<T> result = CollectionFactory.newSet();

        for (T elem : filter)
        {
            if (set.contains(elem))
                result.add(elem);
        }

        return result;
    }

    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return getObject(objectType, annotationProvider, this, null);
    }

    public void addRegistryShutdownListener(RegistryShutdownListener listener)
    {
        lock.check();

        registryShutdownHub.addRegistryShutdownListener(listener);
    }

    public String expandSymbols(String input)
    {
        lock.check();

        // Again, a bit of work to avoid instantiating the SymbolSource until absolutely necessary.

        if (!InternalUtils.containsSymbols(input))
            return input;

        return getSymbolSource().expandSymbols(input);
    }

    /**
     * Defers obtaining the symbol source until actually needed.
     */
    private synchronized SymbolSource getSymbolSource()
    {
        if (symbolSource == null)
            symbolSource = getService(SYMBOL_SOURCE_SERVICE_ID, SymbolSource.class);

        return symbolSource;
    }

    public <T> T autobuild(String description, final Class<T> clazz)
    {
        return invoke(description, new Invokable<T>()
        {
            public T invoke()
            {
                return autobuild(clazz);
            }
        });
    }

    public <T> T autobuild(final Class<T> clazz)
    {
        assert clazz != null;
        final Constructor constructor = InternalUtils.findAutobuildConstructor(clazz);

        if (constructor == null)
            throw new RuntimeException(IOCMessages.noAutobuildConstructor(clazz));

        Map<Class, Object> resourcesMap = CollectionFactory.newMap();
        resourcesMap.put(OperationTracker.class, RegistryImpl.this);

        final InjectionResources resources = new MapInjectionResources(resourcesMap);

        final Invokable<T> operation = new Invokable<T>()
        {
            public T invoke()
            {
                InternalUtils.validateConstructorForAutobuild(constructor);

                Object result = invokeConstructor(constructor, resources);

                InternalUtils.injectIntoFields(result, RegistryImpl.this, resources, RegistryImpl.this);

                return clazz.cast(result);
            }
        };

        T result = invoke("Autobuilding instance of class " + clazz.getName(), operation);

        InternalUtils.invokePostInjectionMethods(result, this, resources, this);

        return result;
    }

    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return proxy(interfaceClass, implementationClass, this);
    }

    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass, ObjectLocator locator)
    {
        assert interfaceClass != null;
        assert implementationClass != null;

        if (InternalUtils.SERVICE_CLASS_RELOADING_ENABLED && InternalUtils.isLocalFile(implementationClass))
            return createReloadingProxy(interfaceClass, implementationClass, locator);

        return createNonReloadingProxy(interfaceClass, implementationClass, locator);
    }

    private <T> T createNonReloadingProxy(Class<T> interfaceClass, final Class<? extends T> implementationClass,
            final ObjectLocator locator)
    {
        final ObjectCreator<T> autobuildCreator = new ObjectCreator<T>()
        {
            public T createObject()
            {
                return locator.autobuild(implementationClass);
            }
        };

        ObjectCreator<T> justInTime = new ObjectCreator<T>()
        {
            private T delegate;

            public synchronized T createObject()
            {
                if (delegate == null)
                    delegate = autobuildCreator.createObject();

                return delegate;
            }
        };

        return proxyFactory.createProxy(interfaceClass, justInTime,
                String.format("<Autobuild proxy %s(%s)>", implementationClass.getName(), interfaceClass.getName()));
    }

    private <T> T createReloadingProxy(Class<T> interfaceClass, final Class<? extends T> implementationClass,
            ObjectLocator locator)
    {
        ReloadableObjectCreator creator = new ReloadableObjectCreator(implementationClass.getClassLoader(),
                implementationClass.getName(), loggerSource.getLogger(implementationClass), this, locator);

        getService(UpdateListenerHub.class).addUpdateListener(creator);

        return proxyFactory.createProxy(interfaceClass, (ObjectCreator<T>) creator,
                String.format("<Autoreload proxy %s(%s)>", implementationClass.getName(), interfaceClass.getName()));
    }

    public Object provideServiceProxy(String serviceId)
    {
        return getService(serviceId, Object.class);
    }

    public void run(String description, Runnable operation)
    {
        operationTracker.run(description, operation);
    }

    public <T> T invoke(String description, Invokable<T> operation)
    {
        return operationTracker.invoke(description, operation);
    }

    private Object invokeConstructor(final Constructor constructor, final InjectionResources resources)
    {
        final String description = proxyFactory.getConstructorLocation(constructor).toString();

        return invoke("Invoking " + description, new Invokable<Object>()
        {
            public Object invoke()
            {
                Throwable failure;

                try
                {
                    Object[] parameters = InternalUtils.calculateParametersForConstructor(constructor,
                            RegistryImpl.this, resources, RegistryImpl.this);

                    return constructor.newInstance(parameters);
                }
                catch (InvocationTargetException ite)
                {
                    failure = ite.getTargetException();
                }
                catch (Exception ex)
                {
                    failure = ex;
                }

                throw new RuntimeException(IOCMessages.autobuildConstructorError(description, failure), failure);
            }
        });
    }

    public Set<Class> getMarkerAnnotations()
    {
        return markerToServiceDef.keySet();
    }
}
