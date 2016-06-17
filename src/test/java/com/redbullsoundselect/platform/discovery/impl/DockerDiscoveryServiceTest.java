package com.redbullsoundselect.platform.discovery.impl;

import io.advantageous.reakt.exception.RejectedPromiseException;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.Promises;
import io.advantageous.test.DockerHostUtils;
import io.advantageous.test.DockerTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Category(DockerTest.class)
public class DockerDiscoveryServiceTest {

    private static final URI TEST_CONFIG = URI.create("docker:http://" + DockerHostUtils.getDockerHost() + ":" + 2375);

    static {
        System.out.println("Test config: " + TEST_CONFIG);
    }

    @Test
    public void testConstruct() throws Exception {
        DockerDiscoveryService service = new DockerDiscoveryService(TEST_CONFIG);
        Assert.assertNotNull(service);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructWithNoConfig() throws Exception {
        new DockerDiscoveryService(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithWrongConfig() throws Exception {
        new DockerDiscoveryService(URI.create("bogus://foo"));
    }

    @Test(expected = RejectedPromiseException.class)
    public void testWithNullQuery() throws Exception {
        Promise<List<URI>> promise = Promises.blockingPromise(Duration.ofSeconds(10));
        DockerDiscoveryService service = new DockerDiscoveryService(TEST_CONFIG);
        service.lookupService((URI) null).invokeWithPromise(promise);
        promise.get();
    }

    @Test
    public void testQueryByName() throws Exception {
        Promise<List<URI>> promise = Promises.blockingPromise(Duration.ofSeconds(10));
        DockerDiscoveryService service = new DockerDiscoveryService(TEST_CONFIG);
        service.lookupService("docker:///consul").invokeWithPromise(promise);
        List<URI> result = promise.get();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertFalse(result.get(0).getHost().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryWithBadScheme() throws Exception {
        Promise<List<URI>> promise = Promises.blockingPromise(Duration.ofSeconds(10));
        DockerDiscoveryService service = new DockerDiscoveryService(TEST_CONFIG);
        service.lookupService("bogus://localhost/consul").invokeWithPromise(promise);
        promise.get();
    }

    @Test
    public void testQueryByNameAndContainerPort() throws Exception {
        Promise<List<URI>> promise = Promises.blockingPromise(Duration.ofSeconds(10));
        DockerDiscoveryService service = new DockerDiscoveryService(TEST_CONFIG);
        service.lookupService("docker:///consul?containerPort=8500").invokeWithPromise(promise);
        List<URI> result = promise.get();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.get(0).getHost().isEmpty());
    }

    @Test
    public void testQueryByNameAndContainerPortNoPublic() throws Exception {
        Promise<List<URI>> promise = Promises.blockingPromise(Duration.ofSeconds(10));
        DockerDiscoveryService service = new DockerDiscoveryService(TEST_CONFIG);
        service.lookupService("docker:///consul?containerPort=8300&requirePublicPort=false").invokeWithPromise(promise);
        List<URI> result = promise.get();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.get(0).getHost().isEmpty());
    }

    @Test
    public void testQueryByNameAndContainerPortNotFound() throws Exception {
        Promise<List<URI>> promise = Promises.blockingPromise(Duration.ofSeconds(10));
        DockerDiscoveryService service = new DockerDiscoveryService(TEST_CONFIG);
        service.lookupService("docker:///consul?containerPort=8080").invokeWithPromise(promise);
        Assert.assertEquals(Collections.emptyList(), promise.get());
    }

}
