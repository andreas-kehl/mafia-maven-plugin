package nl.sijpesteijn.testing.fitnesse.plugins.managers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.sijpesteijn.testing.fitnesse.plugins.pluginconfigs.StarterPluginConfig;
import nl.sijpesteijn.testing.fitnesse.plugins.utils.CommandRunner;
import nl.sijpesteijn.testing.fitnesse.plugins.utils.DependencyResolver;
import nl.sijpesteijn.testing.fitnesse.plugins.utils.FileUtils;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Plugin manager responsible for starting FitNesse.
 * 
 */
public class StarterPluginManager implements PluginManager {

	private final StarterPluginConfig starterPluginConfig;
	private final DependencyResolver resolver;

	/**
	 * 
	 * @param starterPluginConfig
	 *            {@link nl.sijpesteijn.testing.fitnesse.plugins.pluginconfigs.StarterPluginConfig}
	 */
	public StarterPluginManager(final StarterPluginConfig starterPluginConfig) {
		this.starterPluginConfig = starterPluginConfig;
		resolver = new DependencyResolver(starterPluginConfig.getRepositoryDirectory());
	}

	/**
	 * Start FitNesse
	 * 
	 * @throws MojoFailureException
	 *             , MojoExecutionException
	 */
	@Override
	public void run() throws MojoExecutionException, MojoFailureException {
		final Dependency fitnesseDependency = new Dependency();
		fitnesseDependency.setGroupId("org.fitnesse");
		fitnesseDependency.setArtifactId("fitnesse");
		final String jarLocation = resolver.getJarLocation(starterPluginConfig.getDependencies(), fitnesseDependency);
		final String jvmArgumentsString = getJVMArguments(starterPluginConfig.getJvmArguments());
		final String dependencyList = getDependencyList();
		final String command = getCommand(jarLocation, jvmArgumentsString, dependencyList);

		starterPluginConfig.getMavenLogger().info(command);
		final CommandRunner runner = new CommandRunner(starterPluginConfig.getWikiRoot());
		try {
			starterPluginConfig.getMavenLogger().info(
					"Starting FitNesse. This could take some more seconds when first used....");
			runner.start(command);
			starterPluginConfig.getMavenLogger().info(
					"FitNesse available on http://localhost:" + starterPluginConfig.getFitnessePort());
		} catch (final IOException e) {
			throw new MojoExecutionException("Could not start fitnesse.", e);
		} catch (final InterruptedException e) {
			throw new MojoExecutionException("Could not start fitnesse.", e);
		}
		if (!runner.errorBufferContains("patient.") && (runner.getExitValue() != 0 || runner.errorBufferHasContent())) {
			throw new MojoFailureException("Could not start FitNesse: " + runner.getErrorBuffer());
		}

	}

	private String getCommand(final String jarLocation, final String jvmArgumentsString, final String dependencyList) {
		return "java"
				+ jvmArgumentsString
				+ " -cp "
				+ jarLocation
				+ File.pathSeparatorChar
				+ (dependencyList + " fitnesseMain.FitNesseMain -p " + starterPluginConfig.getFitnessePort() + " -d "
						+ FileUtils.formatPath(starterPluginConfig.getWikiRoot()) + " -r "
						+ starterPluginConfig.getNameRootPage() + getLogArgument() + " -e " + starterPluginConfig
							.getRetainDays());
	}

	private String getLogArgument() {
		if (starterPluginConfig.getLogDirectory() != null && !starterPluginConfig.getLogDirectory().equals("")) {
			return " -l " + starterPluginConfig.getLogDirectory();
		}
		return "";
	}

	/**
	 * Return the list with jvm dependencies.
	 * 
	 * @return {@link java.lang.String}
	 */
	private String getDependencyList() {
		if (starterPluginConfig.getJvmDependencies() == null) {
			return "";
		}
		String list = "";
		for (final Dependency dependency : starterPluginConfig.getJvmDependencies()) {
			final String dependencyPath = resolver.resolveDependencyPath(dependency);
			if (!dependencyPath.trim().equals("")) {
				list += dependencyPath + File.pathSeparatorChar;
			}
		}
		return list;
	}

	/**
	 * Return the list of jvm arguments.
	 * 
	 * @param arguments
	 *            {@link java.util.List}
	 * @return {@link java.lang.String}
	 */
	private String getJVMArguments(final List<String> arguments) {
		if (arguments == null) {
			return "";
		}
		String list = "";
		for (final String argument : arguments) {
			if (argument.startsWith("-")) {
				list += argument + " ";
			} else {
				list += " -D" + argument + " ";
			}
		}
		return list;
	}
}
