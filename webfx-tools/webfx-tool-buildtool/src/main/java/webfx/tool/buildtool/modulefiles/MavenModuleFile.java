package webfx.tool.buildtool.modulefiles;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import webfx.tool.buildtool.Module;
import webfx.tool.buildtool.*;
import webfx.tools.util.reusablestream.ReusableStream;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public final class MavenModuleFile extends XmlModuleFile {

    private static final String newDependenciesIndentedLine = "\n    ";
    private static final String newDependencyIndentedLine = "\n        ";
    private static final String newDependencyElementIndentedLine = "\n            ";

    public MavenModuleFile(ProjectModule module) {
        super(module, true);
    }

    @Override
    Path getModulePath() {
        return resolveFromModuleHomeDirectory("pom.xml");
    }

    @Override
    void updateDocument(Document document) {
        Node dependenciesNode = lookupNode("/project/dependencies");
        if (dependenciesNode != null)
            clearNodeChildren(dependenciesNode);
        else {
            dependenciesNode = document.createElement("dependencies");
            Element documentElement = document.getDocumentElement();
            documentElement.appendChild(document.createTextNode(newDependenciesIndentedLine));
            documentElement.appendChild(dependenciesNode);
            documentElement.appendChild(document.createTextNode("\n"));
        }
        final Node finalDependenciesNode = dependenciesNode;
        dependenciesNode.appendChild(document.createTextNode(" "));
        dependenciesNode.appendChild(document.createComment(" Generated by WebFx "));
        ProjectModule module = getModule();
        boolean isForGwt = module.getTarget().isMonoPlatform(Platform.GWT);
        boolean isForJavaFx = module.getTarget().isMonoPlatform(Platform.JRE) && module.getTarget().hasTag(TargetTag.JAVAFX);
        boolean isExecutable = module.isExecutable();
        boolean isRegistry = module.getName().contains("-registry-") || module.getName().endsWith("-registry");
        Set<String> alreadyListedArtifactIds = new HashSet<>(); // Will be used to prevent duplicates (in case 2 different modules have the same artifactId)
        ReusableStream<ModuleDependency> dependencies = isForGwt && isExecutable ? module.getTransitiveDependencies() :
                ReusableStream.concat(
                        module.getDirectDependencies(),
                        module.getTransitiveDependencies().filter(dep -> dep.getType() == ModuleDependency.Type.IMPLICIT_PROVIDER)
                ).distinct();
        dependencies
                .stream().collect(Collectors.groupingBy(ModuleDependency::getDestinationModule)).entrySet()
                .stream().sorted(Map.Entry.comparingByKey())
                .forEach(moduleGroup -> {
                    Node moduleDependencyNode = createModuleDependencyNode(moduleGroup, document, isForGwt, isForJavaFx, isExecutable, isRegistry);
                    if (moduleDependencyNode != null) {
                        String artifactId = ArtifactResolver.getArtifactId(moduleGroup.getKey(), isForGwt, isExecutable, isRegistry);
                        if (!alreadyListedArtifactIds.contains(artifactId)) {
                            finalDependenciesNode.appendChild(document.createTextNode(newDependencyIndentedLine));
                            finalDependenciesNode.appendChild(document.createTextNode(newDependencyIndentedLine));
                            finalDependenciesNode.appendChild(moduleDependencyNode);
                            alreadyListedArtifactIds.add(artifactId);
                        }
                    }
                });
        dependenciesNode.appendChild(document.createTextNode(newDependenciesIndentedLine));
        dependenciesNode.appendChild(document.createTextNode(newDependenciesIndentedLine));
    }

    private static Node createModuleDependencyNode(Map.Entry<Module, List<ModuleDependency>> moduleGroup, Document document, boolean isForGwt, boolean isForJavaFx, boolean isExecutable, boolean isRegistry) {
        Module module = moduleGroup.getKey();
        String artifactId = ArtifactResolver.getArtifactId(module, isForGwt, isExecutable, isRegistry);
        if (artifactId == null)
            return null;
        Element dependencyNode = document.createElement("dependency");
        Element artifactIdElement = document.createElement("artifactId");
        artifactIdElement.setTextContent(artifactId);
        dependencyNode.appendChild(document.createTextNode(newDependencyElementIndentedLine));
        dependencyNode.appendChild(artifactIdElement);
        String groupId = ArtifactResolver.getGroupId(module, isForGwt, isRegistry);
        Element groupIdElement = document.createElement("groupId");
        groupIdElement.setTextContent(groupId);
        dependencyNode.appendChild(document.createTextNode(newDependencyElementIndentedLine));
        dependencyNode.appendChild(groupIdElement);
        String version = ArtifactResolver.getVersion(module, isForGwt, isRegistry);
        if (version != null) {
            Element versionElement = document.createElement("version");
            versionElement.setTextContent(version);
            dependencyNode.appendChild(document.createTextNode(newDependencyElementIndentedLine));
            dependencyNode.appendChild(versionElement);
        }
        String scope = ArtifactResolver.getScope(moduleGroup, isForGwt, isForJavaFx, isExecutable, isRegistry);
        if (scope != null) {
            Element scopeElement = document.createElement("scope");
            scopeElement.setTextContent(scope);
            dependencyNode.appendChild(document.createTextNode(newDependencyElementIndentedLine));
            dependencyNode.appendChild(scopeElement);
        }
        String classifier = ArtifactResolver.getClassifier(moduleGroup, isForGwt, isExecutable);
        if (classifier != null) {
            Element classifierElement = document.createElement("classifier");
            classifierElement.setTextContent(classifier);
            dependencyNode.appendChild(document.createTextNode(newDependencyElementIndentedLine));
            dependencyNode.appendChild(classifierElement);
        }
        if (moduleGroup.getValue().stream().anyMatch(ModuleDependency::isOptional)) {
            Element optionalElement = document.createElement("optional");
            optionalElement.setTextContent("true");
            dependencyNode.appendChild(document.createTextNode(newDependencyElementIndentedLine));
            dependencyNode.appendChild(optionalElement);
        }
        dependencyNode.appendChild(document.createTextNode(newDependencyIndentedLine));
        return dependencyNode;
    }
}
