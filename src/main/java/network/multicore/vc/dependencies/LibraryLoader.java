package network.multicore.vc.dependencies;

import com.google.gson.Gson;
import com.velocitypowered.api.plugin.PluginManager;
import network.multicore.vc.VelocityCompact;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibraryLoader {
    private final Logger logger;
    private final PluginManager pluginManager;
    private final List<Library> libs = new ArrayList<>();
    private final RepositorySystem repository;
    private final DefaultRepositorySystemSession session;
    private final List<RemoteRepository> repositories;

    @SuppressWarnings("deprecation")
    public LibraryLoader(PluginManager pluginManager, File libsDir, Logger logger) throws IOException {
        this.pluginManager = pluginManager;
        this.logger = logger;

        if (!libsDir.exists() || !libsDir.isDirectory()) {
            if (!libsDir.mkdirs()) {
                throw new IOException("Failed to create plugin directory");
            }
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dependencies.json")) {
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Library[] libraries = new Gson().fromJson(reader, Library[].class);
                libs.addAll(Arrays.asList(libraries));
            }
        }

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        repository = locator.getService(RepositorySystem.class);
        session = MavenRepositorySystemUtils.newSession();

        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
        session.setLocalRepositoryManager(repository.newLocalRepositoryManager(session, new LocalRepository(libsDir.getPath())));
        session.setTransferListener(new AbstractTransferListener() {
            @Override
            public void transferStarted(TransferEvent event) {
                logger.info("Downloading {}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
            }
        });

        session.setSystemProperties(System.getProperties());
        session.setReadOnly();

        repositories = repository.newResolutionRepositories(session, List.of(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build()));
    }

    public void downloadDependencies() throws DependencyResolutionException {
        logger.info("Loading {} libraries... please wait", libs.size());

        List<Dependency> dependencies = new ArrayList<>();

        libs.forEach(lib -> {
            Artifact artifact = new DefaultArtifact(lib.groupId() + ":" + lib.artifactId() + ":" + lib.version());
            dependencies.add(new Dependency(artifact, null));
        });

        DependencyResult result = repository.resolveDependencies(session, new DependencyRequest(new CollectRequest((Dependency) null, dependencies, repositories), null));

        result.getArtifactResults().forEach(artifactResult -> pluginManager.addToClasspath(VelocityCompact.getInstance(), artifactResult.getArtifact().getFile().toPath()));
    }
}
