package nl.sijpesteijn.testing.fitnesse.plugins;

import java.io.File;
import java.util.List;

import nl.sijpesteijn.testing.fitnesse.plugins.managers.ContentPluginManager;
import nl.sijpesteijn.testing.fitnesse.plugins.pluginconfigs.ContentPluginConfig;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which creates the content.txt (classpath) file for Fitnesse.
 * 
 * @goal content
 * 
 * @phase process-sources
 * @requiresDependencyResolution compile+runtime
 */
public class FitNesseContentMojo extends AbstractMojo {

	/**
	 * The Maven project instance for the executing project.
	 * <p>
	 * Note: This is passed by Maven and must not be configured by the user.
	 * </p>
	 * 
	 * @parameter expression="${project.dependencies}"
	 * @required
	 * @readonly
	 */
	private List<Dependency> dependencies;

	/**
	 * The Maven project instance for the executing project.
	 * <p>
	 * Note: This is passed by Maven and must not be configured by the user.
	 * </p>
	 * 
	 * @parameter expression="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String> compileClasspathElements;

	/**
	 * Location of the local repository.
	 * <p>
	 * Note: This is passed by Maven and must not be configured by the user.
	 * </p>
	 * 
	 * @parameter expression="${settings.localRepository}"
	 * @readonly
	 * @required
	 */
	private String repositoryDirectory;

	/**
	 * List of static entries to add to content.txt
	 * 
	 * @parameter expression="${content.statics}"
	 */
	private List<String> statics;

	/**
	 * List of target directories to add to content.txt Each target is resolved
	 * as <target>\target\classes
	 * 
	 * @parameter expression="${content.targets}"
	 */
	private List<String> targets;

	/**
	 * List of resource entries to add to content.txt
	 * 
	 * @parameter expression="${content.resources}"
	 */
	private List<String> resources;

	/**
	 * List of dependencies to exclude from the content.txt
	 * 
	 * @parameter expression="${content.excludeDependencies}"
	 */
	private List<Dependency> excludeDependencies;

	/**
	 * Location of the wiki root of FitNesse.
	 * 
	 * @parameter expression="${content.wikiRoot}" default-value="${basedir}"
	 * @required
	 */
	private String wikiRoot;

	/**
	 * Name of the wiki root page
	 * 
	 * @parameter expression="${content.nameRootPage}"
	 *            default-value="FitNesseRoot"
	 * @required
	 */
	private String nameRootPage;

	/**
	 * The location for FitNesse to place the log files.
	 * 
	 * @parameter expression="${content.logDirectory}"
	 *            default-value="${basedir}/log/"
	 */
	private String logDirectory;

	private final int fitNessePort = 9090;
	private final int retainDays = 0;

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final ContentPluginConfig contentPluginConfig = getPluginConfig();
		getLog().debug("Content config: " + contentPluginConfig.toString());
		final ContentPluginManager manager = new ContentPluginManager(contentPluginConfig);
		manager.run();
		getLog().info("Created new content.txt in " + wikiRoot + File.separatorChar + nameRootPage + File.separatorChar);
	}

	/**
	 * Collect the plugin configuration settings
	 * 
	 * @return {@link nl.sijpesteijn.testing.fitnesse.plugins.pluginconfigs.ContentPluginConfig}
	 * @throws MojoExecutionException
	 */
	private ContentPluginConfig getPluginConfig() throws MojoExecutionException {
		return new ContentPluginConfig(wikiRoot, nameRootPage, repositoryDirectory, logDirectory, fitNessePort,
				retainDays, dependencies, getLog(), statics, resources, excludeDependencies, targets,
				compileClasspathElements);
	}
}
